package ru.ifmo.ctddev.gizatullin.walk;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RecursiveWalk {
    private boolean eof;

    private StringTokenizer st;
    private BufferedReader br;
    private PrintWriter out;

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
        try {
            try {
                br = new BufferedReader(new FileReader(inputFile));
            } catch (FileNotFoundException e) {
                System.err.println("InputFile doesn't exist: " + inputFile);
                return;
            }

            try {
                out = new PrintWriter(outputFile);
            } catch (FileNotFoundException e) {
                System.err.println("OutputFile doesn't exist: " + outputFile);
                if (br != null)
                    br.close();
                return;
            }


            if (!(new File(outputFile).canWrite())) {
                System.err.println("OutputFile can't be written to: " + outputFile);
                if (br != null)
                    br.close();
                out.close();
                return;
            }

            solve();

            br.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Output writing error");
        }
    }

    private String nextToken() {
        while (st == null || !st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (IOException | NullPointerException e) {
                eof = true;
                return "";
            }
        }
        return st.nextToken();
    }

    private void solve() {
        List<String> list = new ArrayList<>();
        for (String s = nextToken(); !eof; s = nextToken()) {
            list.add(s);
            if (list.size() > 25) {
                directoryWalker(list.toArray(new String[list.size()]));
                list.clear();
            }
        }
        if (list.size() > 0) {
            directoryWalker(list.toArray(new String[list.size()]));
        }
    }

    private void directoryWalker(File directory) {
        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                        throws IOException {
                    File file = new File(path.toString());
                    if (file.exists() && file.canRead()) {
                        if (file.isFile()) {
                            anakinFileWalker(file);
                        } else if (file.isDirectory()) {
                            directoryWalker(file);
                        }
                    } else {
                        out.printf("%08x %s\n", 0, file.getPath());

                        if (!file.exists()) {
                            System.err.println("File not found: " + file.getPath());
                        }
                        if (!file.canRead()) {
                            System.err.println("File can't be read: " + file.getPath());
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Directory walking exception: " + directory.getPath());
        }
    }

    private void directoryWalker(String[] files) {
        for (String name : files) {
            File file = new File(name);
            if (file.exists() && file.canRead()) {
                if (file.isFile()) {
                    anakinFileWalker(file);
                } else if (file.isDirectory()) {
                    directoryWalker(file);
                }
            } else {
                out.printf("%08x %s\n", 0, file.getPath());

                if (!file.exists()) {
                    System.err.println("File not found: " + file.getPath());
                }
                if (!file.canRead()) {
                    System.err.println("File can't be read: " + file.getPath());
                }
            }
        }
    }

    private void anakinFileWalker(File file) {
        try {
            InputStream in = new FileInputStream(file);

            int hash = 0x811c9dc5;
            int c;

            while ((c = in.read()) >= 0) {
                hash = (hash * 0x01000193) ^ (c & 0xff);
            }

            out.printf("%08x %s\n", hash, file.getPath());
        } catch (FileNotFoundException e) {
            out.printf("%08x %s\n", 0, file.getPath());
            System.err.println("File not found exception: " + file.getPath());
        } catch (IOException e) {
            out.printf("%08x %s\n", 0, file.getPath());
            System.err.println("File reading exception: " + file.getPath());
        }
    }
}
