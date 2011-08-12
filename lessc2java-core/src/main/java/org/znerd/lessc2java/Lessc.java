// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java;

import java.io.File;
import java.io.IOException;

public final class Lessc {

    public static void compile(CommandRunner commandRunner, File baseDir, File sourceDir, String[] includedFiles, File targetDir, String command, long timeOut, boolean overwrite) throws IOException {
        File actualSourceDir = determineSourceDir(baseDir, sourceDir);
        File actualTargetDir = determineTargetDir(actualSourceDir, targetDir);

        LesscExecutor executor = new LesscExecutor(commandRunner, actualSourceDir, includedFiles, actualTargetDir, command, overwrite);
        executor.execute();
    }

    private static File determineSourceDir(final File baseDir, final File specifiedSourceDir) {
        File actualSourceDir;
        if (specifiedSourceDir == null) {
            actualSourceDir = baseDir;
        } else {
            actualSourceDir = specifiedSourceDir;
        }
        return actualSourceDir;
    }

    private static File determineTargetDir(final File sourceDir, final File specifiedTargetDir) {
        File actualTargetDir;
        if (specifiedTargetDir == null) {
            actualTargetDir = sourceDir;
        } else {
            actualTargetDir = specifiedTargetDir;
        }
        return actualTargetDir;
    }

    private Lessc() {
    }
}
