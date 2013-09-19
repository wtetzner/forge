package org.bovinegenius.forge

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
import org.eclipse.aether.internal.impl.DefaultRepositorySystem
import org.eclipse.aether.internal.impl.DefaultArtifactResolver
import org.eclipse.aether.internal.impl.DefaultTransporterProvider
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.internal.impl.DefaultRepositoryConnectorProvider
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.artifact.Artifact
import java.net.URLClassLoader
import java.lang.ClassLoader
import java.net.URL

class Jar(groupId: String, artifactId: String, version: String) {
  private var classloader: ClassLoader = null

  def install() = {
    Fetcher.fetch(groupId, artifactId, version)
  }

  def load() = {
    if (classloader == null) {
      val results = install()
      val urls = new Array[URL](results.size())
      results.indices foreach { i => urls(i) = results(i).getArtifact.getFile.toURI.toURL }
      classloader = Fetcher.loadJars(urls)
    }
    classloader
  }

  def loadClass(classname: String) = {
    val classloader = load()
    Class.forName(classname, true, classloader);
  }
}

object Jar {
  def apply(groupId: String, artifactId: String, version: String) = {
    new Jar(groupId, artifactId, version)
  }
}

// val classToLoad = Class.forName ("com.MyClass", true, child);
    // Method method = classToLoad.getDeclaredMethod ("myMethod");
    // Object instance = classToLoad.newInstance ();
    // Object result = method.invoke (instance);
object Fetcher {
  def loadJars(jars: Array[URL]) = {
    val child = new URLClassLoader(jars, this.getClass().getClassLoader());
    child
  }

  def defaultRepos() = {
    List(RepoHelper.defaultCentralRepo())
  }

  def installRequest(results: java.util.List[ArtifactResult]) = {
    val artifacts = results map { result => result.getArtifact() }
    val request = new InstallRequest()
    request.setArtifacts(artifacts)
    request
  }

  def fetch(groupId: String, artifactId: String,
            version: String,
            repos: List[RemoteRepository] = defaultRepos(),
            localRepo: String = RepoHelper.defaultLocalRepo(),
            extension: String = "jar") = {
    val repoSystem = RepoHelper.system()
    val remoteRepos: java.util.List[RemoteRepository] = repos
    val session = RepoHelper.session(repoSystem, localRepo)

    val artifact = new DefaultArtifact(groupId, artifactId,
                                       extension, version);
    val request = new ArtifactRequest();
    request.setArtifact(artifact);
    request.setRepositories(remoteRepos);

    val results = repoSystem.resolveArtifacts(session, List(request))
    val installReq = installRequest(results)

    repoSystem.install(session, installReq)
    results
  }
}

