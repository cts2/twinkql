/*
 * Copyright: (c) 2004-2012 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.twinkql.instance;

import java.util.HashMap;
import java.util.Map;

import edu.mayo.twinkql.context.TwinkqlContext;

/**
 * The Class AbstractCachingInstantiatingBean.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class AbstractCachingInstantiatingBean extends AbstractInstantiatingBean {
	
	/**
	 * Instantiates a new abstract caching instantiating bean.
	 */
	protected AbstractCachingInstantiatingBean() {
		super();
	}
	
	/**
	 * Instantiates a new abstract caching instantiating bean.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	protected AbstractCachingInstantiatingBean(TwinkqlContext twinkqlContext) {
		super(twinkqlContext);
	}

	private Map<String,Object> cache = new HashMap<String,Object>();

	/**
	 * Instantiate.
	 *
	 * @param className the class name
	 * @return the object
	 */
	public Object instantiate(String className){
		if(! this.cache.containsKey(className)){
			Object callback = this.getInstantiator(className).instantiate(className);

			this.cache.put(className, callback);
		}

		return this.cache.get(className);
	}
}
