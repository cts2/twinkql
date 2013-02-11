package org.twinkql.example.iterator;

import java.lang.ArrayStoreException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.example.param.Novel;
import org.twinkql.template.TwinkqlTemplate;
import org.twinkql.template.TwinkqlTemplateFactory;

public class IteratorExample {
	
	public static void main(String[] args){
        IteratorExample iteratorExample = new IteratorExample();

        iteratorExample.runExample();
	}
	
	private void runExample(){
		TwinkqlContextFactory factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/iterator/*.xml");
		
		TwinkqlTemplate template = 
			new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("filters", Arrays.asList("farm"));
		
		List<Novel> novels = template.selectForList("iterator", "getNovel", params, Novel.class);
	
		System.out.println(novels);	
	}
}
