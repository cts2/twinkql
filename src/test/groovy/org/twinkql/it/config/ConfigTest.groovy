package org.twinkql.it.config;

import static org.junit.Assert.*;


import org.junit.Test

import org.twinkql.context.ContextInitializationException
import org.twinkql.context.TwinkqlContextFactory
import org.twinkql.template.TwinkqlTemplateFactory

class ConfigTest {

	@Test
	void TestConfigureNoSpring(){
		def context = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/it/config/*.xml").getTwinkqlContext();
		
		def template = new TwinkqlTemplateFactory(context).getTwinkqlTemplate()
		
		assertNotNull template
	}
	
	@Test(expected=ContextInitializationException)
	void TestConfigureNoSpringNoMappingFiles(){
		def context = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/it/config/WONT-FIND-ANY*.xml").getTwinkqlContext();
		
		def template = new TwinkqlTemplateFactory(context).getTwinkqlTemplate()
		
		assertNotNull template
	}
}
