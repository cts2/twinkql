package edu.mayo.twinkql.result;

public class MappingException extends RuntimeException {

	private static final long serialVersionUID = 9165099812090727392L;

	public MappingException(String message, Throwable ex) {
		super(message, ex);
	}

	public MappingException(String message) {
		super(message);
	}
}
