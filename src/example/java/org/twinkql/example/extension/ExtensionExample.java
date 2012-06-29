package org.twinkql.example.extension;

import java.util.List;

import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.template.TwinkqlTemplate;
import org.twinkql.template.TwinkqlTemplateFactory;

public class ExtensionExample {
	
	public static void main(String[] args){
		ExtensionExample extensionExample = new ExtensionExample();
		
		extensionExample.runExample();
	}
	
	private void runExample(){
		TwinkqlContextFactory factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/extension/*.xml");
		
		TwinkqlTemplate template = 
				new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate();

		List<Boxer> results = template.selectForList("extension", "getRandallTexCobb", null, null);
		
		System.out.println(results);
	}
}
