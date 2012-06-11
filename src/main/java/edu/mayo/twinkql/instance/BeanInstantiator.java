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
package edu.mayo.twinkql.instance;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.AliasDefinition;
import edu.mayo.twinkql.model.TwinkqlConfig;
import edu.mayo.twinkql.model.TwinkqlConfigItem;

/**
 * The Class CallbackInstantiator.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BeanInstantiator extends AbstractCachingInstantiatingBean
	implements InitializingBean {
	
	private Map<String,AliasDefinition> aliases = new HashMap<String,AliasDefinition>();
	
	public void afterPropertiesSet() throws Exception {
		TwinkqlContext twinkqlContext = this.getTwinkqlContext();
		
		this.cacheAliases(twinkqlContext.getTwinkqlConfig());
	}
	
	private void cacheAliases(TwinkqlConfig twinkqlConfig){
		if(twinkqlConfig != null){
			for(TwinkqlConfigItem item : twinkqlConfig.getTwinkqlConfigItem()){
				AliasDefinition alias = item.getAlias();
				if(alias != null){
					this.aliases.put(alias.getId(), alias);
				}
			}
		}
	}
	
	/**
	 * Instantiates a new bean instantiator.
	 */
	public BeanInstantiator(){
		super();
	}
	
	/**
	 * Instantiates a new bean instantiator.
	 *
	 * @param twinkqlContext the twinkql context
	 */
	public BeanInstantiator(TwinkqlContext twinkqlContext){
		super(twinkqlContext);
	}
	
	/**
	 * Instantiate after callback.
	 *
	 * @param <T> the generic type
	 * @param className the class name
	 * @param requiredType the required type
	 * @return the after result binding
	 */
	public <T> T instantiate(String classNameOrAlias, boolean newInstance) {
		return this.instantiate(classNameOrAlias, null, newInstance);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T instantiate(String classNameOrAlias, Class<T> requiredType, boolean newInstance) {
		classNameOrAlias = StringUtils.strip(classNameOrAlias);
		
		classNameOrAlias = this.convertAliasToClassName(classNameOrAlias);
		
		Object callback = this.doInstantiate(classNameOrAlias, newInstance);
			
		if(requiredType != null){
			if(! ClassUtils.isAssignable(callback.getClass(), requiredType)){
				throw new RuntimeException();
			}
		}
		
		return (T) callback;
	}
	
	protected String convertAliasToClassName(String classNameOrAlias){
		if(this.aliases.containsKey(classNameOrAlias)){
			return this.aliases.get(classNameOrAlias).getType();
		} else {
			return classNameOrAlias;
		}
	}
}
