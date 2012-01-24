package edu.mayo.twinkql.instance;

import org.springframework.context.ApplicationContext;

public class SpringBeanNameInstantiator implements Instantiator {
	
	private ApplicationContext context;

	public SpringBeanNameInstantiator(ApplicationContext context){
		super();
		this.context = context;
	}
	
	public Object instantiate(String className) {
		return this.context.getBean(className);
	}

	public String getPrefix() {
		return "spring";
	}

}
