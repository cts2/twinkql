package edu.mayo.twinkql.result.beans;

import java.util.List;
import static org.junit.Assert.*
import org.junit.Test;

class PropertySetterTest {
	
	@Test
	void TestSetBeanProperty(){
		def p = new PropertySetter();
		
		def t = new TestClass();
		
		p.setBeanProperty(t, "nested.val", "some value")
		
		assertEquals t.nested.val, "some value";
	}
	
	@Test
	void TestSetBeanPropertyList(){
		def p = new PropertySetter();
		
		def t = new TestClassList();
		
		p.setBeanProperty(t, "stringList[0]", "some value")
		
		assertEquals 1, t.stringList.size()
		assertEquals "some value", t.stringList.get(0)
	}

	@Test(expected=NoAccessMethodsForResultPropertyException)
	void TestInstantiateNewPropertyWithBadProperty(){
		def p = new PropertySetter();
		
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
}

class TestClassList {
	List<String> stringList = new ArrayList<String>()
}

class TestClassNestedNoInstantiate {
	String val;
	
	TestClassNestedNoInstantiate(String someIntValue){
		
	}
}

