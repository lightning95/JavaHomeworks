package ru.ifmo.ctddev.gizatullin.uifilecopy;

import com.sun.nio.file.ExtendedCopyOption;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/13/15.
 */

public class CopyFiles extends SwingWorker<Long, Long> {
    private static final CopyOption[] FILE_COPY_OPTION_WITH_PROMPT =
            new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING, ExtendedCopyOption.INTERRUPTIBLE};
    private static final CopyOption[] FILE_COPY_OPTION_WITHOUT_PROMPT =
            new CopyOption[]{REPLACE_EXISTING, ExtendedCopyOption.INTERRUPTIBLE};
    private static final CopyOption[] DIR_COPY_OPTION_WITH_PROMPT =
            new CopyOption[]{COPY_ATTRIBUTES, ExtendedCopyOption.INTERRUPTIBLE};
    private static final CopyOption[] DIR_COPY_OPTION_WITHOUT_PROMPT =
            new CopyOption[]{ExtendedCopyOption.INTERRUPTIBLE};
    private static final EnumSet<FileVisitOption> FILE_VISIT_OPTIONS = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

    private static final int BUF_SIZE = 65_536;

    private final Path source;
    private final Path destination;
    private final boolean prompt;
    private final boolean preserve;

    /**
     * Copy source file to target location. If {@code prompt} is true then
     * prompt user to overwrite target if it exists. The {@code preserve}
     * parameter determines if file attributes should be copied/preserved.
     */
    void copyFile(Path source, Path target, boolean prompt, boolean preserve, AtomicLong curSize) {
        File targetFile = new File(String.valueOf(target));
        if (!prompt || !targetFile.exists()) {
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(String.valueOf(source)));
                 DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(targetFile))) {
                Files.setPosixFilePermissions(target, Files.getPosixFilePermissions(source, LinkOption.NOFOLLOW_LINKS));
                byte[] buf = new byte[BUF_SIZE];
                while (true) {
                    int res = inputStream.read(buf);
                    if (res == -1) {
                        break;
                    }
                    curSize.addAndGet(res);
                    outputStream.write(buf);
                    setProgress(Math.max(getProgress(), (int) (curSize.get() * 100 / finalSize.get())));
                }
//                Files.copy(source, target, (preserve) ? FILE_COPY_OPTION_WITH_PROMPT : FILE_COPY_OPTION_WITHOUT_PROMPT);
            } catch (IOException x) {
//                System.err.format("Unable to copy: %s: %s%n", source, x);
            }
        }
    }

    @Override
    protected void done() {
        if (!isCancelled()) {
            long res = (System.currentTimeMillis() - startTime) / 1000;
            JOptionPane.showMessageDialog(null, "Copying is finished \nTime spent: " + UIFileCopy.timeConvert(res));
        }
    }

    long startTime;

    @Override
    protected Long doInBackground() throws Exception {
        startTime = System.currentTimeMillis();
        final long[] size = {0};
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        size[0] += attrs.size();
                        return super.visitFile(file, attrs);
                    } catch (IOException e) {
                        int res = JOptionPane.showConfirmDialog(null, "Can't read file " +
                                e.getMessage() + ".\n Try again?");
                        if (res == 0) {
                            visitFile(file, attrs);
                        } else if (res == 1) {
                            return CONTINUE;
                        } else {
                            cancel(true);
                        }
                    }
                    return TERMINATE;
                }
            });
        } catch (IOException ignored) {
        }

        finalSize.set(size[0]);
        try {
            timer.start();
            Files.walkFileTree(source, FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
                    new TreeCopier(source, destination, prompt, preserve, curSize));
        } catch (IOException ignored) {
        }
        timer.stop();
        return System.currentTimeMillis() - startTime;
    }

    /**
     * A {@code FileVisitor} that copies a file-tree ("cp -r")
     */
    class TreeCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        private final boolean prompt;
        private final boolean preserve;
        private final AtomicLong curSize;

        TreeCopier(Path source, Path target, boolean prompt, boolean preserve, AtomicLong curSize) {
            this.source = source;
            this.target = target;
            this.prompt = prompt;
            this.preserve = preserve;
            this.curSize = curSize;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
            Path newDir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newDir, (preserve) ? DIR_COPY_OPTION_WITH_PROMPT : DIR_COPY_OPTION_WITHOUT_PROMPT);
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (IOException x) {
                System.err.format("Unable to create: %s: %s%n", newDir, x);
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            copyFile(file, target.resolve(source.relativize(file)), prompt, preserve, curSize);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // fix up modification time of directory when done
            if (exc == null && preserve) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                } catch (IOException x) {
                    System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                System.err.println("cycle detected: " + file);
            } else {
                System.err.format("Unable to copy: %s: %s%n", file, exc);
            }
            return CONTINUE;
        }
    }

    private final AtomicLong finalSize;
    private final AtomicLong curSize;
    private final Timer timer;

    public CopyFiles(Path source, Path destination, boolean prompt, boolean preserve,
                     AtomicLong finalSize, AtomicLong curSize, Timer timer) {
        this.source = source;
        this.destination = destination;
        this.prompt = prompt;
        this.preserve = preserve;
        this.timer = timer;
        this.finalSize = finalSize;
        this.curSize = curSize;
    }
}