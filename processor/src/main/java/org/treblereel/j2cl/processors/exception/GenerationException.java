package org.treblereel.j2cl.processors.exception;


public class GenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GenerationException() {
    }

    public GenerationException(String msg) {
        super(msg);
    }

    public GenerationException(Throwable t) {
        super(t);
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
