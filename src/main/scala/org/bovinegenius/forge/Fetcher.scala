package org.bovinegenius.forge

import org.bovinegenius.forge.classloader.ClassLoaderUtils
import org.apache.maven.plugin.MojoFailureException
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.repository.ArtifactRepository
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

case class Jar(val group: String, val artifact: String, val version: String) {
  private lazy val classloader = {
    val results = install()
    val urls = new Array[URL](results.size())
    results.indices foreach { i =>
      urls(i) = results(i).getArtifact.getFile.toURI.toURL
    }
    Fetcher.loadJars(urls)
  }

  def install(): Seq[ArtifactResult] =
    Fetcher.fetch(group, artifact, version)

  def load() = classloader

  def loadClass(classname: String) = {
    val classloader = load()
    Class.forName(classname, true, classloader);
  }
}

private object ConsoleRepositoryListener extends RepositoryListener {
  def name(event: RepositoryEvent) = {
    val artifact = event.getArtifact
    import artifact._
    s"${getGroupId}/${getArtifactId} ${getVersion}"
  }
  def artifactDeployed(event: RepositoryEvent) {
  }
  def artifactDeploying(event: RepositoryEvent) {
    print(s"Deploy ${name(event)}")
  }
  def artifactDescriptorInvalid(event: RepositoryEvent) {}
  def artifactDescriptorMissing(event: RepositoryEvent) {}
  def artifactDownloaded(event: RepositoryEvent) {
    // println("Downloaded %s".format(name(event)))
  }
  def artifactDownloading(event: RepositoryEvent) {
    val ext = event.getArtifact.getExtension
    val repo = event.getRepository.getId
    println(s"[${repo}] (${ext}) ${name(event)}... (Download)")
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

private object Fetcher {
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
    ClassLoaderUtils.getClassLoader()
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
            extension: String = "jar"): Seq[ArtifactResult] = {
    val repoSystem = system
    val remoteRepos: java.util.List[RemoteRepository] = repos
    val session = this.session(repoSystem, localRepo)

    val artifact = new DefaultArtifact(groupId, artifactId,
                                       extension, version);

    val scope = JavaScopes.COMPILE
    val collectRequest = new CollectRequest()
    collectRequest.setRoot(new Dependency(artifact, scope))
    collectRequest.setRepositories(remoteRepos)

    val filter = DependencyFilterUtils.classpathFilter(scope)
    val depRequest = new DependencyRequest(collectRequest, filter)
    val depResult = repoSystem.resolveDependencies(session, depRequest)
    val artResults = depResult.getArtifactResults

    artResults
  }
}

