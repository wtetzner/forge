package org.bovinegenius.forge

import org.bovinegenius.forge.impl.ForgeFactory
import org.eclipse.aether.RepositorySystem

trait Forge {
  def build(repoSystem: RepositorySystem = null)
}

object Forge {
  def apply(): Forge = ForgeFactory.defaultForge
}

object Main {
  def main(args: Array[String]): Unit = Forge().build()
}


