package org.bovinegenius.forge

import org.eclipse.aether.RepositorySystem

object Forge {
  def build(repoSystem: RepositorySystem) {
    val rhino = Jar("org.mozilla", "rhino", "1.7R4")
    println(rhino.loadClass("org.mozilla.javascript.Context"))
  }

  def main(args: Array[String]) {
    build(null)
    println(Ant.taskNames)
    Ant.tasks.echo(message = "Hello Bob")

    Ant.tasks.mkdir(dir = new java.io.File("/Users/walter/some-test-dir"))
  }
}

