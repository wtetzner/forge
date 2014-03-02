package org.bovinegenius.forge.model

trait Artifact {
  def localPath: String
  def classpath: Seq[String]
}

trait ArtifactSource {
  def artifact(name: String): Artifact
}

trait ArtifactCollection {
  def artifacts: Seq[Artifact]
}

// Combine artifacts and collections into
// a single collection
case class GroupedArtifactCollection(
  artifactSeq: Seq[Artifact],
  collections: Seq[ArtifactCollection])
    extends ArtifactCollection {
  override lazy val artifacts =
    artifactSeq ++ collections.flatMap(_.artifacts).distinct
}

