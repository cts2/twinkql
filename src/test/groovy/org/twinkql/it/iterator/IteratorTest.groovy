package org.twinkql.it.iterator

import org.junit.Test
import org.twinkql.context.TwinkqlContextFactory
import org.twinkql.template.TwinkqlTemplateFactory
import org.twinkql.example.dynamic.Novel

import static org.junit.Assert.assertTrue

class IteratorTest {

	@Test
	void TestIteratorSparql(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/iterator/*.xml")
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def params = ["filters":["farm", "animal"]]
		
		def novels = template.selectForList("iterator", "getNovel", params, Novel)
	
		assertTrue novels.size() > 0
		
		novels.each {
			assertTrue it.novelAbstract.toLowerCase().contains("farm")
            assertTrue it.novelAbstract.toLowerCase().contains("animal")
		}
	}

}
