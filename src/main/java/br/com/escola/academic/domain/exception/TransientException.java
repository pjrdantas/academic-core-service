package br.com.escola.academic.domain.exception;

public class TransientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TransientException(String message, Throwable cause) {
        super(message, cause);
    }
}