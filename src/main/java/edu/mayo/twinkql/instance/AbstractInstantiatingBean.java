package edu.mayo.twinkql.instance;

import org.apache.commons.lang.StringUtils;

import edu.mayo.twinkql.context.TwinkqlContext;

public class AbstractInstantiatingBean {
	
	private TwinkqlContext twinkqlContext;
	
	protected AbstractInstantiatingBean(TwinkqlContext twinkqlContext){
		super();
		this.twinkqlContext = twinkqlContext;
	}

	protected Instantiator getInstantiator(String beanName){
		String prefix = null;
		if(StringUtils.contains(beanName, ':')){
			prefix = StringUtils.substringBefore(beanName, ":");
		}
		
		for(Instantiator instantiator : this.twinkqlContext.getInstantiators()){
			if(StringUtils.equals(prefix, instantiator.getPrefix())){
				return instantiator;
			}
		}
		
		return null;
	}
	
}
