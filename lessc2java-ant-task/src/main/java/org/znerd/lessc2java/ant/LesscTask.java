// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

import org.znerd.lessc2java.Lessc;
import org.znerd.util.proc.AntCommandRunner;
import org.znerd.util.proc.CommandRunner;

/**
 * An Apache Ant task for running a LessCSS compiler on a number of files, to convert them from <code>.less</code>- to <code>.css</code>-format.
 * <p>
 * The most notable parameters supported by this task are:
 * <dl>
 * <dt>includes
 * <dd>The pattern for files in include. Defaults to <code>*.less</code>.
 * <dt>command
 * <dd>The name of the command to execute. Optional, defaults to <code>lessc</code>.
 * <dt>timeOut
 * <dd>The time-out for each individual invocation of the command, in milliseconds. Optional, defaults to 60000 (60 seconds). Set to 0 or lower to disable the time-out.
 * <dt>dir
 * <dd>The source directory to read <code>.less</code> input files from. Optional, defaults to the project base directory.
 * <dt>toDir
 * <dd>The target directory to write <code>.css</code> output files to. Optional, defaults to the source directory.
 * <dt>includes
 * <dd>The files to match in the source directory. Optional, defaults to all files.
 * <dt>excludes
 * <dd>The files to exclude, even if they are matched by the include filter. Optional, default is empty.
 * <dt>overwrite
 * <dd>When set, even newer files will be overwritten.
 * </dl>
 * <p>
 * This task supports more parameters and contained elements, inherited from {@link MatchingTask}. For more information, see <a href="http://ant.apache.org/manual/dirtasks.html">the Ant site</a>.
 */
public final class LesscTask extends MatchingTask {

    public LesscTask() {
        setIncludes("*.less");
    }

    public void setDir(File dir) {
        _sourceDir = dir;
    }

    private File _sourceDir;

    public void setToDir(File dir) {
        _destDir = dir;
    }

    private File _destDir;

    public void setCommand(String command) {
        _command = command;
    }

    private String _command = DEFAULT_COMMAND;

    public static final String DEFAULT_COMMAND = "lessc";

    public void setTimeOut(long timeOut) {
        _timeOut = timeOut;
    }

    private long _timeOut = DEFAULT_TIMEOUT;

    public static final long DEFAULT_TIMEOUT = 60L * 1000L;

    public void setOverwrite(boolean overwrite) {
        _overwrite = true;
    }

    private boolean _overwrite;

    @Override
    public void execute() throws BuildException {
        CommandRunner commandRunner = new AntCommandRunner(getProject(), _timeOut);
        File sourceDir = determineSourceDir();
        String[] includedFiles = getDirectoryScanner(sourceDir).getIncludedFiles();
        try {
            Lessc.compile(commandRunner, sourceDir, includedFiles, _destDir, _command, _overwrite);
        } catch (IOException cause) {
            throw new BuildException("Lessc processing failed", cause);
        }
    }
    
    private File determineSourceDir() {
        final File baseDir = determineProjectBaseDir();
        File actualSourceDir;
        if (_sourceDir == null) {
            actualSourceDir = baseDir;
        } else {
            actualSourceDir = _sourceDir;
        }
        return actualSourceDir;
    }

    private File determineProjectBaseDir() {
        return getProject().getBaseDir();
    }
}
