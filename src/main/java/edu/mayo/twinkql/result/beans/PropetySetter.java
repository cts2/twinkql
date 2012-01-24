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
package edu.mayo.twinkql.result.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * The Class PropetySetter.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class PropetySetter {

	/**
	 * Sets the bean property.
	 *
	 * @param bean the bean
	 * @param propertyName the property name
	 * @param value the value
	 */
	public void setBeanProperty(Object bean, String propertyName, Object value) {
		if (value == null) {
			return;
		}

		String[] propertyChain = propertyName.split("\\.");

		if (propertyChain.length > 1) {
			this.instantiateNestedProperties(bean, propertyChain);
		}

		try {
			BeanUtils.setProperty(bean, propertyName, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Instantiate nested properties.
	 *
	 * @param obj the obj
	 * @param propertyChain the property chain
	 */
	protected void instantiateNestedProperties(Object obj,
			String[] propertyChain) {

		if (propertyChain.length > 1) {
			StringBuffer nestedProperty = new StringBuffer();
			for (int i = 0; i < propertyChain.length - 1; i++) {
				String fn = propertyChain[i];
				if (i != 0) {
					nestedProperty.append(".");
				}
				nestedProperty.append(fn);

				Object value;
				try {
					value = PropertyUtils.getProperty(obj,
							nestedProperty.toString());
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
					throw new IllegalStateException(e);
				} catch (NoSuchMethodException e) {
					throw new NoAccessMethodsForResultPropertyException(
							nestedProperty.toString());
				}

				if (value == null) {
					PropertyDescriptor propertyDescriptor;
					try {
						propertyDescriptor = PropertyUtils
								.getPropertyDescriptor(obj,
										nestedProperty.toString());
					} catch (IllegalAccessException e) {
						throw new IllegalStateException(e);
					} catch (InvocationTargetException e) {
						throw new IllegalStateException(e);
					} catch (NoSuchMethodException e) {
						throw new NoAccessMethodsForResultPropertyException(
								nestedProperty.toString());
					}
					Class<?> propertyType = propertyDescriptor
							.getPropertyType();
					Object newInstance;
					try {
						newInstance = propertyType.newInstance();
					} catch (InstantiationException e) {
						throw new NestedPropertyInstantiationException(
								propertyType);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException(e);
					}
					try {
						PropertyUtils.setProperty(obj,
								nestedProperty.toString(), newInstance);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException(e);
					} catch (InvocationTargetException e) {
						throw new IllegalStateException(e);
					} catch (NoSuchMethodException e) {
						throw new NoAccessMethodsForResultPropertyException(
								nestedProperty.toString());
					}
				}
			}
		}
	}
}
