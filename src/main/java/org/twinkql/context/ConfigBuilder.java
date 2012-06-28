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
package org.twinkql.context;

import java.util.Arrays;

import org.twinkql.model.AliasDefinition;
import org.twinkql.model.NamespaceDefinition;
import org.twinkql.model.ReasonerDefinition;
import org.twinkql.model.TwinkqlConfig;
import org.twinkql.model.TwinkqlConfigItem;

/**
 * The Builder-style class for programmatically building a {@link TwinklContext}.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ConfigBuilder {
	
	private TwinkqlConfig twinkqlConfig = new TwinkqlConfig();
	
	/**
	 * The Interface TwinkqlConfigItemBuilder.
	 *
	 * @param <T> the generic type
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private interface TwinkqlConfigItemBuilder<T> {
		
		/**
		 * Adds the.
		 *
		 * @param item the item
		 * @param obj the obj
		 */
		public void add(TwinkqlConfigItem item, T obj);
	}
	
	/**
	 * Adds the reasoner.
	 *
	 * @param definitions the definitions
	 * @return the configuration builder
	 */
	public ConfigBuilder addReasoner(Iterable<ReasonerDefinition> definitions){
		this.addTwinkqlConfigItem(definitions, new TwinkqlConfigItemBuilder<ReasonerDefinition>(){

			public void add(TwinkqlConfigItem item, ReasonerDefinition obj) {
				item.setReasoner(obj);
			}
			
		});
		
		return this;
	}
	
	/**
	 * Adds the reasoner.
	 *
	 * @param definition the definition
	 * @return the configuration builder
	 */
	public ConfigBuilder addReasoner(ReasonerDefinition definition){
		return this.addReasoner(Arrays.asList(definition));
	}
	
	/**
	 * Adds the alias.
	 *
	 * @param definitions the definitions
	 * @return the configuration builder
	 */
	public ConfigBuilder addAlias(Iterable<AliasDefinition> definitions){
		this.addTwinkqlConfigItem(definitions, new TwinkqlConfigItemBuilder<AliasDefinition>(){

			public void add(TwinkqlConfigItem item, AliasDefinition obj) {
				item.setAlias(obj);
			}
			
		});
		
		return this;
	}
	
	/**
	 * Adds the alias.
	 *
	 * @param definition the definition
	 * @return the configuration builder
	 */
	public ConfigBuilder addAlias(AliasDefinition definition){
		return this.addAlias(Arrays.asList(definition));
	}
	
	/**
	 * Adds the namespace.
	 *
	 * @param definitions the definitions
	 * @return the configuration builder
	 */
	public ConfigBuilder addNamespace(Iterable<NamespaceDefinition> definitions){
		this.addTwinkqlConfigItem(definitions, new TwinkqlConfigItemBuilder<NamespaceDefinition>(){

			public void add(TwinkqlConfigItem item, NamespaceDefinition obj) {
				item.setNamespace(obj);
			}
			
		});
		
		return this;
	}
	
	/**
	 * Adds the namespace.
	 *
	 * @param definition the definition
	 * @return the configuration builder
	 */
	public ConfigBuilder addNamespace(NamespaceDefinition definition){
		return this.addNamespace(Arrays.asList(definition));
	}
	
	/**
	 * Builds the.
	 *
	 * @return the twinkql config
	 */
	protected TwinkqlConfig build(){
		return this.twinkqlConfig;
	}
	
	/**
	 * Adds the twinkql config item.
	 *
	 * @param <T> the generic type
	 * @param items the items
	 * @param builder the builder
	 */
	private <T> void addTwinkqlConfigItem(Iterable<T> items, TwinkqlConfigItemBuilder<T> builder) {
		for(T item : items){
			TwinkqlConfigItem configItem = new TwinkqlConfigItem(); 
			builder.add(configItem, item);
			
			this.twinkqlConfig.addTwinkqlConfigItem(configItem);
		}
	}

}
