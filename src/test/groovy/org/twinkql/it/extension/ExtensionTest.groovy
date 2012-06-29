package org.twinkql.it.extension;

import static org.junit.Assert.*;

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import org.twinkql.template.TwinkqlTemplate

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations=["../it-context.xml"])
class ExtensionTest {
	
	@Resource
	TwinkqlTemplate template

	@Test
	void TestLabelMatch(){
		def result = template.selectForList("extension", "getRandallTexCobb", null, null)
		
		assertNotNull result
		
		assertEquals 1, result.size()
	}
}
