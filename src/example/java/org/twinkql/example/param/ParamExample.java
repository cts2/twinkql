package org.twinkql.example.param;

import java.util.HashMap;
import java.util.Map;

import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.template.TwinkqlTemplate;
import org.twinkql.template.TwinkqlTemplateFactory;

public class ParamExample {

	public static void main(String[] args){
		ParamExample paramExample = new ParamExample();
		
		paramExample.runExample();
	}
	
	private void runExample(){
		TwinkqlContextFactory factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/param/*.xml");
		
		TwinkqlTemplate template = 
			new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("novelName", "The Lord of the Rings");
		
		Novel novel = template.selectForObject("param", "getNovel", params, Novel.class);
	
		System.out.println(novel);	
	}
	

}
