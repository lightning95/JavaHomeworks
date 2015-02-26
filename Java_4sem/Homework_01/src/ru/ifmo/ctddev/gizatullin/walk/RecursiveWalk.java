package ru.ifmo.ctddev.gizatullin.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {
    static final int PRIME = 0x01000193;
    static final int BUFF_SIZE = 2048;
    static final int INIT_HASH = 0x811c9dc5;
    static final String ERROR_HASH = String.format("%08x", 0x0);

    public static void main(String[] args) {
        if (args != null) {
            if (args.length < 2) {
                System.err.println("Not enough arguments: required 2, got " + args.length);
            } else {
                new RecursiveWalk().run(args[0], args[1]);
            }
        } else {
            System.err.println("Not enough arguments: required 2, got 0");
        }
    }

    private void run(String inputFile, String outputFile) {
        try (InputStreamReader isReader = new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8);
             OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isReader);
             BufferedWriter writer = new BufferedWriter(osWriter)) {
            String path;
            while ((path = reader.readLine()) != null) {
                fileVisitor(Paths.get(path), writer);
            }
        } catch (IOException e) {
            System.err.println("Some I/O error occurred");
        }
    }

    static void fileVisitor(Path startPath, final BufferedWriter osWriter) {
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    print(anakinFileWalker(file.toFile()) + " " + file.toString() + '\n', osWriter);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    print(ERROR_HASH + " " + file.toString() + '\n', osWriter);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            print(ERROR_HASH + " " + startPath.toString() + '\n', osWriter);
        }
    }

    static String anakinFileWalker(File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFF_SIZE)) {
            int hash = INIT_HASH;
            byte[] bytes = new byte[BUFF_SIZE];
            int nBytes;
            while ((nBytes = bis.read(bytes, 0, BUFF_SIZE)) != -1) {
                for (int i = 0; i < nBytes; i++) {
                    hash = (hash * PRIME) ^ (bytes[i] & 0xff);
                }
            }
            return String.format("%08x", hash);
        } catch (IOException e) {
            return ERROR_HASH;
        }
    }

    static void print(String s, final BufferedWriter osWriter) {
        try {
            osWriter.write(s);
        } catch (IOException e) {
            System.err.println("Error during writing result");
        }
    }
}
