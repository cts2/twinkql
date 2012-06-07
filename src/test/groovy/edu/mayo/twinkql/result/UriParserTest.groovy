package edu.mayo.twinkql.result;

import static org.junit.Assert.*;

import org.junit.Test

class UriParserTest {
	
	@Test
	void testSplitUriName(){
		def p = new UriParser();
		
		def n = p.getLocalPart("http://there/is/a/name")
		
		assertEquals "name", n
	}
	
	@Test
	void testSplitUriNamsespace(){
		def p = new UriParser();
		
		def n = p.getNamespace("http://there/is/a/name")
		
		assertEquals "http://there/is/a/", n
	}
	
	@Test
	void testSplitUriNameHash(){
		def p = new UriParser();
		
		def n = p.getLocalPart("http://there/is/a#name")
		
		assertEquals "name", n
	}
	
	@Test
	void testSplitUriNamsespaceHash(){
		def p = new UriParser();
		
		def n = p.getNamespace("http://there/is/a#name")
		
		assertEquals "http://there/is/a#", n
	}
	
	@Test
	void testSplitUriNameColon(){
		def p = new UriParser();
		
		def n = p.getLocalPart("http://there/is/a:name")
		
		assertEquals "name", n
	}
	
	@Test
	void testSplitUriNamsespaceColon(){
		def p = new UriParser();
		
		def n = p.getNamespace("http://there/is/a:name")
		
		assertEquals "http://there/is/a:", n
	}

}
