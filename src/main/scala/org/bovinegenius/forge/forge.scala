package org.bovinegenius.forge

import org.bovinegenius.forge.impl.ForgeFactory

object Forge {
  def apply(): org.bovinegenius.forge.impl.Forge = {
    // val parent = getClass.getClassLoader
    // val classLoader = new DynamicClassLoader(parent)
    // println("classloader: %s".format(classLoader))
    // val forgeClass = classLoader.loadClass("org.bovinegenius.forge.impl.ForgeFactory")
    // val forgeObj = forgeClass.newInstance() //.asInstanceOf[ForgeFactory]
    // forgeClass.getMethod("defaultForge").invoke(forgeObj).asInstanceOf[org.bovinegenius.forge.impl.Forge]
    // Jar("org.apache.ant", "ant", "1.9.2").load()
    new org.bovinegenius.forge.impl.ForgeFactory().defaultForge
    // forgeObj.defaultForge
  }

  def main(args: Array[String]) {
    val forge = Forge()
    forge.build(null)
  }
}

