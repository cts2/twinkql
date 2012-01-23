package edu.mayo.twinkql.result.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

public class PropetySetter {

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
