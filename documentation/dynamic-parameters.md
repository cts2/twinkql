---
layout: documentation
title: Adding Dyanmic Parameters SPARQL Select
---

### Purpose
Defining SPARQL Select statement is one of the basic functions of Twinkql. These queries are defined
in XML files, keeping SPARQL out of your Java code.

### Example
	
	<select id="getNovel" resultMap="novelResultMap">
	<![CDATA[
		SELECT ?novel ?author 
		WHERE { 
			?novel a <http://dbpedia.org/class/yago/EnglishNovels> ;
				<http://dbpedia.org/property/name> "#{novelName}"@en ;
				<http://dbpedia.org/property/author> ?author .
		}
	]]>
	</select>
	
In the above example, we define a ```SELECT``` SPARQL query with a dynamic parameter, ```#{novelName}```. This
parameter can be set at runtime by passing parameters into the [TwinkqlTemplate](../maven-site/apidocs/org/twinkql/template/TwinkqlTemplate.html).

### Groovy Template Example
	def factory = new TwinkqlContextFactory(
			"http://dbpedia.org/sparql",
			"classpath:org/twinkql/it/param/*.xml")
	
	def template = new TwinkqlTemplateFactory(factory.getTwinkqlContext()).getTwinkqlTemplate()
	
	def params = ["novelName":"The Lord of the Rings"]
	
	def novel = template.selectForObject("param", "getNovel", params, Novel)

In the above example, the variable ```params``` is the Map which drives the parameter substitution. In this example, the Map _key_ is the name
of the placeholder, and the Map _value_ is the value that will be substituted. Paring this with the above ```SELECT``` statement,
the ```#{novelName}``` placeholder will be replaced with ```The Lord of the Rings```.