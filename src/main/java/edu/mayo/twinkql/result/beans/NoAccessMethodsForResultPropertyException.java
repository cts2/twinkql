package edu.mayo.twinkql.result.beans;

public class NoAccessMethodsForResultPropertyException extends RuntimeException {

	private static final long serialVersionUID = 5966842033787997277L;
	
	public NoAccessMethodsForResultPropertyException(String propertyName){
		super("Could not Get/Set value for: " + propertyName + ". Are Getter/Setter methods available?");
	}

}
