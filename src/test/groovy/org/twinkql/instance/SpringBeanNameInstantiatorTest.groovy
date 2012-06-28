package org.twinkql.instance;

import org.junit.Test;
import org.twinkql.instance.SpringBeanNameInstantiator;

import static org.junit.Assert.*

class SpringBeanNameInstantiatorTest {
	
	@Test
	void TestGetBeanName(){
		SpringBeanNameInstantiator i = new SpringBeanNameInstantiator();
		
		def beanName = i.getBeanName("spring:someBean")
		
		assertEquals "someBean", beanName
	}

}
