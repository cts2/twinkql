package org.twinkql.it.param;

import static org.junit.Assert.*;


import org.junit.Test

import org.twinkql.context.ConfigBuilder
import org.twinkql.context.TwinkqlContextFactory
import org.twinkql.model.AliasDefinition
import org.twinkql.model.NamespaceDefinition
import org.twinkql.template.TwinkqlTemplateFactory

class ParamTest {

	@Test
	void TestParameter(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/it/param/*.xml")
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def params = ["novelName":"The Lord of the Rings"]
		
		def novel = template.selectForObject("param", "getNovel", params, Novel)
	
		assertTrue novel instanceof Novel
		
		assertEquals "http://dbpedia.org/resource/The_Lord_of_the_Rings", novel.novel
		assertEquals "http://dbpedia.org/resource/J._R._R._Tolkien", novel.author
		
	}
	

}
