package edu.mayo.twinkql.result.beans;

public class NestedPropertyInstantiationException extends RuntimeException {

	private static final long serialVersionUID = 5966842033787997277L;
	
	public NestedPropertyInstantiationException(Class<?> propertyClass){
		super("Could not instantiate nested Bean property of class: " + propertyClass + ".");
	}

}
