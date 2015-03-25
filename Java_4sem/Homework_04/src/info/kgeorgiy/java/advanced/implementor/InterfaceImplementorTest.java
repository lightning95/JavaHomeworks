package info.kgeorgiy.java.advanced.implementor;

import info.kgeorgiy.java.advanced.implementor.examples.InterfaceWithDefaultMethod;
import info.kgeorgiy.java.advanced.implementor.examples.InterfaceWithStaticMethod;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.omg.DynamicAny.DynAny;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.annotation.Generated;
import javax.management.Descriptor;
import javax.management.loading.PrivateClassLoader;
import javax.sql.rowset.CachedRowSet;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.xml.bind.Element;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InterfaceImplementorTest {
    private String methodName;
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        protected void starting(final Description description) {
            methodName = description.getMethodName();
            System.out.println("== Running " + description.getMethodName());
        }
    };

    @Test
    public void test01_constructor() throws ClassNotFoundException, NoSuchMethodException {
        final Class<?> token = loadClass();
        assertImplements(token, Impler.class);
        assertImplements(token, JarImpler.class);
        checkConstructor("public default constructor", token);
    }

    private void assertImplements(final Class<?> token, final Class<?> iface) {
        Assert.assertTrue(token.getName() + " should implement " + iface.getName() + " interface", iface.isAssignableFrom(token));
    }

    @Test
    public void test02_standardMethodlessInterfaces() {
        test(false, Element.class, PrivateClassLoader.class);
    }

    @Test
    public void test03_standardInterfaces() {
        test(false, Accessible.class, AccessibleAction.class, Generated.class);
    }

    @Test
    public void test04_extendedInterfaces() {
        test(false, Descriptor.class, CachedRowSet.class, DynAny.class);
    }

    @Test
    public void test05_standardNonInterfaces() {
        test(true, void.class, String[].class, int[].class, String.class, boolean.class);
    }

    @Test
    public void test06_java8Interfaces() {
        test(false, InterfaceWithStaticMethod.class, InterfaceWithDefaultMethod.class);
    }

    protected void test(final boolean shouldFail, final Class<?>... classes) {
        final File root = getRoot();
        try {
            implement(shouldFail, root, Arrays.asList(classes));
            if (!shouldFail) {
                compile(root, Arrays.asList(classes));
                check(root, Arrays.asList(classes));
            }
        } finally {
            clean(root);
        }
    }

    private File getRoot() {
        return new File(".", methodName);
    }

    private URLClassLoader getClassLoader(final File root) {
        try {
            return new URLClassLoader(new URL[]{root.toURI().toURL()});
        } catch (final MalformedURLException e) {
            throw new AssertionError(e);
        }
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
        Assert.assertNotNull("Could not find java compiler, include tools.jar to classpath", compiler);
        final List<String> args = new ArrayList<>();
        args.addAll(files);
        args.add("-cp");
        args.add(root.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        final int exitCode = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        Assert.assertEquals("Compiler exit code", 0, exitCode);
    }

    private void clean(final File file) {
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File child : files) {
                    clean(child);
                }
            }
        }
        if (!file.delete()) {
            System.out.println("Warning: unable to delete " + file);
        }
    }

    private void checkConstructor(final String description, final Class<?> token, final Class<?>... params) {
        try {
            token.getConstructor(params);
        } catch (final NoSuchMethodException e) {
            Assert.fail(token.getName() + " should have " + description);
        }
    }

    private void implement(final boolean shouldFail, final File root, final List<Class<?>> classes) {
        JarImpler implementor;
        try {
            implementor = (JarImpler) loadClass().newInstance();
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail("Instantiation error");
            implementor = null;
        }
        for (final Class<?> clazz : classes) {
            try {
                implementor.implement(clazz, root);

                final File jarFile = new File(root, clazz.getName() + ".jar");
                implementor.implementJar(clazz, jarFile);
                checkJar(jarFile, clazz);

                Assert.assertTrue("You may not implement " + clazz, !shouldFail);
            } catch (final ImplerException e) {
                if (shouldFail) return;
                throw new AssertionError("Error implementing " + clazz, e);
            } catch (final Throwable e) {
                throw new AssertionError("Error implementing " + clazz, e);
            }
            final File file = getFile(root, clazz);
            Assert.assertTrue("Error implementing clazz: File '" + file + "' not found", file.exists());
        }
    }

    private File getFile(final File root, final Class<?> clazz) {
        final String path = clazz.getCanonicalName().replace(".", "/") + "Impl.java";
        return new File(root, path).getAbsoluteFile();
    }

    private Class<?> loadClass() throws ClassNotFoundException {
        final String className = System.getProperty("cut");
        Assert.assertTrue("Class name not specified", className != null);

        return Class.forName(className);
    }

    private void check(final File root, final List<Class<?>> classes) {
        final URLClassLoader loader = getClassLoader(root);
        for (final Class<?> token : classes) {
            check(loader, token);
        }
    }

    private void check(final URLClassLoader loader, final Class<?> token) {
        final String name = token.getCanonicalName() + "Impl";
        try {
            final Class<?> impl = loader.loadClass(name);

            if (token.isInterface()) {
                Assert.assertTrue(name + " should implement " + token, Arrays.asList(impl.getInterfaces()).contains(token)) ;
            } else {
                Assert.assertEquals(name + " should extend " + token, token, impl.getSuperclass()) ;
            }
            Assert.assertFalse(name + " should not be abstract", Modifier.isAbstract(impl.getModifiers()));
            Assert.assertFalse(name + " should not be interface", Modifier.isInterface(impl.getModifiers()));
        } catch (final ClassNotFoundException e) {
            throw new AssertionError("Error loading class " + name, e);
        }
    }

    private void checkJar(final File jarFile, final Class<?> token) {
        try (final URLClassLoader classLoader = getClassLoader(jarFile)) {
            check(classLoader, token);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
