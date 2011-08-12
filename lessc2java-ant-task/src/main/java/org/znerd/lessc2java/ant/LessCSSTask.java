// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import static org.apache.tools.ant.Project.MSG_ERR;
import static org.apache.tools.ant.Project.MSG_VERBOSE;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * An Apache Ant task for running a LessCSS compiler on a number of files, to convert them from <code>.less</code>- to <code>.css</code>-format.
 * <p>
 * The most notable parameters supported by this task are:
 * <dl>
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
public final class LessCSSTask extends MatchingTask {

    /**
     * Returns a quoted version of the specified string, or <code>"(null)"</code> if the argument is <code>null</code>.
     * 
     * @param s the character string, can be <code>null</code>, e.g. <code>"foo bar"</code>.
     * @return the quoted string, e.g. <code>"\"foo bar\""</code>, or <code>"(null)"</code> if the argument is <code>null</code>.
     */
    private static final String quote(String s) {
        return s == null ? "(null)" : "\"" + s + '"';
    }

    /**
     * Checks if the specified string is either null or empty (after trimming the whitespace off).
     * 
     * @param s the string to check.
     * @return <code>true</code> if <code>s == null || s.trim().length() &lt; 1</code>; <code>false</code> otherwise.
     */
    private static final boolean isEmpty(String s) {
        return s == null || s.trim().length() < 1;
    }

    /**
     * Checks if the specified abstract path name refers to an existing directory.
     * 
     * @param description the description of the directory, cannot be <code>null</code>.
     * @param path the abstract path name as a {@link File} object.
     * @param mustBeReadable <code>true</code> if the directory must be readable.
     * @param mustBeWritable <code>true</code> if the directory must be writable.
     * @throws IllegalArgumentException if <code>location == null
     *          || {@linkplain TextUtils}.{@linkplain TextUtils#isEmpty(String) isEmpty}(description)</code>.
     * @throws BuildException if <code>  path == null
     *          || ! path.exists()
     *          || ! path.isDirectory()
     *          || (mustBeReadable &amp;&amp; !path.canRead())
     *          || (mustBeWritable &amp;&amp; !path.canWrite())</code>.
     */
    private static final void checkDir(String description, File path, boolean mustBeReadable, boolean mustBeWritable) throws IllegalArgumentException, BuildException {

        // Check preconditions
        if (description == null) {
            throw new IllegalArgumentException("description == null");
        } else if ("".equals(description)) {
            throw new IllegalArgumentException("description is empty");

        // Make sure the path refers to an existing directory
        } else if (path == null) {
            throw new BuildException(description + " is not set.");
        } else if (!path.exists()) {
            throw new BuildException(description + " (\"" + path + "\") does not exist.");
        } else if (!path.isDirectory()) {
            throw new BuildException(description + " (\"" + path + "\") is not a directory.");

            // Make sure the directory is readable, if that is required
        } else if (mustBeReadable && (!path.canRead())) {
            throw new BuildException(description + " (\"" + path + "\") is not readable.");

            // Make sure the directory is writable, if that is required
        } else if (mustBeWritable && (!path.canWrite())) {
            throw new BuildException(description + " (\"" + path + "\") is not writable.");
        }
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

        // Source directory defaults to current directory
        if (_sourceDir == null) {
            _sourceDir = getProject().getBaseDir();
        }

        // Destination directory defaults to source directory
        if (_destDir == null) {
            _destDir = _sourceDir;
        }

        // Check the directories
        checkDir("Source directory", _sourceDir, true, false);
        checkDir("Destination directory", _destDir, false, true);

        // Create a watch dog, if there is a time-out configured
        ExecuteWatchdog watchdog = (_timeOut > 0L) ? new ExecuteWatchdog(_timeOut) : null;

        // Check that the command is executable
        Buffer buffer = new Buffer();
        Execute execute = new Execute(buffer, watchdog);
        String[] cmdline = new String[] { _command, "-v" };
        execute.setAntRun(getProject());
        execute.setCommandline(cmdline);
        try {
            if (execute.execute() != 0) {
                throw new BuildException("Unable to execute LessCSS command " + quote(_command) + ". Running it with the argument \"-v\" resulted in exit code " + execute.getExitValue() + '.');
            }
        } catch (IOException cause) {
            throw new BuildException("Unable to execute LessCSS command " + quote(_command) + '.', cause);
        }

        // Display the command and version number
        String versionString = buffer.getOutString().trim();
        if (versionString.startsWith(_command)) {
            versionString = versionString.substring(_command.length()).trim();
        }
        if (versionString.startsWith("v")) {
            versionString = versionString.substring(1).trim();
        }
        log("Using command " + quote(_command) + ", version is " + quote(versionString) + '.', MSG_VERBOSE);

        // TODO: Improve this, detect kind of command
        boolean createOutputFile = _command.indexOf("plessc") >= 0;

        // Preparations done, consider each individual file for processing
        log("Transforming from " + _sourceDir.getPath() + " to " + _destDir.getPath() + '.', MSG_VERBOSE);
        long start = System.currentTimeMillis();
        int failedCount = 0, successCount = 0, skippedCount = 0;
        for (String inFileName : getDirectoryScanner(_sourceDir).getIncludedFiles()) {

            // Make sure the input file exists
            File inFile = new File(_sourceDir, inFileName);
            if (!inFile.exists()) {
                continue;
            }

            // Determine if the file type is supported
            if (inFileName.toLowerCase().matches("\\.less$")) {
                log("Skipping " + quote(inFileName) + " because the file does not end in \".less\" (case-insensitive).", MSG_VERBOSE);
                skippedCount++;
                continue;
            }

            // Some preparations related to the input file and output file
            long thisStart = System.currentTimeMillis();
            String outFileName = inFile.getName().replaceFirst("\\.less$", ".css");
            File outFile = new File(_destDir, outFileName);
            String outFilePath = outFile.getPath();
            String inFilePath = inFile.getPath();

            if (!_overwrite) {

                // Skip this file is the output file exists and is newer
                if (outFile.exists() && (outFile.lastModified() > inFile.lastModified())) {
                    log("Skipping " + quote(inFileName) + " because output file is newer.", MSG_VERBOSE);
                    skippedCount++;
                    continue;
                }
            }

            // Prepare for the command execution
            buffer = new Buffer();
            watchdog = (_timeOut > 0L) ? new ExecuteWatchdog(_timeOut) : null;
            execute = new Execute(buffer, watchdog);
            cmdline = createOutputFile ? new String[] { _command, inFilePath } : new String[] { _command, inFilePath, outFilePath };

            execute.setAntRun(getProject());
            execute.setCommandline(cmdline);

            // Execute the command
            boolean failure;
            try {
                execute.execute();
                failure = execute.isFailure();
            } catch (IOException cause) {
                failure = true;
            }

            // Output to stderr or stdout indicates a failure
            String errorOutput = buffer.getErrString();
            errorOutput = (errorOutput == null || "".equals(errorOutput)) ? buffer.getOutString() : errorOutput;
            failure = failure ? true : !isEmpty(errorOutput);

            // Create the output file if the command just sent everything to
            // standard out
            if (createOutputFile) {
                try {
                    buffer.writeOutTo(new FileOutputStream(outFile));
                } catch (IOException cause) {
                    throw new BuildException("Failed to write output to file \"" + outFile.getPath() + "\".", cause);
                }
            }

            // Log the result for this individual file
            long thisDuration = System.currentTimeMillis() - thisStart;
            if (failure) {
                String logMessage = "Failed to transform " + quote(inFilePath);
                if (isEmpty(errorOutput)) {
                    logMessage += '.';
                } else {
                    logMessage += ':' + System.getProperty("line.separator") + errorOutput;
                }
                log(logMessage, MSG_ERR);
                failedCount++;
            } else {
                log("Transformed " + quote(inFileName) + " in " + thisDuration + " ms.", MSG_VERBOSE);
                successCount++;
            }
        }

        // Log the total result
        long duration = System.currentTimeMillis() - start;
        if (failedCount > 0) {
            throw new BuildException("" + failedCount + " file(s) failed to transform, while " + successCount + " succeeded. Total duration is " + duration + " ms.");
        } else {
            log("" + successCount + " file(s) transformed in " + duration + " ms; " + skippedCount + " file(s) skipped.");
        }
    }
}
