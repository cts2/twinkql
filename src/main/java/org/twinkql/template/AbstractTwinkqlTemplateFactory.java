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
package org.twinkql.template;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.twinkql.context.TwinkqlContext;


/**
 * A factory for creating TwinkqlTemplate objects.
 */
public abstract class AbstractTwinkqlTemplateFactory {
	
	private static String PACKAGE_SCAN = "org.twinkql";

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	/**
	 * Gets the twinkql template.
	 *
	 * @return the twinkql template
	 * @throws Exception the exception
	 */
	public TwinkqlTemplate getTwinkqlTemplate() throws Exception {
		
		DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
		
		parentBeanFactory.registerSingleton("twinkqlContext", this.getTwinkqlContext());

		GenericApplicationContext parentContext = 
		        new GenericApplicationContext(parentBeanFactory);
		
		parentContext.refresh();
		
		AnnotationConfigApplicationContext annotationConfigApplicationContext = 
			this.decorateContext(new AnnotationConfigApplicationContext());
        annotationConfigApplicationContext.setParent(parentContext);
        annotationConfigApplicationContext.scan(PACKAGE_SCAN);
        annotationConfigApplicationContext.refresh();

		TwinkqlTemplate template = annotationConfigApplicationContext.getBean(TwinkqlTemplate.class);
		
		return template;
	}
	
	/**
	 * Gets the twinkql context.
	 *
	 * @return the twinkql context
	 */
	protected abstract TwinkqlContext getTwinkqlContext();

	/**
	 * Decorate context.
	 *
	 * @param applicationContext the application context
	 * @return the annotation config application context
	 */
	protected AnnotationConfigApplicationContext decorateContext(
			AnnotationConfigApplicationContext applicationContext) {
		//for subclasses
		return applicationContext;
	}

}
