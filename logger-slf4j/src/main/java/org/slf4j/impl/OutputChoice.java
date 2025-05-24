package org.slf4j.impl;

import java.io.PrintStream;

/**
 * Encapsulates the user's choice of output target.
 *
 * <p>The {@link OutputChoiceType} is selected by
 * {@link SimpleLoggerConfiguration}. Examples:
 * <ul>
 * <li>{@code SYS_OUT} uses {@link System#out} directly.</li>
 * <li>{@code CACHED_SYS_OUT} keeps a cached {@link System#out}.</li>
 * <li>{@code SYS_ERR} uses {@link System#err} directly.</li>
 * <li>{@code CACHED_SYS_ERR} keeps a cached {@link System#err}.</li>
 * <li>{@code FILE} writes to a supplied {@link PrintStream}.</li>
 * </ul>
 *
 * @author Ceki G&uuml;lc&uuml;
 */
class OutputChoice {

    enum OutputChoiceType {
        SYS_OUT, CACHED_SYS_OUT, SYS_ERR, CACHED_SYS_ERR, FILE;
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
