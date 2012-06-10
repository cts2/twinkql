package edu.mayo.twinkql.context;

public class ContextInitializationException extends RuntimeException {

	private static final long serialVersionUID = 344333018023149377L;
	
	protected ContextInitializationException(String message){
		super("Error initializing the Twinkql Context: " + message);
	}

}
