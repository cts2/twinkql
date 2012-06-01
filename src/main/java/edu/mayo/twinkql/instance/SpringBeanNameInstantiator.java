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

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;


/**
 * The Class SpringBeanNameInstantiator.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class SpringBeanNameInstantiator implements Instantiator {
	
	private final static String SPRING_PREFIX = "spring";
	
	private ApplicationContext context;

	/**
	 * Instantiates a new spring bean name instantiator.
	 *
	 * @param context the context
	 */
	public SpringBeanNameInstantiator(ApplicationContext context){
		super();
		this.context = context;
	}
	
	/**
	 * Gets the bean name.
	 *
	 * @param alias the alias
	 * @return the bean name
	 */
	protected String getBeanName(String alias){
		return StringUtils.substringAfter(alias, SPRING_PREFIX + ":");
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.instance.Instantiator#instantiate(java.lang.String)
	 */
	public Object instantiate(String alias) {
		return this.context.getBean(
				this.getBeanName(alias));
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.instance.Instantiator#getPrefix()
	 */
	public String getPrefix() {
		return SPRING_PREFIX;
	}

}
