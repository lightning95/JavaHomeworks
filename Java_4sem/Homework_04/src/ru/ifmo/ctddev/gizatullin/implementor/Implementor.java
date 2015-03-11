package ru.ifmo.ctddev.gizatullin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import org.apache.commons.compress.utils.IOUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Created by lightning95 on 3/5/15.
 */

public class Implementor implements Impler, JarImpler {
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Not enough arguments!");
        }
        try {
            Class<?> c = Class.forName(args[0]);
            new Implementor().implementJar(c, new File(args[0]));
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found exception: " + args[0]);
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> c, File root) throws ImplerException {
        if (c == null) {
            throw new ImplerException("Implementing class is null");
        }
        if (root == null) {
            throw new ImplerException("File root is null");
        }
        if (inappropriateModifiers(c.getModifiers()) ||
                !Modifier.isAbstract(c.getModifiers()) && !c.isInterface() && c.getConstructors().length == 0) {
            throw new ImplerException((inappropriateModifiers(c.getModifiers()) ?
                    "Shouldn't override static/final class " : "No public constructors ") + c.getName());
        }
        File last = root;
        if (c.getPackage() != null) {
            last = new File(root, c.getPackage().getName().replace(".", File.separator));
        }
        if (!last.mkdirs()) {
            System.err.println("Cannot create package: " + last.getName());
        }
        File file = new File(last, c.getSimpleName() + "Impl.java");
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            printClass(c, out);
        } catch (IOException e) {
            System.err.println("Class " + c.getName() + " can't be printed to " + file);
        }
    }

    private static void printClass(Class c, PrintWriter out) {
        out.println("package " + c.getPackage().getName() + ";\n"); // print package
        out.print("public class " + c.getSimpleName() + "Impl"); // print name
        out.print(!c.isInterface() ? " extends " + c.getSimpleName() : ""); // print superclass if exists
        Class[] interfaces = c.getInterfaces(); // print implementing interfaces if exist
        if (interfaces.length > 0 || c.isInterface()) {
            out.print(" implements " + (c.isInterface() ? c.getName() + (interfaces.length > 0 ? ", " : "") : ""));
            for (int i = 0; i < interfaces.length; ++i) {
                out.print(interfaces[i].getName() + (i + 1 < interfaces.length ? ", " : " "));
            }
        }
        out.println("{\n"); // begin
        for (Constructor constructor : c.getConstructors()) { // print constructors
            if (!inappropriateModifiers(constructor.getModifiers())) {
                printConstructor(constructor, out, c.getSimpleName());
            }
        } // getDeclaredMethods() -> this class's, getMethods() -> all public
        Map<String, Method> hashMap = new HashMap<>();
        putProtectedMethods(c, hashMap); // put protected methods to the hashMap
        for (Method method : c.getMethods()) {
            if (!inappropriateModifiers(method.getModifiers())) {
                hashMap.put(getHash(method), method);
            }
        }
        for (String s : hashMap.keySet()) { // print methods
            printMethod(hashMap.get(s), out);
        }
        out.println("}"); // end
    }

    private static boolean inappropriateModifiers(int modifiers) {
        return Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers);
    }

    private static void putProtectedMethods(Class c, Map<String, Method> map) {
        for (Method method : c.getDeclaredMethods()) {
            if (!inappropriateModifiers(method.getModifiers())) {
                map.put(getHash(method), method);
            }
        }
        if (c.getSuperclass() != null) {
            putProtectedMethods(c.getSuperclass(), map);
        }
    }

    private static String getHash(Method method) {
        String res = method.getName();
        for (Parameter parameter : method.getParameters()) {
            res += "#" + parameter.getType().getName();
        }
        return res;
    }

    private static void printConstructor(Constructor constructor, PrintWriter out, String simpleName) {
        out.print("\t " + simpleName + "Impl (");
        printParameters(constructor.getParameters(), out); // print parameters
        out.print(") ");
        printExceptions(constructor.getExceptionTypes(), out); // print exceptions
        out.print("{\n\t\tsuper(");
        for (int i = 0; i < constructor.getParameterCount(); ++i) {
            out.print("arg" + (i) + (i + 1 < constructor.getParameterCount() ? ", " : ""));
        }
        out.println(");\n\t}\n");
    }

    private static void printMethod(Method method, PrintWriter out) {
        out.print("\tpublic " + method.getReturnType().getCanonicalName() + " " + method.getName() + "(");
        printParameters(method.getParameters(), out); // print parameters
        out.print(") ");
        printExceptions(method.getExceptionTypes(), out); // print exceptions
        if (method.getReturnType().equals(void.class)) { // if void
            out.println("{}\n");
        } else { // print return ~;
            out.println("{\n\t\treturn " + (method.getReturnType().isPrimitive() ?
                    (method.getReturnType().equals(boolean.class) ? false : 0) : null) + ";\n\t}\n");
        }
    }

    private static void printParameters(Parameter[] parameters, PrintWriter out) {
        for (int i = 0; i < parameters.length; ++i) {
            out.print(parameters[i].getType().getCanonicalName() + " arg" + i + (i + 1 < parameters.length ? ", " : " "));
        }
    }

    private static void printExceptions(Class[] exceptions, PrintWriter out) {
        out.print((exceptions.length > 0 ? " throws " : ""));
        for (int i = 0; i < exceptions.length; ++i) {
            out.print(exceptions[i].getName() + (i + 1 < exceptions.length ? ", " : " "));
        }
    }

    @Override
    public void implementJar(Class<?> c, File jarFile) throws ImplerException {
        if (c == null) {
            throw new ImplerException("Implementing class is null");
        }
        if (jarFile == null) {
            throw new ImplerException("JarFile is null");
        }
        File root = new File(".");
        try {
            root = Files.createTempDirectory("ImplTempRoot").toFile();
        } catch (IOException e) {
            System.err.println("Couldn't create temp directory");
        }
        implement(c, root);
        String name = (c.getPackage() != null ? c.getPackage().getName().replace(".", File.separator) : "")
                + File.separator + c.getSimpleName() + "Impl";
        int exitCode = compile(root, root.getAbsolutePath() + File.separator + name + ".java");
        if (exitCode != 0) {
            throw new ImplerException("Compilation error, exitCode = " + exitCode + ", name = " + name );
        }
        createJar(root.getAbsolutePath(), jarFile.getAbsolutePath(), name + ".class");
    }

    private void createJar(String root, String jarName, String name) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (FileInputStream fileInputStream = new FileInputStream(root + File.separator + name);
             JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarName), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(name));
            IOUtils.copy(fileInputStream, jarOutputStream);
            jarOutputStream.closeEntry();
        } catch (IOException e) {
            System.err.println("== CreateJar, name = " + name + ", message = " + e.getMessage());
        }
    }

    private static int compile(final File root, final String file) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final List<String> args = new ArrayList<>();
        args.add(file);
        args.add("-cp");
        args.add(root.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        return compiler.run(null, null, null, args.toArray(new String[args.size()]));
    }
}
