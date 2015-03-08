package ru.ifmo.cttdev.gizatullin.compile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private File getRoot() {
        return new File("src");
    }

    private void compile(final File root, final List<Class<?>> classes) {
        final List<String> files = new ArrayList<>();
        for (final Class<?> token : classes) {
            files.add(getFile(root, token).getPath());
        }
        compileFiles(root, files);
    }

    private void compileFiles(final File root, final List<String> files) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final List<String> args = new ArrayList<>();
        args.addAll(files);
        args.add("-cp");
        args.add(root.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        final int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        System.out.println(exitCode);
    }

    private File getFile(final File root, final Class<?> clazz) {
        final String path = clazz.getCanonicalName().replace(".", "/") + ".java";
        return new File(root, path).getAbsoluteFile();
    }


    void run() {
        ArrayList<Class<?>> l = new ArrayList<>();
        l.add(HelloWorldClass.class);
        compile(getRoot(), l);
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
