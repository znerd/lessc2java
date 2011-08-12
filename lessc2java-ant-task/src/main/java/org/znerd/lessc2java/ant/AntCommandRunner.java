// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java.ant;

import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.znerd.lessc2java.CommandRunResult;
import org.znerd.lessc2java.CommandRunner;

public class AntCommandRunner implements CommandRunner {

    public AntCommandRunner(Project project, long timeOut) {
        if (project == null) {
            throw new IllegalArgumentException("project == null");
        }
        _project = project;
        _timeOut = timeOut;
    }

    private Project _project;
    private long _timeOut;

    @Override
    public CommandRunResult runCommand(String command, String... arguments) {
        long start = System.currentTimeMillis();
        String[] cmdline = createCmdline(command, arguments);
        ExecuteWatchdog watchdog = (_timeOut > 0L) ? new ExecuteWatchdog(_timeOut) : null;
        Buffer buffer = new Buffer();
        Execute execute = new Execute(buffer, watchdog);
        execute.setAntRun(_project);
        execute.setCommandline(cmdline);

        CommandRunResult result = new CommandRunResult();
        int exitCode;
        try {
            exitCode = execute.execute();
        } catch (IOException cause) {
            result.setException(cause);
            exitCode = -1;
        }

        result.setDuration(System.currentTimeMillis() - start);
        result.setExitCode(exitCode);
        result.setOutString(buffer.getOutString());
        result.setErrString(buffer.getErrString());

        return result;
    }

    private String[] createCmdline(String command, String... arguments) {
        final int argumentCount = arguments.length;
        final String[] cmdline = new String[argumentCount + 1];
        cmdline[0] = command;
        System.arraycopy(arguments, 0, cmdline, 1, argumentCount);
        return cmdline;
    }
}
