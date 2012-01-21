package edu.mayo.twinkql.result;

import static org.easymock.EasyMock.*
import static org.junit.Assert.*

import org.junit.Test

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.ResourceFactory

import edu.mayo.twinkql.model.BindingPart
import edu.mayo.twinkql.model.ResultMap
import edu.mayo.twinkql.model.TripleMap
import edu.mayo.twinkql.model.TriplesMap

class ResultBindingProcessorTest {

	@Test
	void testBind(){
		def binding = new ResultBindingProcessor()
		
		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new ResultMap(
			resultClass: "edu.mayo.twinkql.result.TestResult",
			triplesMap:
				new TriplesMap(
					predicateVar: "p",
					objectVar: "o",
					tripleMapList: [
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							objectPart: BindingPart.LITERAL_VALUE
						)
					]
				)
		);
		
		def r = binding.bindToTriples(resultset, result)

		assertEquals "my value", r.oneProp;
	
	}
}

class TestResult {
	def oneProp;
	def twoProp;
}
