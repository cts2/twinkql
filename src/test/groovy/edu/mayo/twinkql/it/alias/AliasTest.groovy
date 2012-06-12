package edu.mayo.twinkql.it.alias;

import static org.junit.Assert.*;


import org.junit.Test

import edu.mayo.twinkql.context.ConfigBuilder
import edu.mayo.twinkql.context.TwinkqlContextFactory
import edu.mayo.twinkql.model.AliasDefinition
import edu.mayo.twinkql.template.TwinkqlTemplateFactory

class AliasTest {

	@Test
	void TestAlias(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:edu/mayo/twinkql/it/alias/*.xml")
		
		def configBuilder = new ConfigBuilder().addAlias(new AliasDefinition(id:"tetris", type:"edu.mayo.twinkql.it.alias.Tetris"))
		
		factory.setConfigBuilder(configBuilder)
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def tetris = template.selectForObject("alias", "getTetrisComments", null, Tetris)
	
		assertTrue tetris instanceof Tetris
		
		assertTrue tetris.comments.size() > 1
	}
}
