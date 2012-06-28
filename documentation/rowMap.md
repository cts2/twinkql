---
layout: documentation
title: A rowMap
---

### Purpose
A ```rowMap``` is a binding of a SPARQL result row to a Bean property

### Example
	
	<resultMap id="someResultMap" resultClass="org.test.SomeClass">
		<rowMap var="name" varType="localName" beanProperty="name" />
		<rowMap var="comment" varType="uri" beanProperty="comment"/>
	</resultMap>

### Attributes

* ```var```: The SPARQL variable name in the row to be mapped

* ```varType```: What portion of the result to be mapped. Options are:
	
	* ___localName___: selects the _local_ part of the result
	
		_Example_: http://dbpedia.org/ontology/Software -> "Software"
	
	* ___namespace___: selects the _namespace_ part of the result
	
		_Example_: http://dbpedia.org/ontology/Software -> "http://dbpedia.org/ontology/"
	
	* ___uri___: selects the _entire_ uri of the result
	
		_Example_: http://dbpedia.org/ontology/Software -> "http://dbpedia.org/ontology/Software"
	
	* ___literalValue___: selects the _literalValue_ uri of the result. ___NOTE___: if the result is not a literal, an exception will be thrown.
	
		_Example_: "The Lord of the Rings" -> "The Lord of the Rings"
		
* ```beanProperty```: The name of the attribute on the ```resultClass``` to map the result to.
This will first try to map to a corresponding ___set___ method on the ```resultClass```
If that fails, it will look for a corresponding ___attribute___ (public or private) on the ```resultClass```

	Special ```beanProperty``` syntax includes
	
	* Nested Values:
	```someprop.nested.value```
	
	* Collections (auto add):
	```someprops[]```
	
	* A specific Collection entry:
	```someprops[0]```
	
	Bean properties will be instantiated where necessary. 
	
	For example:
	```someprop.nested.value```
	
	if ```nested``` is not present, it will be created before adding the ```value``` attribute.
	
* ```callbackId```: Instead of setting a bean property, the result may be saved for later processing. By
using a ```callbackId```, the result may be later accessed in an [afterMap callback](afterMap-callback.html).