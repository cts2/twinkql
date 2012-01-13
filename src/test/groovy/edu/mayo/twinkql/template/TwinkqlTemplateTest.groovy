package edu.mayo.twinkql.template;

import static org.junit.Assert.*;

import org.junit.Test

import edu.mayo.sparqler.model.SparqlMap
import edu.mayo.sparqler.model.SparqlMappings

public class TwinkqlTemplateTest {

	@Test
	void TestFindSparqlMap(){
		def template = new TwinkqlTemplate()
		def mappings = new SparqlMappings(sparqlMapList:[
			new SparqlMap(id:"test", string:"myQueryString")
		])
	
		assertNotNull template.findSparqlMap(mappings, "test");
		
	}
}
