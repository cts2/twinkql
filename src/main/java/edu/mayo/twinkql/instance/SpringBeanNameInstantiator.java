package edu.mayo.twinkql.instance;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

public class SpringBeanNameInstantiator implements Instantiator {
	
	private final static String SPRING_PREFIX = "spring";
	
	private ApplicationContext context;

	public SpringBeanNameInstantiator(ApplicationContext context){
		super();
		this.context = context;
	}
	
	protected String getBeanName(String alias){
		return StringUtils.substringAfter(alias, SPRING_PREFIX + ":");
	}
	
	public Object instantiate(String alias) {
		return this.context.getBean(
				this.getBeanName(alias));
	}

	public String getPrefix() {
		return SPRING_PREFIX;
	}

}
