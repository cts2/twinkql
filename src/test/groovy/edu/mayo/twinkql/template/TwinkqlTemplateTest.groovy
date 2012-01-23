package edu.mayo.twinkql.template;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.twinkql.context.TwinkqlContext
import edu.mayo.twinkql.model.Select
import edu.mayo.twinkql.model.SparqlMap
import edu.mayo.twinkql.model.SparqlMapSequence

public class TwinkqlTemplateTest {

	@Test
	void TestQueryForStringParameterSubstitution(){
		def maps = [
			new SparqlMap(
				namespace:"ns",
				sparqlMapSequence:new SparqlMapSequence(
					select:[
						new Select(
							id:"test",
							content:"test #{param} substitution"
							)	
					]
				)
			)	
		] as Set
		
		def twinkqlContext = [
			getSparqlMaps : {-> maps}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext)
	
		assertEquals "test sub substitution",
			 template.getSelectQueryString("ns", "test", ["param":"sub"])
	}
}
