package ru.ifmo.ctddev.gizatullin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lightning95 on 3/5/15.
 */

public class Implementor implements Impler {
    public static void main(String[] args) { // just for local testing
        if (args.length > 0) {
            try {
                createFile(Class.forName(args[0]));
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found exception: " + args[0]);
            }
        } else {
            System.err.println("Not enough arguments: 0");
        }
    }

    private static void createFile(Class c) { // just for local testing
        try (PrintWriter out = new PrintWriter(new FileWriter(new File(System.getProperty("user.dir")
                + "/src/javax/annotation/processing", c.getSimpleName() + "Impl.java")))) {
            printClass(c, out); // firstly, create package u need
            out.close();
        } catch (IOException e) {
            System.err.println("Class " + c.getName() + " can't be printed to " + c.getName() + "Impl");
        }
    }

    @Override
    public void implement(Class<?> c, File root) throws ImplerException {
        if (Modifier.isFinal(c.getModifiers()) || Modifier.isStatic(c.getModifiers()) ||
                !Modifier.isAbstract(c.getModifiers()) && !c.isInterface() && c.getConstructors().length == 0) {
            throw new ImplerException("Shouldn't override static/final class " + c.getName());
        }
        File last = root != null ? root : new File(System.getProperty("user.dir"));
        if (c.getPackage() != null) {
            last = new File(last, c.getPackage().getName().replace(".", "/"));
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
}
