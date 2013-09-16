package org.bovinegenius.forge;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.bovinegenius.forge.Forge;

@Mojo(name = "build", requiresProject = false)
public class Build extends AbstractMojo {
  public void execute() throws MojoExecutionException {
    Log log = getLog();
    log.info("Building...");
    Forge.build(new String[] {});
  }
}

