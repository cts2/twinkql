package org.twinkql.it.alias;

import static org.junit.Assert.*;


import org.junit.Test

import org.twinkql.context.ConfigBuilder
import org.twinkql.context.TwinkqlContextFactory
import org.twinkql.model.AliasDefinition
import org.twinkql.model.NamespaceDefinition
import org.twinkql.template.TwinkqlTemplateFactory

class AliasTest {

	@Test
	void TestAlias(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/it/alias/*.xml")
		
		def configBuilder = new ConfigBuilder().addAlias(new AliasDefinition(id:"tetris", type:"org.twinkql.it.alias.Tetris"))
		
		factory.setConfigBuilder(configBuilder)
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def tetris = template.selectForObject("alias", "getTetrisComments", null, Tetris)
	
		assertTrue tetris instanceof Tetris
		
		assertTrue tetris.comments.size() > 1
	}
	
	@Test
	void TestAliasAndNamespaces(){
		def factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/it/alias/*.xml")
		
		def configBuilder = new ConfigBuilder().
			addAlias(new AliasDefinition(id:"tetris", type:"org.twinkql.it.alias.Tetris")).
			addNamespace(new NamespaceDefinition(prefix:"rdfs_alias", uri:"http://www.w3.org/2000/01/rdf-schema#")).
			addNamespace(new NamespaceDefinition(prefix:"dbpedia_alias", uri:"http://dbpedia.org/resource/"))
		
		factory.setConfigBuilder(configBuilder)
		
		def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
		
		def tetris = template.selectForObject("alias", "getTetrisCommentsWithNamespaces", null, Tetris)
	
		assertTrue tetris instanceof Tetris
		
		assertTrue tetris.comments.size() > 1
	}

}
