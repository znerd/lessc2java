// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java;

import java.io.File;
import java.io.IOException;

import static org.znerd.util.io.DirectoryUtils.checkDir;
import static org.znerd.util.log.Limb.log;
import static org.znerd.util.log.LogLevel.*;

import org.znerd.util.proc.CommandRunResult;
import org.znerd.util.proc.CommandRunner;
import static org.znerd.util.text.TextUtils.isEmpty;

class LesscExecutor {

    private static void illegalArgumentCheck(boolean cond, String message) {
        if (cond) {
            throw new IllegalArgumentException(message);
        }
    }

    public LesscExecutor(CommandRunner commandRunner, File sourceDir, String[] includes, File targetDir, String command, boolean overwrite) {
        illegalArgumentCheck(commandRunner == null, "commandRunner == null");
        illegalArgumentCheck(sourceDir == null, "sourceDir == null");
        illegalArgumentCheck(includes == null, "includes == null");

        _commandRunner = commandRunner;
        _sourceDir = sourceDir;
        _includes = includes;
        _targetDir = targetDir;
        _command = command;
        _overwrite = overwrite;
    }

    private final CommandRunner _commandRunner;
    private final File _sourceDir;
    private final String[] _includes;
    private final File _targetDir;
    private final String _command;
    private final boolean _overwrite;

    public void execute() throws IOException {
        checkDirs();
        logCommandVersion();
        processFiles(_sourceDir, _targetDir);
    }

    private void checkDirs() throws IOException {
        checkDir("Source directory", _sourceDir, true, false, false);
        checkDir("Destination directory", _targetDir, false, true, true);
    }

    private void logCommandVersion() {
        String output;
        try {
            output = executeVersionCommand();
        } catch (IOException cause) {
            log(INFO, "Failed to determine command version.");
            return;
        }
        String version = parseVersionString(output);
        log(INFO, "Using command " + quote(_command) + ", version is " + quote(version) + '.');
    }

    private String executeVersionCommand() throws IOException {
        CommandRunResult runResult = _commandRunner.runCommand(_command, "-v");
        IOException cause = runResult.getException();
        if (cause != null) {
            throw new IOException("Failed to execute LessCSS command " + quote(_command) + '.', cause);
        }
        int exitCode = runResult.getExitCode();
        if (exitCode != 0) {
            throw new IOException("Failed to execute LessCSS command " + quote(_command) + " with the argument \"-v\". Received exit code " + exitCode + '.');
        }
        return runResult.getOutString();
    }

    private String parseVersionString(final String output) {
        String version = output.trim();
        if (version.startsWith(_command)) {
            version = version.substring(_command.length()).trim();
        }
        if (version.startsWith("v")) {
            version = version.substring(1).trim();
        }
        return version;
    }

    private static final String quote(String s) {
        return s == null ? "(null)" : "\"" + s + '"';
    }

    private void processFiles(File sourceDir, File destDir) throws IOException {
        log(INFO, "Transforming from " + sourceDir.getPath() + " to " + destDir.getPath() + '.');
        long start = System.currentTimeMillis();
        int failedCount = 0, successCount = 0, skippedCount = 0;
        for (String inFileName : _includes) {
            switch (processFile(sourceDir, destDir, inFileName)) {
                case SKIPPED:
                    skippedCount++;
                    break;
                case SUCCEEDED:
                    successCount++;
                    break;
                case FAILED:
                    failedCount++;
                    break;
            }
        }
        handleGrandResult(start, skippedCount, successCount, failedCount);
    }

    private void handleGrandResult(long start, int skippedCount, int successCount, int failedCount) throws IOException {
        long duration = System.currentTimeMillis() - start;
        if (failedCount > 0) {
            throw new IOException("" + failedCount + " file(s) failed to transform, while " + successCount + " succeeded. Total duration is " + duration + " ms.");
        } else {
            log(NOTICE, "" + successCount + " file(s) transformed in " + duration + " ms; " + skippedCount + " file(s) skipped.");
        }
    }

    private FileTransformResult processFile(File sourceDir, File destDir, String inFileName) {

        File inFile = new File(sourceDir, inFileName);
        long thisStart = System.currentTimeMillis();
        String outFileName = inFile.getName().replaceFirst("\\.less$", ".css");
        File outFile = new File(destDir, outFileName);
        String outFilePath = outFile.getPath();
        String inFilePath = inFile.getPath();

        FileTransformResult result;
        if (shouldSkipFile(inFileName, inFile, outFile)) {
            result = FileTransformResult.SKIPPED;
        } else {
            result = transform(inFileName, thisStart, outFilePath, inFilePath);
        }
        return result;
    }

    private boolean shouldSkipFile(String inFileName, File inFile, File outFile) {
        boolean skip = false;
        if (!_overwrite) {
            if (outFile.exists() && (outFile.lastModified() > inFile.lastModified())) {
                log(INFO, "Skipping " + quote(inFileName) + " because output file is newer.");
                skip = true;
            }
        }
        return skip;
    }

    private FileTransformResult transform(String inFileName, long thisStart, String outFilePath, String inFilePath) {
        File workingDirectory = new File(inFilePath).getParentFile();
        CommandRunResult runResult = _commandRunner.runCommand(workingDirectory, _command, inFilePath, outFilePath);
        long duration = System.currentTimeMillis() - thisStart;
        if (runResult.isSucceeded()) {
            logSucceededTransformation(inFileName, duration);
            return FileTransformResult.SUCCEEDED;
        } else {
            logFailedTransformation(inFilePath, duration, runResult.getOutString(), runResult.getErrString());
            return FileTransformResult.FAILED;
        }
    }

    private void logFailedTransformation(String inFilePath, long duration, String stdout, String stderr) {
        String logMessage = "Failed to transform " + quote(inFilePath) + " (took " + duration + " ms). ";
        if (!isEmpty(stderr)) {
            logMessage += "Stderr output was received:" + System.getProperty("line.separator") + stderr;
        } else if (!isEmpty(stdout)) {
            logMessage += "No stderr output was received, but stdout was received:" + System.getProperty("line.separator") + stdout;
        } else {
            logMessage += "No output was received on either stderr or stdout.";
        }
        log(ERROR, logMessage);
    }

    private void logSucceededTransformation(String inFileName, long duration) {
        log(INFO, "Transformed " + quote(inFileName) + " (took " + duration + " ms).");
    }

    private enum FileTransformResult {
        SKIPPED, SUCCEEDED, FAILED;
    }
}
