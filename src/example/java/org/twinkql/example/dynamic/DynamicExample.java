package org.twinkql.example.dynamic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.example.param.Novel;
import org.twinkql.template.TwinkqlTemplate;
import org.twinkql.template.TwinkqlTemplateFactory;

public class DynamicExample {
	
	public static void main(String[] args){
		DynamicExample extensionExample = new DynamicExample();
		
		extensionExample.runExample();
	}
	
	private void runExample(){
		TwinkqlContextFactory factory = new TwinkqlContextFactory(
				"http://dbpedia.org/sparql",
				"classpath:org/twinkql/example/dynamic/*.xml");
		
		TwinkqlTemplate template = 
			new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate();
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("keywordSearch", "farm");
		
		List<Novel> novels = template.selectForList("dynamic", "getNovel", params, Novel.class);
	
		System.out.println(novels);	
	}
}
