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
package org.twinkql.result;

import java.util.Collection;

import javax.annotation.Resource;

import jodd.bean.BeanUtil;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.twinkql.instance.BeanInstantiator;
import org.twinkql.result.callback.Modifier;

import org.twinkql.model.Association;
import org.twinkql.model.RowMap;

/**
 * The Class PropertySetter.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class PropertySetter {
	
	@Resource
	private BeanInstantiator beanInstantiator;
	
	/**
	 * Instantiates a new property setter.
	 */
	public PropertySetter(){
		super();
	}
	
	/**
	 * Instantiates a new property setter.
	 *
	 * @param beanInstantiator the bean instantiator
	 */
	public PropertySetter(BeanInstantiator beanInstantiator){
		super();
		this.beanInstantiator = beanInstantiator;
	}

	/**
	 * Sets the property.
	 *
	 * @param targetObj the target obj
	 * @param result the result
	 * @param association the association
	 * @param tracker the tracker
	 */
	public void setProperty(
			Object targetObj, 
			Object result, 
			Association association, 
			Tracker tracker){
		String callbackId = association.getCallbackId();
		String property = association.getBeanProperty();
		
		result = this.processModifier(association.getModifier(), result);
		
		this.setProperty(targetObj, result, property, callbackId, tracker);
	}
	
	/**
	 * Sets the property.
	 *
	 * @param targetObj the target obj
	 * @param result the result
	 * @param rowMap the row map
	 * @param tracker the tracker
	 */
	public void setProperty(
			Object targetObj, 
			Object result, 
			RowMap rowMap, 
			Tracker tracker){
		String callbackId = rowMap.getCallbackId();
		String property = rowMap.getBeanProperty();
		
		result = this.processModifier(rowMap.getModifier(), result);
		
		this.setProperty(targetObj, result, property, callbackId, tracker);
	}
	
	/**
	 * Sets the property.
	 *
	 * @param targetObj the target obj
	 * @param result the result
	 * @param property the property
	 * @param callbackId the callback id
	 * @param tracker the tracker
	 */
	private void setProperty(
			Object targetObj, 
			Object result, 
			String property, 
			String callbackId, 
			Tracker tracker){
	
		if(StringUtils.isNotBlank(callbackId)){
			tracker.getCallbackParams().put(callbackId, result);
		}
		
		if(StringUtils.isNotBlank(property)){
			
			property = this.adjustForCollection(targetObj, property);
			
			BeanUtil.setPropertyForced(targetObj,
					property, result);
		}	
	}
	
	/**
	 * Adjust for collection.
	 *
	 * @param target the target
	 * @param property the property
	 * @return the string
	 */
	private String adjustForCollection(Object target, String property){
		if(StringUtils.countMatches(property, "[]") > 1){
			throw new MappingException("Cannot have more than one Collection Indicator ('[]') in a 'property' attribute.");
		}
		
		String[] parts = StringUtils.split(property, '.');
		
		for(int i=0;i<parts.length;i++){
			String part = parts[i];
			if(StringUtils.endsWith(part, "[]")){
				String propertySoFar = 
					StringUtils.removeEnd(StringUtils.join(
						ArrayUtils.subarray(parts, 0, i+1), '.'), "[]");
				
				Collection<?> collection = (Collection<?>) BeanUtil.getSimplePropertyForced(target, propertySoFar, true);
				int index = collection.size();
				
				parts[i] = StringUtils.replace(part, "[]", "["+index+"]");
			}
		}
		
		return StringUtils.join(parts, '.');
	}
	
	/**
	 * Process modifier.
	 *
	 * @param modifierString the modifier string
	 * @param result the result
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	private Object processModifier(String modifierString, Object result){
		
		if(StringUtils.isNotBlank(modifierString)){
			Modifier<Object> modifier = 
				this.beanInstantiator.instantiate(modifierString, Modifier.class, false);
			
			result = modifier.beforeSetting(result);
		}
		
		return result;
	}
}
