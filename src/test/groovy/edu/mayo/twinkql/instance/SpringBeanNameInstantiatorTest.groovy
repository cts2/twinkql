package edu.mayo.twinkql.instance;

import org.junit.Test;

import static org.junit.Assert.*

class SpringBeanNameInstantiatorTest {
	
	@Test
	void TestGetBeanName(){
		SpringBeanNameInstantiator i = new SpringBeanNameInstantiator();
		
		def beanName = i.getBeanName("spring:someBean")
		
		assertEquals "someBean", beanName
	}

}
