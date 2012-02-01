package edu.mayo.twinkql.instance;


public class DefaultClassForNameInstantiator implements Instantiator {

	public Object instantiate(String className) {
		try {
			return Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	public String getPrefix() {
		//default prefix
		return null;
	}

}
