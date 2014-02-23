package org.bovinegenius.forge.model

trait Artifact {
  def localPath: String
}

trait ArtifactSource {
  def artifact(name: String): Artifact
}

