package org.bovinegenius.forge.model

import java.lang.ClassLoader
import org.bovinegenius.forge.Fetcher
import org.eclipse.aether.repository.RemoteRepository
import java.net.URLClassLoader
import org.eclipse.aether.resolution.ArtifactResult
import java.net.URL

trait Jar {
  // Force the jar to exist on the local filesystem.
  // Returns the path to the jar on the local filesystem.
  def ensure: ArtifactPath

  // Construct a new classloader for this jar and its dependencies
  lazy val load: ClassLoader = {
    new URLClassLoader(ensure.paths.map(path => {
      new URL(path)
    }).toArray)
  }

  def loadClass[T](name: String): Class[T] = {
    val classloader = load
    classloader.loadClass(name).asInstanceOf[Class[T]]
  }
}

case class ArtifactPath(val path: String, val paths: Seq[String])

trait MavenRepository {
  def name: String
  def jar(groupId: String, artifactId: String, version: String): Jar
}

case class StandardMavenJar(
  val repositories: List[RemoteRepository],
  val localRepository: String,
  val groupId: String,
  val artifactId: String,
  val version: String)
    extends Jar {
  private lazy val artifacts = {
    Fetcher.fetch(
      groupId,
      artifactId,
      version,
      repositories,
      localRepository)
  }

  override lazy val ensure: ArtifactPath = {
    val source = artifacts.filter(a => {
      val art = a.getArtifact
      (art.getGroupId == groupId
        && art.getArtifactId == artifactId
        && art.getVersion == version
        && art.getExtension == "jar")
    }).head.getArtifact.getFile.toString
    val all = artifacts.map(a => {
      a.getArtifact.getFile.toString
    })
    ArtifactPath(source, all)
  }

}

case class StandardMavenRepository(
  val name: String,
  val url: String,
  val localRepository: String = "%s/.m2/repository".format(System.getProperty("user.home")))
  extends MavenRepository {
  override def jar(groupId: String, artifactId: String, version: String) = {
    StandardMavenJar(
      List(new RemoteRepository.Builder(name, "default", url).build()),
      localRepository,
      groupId,
      artifactId,
      version)
  }
}

case class CombinedMavenJar(repos: Seq[MavenRepository]) extends Jar {
  override def ensure: ArtifactPath = {
    
  }
}

case class CombinedMavenRepository(
  val name: String,
  repos: Seq[MavenRepository])
    extends MavenRepository {
  override def jar(groupId: String, artifactId: String, version: String) =
    CombinedMavenJar(repos)
}

