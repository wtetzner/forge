package org.bovinegenius.forge

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.plugin.MojoFailureException
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.transport.wagon.WagonTransporterFactory
import org.eclipse.aether.repository.LocalRepository
import scala.collection.JavaConversions._
//import org.eclipse.aether.repository.RemoteRepository.Builder;

object Forge {
  def repositorySystem() = {
    val locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(classOf[RepositoryConnectorFactory],
                       classOf[BasicRepositoryConnectorFactory]);
    locator.addService(classOf[RepositoryConnectorFactory],
                       classOf[FileRepositoryConnectorFactory]);
    locator.addService(classOf[RepositoryConnectorFactory],
                       classOf[WagonRepositoryConnectorFactory]);
    locator.addService(classOf[TransporterFactory],
                       classOf[FileTransporterFactory]);
    locator.addService(classOf[TransporterFactory],
                       classOf[HttpTransporterFactory]);
    locator.addService(classOf[TransporterFactory],
                       classOf[WagonTransporterFactory]);
    locator.getService(classOf[RepositorySystem]);
  }

  def repositorySession(repoSystem: RepositorySystem) = {
    val session = MavenRepositorySystemUtils.newSession()
    val localRepo = new LocalRepository("target/local-repo");
    session.setLocalRepositoryManager(
      repoSystem.newLocalRepositoryManager(session, localRepo))
    session
  }

  def fetch(repos: List[RemoteRepository], groupId: String,
            artifactId: String, version: String,
            extension: String = "jar") {
    val repoSystem = repositorySystem()
    val remoteRepos: java.util.List[RemoteRepository] = repos
    val artifact = new DefaultArtifact(groupId, artifactId,
                                       extension, version);
    val request = new ArtifactRequest();
    request.setArtifact(artifact);
    request.setRepositories(remoteRepos);

    val session = repositorySession(repoSystem)
    val result = repoSystem.resolveArtifact(session, request)
  }

  def build(repoSystem: RepositorySystem) {
    val repo = new RemoteRepository.Builder("maven-central", "repository", "https://repo1.maven.org/maven2").build()
    val repos = List(repo)
    println("Running...")
    val repoS = if (repoSystem == null) repositorySystem() else repoSystem
    fetch(repos, "org.mozilla", "rhino", "1.7R4");
    //fetch()
  }

  def main(args: Array[String]) {
    println("dunno")
    build(null)
  }
}

