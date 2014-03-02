package org.bovinegenius.forge.impl

import org.bovinegenius.forge.Ant
import org.bovinegenius.forge.Forge
import org.bovinegenius.forge.model.StandardMavenRepository

private class DefaultForge extends Forge {
  override def build(): Unit = {
    val repo = new StandardMavenRepository("central", "http://repo1.maven.org/maven2/")
    val rhino = repo.jar(
      groupId = "org.mozilla",
      artifactId = "rhino",
      version = "1.7R4"
    ).load
    val proguard = repo.jar("net.sf.proguard", "proguard-anttask", "4.10").load
    val ant = Ant.tasks
    ant.echo(message = "Some message")
    val dir1 = "/Users/walter/some-test-dir"
    val dir2 = new java.io.File("/Users/walter/some-test-dir2")
    ant.mkdir(dir = dir1)
    ant.mkdir(dir = dir2)
    ant.delete(dir = dir1)
    ant.delete(dir = dir2)
    ant.delete(dir = dir2)
    ant.echo(message = "Done.")
  }
}

object ForgeFactory {
  def defaultForge: Forge = new DefaultForge()
}

