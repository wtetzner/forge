package org.bovinegenius.forge;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

class RepoHelper {
  public static RepositorySystem system() {
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
    return locator.getService(RepositorySystem.class);
  }

  public static DefaultRepositorySystemSession session(RepositorySystem system, String localRepoName) {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    LocalRepository localRepo = new LocalRepository(localRepoName);
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
    // session.setTransferListener(new ConsoleTransferListener());
    // session.setRepositoryListener(new ConsoleRepositoryListener());
    return session;
  }

  public static RemoteRepository defaultCentralRepo() {
    return new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/").build();
  }

  public static String defaultLocalRepo() {
    return String.format("%s/.m2/repository",
                         System.getProperty("user.home"));
  }
}
