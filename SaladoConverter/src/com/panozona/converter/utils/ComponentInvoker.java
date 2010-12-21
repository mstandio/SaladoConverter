package com.panozona.converter.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 *
 * @author Marek
 */
public class ComponentInvoker {

    public static final String ILLEGAL_ACCESS_EXCEPTION = "ILLEGAL_ACCESS_EXCEPTION";
    public static final String CLASS_NOT_FOUND_EXCEPTION = "CLASS_NOT_FOUND_EXCEPTION";
    public static final String NO_SUCH_METHOD_EXCEPTION = "NO_SUCH_METHOD_EXCEPTION";
    public static final String INVOCATION_TARGET_EXCEPTION = "INVOCATION_TARGET_EXCEPTION";
    public static final String MALFORMED_URL_EXCEPTION = "MALFORMED_URL_EXCEPTION";
    public static final String INTERRUPTED_EXCEPTION = "INTERRUPTED_EXCEPTION";

    public ComponentInvoker(){

    }
    
    public void run(String path, String name, String[] args) throws InfoException{
        //System.out.println(path +" "+name+" "+Arrays.toString(args));

        try {
            invokeClass(path, name, args);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new InfoException(ILLEGAL_ACCESS_EXCEPTION);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new InfoException(CLASS_NOT_FOUND_EXCEPTION);            
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new InfoException(NO_SUCH_METHOD_EXCEPTION);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new InfoException(INVOCATION_TARGET_EXCEPTION);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new InfoException(MALFORMED_URL_EXCEPTION);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            throw new InfoException(INTERRUPTED_EXCEPTION);
        }
    }

    private void invokeClass(String path, String name, String[] args) throws
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            MalformedURLException,
            InterruptedException,
            IllegalAccessException {
        File f = new File(path);
        URLClassLoader u = new URLClassLoader(new URL[]{f.toURI().toURL()});
        Class c = u.loadClass(name);
        Method m = c.getMethod("main", new Class[]{args.getClass()});
        m.setAccessible(true);
        int mods = m.getModifiers();
        if (m.getReturnType() != void.class || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
            throw new NoSuchMethodException("main");
        }
        m.invoke(null, new Object[]{args});
        m = null;
        System.gc(); // uh-oh
    }
}
