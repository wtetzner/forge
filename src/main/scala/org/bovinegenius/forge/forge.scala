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

object Forge {
  def repositorySystem() {
    val locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory]);
    locator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory]);
    locator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory]);
    locator.getService(classOf[RepositorySystem]);
  }

  def fetch(repos: java.util.List[RemoteRepository], groupId: String,
            artifactId: String, version: String,
            extension: String = "jar") {
    val artifact = new DefaultArtifact(groupId, artifactId, extension, version);
    val request = new ArtifactRequest();
    request.setArtifact(artifact);
    request.setRepositories(repos);

    // getLog().info("Resolving artifact " + artifact + " from " + remoteRepos);
    val repoSystem = repositorySystem()
    //getLog().info("Resolved artifact " + artifact + " to " + result.getArtifact().getFile() + " from " + result.getRepository());
  }

  def build(args: Array[String]) {
    println("Running...")
    //fetch()
  }
}

