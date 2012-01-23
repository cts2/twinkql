package edu.mayo.twinkql.result.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

public class PropetySetter {
	
	public void setBeanProperty(Object bean, String propertyName, Object value){
		if(value == null){
			return;
		}
		
		String[] propertyChain = propertyName.split("\\.");
		
		if(propertyChain.length > 1){
			this.instantiateNestedProperties(bean, propertyChain);
		}
		
		try {
			BeanUtils.setProperty(bean, propertyName,
					value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	public void instantiateNestedProperties(Object obj, String[] propertyChain) {
	    try {     
	        if (propertyChain.length > 1) {
	            StringBuffer nestedProperty = new StringBuffer();
	            for (int i = 0; i < propertyChain.length - 1; i++) {
	                String fn = propertyChain[i];
	                if (i != 0) {
	                    nestedProperty.append(".");
	                }
	                nestedProperty.append(fn);

	                Object value = PropertyUtils.getProperty(obj, nestedProperty.toString());

	                if (value == null) {
	                    PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(obj, nestedProperty.toString());
	                    Class<?> propertyType = propertyDescriptor.getPropertyType();
	                    Object newInstance = propertyType.newInstance();
	                    PropertyUtils.setProperty(obj, nestedProperty.toString(), newInstance);
	                }
	            }
	        }
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}

}
