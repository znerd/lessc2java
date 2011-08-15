// See the COPYRIGHT file for copyright and license information
package org.znerd.lessc2java.maven.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.znerd.lessc2java.Lessc;
import org.znerd.util.io.DirectoryUtils;
import org.znerd.util.log.Limb;
import org.znerd.util.log.LogLevel;
import org.znerd.util.log.MavenLimb;
import org.znerd.util.proc.CommandRunner;
import org.znerd.util.proc.CommonsExecCommandRunner;
import org.znerd.util.text.TextUtils;

import static org.znerd.util.text.TextUtils.quote;

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
        checkSourceDirExists();
        CommandRunner commandRunner = new CommonsExecCommandRunner(_timeOut);
        String[] includedFiles = determineIncludedFiles();
        try {
            Lessc.compile(commandRunner, _sourceDir, includedFiles, _targetDir, _command, _overwrite);
        } catch (IOException cause) {
            throw new MojoExecutionException("Failed to perform transformation.", cause);
        }
    }

    private void checkSourceDirExists() throws MojoExecutionException {
        try {
            DirectoryUtils.checkDir("Source directory containing Less files", _sourceDir, true, false, false);
        } catch (IOException cause) {
            throw new MojoExecutionException(cause.getMessage(), cause);
        }
    }

    private String[] determineIncludedFiles() {
        FilenameFilter filter = new IncludeFilenameFilter();
        return _sourceDir.list(filter);
    }

    class IncludeFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            boolean match = isFileNameMatch(name) && isNotDirectory(dir, name);
            if (match) {
                Limb.log(LogLevel.INFO, "File " + quote(name) + " matches; including.");
            } else {
                Limb.log(LogLevel.INFO, "File " + quote(name) + " does not match; excluding.");
            }
            return match;
        }

        private boolean isFileNameMatch(String name) {
            return TextUtils.matches(name, "\\.less$") && TextUtils.matches(name, "^[^.]");
        }
        
        private boolean isNotDirectory(File dir, String name) {
            File file = new File(dir, name);
            return ! file.isDirectory();
        }
    }

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
     * @parameter name="command" default-value="lessc"
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
