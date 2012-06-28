---
layout: documentation
title: A sparqlMap
---

### Purpose
A `sparqlMap` is the basic logical unit of Twinqkl. This XML file is where all SPARQL selects and bindings are
described.

### Example

	<?xml version="1.0" encoding="UTF-8"?>
	<sparqlMap namespace="org.twinkql.example"
		xmlns="http://mayo.edu/twinkql" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		
		<resultMap id="whatGovernorsAreShort" resultClass="org.twinkql.example.Governor">
			<uniqueResult>s</uniqueResult>
			<rowMap  var="s" varType="localName" beanProperty="name" />
			<rowMap var="isa" varType="localName" beanProperty="isA[]"/>
		</resultMap>
	
		<select id="whatGovernorsAre" resultMap="whatGovernorsAreResultMap">
	
			SELECT DISTINCT ?s ?isa 
			WHERE {
			
			<![CDATA[
			?s a <http://dbpedia.org/ontology/Governor> ;
			<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?isa
			]]>
			
			} LIMIT 100
		
		</select>
	
	</sparqlMap>

### Elements
* ```resultMap```: a [resultMap](resultMap.html) is a binding between SPARQL results and bean attributes.
* ```select```: a [select](select.html) is where a SPARQL SELECT statement is defined.