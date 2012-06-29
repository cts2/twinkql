package org.twinkql.example.association;

import java.util.Arrays;
import java.util.List;

import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.template.TwinkqlTemplate;
import org.twinkql.template.TwinkqlTemplateFactory;

public class AssociationExample {
	
	public static void main(String[] args){
		AssociationExample associationExample = new AssociationExample();
		
		associationExample.runExample();
	}
	
	private void runExample(){
		TwinkqlContextFactory factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/association/*.xml");

		TwinkqlTemplate template = 
			new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate();
				
		List<String> selectsToRun = Arrays.asList(
				"whatGovernorsAreShort",
				"whatGovernorsAreObject",
				"whatGovernorsAreDoubleObject",
				"whatGovernorsAreNonInline");
		
		for(String select : selectsToRun){
			System.out.println("===========================");
			System.out.println("Running SELECT: " + select);
			List<Governor> governors = 
				template.selectForList("association", select, null, Governor.class);
			
			System.out.println(governors);
			System.out.println("===========================");
		}
	}
}
