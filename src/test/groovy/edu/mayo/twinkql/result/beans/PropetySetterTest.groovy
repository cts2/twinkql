package edu.mayo.twinkql.result.beans;

import static org.junit.Assert.*

import org.junit.Test

class PropetySetterTest {
	
	@Test
	void TestSetBeanProperty(){
		def p = new PropetySetter();
		
		def t = new TestClass();
		
		p.setBeanProperty(t, "nested.val", "some value")
		
		assertEquals t.nested.val, "some value";
	}
	
	@Test
	void TestInstantiateNewProperty(){
		def p = new PropetySetter();
		
		def t = new TestClass();
		
		p.instantiateNestedProperties(t, ["nested","val"] as String[])
		
		assertNotNull t.nested;
	}
	
	@Test(expected=NestedPropertyInstantiationException)
	void TestInstantiateNewPropertyWithInstantiationException(){
		def p = new PropetySetter();
		
		def t = new TestClass();
		
		p.instantiateNestedProperties(t, ["badNested","val"] as String[])
	}
	
	@Test(expected=NoAccessMethodsForResultPropertyException)
	void TestInstantiateNewPropertyWithBadProperty(){
		def p = new PropetySetter();
		
		def t = new TestClass();
		
		p.setBeanProperty(t, "BADnested.val", "some value")
	}
}

class TestClass {
	TestClassNested nested;
	TestClassNestedNoInstantiate badNested
}

class TestClassNested {
	String val;
	List<String> stringList = new ArrayList<String>()
}

class TestClassNestedNoInstantiate {
	String val;
	
	TestClassNestedNoInstantiate(String someIntValue){
		
	}
}
