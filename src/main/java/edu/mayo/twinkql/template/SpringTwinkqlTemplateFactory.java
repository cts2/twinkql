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
package edu.mayo.twinkql.template;

import javax.annotation.Resource;

import org.springframework.beans.factory.FactoryBean;

import edu.mayo.twinkql.context.TwinkqlContext;

/**
 * A factory for creating TwinkqlTemplate objects.
 */
public class SpringTwinkqlTemplateFactory extends AbstractTwinkqlTemplateFactory
	implements FactoryBean<TwinkqlTemplate> {
	
	@Resource
	private TwinkqlContext twinkqlContext;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public TwinkqlTemplate getObject() throws Exception {
		return this.getTwinkqlTemplate();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<?> getObjectType() {
		return TwinkqlTemplate.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
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
