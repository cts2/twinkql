---
layout: documentation
title: The "modifier" callback
---

### Purpose
The ```modifier``` callback is used to alter a mapped result before it is set on the target Object.

### Example
	<resultMap id="governorsResultMap"
		resultClass="org.twinkql.it.association.Governor">
		<uniqueResult>s</uniqueResult>
		<rowMap  var="s" varType="localName" beanProperty="name" 
			modifier="org.twinkql.NameAlteringModifier" />
	</resultMap>

In the above example, we define a ```rowMap``` that will fire a ```modifier``` callback.

### Attributes

 * ```modifier```: The full Class Name (or alias) of the callback. This class must implement the [AfterResultBinding](../maven-site/apidocs/org/twinkql/result/callback/Modifier.html) interface
