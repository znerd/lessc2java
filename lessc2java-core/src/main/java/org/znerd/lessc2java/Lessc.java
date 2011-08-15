// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java;

import java.io.File;
import java.io.IOException;

import org.znerd.util.proc.CommandRunner;

public final class Lessc {

    public static void compile(CommandRunner commandRunner, File sourceDir, String[] includedFiles, File targetDir, String command, boolean overwrite) throws IOException {
        File actualTargetDir = determineTargetDir(sourceDir, targetDir);
        LesscExecutor executor = new LesscExecutor(commandRunner, sourceDir, includedFiles, actualTargetDir, command, overwrite);
        executor.execute();
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
