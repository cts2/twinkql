---
layout: documentation
title: The "afterMap" callback
---

### Purpose
Additional processing may be required after results are mapped to a Java bean. There may be extra values that need to be
added, or postprocessing steps, etc. The `afterMap` callback allows you to access results as they are being bound to the Java
bean for further processing

### Example
	<resultMap id="governorsResultMap" 
		afterMap="edu.mayo.twinkql.it.association.AfterGovernorMapCallback"
		resultClass="edu.mayo.twinkql.it.association.Governor">
		<uniqueResult>s</uniqueResult>
		<rowMap  var="s" varType="localName" beanProperty="name" />
		<rowMap var="isa" varType="localName" beanProperty="isA[]"/>
	</resultMap>

In the above example, we define a ```resultMap``` that will fire a ```afterMap``` callback.

### Attributes

 * ```afterMap```: The full Class Name of the callback