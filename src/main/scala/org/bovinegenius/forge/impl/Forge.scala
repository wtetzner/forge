package org.bovinegenius.forge.impl

import org.bovinegenius.forge.Ant
import org.bovinegenius.forge.Jar
import org.eclipse.aether.RepositorySystem

trait Forge {
  def build(repoSystem: RepositorySystem)
}

class ForgeFactory {
  private class DefaultForge extends Forge {
    override def build(repoSystem: RepositorySystem) {
      val rhino = Jar("org.mozilla", "rhino", "1.7R4")
      println(rhino.loadClass("org.mozilla.javascript.Context"))
      // println(Ant.taskNames)
      Ant.tasks.echo(message = "Hello Bob")
      val dir1 = new java.io.File("/Users/walter/some-test-dir")
      val dir2 = new java.io.File("/Users/walter/some-test-dir2")
      Ant.tasks.mkdir(dir = dir1)
      Ant.tasks.mkdir(dir = dir2)
      Ant.tasks.delete(dir = dir1)
      Ant.tasks.delete(dir = dir2)
    }
  }

  def defaultForge: Forge = new DefaultForge()
}
