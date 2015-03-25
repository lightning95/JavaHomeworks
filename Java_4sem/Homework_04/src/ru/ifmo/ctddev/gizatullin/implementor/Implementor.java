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
 * This class implements class or interface you provide.
 * This class can implement only non-static and non-private classes and interfaces
 * with non-private constructors.
 *
 * @author Aydar Gizatullin a.k.a. lightning95 (aydar.gizatullin@gmail.com)
 * @see info.kgeorgiy.java.advanced.implementor.Impler
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 */

public class Implementor implements Impler, JarImpler {
    /**
     * LineSeparator for each OS.
     */
    private static final String lS = System.lineSeparator();

    /**
     * Main method to execute.
     * <p/>
     * If the first arguments is "-jar", tries to implement "class" as the second argument
     * in the given jar-file - the third argument.
     * Otherwise, tries to implement "class" as the first argument, "root" - file to locate
     * into as second.
     *
     * @param args arguments from the command line
     * @see #implementJar
     * @see #implement
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Not enough arguments! Must be at least 2.");
            return;
        }

        if (args[0].equals("-jar") && args.length >= 3) {
            try {
                Class<?> c = Class.forName(args[1]);
                new Implementor().implementJar(c, new File(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found exception: " + args[1]);
            } catch (ImplerException e) {
                System.err.println("Class wasn't implemented, cause" + lS + e.getMessage());
            }
        } else if (!args[0].equals("-jar")) {
            try {
                Class<?> c = Class.forName(args[0]);
                new Implementor().implement(c, new File(args[1]));
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found exception: " + args[0]);
            } catch (ImplerException e) {
                System.err.println("Class wasn't implemented, cause" + lS + e.getMessage());
            }
        } else {
            System.err.println("Usage 1: \"-jar\" <ClassName> existing <JarFile>\n" +
                    "Usage 2: <ClassName> directory <File>");
        }
    }

    /**
     * Creates a file that implements or extends interface or class.
     * <p/>
     * Creates a file that implements or extends interface or class.
     * Output file is created in the folder that corresponds to the package of
     * given class or interface. Output file contains java class, that implements
     * or extends given class or interface and compiles without errors.
     * Class and interface must not contain generics.
     *
     * @param c    class or interface will be implemented
     * @param root directory to create class's implementation
     * @throws ImplerException if <code>c</code> or <code>root</code> is <code>null</code>,
     *                         <code>c</code> is <code>final</code> or has no non-private constructors
     */
    @Override
    public void implement(Class<?> c, File root) throws ImplerException {
        if (c == null) {
            throw new ImplerException("Implementing class is null");
        }
        if (root == null) {
            throw new ImplerException("File root is null");
        }
        if (Modifier.isFinal(c.getModifiers()) ||
                !Modifier.isAbstract(c.getModifiers()) && !c.isInterface() && c.getConstructors().length == 0) {
            throw new ImplerException((Modifier.isFinal(c.getModifiers()) ?
                    "Shouldn't override final class " : "No public constructors ") + c.getName());
        }
        File last = c.getPackage() == null ? root : new File(root, c.getPackage().getName().replace(".", File.separator));
        if (!last.mkdirs()) {
            System.err.println("Cannot create package: " + last.getName());
        }
        File file = new File(last, c.getSimpleName() + "Impl.java");
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            printClass(c, out);
        } catch (IOException e) {
            throw new ImplerException("Class " + c.getName() + " can't be printed to " + file);
        }
    }

    /**
     * Prints given class to PrintWriter.
     * <p/>
     * Prints class {@code c} to {@code PrintWriter out}, with all it's constructors, methods and their exceptions,
     * meanwhile storing them in the {@code HashMap}.
     *
     * @param c   class to print
     * @param out PrintWriter to print to
     * @see #putProtectedMethods
     * @see #inappropriateModifiers
     * @see #printConstructor
     * @see #printMethod
     * @see #getHash
     * @see java.io.PrintWriter
     * @see java.lang.reflect.Modifier
     */
    private static void printClass(Class c, PrintWriter out) {
        out.println("package " + c.getPackage().getName() + ";" + lS); // print package
        out.print("public class " + c.getSimpleName() + "Impl"); // print name
        out.print(!c.isInterface() ? " extends " + c.getSimpleName() : ""); // print superclass if exists
        Class[] interfaces = c.getInterfaces(); // print implementing interfaces if exist
        if (interfaces.length > 0 || c.isInterface()) {
            out.print(" implements " + (c.isInterface() ? c.getName() + (interfaces.length > 0 ? ", " : "") : ""));
            for (int i = 0; i < interfaces.length; ++i) {
                out.print(interfaces[i].getName() + (i + 1 < interfaces.length ? ", " : " "));
            }
        }
        out.println("{" + lS); // begin
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

    /**
     * Checks modifiers for possibility to implement.
     * <p/>
     * Returns {@code true} if the integer argument includes the
     * {@code private} or {@code final} modifier, {@code false} otherwise.
     *
     * @param modifiers modifiers to check
     * @return {@code true} if {@code modifiers} is {@code final} or {@code private},
     * {@code false} otherwise.
     * @see java.lang.reflect.Modifier
     */
    private static boolean inappropriateModifiers(int modifiers) {
        return Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers);
    }

    /**
     * Puts methods of the superclasses into the map.
     * <p/>
     * Recursive method to look through superclasses of {@code Class c} and put methods to {@code map},
     * if the modifiers of them are appropriate.
     *
     * @param c   class to get methods from
     * @param map map to put methods to
     * @see java.util.Map
     * @see java.lang.Class#getDeclaredMethods
     * @see #getHash
     * @see #putProtectedMethods
     */
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

    /**
     * Returns hash of the method.
     * <p/>
     * Gets hash-like string from {@code method} and it's parameters.
     *
     * @param method method to get hash from
     * @return {@code String} that represents this method ans it's parameters
     * @see java.lang.reflect.Method#getParameters
     */
    private static String getHash(Method method) {
        String res = method.getName();
        for (Parameter parameter : method.getParameters()) {
            res += "#" + parameter.getType().getName();
        }
        return res;
    }

    /**
     * Prints given constructor of the class to PrintWriter.
     * <p/>
     * Method to print {@code constructor} of the class to the {@code PrintWriter out}
     * with this {@code simpleName}. Includes parameters and exceptions if necessary.
     *
     * @param constructor {@code Constructor} to print
     * @param out         {@code PrintWriter} to print to
     * @param simpleName  simple name of the class
     * @see java.io.PrintWriter
     * @see java.lang.reflect.Constructor
     * @see #printParameters
     * @see #printExceptions
     */
    private static void printConstructor(Constructor constructor, PrintWriter out, String simpleName) {
        out.print("\t" + Modifier.toString(constructor.getModifiers() &
                Modifier.constructorModifiers()) + " " + simpleName + "Impl (");
        printParameters(constructor.getParameters(), out); // print parameters
        out.print(") ");
        printExceptions(constructor.getExceptionTypes(), out); // print exceptions
        out.print("{" + lS + "\t\tsuper(");
        for (int i = 0; i < constructor.getParameterCount(); ++i) {
            out.print("arg" + (i) + (i + 1 < constructor.getParameterCount() ? ", " : ""));
        }
        out.println(");" + lS + "\t}" + lS);
    }

    /**
     * Prints method to the give PrintWriter.
     * <p/>
     * Prints {@code method} to given {@code PrintWriter out}, with all it's exceptions,
     * parameters and return type if necessary.
     *
     * @param method method to print
     * @param out    {@code PrintWriter} to print to
     * @see #printParameters
     * @see #printExceptions
     * @see java.io.PrintWriter
     */
    private static void printMethod(Method method, PrintWriter out) {
        out.print("\t" + Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE &
                ~Modifier.TRANSIENT) + " " + method.getReturnType().getCanonicalName() + " " + method.getName() + "(");
        printParameters(method.getParameters(), out); // print parameters
        out.print(") ");
        printExceptions(method.getExceptionTypes(), out); // print exceptions
        if (method.getReturnType().equals(void.class)) { // if void
            out.println("{}" + lS);
        } else { // print return ~;
            out.println("{" + lS + "\t\treturn " + (method.getReturnType().isPrimitive() ?
                    (method.getReturnType().equals(boolean.class) ? false : 0) : null) + ";" + lS + "\t}" + lS);
        }
    }

    /**
     * Prints parameters of the method to the given PrintWriter.
     * <p/>
     * Prints {@code parameters} of the method to {@code PrintWriter out}.
     *
     * @param parameters array of parameters to print
     * @param out        {@code PrintWriter} to print to
     * @see java.io.PrintWriter
     */
    private static void printParameters(Parameter[] parameters, PrintWriter out) {
        for (int i = 0; i < parameters.length; ++i) {
            out.print(parameters[i].getType().getCanonicalName() + " arg" + i + (i + 1 < parameters.length ? ", " : " "));
        }
    }

    /**
     * Prints exceptions to the given PrintWriter.
     * <p/>
     * Prints {@code exceptions} of the given method to {@code PrintWriter out}.
     *
     * @param exceptions array of exceptions of this method to print
     * @param out        {@code PrintWriter} to print to
     * @see java.io.PrintWriter
     */
    private static void printExceptions(Class[] exceptions, PrintWriter out) {
        out.print((exceptions.length > 0 ? " throws " : ""));
        for (int i = 0; i < exceptions.length; ++i) {
            out.print(exceptions[i].getName() + (i + 1 < exceptions.length ? ", " : " "));
        }
    }

    /**
     * Implements given class and puts in the jar-file.
     * <p/>
     * Creates temporary directory and generates implementation of the class {@code c}
     * with the name "classname" + "Impl.class". Puts this implementation and {@code Manifest}
     * into given {@code jarFile}.
     *
     * @param c       {@code Class} to implement to
     * @param jarFile file to print to
     * @throws ImplerException if {@code c} or {@code jarFile} is {@code null},
     *                         or if {@code exitCode} of compilation of the implementing class is not {@code 0}
     * @see #implement
     * @see #createJar
     * @see #compile
     */
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
            throw new ImplerException("Compilation error, exitCode = " + exitCode + ", name = " + name);
        }
        createJar(root.getAbsolutePath(), jarFile.getAbsolutePath(), name + ".class");
    }

    /**
     * Packs the class in the given jar-file.
     * <p/>
     * Puts {@code Manifest} and compiled class-file {@code classFile} in the the given {@code jarFile}
     * in the given directory {@code root}.
     *
     * @param root      directory to locate {@code jarFile}
     * @param jarFile   {@code JarFile} to write to
     * @param classFile {@code String} representing classFile of the {@code Class}
     * @see java.util.jar.Manifest
     */
    private static void createJar(String root, String jarFile, String classFile) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (FileInputStream fileInputStream = new FileInputStream(root + File.separator + classFile);
             JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(classFile));
            IOUtils.copy(fileInputStream, jarOutputStream);
            jarOutputStream.closeEntry();
        } catch (IOException e) {
            System.err.println("== CreateJar, classFile = " + classFile + ", message = " + e.getMessage());
        }
    }

    /**
     * Returns result of the compiling.
     * <p/>
     * Compiles the given {@code file} in the directory {@code root} and returns the exit code of the compilation
     *
     * @param root directory where the file is located
     * @param file name of the file to compile
     * @return resulting {@code int} of the compiling
     * @see javax.tools.JavaCompiler
     * @see javax.tools.ToolProvider
     */
    private static int compile(final File root, final String file) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final List<String> args = new ArrayList<>();
        args.add(file);
        args.add("-cp");
        args.add(root.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        return compiler.run(null, null, null, args.toArray(new String[args.size()]));
    }
}
