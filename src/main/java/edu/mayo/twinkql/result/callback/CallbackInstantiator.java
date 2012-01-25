/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
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
package edu.mayo.twinkql.result.callback;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.instance.Instantiator;

/**
 * The Class CallbackInstantiator.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class CallbackInstantiator {
	
	private Map<String,Object> callbackCache = new HashMap<String,Object>();
	
	private TwinkqlContext twinkqlContext;
	
	public CallbackInstantiator(TwinkqlContext twinkqlContext){
		super();
		this.twinkqlContext = twinkqlContext;
		
	}
	
	/**
	 * Instantiate after callback.
	 *
	 * @param className the class name
	 * @return the after result binding
	 */
	@SuppressWarnings("unchecked")
	public <T extends Callback> T instantiateCallback(String className, Class<T> requiredType){
		if(! this.callbackCache.containsKey(className)){
			Object callback = this.getInstantiator(className).instantiate(className);
			
			if(! ClassUtils.isAssignable(callback.getClass(), requiredType)){
				throw new RuntimeException();
			}
			
			this.callbackCache.put(className, callback);
		}
		
		
		return (T) this.callbackCache.get(className);
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
