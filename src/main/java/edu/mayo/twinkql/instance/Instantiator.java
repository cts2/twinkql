package edu.mayo.twinkql.instance;

public interface Instantiator {
	
	public Object instantiate(String className);
	
	public String getPrefix();

}
