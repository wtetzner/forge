package org.bovinegenius.forge

import org.bovinegenius.forge.classloader.ClassLoaderUtils
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
import java.lang.ClassLoader
import java.net.URL
import org.eclipse.aether.RepositoryListener
import org.eclipse.aether.transfer.TransferListener
import org.eclipse.aether.RepositoryEvent
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.aether.collection.CollectRequest

class Jar(groupId: String, artifactId: String, version: String) {
  private var classloader: ClassLoader = null

  def install() = {
    Fetcher.fetch(groupId, artifactId, version)
  }

  def load() = {
    if (classloader == null) {
      val results = install()
      val urls = new Array[URL](results.size())
      results.indices foreach { i =>
        urls(i) = results(i).getArtifact.getFile.toURI.toURL
      }
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

object ConsoleRepositoryListener extends RepositoryListener {
  def name(event: RepositoryEvent) = {
    val artifact = event.getArtifact
    "%s:%s:%s".format(artifact.getGroupId, artifact.getArtifactId, artifact.getVersion)
  }
  def artifactDeployed(event: RepositoryEvent) {
  }
  def artifactDeploying(event: RepositoryEvent) {
    print("Deploy %s".format(name(event)))
  }
  def artifactDescriptorInvalid(event: RepositoryEvent) {}
  def artifactDescriptorMissing(event: RepositoryEvent) {}
  def artifactDownloaded(event: RepositoryEvent) {
    // println("Downloaded %s".format(name(event)))
  }
  def artifactDownloading(event: RepositoryEvent) {
    val ext = event.getArtifact.getExtension
    println("[%s] Download %s from %s".format(ext, name(event), event.getRepository.getId))
  }
  def artifactInstalled(event: RepositoryEvent) {
    // println("Installed %s".format(name(event)))
  }
  def artifactInstalling(event: RepositoryEvent) {
    // println("Install %s".format(name(event)))
  }
  def artifactResolved(event: RepositoryEvent) {
    // println("Resolved %s".format(name(event)))
  }
  def artifactResolving(event: RepositoryEvent) {
    // println("Resolve %s".format(name(event)))
  }
  def metadataDeployed(event: RepositoryEvent) {}
  def metadataDeploying(event: RepositoryEvent) {}
  def metadataDownloaded(event: RepositoryEvent) {}
  def metadataDownloading(event: RepositoryEvent) {}
  def metadataInstalled(event: RepositoryEvent) {}
  def metadataInstalling(event: RepositoryEvent) {}
  def metadataInvalid(event: RepositoryEvent) {}
  def metadataResolved(event: RepositoryEvent) {}
  def metadataResolving(event: RepositoryEvent) {}
}

// val classToLoad = Class.forName ("com.MyClass", true, child);
    // Method method = classToLoad.getDeclaredMethod ("myMethod");
    // Object instance = classToLoad.newInstance ();
    // Object result = method.invoke (instance);
object Fetcher {
  def system = RepoHelper.system()
  def session(system: RepositorySystem = system,
              localRepo: String = defaultLocalRepo,
              repoListener: RepositoryListener = ConsoleRepositoryListener,
              transferListener: TransferListener = null) = {
    RepoHelper.session(system, localRepo, repoListener, transferListener)
  }
  def defaultLocalRepo = RepoHelper.defaultLocalRepo()
  def defaultCentralRepo = RepoHelper.defaultCentralRepo()

  def loadJars(jars: Array[URL]) = {
    jars foreach { url =>
      ClassLoaderUtils.addURL(url)
    }
    getClass.getClassLoader
    // val child = new URLClassLoader(jars, this.getClass().getClassLoader());
    // child
  }

  def defaultRepos = {
    List(defaultCentralRepo)
  }

  def installRequest(results: java.util.List[ArtifactResult]) = {
    val artifacts = results map { result => result.getArtifact() }
    val request = new InstallRequest()
    request.setArtifacts(artifacts)
    request
  }

  def fetch(groupId: String, artifactId: String,
            version: String,
            repos: List[RemoteRepository] = defaultRepos,
            localRepo: String = defaultLocalRepo,
            extension: String = "jar") = {
    val repoSystem = system
    val remoteRepos: java.util.List[RemoteRepository] = repos
    val session = this.session(repoSystem, localRepo)

    val artifact = new DefaultArtifact(groupId, artifactId,
                                       extension, version);

    val scope = JavaScopes.COMPILE
    val collectRequest = new CollectRequest()
    collectRequest.setRoot(new Dependency(artifact, scope))
    collectRequest.setRepositories(remoteRepos)
    // val depNode = new DefaultDependencyNode(artifact)
    // val filter = new ExclusionsDependencyFilter(List())
    val filter = DependencyFilterUtils.classpathFilter(scope)
    val depRequest = new DependencyRequest(collectRequest, filter)
    val depResult = repoSystem.resolveDependencies(session, depRequest)
    val artResults = depResult.getArtifactResults

    // val arts = List(artifact) ++ (artResults map { res => res.getArtifact })

    // val requests = arts map { art =>
    //   val request = new ArtifactRequest();
    //   request.setArtifact(art);
    //   request.setRepositories(remoteRepos);
    // }

    // val results = repoSystem.resolveArtifacts(session, requests)
    // val installReq = installRequest(results)

    // repoSystem.install(session, installReq)
    artResults
  }
}
