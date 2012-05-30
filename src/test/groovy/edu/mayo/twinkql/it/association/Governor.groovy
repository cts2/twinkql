package edu.mayo.twinkql.it.association

class Governor {
	
	String name
	
	List<String> isA
	
	List<IsA> isAObject

}

class IsA {
	
	String isA

}