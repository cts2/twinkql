---
layout: documentation
title: Using the TwinkqlTemplateFactory
---

### Purpose
A [TwinkqlTemplateFactory](../maven-site/apidocs/edu/mayo/twinkql/template/TwinkqlTemplateFactory.html) is a Factory for
creating a [TwinkqlTemplate](template.html), which is the main user entry point into Twinkql.

### Example

	TwinkqlContext context = //init the context via the Factory or Spring

	TwinkqlTemplateFactory factory = TwinkqlTemplateFactory(context);
	
	TwinkqlTemplate template = factory.getTwinkqlTemplate();

___NOTE:___ For more information on how to initialize the ```TwinkqlContext```, see [here](context.html)

