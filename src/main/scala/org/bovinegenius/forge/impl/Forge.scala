package org.bovinegenius.forge.impl

import org.bovinegenius.forge.Ant
import org.bovinegenius.forge.Forge
import org.bovinegenius.forge.model._
import org.eclipse.aether.resolution.DependencyResolutionException
import org.bovinegenius.forge.xml._
import org.bovinegenius.forge.Script

private class DefaultForge extends Forge {
  override def build(): Unit = {
    try {
      val repo = StandardMavenRepository("central", "http://repo1.maven.org/maven2/")
      val repo2 = StandardMavenRepository("local", "file:///Users/wtetzner/.m2/repository/")
      val repo3 = CombinedMavenRepository("combined", Seq(repo, repo2))
      val rhino = repo3.jar(
        groupId = "org.mozilla",
        artifactId = "rhino",
        version = "1.7R4"
      ).ensure
      val proguard = repo3.jar("net.sf.proguard", "proguard-anttask", "4.10").ensure
      val ant = Ant.tasks
      ant.echo(message = "Some message")
      ant.echo(message = Element("blah", Seq("a" -> "b"), Body(Text("asdf"), Element("elem", Seq(), Body()))))
      val dir1 = "/Users/walter/some-test-dir"
      val dir2 = new java.io.File("/Users/walter/some-test-dir2")
      ant.mkdir(dir = dir1)
      ant.mkdir(dir = dir2)
      ant.delete(dir = dir1)
      ant.delete(dir = dir2)
      ant.delete(dir = dir2)
      ant.echo(message = "Done.")

      Script("test.js").println("text")
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

