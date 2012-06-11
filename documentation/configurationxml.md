---
layout: documentation
title: Configuration XML
---

### Purpose
Twinkql is configured by a main XML file. This file allows users to supply extra context
information. __NOTE__ that this file is _optional_, and if omitted sensible defaults will
be assumed.

### Example

	<?xml version="1.0" encoding="UTF-8"?>
	<twinkqlConfig
		xmlns="http://mayo.edu/twinkql" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		
		<namespace prefix="rdfs" uri="http://www.w3.org/2000/01/rdf-schema#"/>
	
	</twinkqlConfig>

### Configuration

 * **namespace**

	A `namespace` is a way to assign a shorter prefix (`rdfs`) to a longer URI:
	
	`http://www.w3.org/2000/01/rdf-schema#`.
	
	This allows you to create shorthand references in your Mapping XML files, such as `rdfs:Class`, instead of using
	the entire URI, such as: 
	
	`<http://www.w3.org/2000/01/rdf-schema#Class>`.
	