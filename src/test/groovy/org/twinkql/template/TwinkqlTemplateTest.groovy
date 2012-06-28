package org.twinkql.template;


import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.twinkql.template.SelectNotFoundException;
import org.twinkql.template.TwinkqlTemplate;

import org.twinkql.context.TwinkqlContext
import org.twinkql.context.TwinkqlContextFactory;
import org.twinkql.model.Select
import org.twinkql.model.SparqlMap
import org.twinkql.model.SparqlMapChoice
import org.twinkql.model.SparqlMapChoiceItem
import org.twinkql.model.SparqlMapItem
import org.twinkql.model.types.TestType
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
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
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
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
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
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
		def query = template.getSelectQueryString("myTestNamespace", "testIterativeQuery", [
			sub:"something",
			someProp1:"imNotNull"])
		
		println query
		
		assertTrue query.contains("Test for inner something")
		
	}
	
	@Test
	void TestQueryForStringParameterSubstitutionWithNotNullNull(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
		def query = template.getSelectQueryString("myTestNamespace", "testIterativeQuery", [
			someProp1:null])
		
		println query
		
		assertFalse query.contains("This is a not Null test")
		
	}
	
	@Test(expected=SelectNotFoundException)
	void TestQuerySelectNotFoundBadName(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
		def query = template.getSelectQueryString("myTestNamespace", "__INVALID__", [
			someProp1:null])
	}
	
	@Test(expected=SelectNotFoundException)
	void TestQuerySelectNotFoundBadNamespace(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
		def query = template.getSelectQueryString("__INVALID__", "testIterativeQuery", [
			someProp1:null])
	}
	
	@Test(expected=SelectNotFoundException)
	void TestSelectNotFoundBothInvalid(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
		def query = template.getSelectQueryString("__INVALID__", "__INVALID__", [
			someProp1:null])
	}
	
	@Test
	void TestQueryForStringParameterSubstitutionWithNotNullNestedSubstitution(){
			
		TwinkqlContextFactory factory = new TwinkqlContextFactory()
		SparqlMap map = factory.loadSparqlMap(new ClassPathResource("xml/testMap.xml"))
		
		def twinkqlContext = [
			getSparqlMaps : {-> [map] as Set},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def template = new TwinkqlTemplate(twinkqlContext, null)
	
		def query = template.getSelectQueryString("myTestNamespace", "testIterativeQuery", [
			sub:"value",
			someProp1:"test"])

		assertTrue query.contains("Test for inner value")
		
	}
	
	@Test
	void TestDoesTestPassIsNullTrue(){
		
		def template = new TwinkqlTemplate(null, null){
			protected void init(){
				//
			}
		}
	
		assertTrue template.doesTestPass(null, TestType.IS_NULL)	
	}
	
	@Test
	void TestDoesTestPassIsNullFalse(){
		
		def template = new TwinkqlTemplate(null, null){
			protected void init(){
				//
			}
		}
	
		assertFalse template.doesTestPass("hi", TestType.IS_NULL)
	}
	
	@Test
	void TestDoesTestPassIsNotNullNullTrue(){
		
		def template = new TwinkqlTemplate(null, null){
			protected void init(){
				//
			}
		}
	
		assertTrue template.doesTestPass("hi", TestType.IS_NOT_NULL)
	}
	
	@Test
	void TestDoesTestPassIsNotNullNullFalse(){
		
		def template = new TwinkqlTemplate(null, null){
			protected void init(){
				//
			}
		}
	
		assertFalse template.doesTestPass(null, TestType.IS_NOT_NULL)
	}
}

class TestQuery {
	def var
	def text
}
