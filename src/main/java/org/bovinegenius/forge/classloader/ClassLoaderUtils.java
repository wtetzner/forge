package org.bovinegenius.forge.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.lang.ClassLoader;
import java.lang.reflect.Method;
import java.io.IOException;

public class ClassLoaderUtils {
  public static void addURL(URL url) throws IOException {
    URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;

    try {
      Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] { url });
    } catch (Throwable t) {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader");
    }
  }
}

