package edu.mayo.twinkql.result;

import static org.junit.Assert.*
import static org.easymock.EasyMock.*

import org.junit.Test

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.ResourceFactory

import edu.mayo.twinkql.context.Qname
import edu.mayo.twinkql.context.TwinkqlContext
import edu.mayo.twinkql.instance.internal.DefaultClassForNameInstantiator;
import edu.mayo.twinkql.model.CompositeResultMap
import edu.mayo.twinkql.model.PerRowResultMap
import edu.mayo.twinkql.model.RowMap
import edu.mayo.twinkql.model.SparqlMap
import edu.mayo.twinkql.model.SparqlMapItem
import edu.mayo.twinkql.model.TripleMap
import edu.mayo.twinkql.model.types.BindingPart
import edu.mayo.twinkql.result.callback.AfterResultBinding



class ResultBindingProcessorTest {

	@Test
	void testBindForObjectWithTripleMappings(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true).times(2)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") },
			isLiteral: { false }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			tripleMap:[
	
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
			
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultId"))

		assertEquals "my value", r.oneProp;
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsDefault(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true).times(2)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			tripleMap:[
				
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultId"))

		assertEquals "my value", r.oneProp;
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsWithList(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true).times(2)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			tripleMap: [
						new TripleMap(
							beanProperty: "list[]",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.list.size()
		assertEquals "my value", r.list.get(0)
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsWithNestedCompositeList(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true).times(2)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			tripleMap: [
						new TripleMap(
							beanProperty: "compositeList[].threeProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.compositeList.size()
		assertEquals "my value", r.compositeList.get(0).threeProp
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsWithListTwoEntries(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true).times(2)
		
		QuerySolution querysolution1 = createMock(QuerySolution)
		
		def predicate1 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution1.get("p")).andReturn(predicate1)
		
		def object1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 1") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution1.get("o")).andReturn(object1)
		
		expect(resultset.next()).andReturn(querysolution1)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution2 = createMock(QuerySolution)
		
		def predicate2 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution2.get("p")).andReturn(predicate2)
		
		def object2 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 2") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution2.get("o")).andReturn(object2)
		
		expect(resultset.next()).andReturn(querysolution2)
		expect(resultset.hasNext()).andReturn(false)
		replay(resultset, querysolution1, querysolution2)
		
		def result = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			tripleMap: [
						new TripleMap(
							beanProperty: "list[]",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultId"))

		assertEquals 2, r.list.size()
		assertEquals "my value 1", r.list.get(0)
		assertEquals "my value 2", r.list.get(1)
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsWithAfterCallback(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			afterMap:"edu.mayo.twinkql.result.TestAfterBinding",
			id: "resultId",
			tripleMap: [
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getInstantiators:{ [ new DefaultClassForNameInstantiator() ] as Set },
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultId"))

		assertEquals "Modified!!", r.oneProp;
	
	}

	@Test
	void testBindForObjectWithTripleMappingsWithExtends(){
	
		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true).times(2)
		
		QuerySolution querysolution1 = createMock(QuerySolution)
		
		def predicate1 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri1") }
		] as RDFNode
		
		expect(querysolution1.get("p")).andReturn(predicate1)
		
		def object1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution1.get("o")).andReturn(object1)
		
		expect(resultset.next()).andReturn(querysolution1)

		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution2 = createMock(QuerySolution)
		
		def predicate2 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri2") }
		] as RDFNode
		
		expect(querysolution2.get("p")).andReturn(predicate2)
		
		def object2 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my extended value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution2.get("o")).andReturn(object2)
		
		expect(resultset.next()).andReturn(querysolution2)

		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution1, querysolution2)
		
		def result1 = new CompositeResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultMap1",
			extends:"ns:resultMap2",
			tripleMap: [
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri1",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def result2 = new CompositeResultMap(
			id: "resultMap2",
			tripleMap: [
						new TripleMap(
							beanProperty: "twoProp",
							predicateUri: "http://predicateUri2",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
					]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result1
						),
						new SparqlMapItem(
							compositeResultMap:result2
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, Qname.toQname("ns:resultMap1"))
	
		assertEquals "my value", r.oneProp;
		assertEquals "my extended value", r.twoProp;
	
	}
	
	@Test
	void testBindForRows(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			rowMap:[
					new RowMap(
						beanProperty:"oneProp",
						var:"var1",
						varType:BindingPart.LITERALVALUE
					)
				]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							perRowResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForList(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.size()
		assertEquals "my value", r.get(0).oneProp;
	
	}
	
	@Test
	void testBindForRowsWithAfterCallback(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultId",
			afterMap:"edu.mayo.twinkql.result.TestAfterBinding",
			rowMap:[
					new RowMap(
						beanProperty:"oneProp",
						var:"var1",
						varType:BindingPart.LITERALVALUE
					)
				]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							perRowResultMap:result
						)
					]
				)
				] as Set
			},
			getInstantiators:{ [ new DefaultClassForNameInstantiator() ] as Set },
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForList(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.size()
		assertEquals "Modified!!", r.get(0).oneProp
	}
	
	@Test
	void testBindForRowsWithCompositeResultMap(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 1") },
			isLiteral: { true }
		] as RDFNode
	
		def var3 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 3") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		expect(querysolution.get("var3")).andReturn(var3)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result1 = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultMap1",
			rowMap:[
					new RowMap(
						beanProperty:"oneProp",
						var:"var1",
						varType:BindingPart.LITERALVALUE
					),
					new RowMap(
						beanProperty:"testResult2",
						resultMapping:"ns:resultMap2"
				)
				]
		);
	
		def result2 = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult2",
			id: "resultMap2",
			rowMap:[
					new RowMap(
						beanProperty:"threeProp",
						var:"var3",
						varType:BindingPart.LITERALVALUE
					)
				]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							perRowResultMap:result1
						),
						new SparqlMapItem(
							perRowResultMap:result2
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForList(resultset, Qname.toQname("ns:resultMap1"))

		assertEquals 1, r.size()
		assertEquals "my value 1", r.get(0).oneProp
		assertNotNull r.get(0).testResult2
		assertEquals "my value 3", r.get(0).testResult2.threeProp
	}
	
	@Test
	void testBindForRowsWithDoubleCompositeResultMap(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 1") },
			isLiteral: { true }
		] as RDFNode
	
		def var3 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 3") },
			isLiteral: { true }
		] as RDFNode
	
		def var4 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 4") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		expect(querysolution.get("var3")).andReturn(var3)
		expect(querysolution.get("var4")).andReturn(var4)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result1 = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			id: "resultMap1",
			rowMap:[
					new RowMap(
						beanProperty:"oneProp",
						var:"var1",
						varType:BindingPart.LITERALVALUE
					),
					new RowMap(
						beanProperty:"testResult2",
						resultMapping:"ns:resultMap2"
				)
				]
		);
	
		def result2 = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult2",
			id: "resultMap2",
			rowMap:[
					new RowMap(
						beanProperty:"threeProp",
						var:"var3",
						varType:BindingPart.LITERALVALUE
					),
					new RowMap(
						beanProperty:"testResult3",
						resultMapping:"ns:resultMap3"
						)
				]
		);
	
		def result3 = new PerRowResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult3",
			id: "resultMap3",
			rowMap:[
					new RowMap(
						beanProperty:"fourProp",
						var:"var4",
						varType:BindingPart.LITERALVALUE
					)
				]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							perRowResultMap:result1
						),
						new SparqlMapItem(
							perRowResultMap:result2
						),
						new SparqlMapItem(
							perRowResultMap:result3
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForList(resultset, Qname.toQname("ns:resultMap1"))

		assertEquals 1, r.size()
		assertEquals "my value 1", r.get(0).oneProp
		assertNotNull r.get(0).testResult2
		assertEquals "my value 3", r.get(0).testResult2.threeProp
		assertNotNull r.get(0).testResult2.testResult3
		assertEquals "my value 4", r.get(0).testResult2.testResult3.fourProp
	}
}

class TestAfterBinding implements AfterResultBinding {

	public void afterBinding(bindingResult, Map callbackParams) {
		bindingResult.oneProp = "Modified!!"
	}
	
}

class TestResult {
	def oneProp;
	def twoProp;
	TestResult2 testResult2;
	List<String> list
	List<TestResult2> compositeList
}

class TestResult2 {
	def threeProp;
	TestResult3 testResult3;
}

class TestResult3 {
	def fourProp;
}

class TestTagValue {
	List<TagValue> tagValues = new ArrayList<TagValue>()
}

class TagValue {
	String tag
	String value
}
