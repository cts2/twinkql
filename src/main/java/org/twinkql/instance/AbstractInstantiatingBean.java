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
package org.twinkql.instance;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.twinkql.context.TwinkqlContext;


/**
 * The Class AbstractInstantiatingBean.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class AbstractInstantiatingBean {
	
	@Autowired
	private TwinkqlContext twinkqlContext;
	
	/**
	 * Instantiates a new abstract instantiating bean.
	 */
	protected AbstractInstantiatingBean(){
		super();
	}
	
	/**
	 * Instantiates a new abstract instantiating bean.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	protected AbstractInstantiatingBean(TwinkqlContext twinkqlContext){
		super();
		this.twinkqlContext = twinkqlContext;
	}

	/**
	 * Gets the instantiator.
	 *
	 * @param beanName the bean name
	 * @return the instantiator
	 */
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

	/**
	 * Gets the twinkql context.
	 *
	 * @return the twinkql context
	 */
	public TwinkqlContext getTwinkqlContext() {
		return twinkqlContext;
	}

	/**
	 * Sets the twinkql context.
	 *
	 * @param twinkqlContext the new twinkql context
	 */
	public void setTwinkqlContext(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
	}
}
