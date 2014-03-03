package org.bovinegenius.forge.impl

import org.bovinegenius.forge.Ant
import org.bovinegenius.forge.Forge
import org.bovinegenius.forge.model.StandardMavenRepository
import org.eclipse.aether.resolution.DependencyResolutionException

private class DefaultForge extends Forge {
  override def build(): Unit = {
    try {
    val repo = StandardMavenRepository("central", "http://repo1.maven.org/maven2/")
    val rhino = repo.jar(
      groupId = "org.mozilla",
      artifactId = "rhino",
      version = "1.7R4"
    ).ensure
    println(s"rhino jar: ${rhino}")
    val proguard = repo.jar("net.sf.proguard", "proguard-anttask-x", "4.10").ensure
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
    } catch {
      case e: DependencyResolutionException => {
        println(s"[ERROR] ${e.getMessage}")
        System.exit(1)
      }
    }
  }
}

object ForgeFactory {
  def defaultForge: Forge = new DefaultForge()
}

