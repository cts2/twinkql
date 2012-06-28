---
layout: documentation
title: TwinkqlTemplate
---

### Purpose
The [TwinkqlTemplate](../maven-site/apidocs/org/twinkql/template/TwinkqlTemplate.html) is access point into
the Twinkql API. It is here that queries are executed and results are returned.

### Example
	TwinkqlTemplateFactory factory = TwinkqlTemplateFactory(context);
		
	TwinkqlTemplate template = factory.getTwinkqlTemplate();
	
	//selecting a single result
	ResultType result = 
		template.selectForObject("myNs", "mySelectStatement", null, ResultType.class);
	
	//selecting multiple results	
	List<ResultType> result = 
		template.selectForList("myNs", "mySelectStatement", null, ResultType.class);

__NOTE:__ when using [selectForObject](http://cts2.github.com/twinkql/maven-site/apidocs/org/twinkql/template/TwinkqlTemplate.html#selectForObject%28java.lang.String,%20java.lang.String,%20java.util.Map,%20java.lang.Class%29),
only __ONE__ unique result should be expected. If any more results are returned by the SPARQL query, a [TooManyResultsException](http://cts2.github.com/twinkql/maven-site/apidocs/org/twinkql/template/TooManyResultsException.html)
will be thrown.



