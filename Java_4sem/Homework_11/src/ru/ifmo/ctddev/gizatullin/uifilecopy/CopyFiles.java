package ru.ifmo.ctddev.gizatullin.uifilecopy;

import com.sun.nio.file.ExtendedCopyOption;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.List;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/13/15.
 */

public class CopyFiles extends SwingWorker<Long, CopyFiles.Status> {
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
    private final UIFileCopy.Transfer transfer;
    private final boolean preserve;
    private final boolean prompt;

    public class Status {
        final long curSize;
        final String name;
        final long size;

        Status(long curSize, String name, long size) {
            this.curSize = curSize;
            this.name = name;
            this.size = Math.max(size, 1);
        }
    }

    /**
     * Copy source file to target location. If {@code prompt} is true then
     * prompt user to overwrite target if it exists. The {@code preserve}
     * parameter determines if file attributes should be copied/preserved.
     */

    String show() {
        Object[] possibilities = {"Once", "For all"};
        String s = (String) JOptionPane.showInputDialog(
                null,
                "Complete the sentence:\n"
                        + "\"Green eggs and...\"",
                "Customized Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "ham");

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {

        }
        return s;
    }

    int yesForAll = 0;

    void copyFile(Path source, Path target, boolean prompt, boolean preserve, BasicFileAttributes attrs) {
        File targetFile = new File(String.valueOf(target));
        String name = source.getFileName().toString();
        if (targetFile.exists()) {
            if (prompt && yesForAll == 0) {
//                int exists = JOptionPane.showConfirmDialog(null, "File already exists " + name + "\nOverwrite?");
                Object[] possibilities = {"Yes, once", "Yes, for all", "No, once", "No, for all", "Cancel"};
                int exists = JOptionPane.showOptionDialog(null, "File already exists " + name + "\nOverwrite?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, possibilities, "Yes, once");
                System.out.println(exists);
                if (exists == 1) {
                    exists = 0;
                    yesForAll = 1;
                }
                if (exists == 3) {
                    exists = 2;
                    yesForAll = 2;
                }

                if (exists == 0) {
                    while (true) {
                        boolean delete = targetFile.delete();
                        if (!delete) {
                            int deleteRetry = JOptionPane.showConfirmDialog(null, "Can't delete file " + name + ".\n Try again?");
                            if (deleteRetry == 0) {
//                                copyFile(source, target, prompt, preserve, attrs);
                            } else if (deleteRetry == 2) {
                                cancel(true);
                            } else {
                                break;
                            }
                        } else {
//                            copyFile(source, target, prompt, preserve, attrs);
                            break;
                        }
                    }
                } else if (exists == 4) {
                    cancel(true);
                } else {
                    return;
                }
            } else if (yesForAll == 2) {
                return;
            }
        }

//        if (!prompt || !targetFile.exists()) {
        try (FileInputStream inputStream = new FileInputStream(String.valueOf(source));
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            Files.setPosixFilePermissions(target, Files.getPosixFilePermissions(source, LinkOption.NOFOLLOW_LINKS));
            byte[] buf = new byte[BUF_SIZE];

            long lastTime = System.currentTimeMillis();
            long cur = 0;
            long thisSize = attrs.size();

            while (true) {
                if (isCancelled()) {
                    return;
                }
                long res = inputStream.read(buf);
                if (res == -1) {
                    break;
                }
                curSize += res;
                cur += res;
                long curTime = System.currentTimeMillis();
                if (curTime - lastTime >= 500) {
                    publish(new Status(cur, name, thisSize));
                    lastTime = curTime;
                }
                outputStream.write(buf);
                setProgress(Math.max(getProgress(), (int) (curSize * 100 / finalSize)));
            }
            publish(new Status(cur, name, thisSize));
//                Files.copy(source, target, (preserve) ? FILE_COPY_OPTION_WITH_PROMPT : FILE_COPY_OPTION_WITHOUT_PROMPT);
        } catch (IOException x) {
            int writeRetry = JOptionPane.showConfirmDialog(null, "Can't write file " + name + ".\n Try again?");
            if (writeRetry == 0) {
                copyFile(source, target, prompt, preserve, attrs);
            } else if (writeRetry != 1) {
                cancel(true);
            }
        }
    }

    long lastTime;
    long lastSize;

    public static String timeConvert(long time) {
        String res;
        if (time < 60) {
            res = time + " second" + (time > 1 ? "s" : "");
        } else if (time < 3600) {
            res = time / 60 + " minute" + (time / 60 > 1 ? "s" : "");
        } else {
            res = time / 3600 + " hour" + (time / 3600 > 1 ? "s" : "");
        }
        return res;
    }

    public static String sizeConvert(long size) {
        String res;
        if (size < (1 << 10)) {
            res = size + " B";
        } else if (size < (1 << 20)) {
            res = size / (1 << 10) + " KB";
        } else if (size < (1 << 30)) {
            res = size / (1 << 20) + " MB";
        } else {
            res = size / (1L << 30) + " GB";
        }
        return res;
    }

    @Override
    protected void process(List<Status> chunks) {
        long curTime = System.currentTimeMillis();
        long timeLen = (curTime - lastTime);
        long lCurSize = curSize;

        long el = ((System.currentTimeMillis() - startTime) / 1000);
        long ave = el > 0 ? lCurSize / (1 << 20) / el : 0;
        long est = Math.max(ave > 0 ? (finalSize - lCurSize) / (1 << 20) / ave : 0, 0);
        long cur = lCurSize - lastSize > 0 ? (lCurSize - lastSize) * 1000 / timeLen / (1 << 20) : ave;

        transfer.elapsedTimeLabel.setText("Elapsed time: " + timeConvert(el));
        transfer.estimatingTimeLabel.setText("Estimating time: " + timeConvert(est));
        transfer.averageSpeedLabel.setText("Average speed: " + ave + " mb/s");
        transfer.currentSpeedLabel.setText("Current speed: " + cur + " mb/s");
        Status last = chunks.get(chunks.size() - 1);
        transfer.currentFileLabel.setText("<html>Current file: " + last.name + "</html>");
        transfer.currentFileProgressBar.setValue((int) (last.curSize * 100 / last.size));
        transfer.currentFileProgressBar.setString(sizeConvert(last.curSize) + " / " + sizeConvert(last.size));

        transfer.progressBar.setString(sizeConvert(lCurSize) + " / " + sizeConvert(finalSize));

        lastSize = lCurSize;
        lastTime = curTime;
    }

    @Override
    protected void done() {
        transfer.frame.dispose();
        transfer.frame.setVisible(false);
        if (!isCancelled()) {
            long res = (System.currentTimeMillis() - startTime) / 1000;
            JOptionPane.showMessageDialog(null, "Copying is finished \nTime spent: " + UIFileCopy.timeConvert(res));
        } else {
            JOptionPane.showMessageDialog(null, "Copying is canceled");
        }
    }

    long startTime;
    long finalSize;
    long curSize;

    @Override
    protected Long doInBackground() throws Exception {
        startTime = System.currentTimeMillis();
        final long[] tmp = {0};
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isCancelled()) {
                        return TERMINATE;
                    }
                    try {
                        tmp[0] += attrs.size();
                        return super.visitFile(file, attrs);
                    } catch (IOException e) {
                        int res = JOptionPane.showConfirmDialog(null, "Can't read file " +
                                e.getMessage() + ".\n Try again?");
                        if (res == 0) {
                            return visitFile(file, attrs);
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
        finalSize = tmp[0];
        curSize = 0;
        try {
            Files.walkFileTree(source, FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
                    new TreeCopier(source, destination, prompt, preserve));
        } catch (IOException ignored) {
        }
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

        TreeCopier(Path source, Path target, boolean prompt, boolean preserve) {
            this.source = source;
            this.target = target;
            this.prompt = prompt;
            this.preserve = preserve;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
            if (isCancelled()) {
                return TERMINATE;
            }
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
            if (isCancelled()) {
                return TERMINATE;
            }
            copyFile(file, target.resolve(source.relativize(file)), prompt, preserve, attrs);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // fix up modification time of directory when done
            if (isCancelled()) {
                return TERMINATE;
            }
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
            if (isCancelled()) {
                return TERMINATE;
            }
            if (exc instanceof FileSystemLoopException) {
                System.err.println("cycle detected: " + file);
            } else {
                System.err.format("Unable to copy: %s: %s%n", file, exc);
            }
            return CONTINUE;
        }
    }

    public CopyFiles(Path source, Path destination, boolean prompt, boolean preserve, UIFileCopy.Transfer transfer) {
        this.source = source;
        this.destination = destination;
        this.prompt = prompt;
        this.preserve = preserve;
        this.transfer = transfer;
    }
}