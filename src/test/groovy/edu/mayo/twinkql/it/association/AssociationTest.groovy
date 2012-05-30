package edu.mayo.twinkql.it.association;

import static org.junit.Assert.*;

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.twinkql.template.TwinkqlTemplate

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations=["../it-context.xml"])
class AssociationTest {
	
	@Resource
	TwinkqlTemplate template

	@Test
	void TestShortHandCollectionNotation(){
		def result = template.selectForList("association", "whatGovernorsAreShort", null, null)
		
		assertNotNull result
		
		assertTrue result.size() > 0
		
		assertTrue result.size() < 100
		
		result.each {
			assertTrue it.isA.size() > 1
		}
	}
	
	@Test
	void TestFullAssociationNotation(){
		def result = template.selectForList("association", "whatGovernorsAreObject", null, null)
		
		assertNotNull result
		
		assertTrue result.size() > 0
		
		assertTrue result.size() < 100
		
		result.each {
			assertNull it.isA 
			
			assertNotNull it.isAObject
			assertNotNull it.isAObject.isA
			assertTrue it.isAObject.isA.size() > 1
		}
	}
}
