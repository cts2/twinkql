package org.twinkql.it.association;

import static org.junit.Assert.*;

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import org.twinkql.template.TwinkqlTemplate

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
	
	@Test
	void TestDoubleNestedAssociation(){
		def result = template.selectForList("association", "whatGovernorsAreDoubleObject", null, null)
		
		assertNotNull result
		
		assertTrue result.size() > 0
		
		assertTrue result.size() < 100
		
		result.each {
			assertNull it.isA
			
			assertNotNull it.isAObject
			assertNotNull it.isAObject.isA
			
			assertNotNull it.isAObject.details
			assertTrue it.isAObject.details.size() > 0
			
			it.isAObject.details.each {
				assertNotNull it.name
				assertNotNull it.values
				
				assertTrue it.values.size() > 0
			}
			
		}
	}
	
	@Test
	void TestNonInlineAssociation(){
		def result = template.selectForList("association", "whatGovernorsAreNonInline", null, null)
		
		assertNotNull result
		
		assertTrue result.size() > 0
		
		assertTrue result.size() < 100
		
		result.each {
			assertNull it.isA
			
			assertNotNull it.isAObject
			assertNotNull it.isAObject.isA
			
			assertNotNull it.isAObject.details
			assertTrue it.isAObject.details.size() > 0
			
			it.isAObject.details.each {
				assertNotNull it.name
				assertNotNull it.values
				
				assertTrue it.values.size() > 0
			}
			
		}
	}
}
