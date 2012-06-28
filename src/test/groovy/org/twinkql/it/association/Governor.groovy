package org.twinkql.it.association

class Governor {
	
	String name
	
	List<String> isA
	
	List<IsA> isAObject

}

class IsA {
	
	String isA

	List<Detail> details
}

class Detail {
	
	String name
	
	List<String> values
	
}