package org.bovinegenius.forge;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugin.logging.Log;
import org.bovinegenius.forge.Forge;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import java.util.List;

@Mojo(name = "build", requiresProject = false)
public class Build extends AbstractMojo {
  /**
   * @component
   */
  private RepositorySystem repoSystem;

  /**
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  private RepositorySystemSession repoSession;

  /**
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  private List<RemoteRepository> remoteRepos;

  /**
   * @parameter expression="${aether.artifactCoords}"
   */
  private String artifactCoords;

  public void execute() throws MojoExecutionException {
    Log log = getLog();
    log.info("Building...");
    log.info(String.format("artifactCode: %s", artifactCoords));
    Forge.build(new String[] {});
  }
}

