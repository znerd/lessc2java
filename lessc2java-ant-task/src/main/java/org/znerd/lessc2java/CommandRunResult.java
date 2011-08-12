// Copyright 2011, Ernst de Haan
package org.znerd.lessc2java;

import java.io.IOException;

public class CommandRunResult {

    public void setDuration(long duration) {
        _duration = duration;
    }

    private long _duration;

    public long getDuration() {
        return _duration;
    }

    public void setException(IOException exception) {
        _exception = exception;
    }

    private IOException _exception;

    public IOException getException() {
        return _exception;
    }

    public void setExitCode(int exitCode) {
        _exitCode = exitCode;
    }

    private int _exitCode = 0;

    public int getExitCode() {
        return _exitCode;
    }

    public boolean isSucceeded() {
        return _exitCode == 0 && _exception == null;
    }

    public boolean isFailed() {
        return !isSucceeded();
    }

    public void setOutString(String outString) {
        _outString = outString;
    }

    private String _outString;

    public String getOutString() {
        return _outString;
    }

    public void setErrString(String errString) {
        _errString = errString;
    }

    private String _errString;

    public String getErrString() {
        return _errString;
    }
}
