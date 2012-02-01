package edu.mayo.twinkql.instance;

import java.util.HashMap;
import java.util.Map;

import edu.mayo.twinkql.context.TwinkqlContext;

public class AbstractCachingInstantiatingBean extends AbstractInstantiatingBean {
	
	protected AbstractCachingInstantiatingBean(TwinkqlContext twinkqlContext) {
		super(twinkqlContext);
	}

	private Map<String,Object> cache = new HashMap<String,Object>();

	public Object instantiate(String className){
		if(! this.cache.containsKey(className)){
			Object callback = this.getInstantiator(className).instantiate(className);

			this.cache.put(className, callback);
		}

		return this.cache.get(className);
	}
}
