package edu.mayo.twinkql.instance;

import static org.junit.Assert.*

import org.junit.Test

class SpringBeanNameInstantiatorTest {
	
	@Test
	void TestGetBeanName(){
		SpringBeanNameInstantiator i = new SpringBeanNameInstantiator();
		
		def beanName = i.getBeanName("spring:someBean")
		
		assertEquals "someBean", beanName
	}

}
