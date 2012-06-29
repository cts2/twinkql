package org.twinkql.it.dynamic;

import static org.junit.Assert.*;

import org.junit.Test;
import org.twinkql.context.TwinkqlContextFactory
import org.twinkql.example.dynamic.Novel
import org.twinkql.template.TwinkqlTemplateFactory

class DynamicTest {

	@Test
	void TestDynamicSparql(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/dynamic/*.xml")
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def params = ["keywordSearch":"farm"]
		
		def novels = template.selectForList("dynamic", "getNovel", params, Novel)
	
		assertTrue novels.size() > 0
		
		novels.each {
			assertTrue it.novelAbstract.contains("farm")
		}
	}
	

	@Test
	void TestDynamicSparqlNullParam(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/dynamic/*.xml")
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def params = null
		
		def novels = template.selectForList("dynamic", "getNovel", params, Novel)
	
		assertTrue novels.size() > 0
		
		def found = false
		
		novels.each {
			if(! it.novelAbstract.contains("farm") ){
				found = true
			}
		}
		
		assertTrue found
	}
}
