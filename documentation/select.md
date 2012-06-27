---
layout: documentation
title: A SPARQL Select
---

### Purpose
Defining SPARQL Select statement is one of the basic functions of Twinkql. These queries are defined
in XML files, keeping SPARQL out of your Java code.

### Example
	
	<select id="governorsSelect" resultMap="governorsResultMap">

		SELECT DISTINCT ?s ?isa 
		WHERE {
		
		<![CDATA[
		?s a <http://dbpedia.org/ontology/Governor> ;
		<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?isa
		]]>
		
		} LIMIT 100
	
	</select>
	
In the above example, we define a ```SELECT``` SPARQL query with attributes as follows:

### Attributes

* ```id```: The the unique name of the query. The __id__ value, when paired with
the containing __namespace__, must be unique.

* ```resultMap```: The id of the __resultMap__ that the results of this query will be bound
to. The value should be in the form \'__namespace:name__\', unless the __resultMap__ is contained
in the same namespace as the query -- in this case, the namespace may be omitted.

The contents of the SELECT Query are specified in the body of the ```<select>``` tag.