package edu.mayo.twinkql.template;


import org.junit.Test
import org.springframework.core.io.ClassPathResource

import edu.mayo.twinkql.context.TwinkqlContext
import edu.mayo.twinkql.context.TwinkqlContextFactory;
import edu.mayo.twinkql.model.Select
import edu.mayo.twinkql.model.SparqlMap
import edu.mayo.twinkql.model.SparqlMapChoice
import edu.mayo.twinkql.model.SparqlMapChoiceItem
import edu.mayo.twinkql.model.SparqlMapItem
import static org.junit.Assert.*



public class TwinkqlTemplateTest {

	@Test
	void TestQueryForStringParameterSubstitution(){
		def maps = [
			new SparqlMap(
				namespace:"ns",
				sparqlMapItem:[
					new SparqlMapItem(
						sparqlMapChoice:new SparqlMapChoice(
							sparqlMapChoiceItem: [
								new SparqlMapChoiceItem(
								select: new Select(
										id:"test",
										content:"test #{param} substitution"
										)	
								)
								]
							)
						)
				]
			)	
		] as Set
		
		def twinkqlContext = [
			getSparqlMaps : {-> maps},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext)
	
		assertEquals "test sub substitution",
			 template.getSelectQueryString("ns", "test", ["param":"sub"])
	}
	
	@Test
	void TestQueryForStringParameterSubstitutionWithIterator(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext)
	
		println  template.getSelectQueryString("myTestNamespace", "testIterativeQuery", [
			myCollection:[
				new TestQuery(var:"var1", text:"someText1"), new TestQuery(var:"var2", text:"someText2")]
		 ])
	}
	
	@Test
	void TestQueryForStringParameterSubstitutionWithNotNull(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext)
	
		def query = template.getSelectQueryString("myTestNamespace", "testIterativeQuery", [
			someProp1:"imNotNull"])
		
		println query
		
		assertTrue query.contains("This is a not Null test")
		
	}
	
	@Test
	void TestQueryForStringParameterSubstitutionWithNotNullNull(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext)
	
		def query = template.getSelectQueryString("myTestNamespace", "testIterativeQuery", [
			someProp1:null])
		
		println query
		
		assertFalse query.contains("This is a not Null test")
		
	}
	
	
}

class TestQuery {
	def var
	def text
}
