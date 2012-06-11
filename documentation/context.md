---
layout: documentation
title: The Twinkql Context
---

### Purpose
The Twinkql Context sets up all configuration necessary to use Twinkql in an application.
There are multiple configuration options, but for most applications a simple setup is all
that is necessary

### Example

	TwinkqlContext context = new TwinkqlContextFactory(
		"http://dbpedia.org/sparql").getTwinkqlContext();

A `TwinkqlContextFactory` is the starting point for initializing a `TwinkqlContext`. If you're connecting
to an HTTP SPARQL Endpoint, this may be all that you need! If, however, you need more control, there are
several more configuration options.

### Configuration

 * **QueryExecutionProvider**

	The `QueryExecutionProvider` describes how your queries will actually be executed. This interface could
	connect to a remote HTTP SPARQL Endpoint, a local service, etc. A default HTTP implementation is included,
	and can be used by simply providing at SPARQL Endpoint URL in the constructor as above.

 * **mappingFiles**
 
	The `mappingFiles` specifies the location to look for Twinkql mapping XML files. This may be any
	filesystem/classpath locatiion.
 
	Resources are searched via a [PathMatchingResourcePatternResolver](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html)
	and resources are matched using `Ant-style` pattern maching. For example:

		/WEB-INF/*-TwinkqlMaps.xml
		com/mycompany/**/MyMap.xml
		file:C:/some/path/Twinkql*.xml
		classpath:com/mycompany/**/TwinkqlMap.xml
	
	By default, this property is set to `classpath:twinkql/**/*Map.xml`, which will match any file under a classpath
	`twinkql` directory (and sub-directories) that are named `*Map.xml`.


 * **configurationFile**
 
	The `configurationFile` specifies the location to look for the main Twinkql configuration XML file. This file is optional, and if
	ommitted sensible defaults will be assumed. The default location is `classpath:twinkql/configuration.xml`.


