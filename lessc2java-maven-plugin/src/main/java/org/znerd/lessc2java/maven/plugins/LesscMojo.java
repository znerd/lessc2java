// See the COPYRIGHT file for copyright and license information
package org.znerd.lessc2java.maven.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.znerd.lessc2java.Lessc;
import org.znerd.util.log.Limb;
import org.znerd.util.log.MavenLimb;

/**
 * A Maven plugin for generating source files and/or documentation from Logdoc definitions.
 * 
 * @goal css
 * @phase generate-resources
 */
public class LesscMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        sendInternalLoggingThroughMaven();
        generate();
        // TODO: Add each generated file as a resource via MavenProject.addResource ?
    }

    private void sendInternalLoggingThroughMaven() {
        Limb.setLogger(new MavenLimb(getLog()));
    }

    private void generate() throws MojoExecutionException {
        CommandRunner commandRunner = 
        try {
            Lessc.compile(commandRunner, _sourceDir, includedFiles, _targetDir, _command, _timeOut, _overwrite)
        } catch (IOException cause) {
            throw new MojoExecutionException("Failed to perform transformation.", cause);
        }
    }

    /**
     * @parameter name="project" default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject _project;

    /**
     * @parameter name="in" expression="${basedir}/src/main/resources/less"
     * @required
     */
    private File _sourceDir;

    /**
     * @parameter name="out" expression="${basedir}/target/css"
     * @required
     */
    private File _targetDir;

    /**
     * @parameter name="command" expression="lessc"
     * @required
     */
    private String _command;

    /**
     * @parameter name="time-out" default-value="0"
     */
    private long _timeOut;

    /**
     * @parameter name="overwrite" default-value="false"
     */
    private boolean _overwrite;
}
