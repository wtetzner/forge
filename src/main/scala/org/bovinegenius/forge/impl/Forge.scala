package org.bovinegenius.forge.impl

import org.bovinegenius.forge.Ant
import org.bovinegenius.forge.Forge
import org.bovinegenius.forge.Jar
import org.eclipse.aether.RepositorySystem

private class DefaultForge extends Forge {
  override def build(repoSystem: RepositorySystem): Unit = {
    val rhino = Jar (
      group = "org.mozilla",
      artifact = "rhino",
      version = "1.7R4"
    ).load()
    val proguard = Jar("net.sf.proguard", "proguard-anttask", "4.10").load()
    Ant.tasks.echo(message = "Some message")
    val dir1 = "/Users/walter/some-test-dir"
    val dir2 = new java.io.File("/Users/walter/some-test-dir2")
    Ant.tasks.mkdir(dir = dir1)
    Ant.tasks.mkdir(dir = dir2)
    Ant.tasks.delete(dir = dir1)
    Ant.tasks.delete(dir = dir2)
    Ant.tasks.delete(dir = dir2)
  }
}

object ForgeFactory {
  def defaultForge: Forge = new DefaultForge()
}

