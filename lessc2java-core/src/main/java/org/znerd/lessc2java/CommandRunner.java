// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java;

public interface CommandRunner {
    CommandRunResult runCommand(String command, String... arguments);
}
