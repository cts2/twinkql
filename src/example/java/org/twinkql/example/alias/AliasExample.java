package org.twinkql.example.alias;

import org.twinkql.context.ConfigBuilder;
import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.model.AliasDefinition;
import org.twinkql.template.TwinkqlTemplate;
import org.twinkql.template.TwinkqlTemplateFactory;

public class AliasExample {
	
	public static void main(String[] args){
		AliasExample aliasExample = new AliasExample();
		
		aliasExample.runExample();
	}
	
	private void runExample(){
		TwinkqlContextFactory factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/alias/*.xml");
		
		AliasDefinition alias = new AliasDefinition();
		alias.setId("tetris");
		alias.setType("org.twinkql.example.alias.Tetris");
		
		ConfigBuilder configBuilder = new ConfigBuilder().addAlias(alias);
				
		factory.setConfigBuilder(configBuilder);
				
		TwinkqlTemplate template = 
			new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate();
				
		Tetris tetris = template.selectForObject("alias", "getTetrisComments", null, Tetris.class);
		
		System.out.println(tetris);
	}
}
