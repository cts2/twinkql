package edu.mayo.twinkql.it.config;

import static org.junit.Assert.*;


import org.junit.Test

import edu.mayo.twinkql.context.ContextInitializationException
import edu.mayo.twinkql.context.TwinkqlContextFactory
import edu.mayo.twinkql.template.TwinkqlTemplateFactory

class ConfigTest {

	@Test
	void TestConfigureNoSpring(){
		def context = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:edu/mayo/twinkql/it/config/*.xml").getTwinkqlContext();
		
		def template = new TwinkqlTemplateFactory(context).getTwinkqlTemplate()
	}
	
	@Test(expected=ContextInitializationException)
	void TestConfigureNoSpringNoMappingFiles(){
		def context = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:edu/mayo/twinkql/it/config/WONT-FIND-ANY*.xml").getTwinkqlContext();
		
		def template = new TwinkqlTemplateFactory(context).getTwinkqlTemplate()
	}
}
