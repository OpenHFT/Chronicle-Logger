/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import java.io.PrintStream;

/**
 * Encapsulates the user's chosen output destination for
 * {@link SimpleLogger}.  This class mirrors the SLF4J&nbsp;1.x
 * implementation. The only difference from the SLF4J&nbsp;1.x source is
 * the package name so that Chronicle can compile against the
 * SLF4J&nbsp;2 APIs without behaviour change.
 * <p>
 * Instances are immutable and therefore thread-safe.  Be aware that
 * {@link System#out} and {@link System#err} may be replaced at runtime,
 * so {@link #getTargetPrintStream()} always resolves the stream on
 * each call.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
class OutputChoice {

    enum OutputChoiceType {
        SYS_OUT, CACHED_SYS_OUT, SYS_ERR, CACHED_SYS_ERR, FILE
    }

    final OutputChoiceType outputChoiceType;
    final PrintStream targetPrintStream;

    OutputChoice(OutputChoiceType outputChoiceType) {
        if (outputChoiceType == OutputChoiceType.FILE) {
            throw new IllegalArgumentException();
        }
        this.outputChoiceType = outputChoiceType;
        if (outputChoiceType == OutputChoiceType.CACHED_SYS_OUT) {
            this.targetPrintStream = System.out;
        } else if (outputChoiceType == OutputChoiceType.CACHED_SYS_ERR) {
            this.targetPrintStream = System.err;
        } else {
            this.targetPrintStream = null;
        }
    }

    OutputChoice(PrintStream printStream) {
        this.outputChoiceType = OutputChoiceType.FILE;
        this.targetPrintStream = printStream;
    }

    PrintStream getTargetPrintStream() {
        switch (outputChoiceType) {
            case SYS_OUT:
                return System.out;
            case SYS_ERR:
                return System.err;
            case CACHED_SYS_ERR:
            case CACHED_SYS_OUT:
            case FILE:
                return targetPrintStream;
            default:
                throw new IllegalArgumentException();
        }
    }
}
