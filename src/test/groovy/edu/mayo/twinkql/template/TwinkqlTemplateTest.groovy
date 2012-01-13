package edu.mayo.twinkql.template;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.sparqler.model.SparqlMap
import edu.mayo.sparqler.model.SparqlMappings
import edu.mayo.twinkql.context.TwinkqlContext

public class TwinkqlTemplateTest {

	@Test
	void TestFindSparqlMap(){
		def template = new TwinkqlTemplate()
		def mappings = new SparqlMappings(sparqlMapList:[
			new SparqlMap(id:"test", string:"myQueryString")
		])
	
		assertNotNull template.findSparqlMap(mappings, "test");
	}
	
	@Test(expected=SparqlMapNotFoundException)
	void TestFindSparqlMapNotFound(){
		def template = new TwinkqlTemplate()
		def mappings = new SparqlMappings(sparqlMapList:[
			new SparqlMap(id:"test", string:"myQueryString")
		])
	
		assertNotNull template.findSparqlMap(mappings, "__INVALID__");
	}
	
	@Test
	void TestQueryForStringParameterSubstitution(){
		def mappings = new SparqlMappings(sparqlMapList:[
			new SparqlMap(id:"test", string:"test #{param} substitution")
		])
		
		def twinkqlContext = [
			getSparqlMappings : {namespace -> mappings}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext)
	
		assertEquals "test sub substitution",
			 template.queryForString("ns", "test", ["param":"sub"])
	}
}
