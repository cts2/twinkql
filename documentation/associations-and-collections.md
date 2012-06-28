---
layout: documentation
title: Mapping Associations and Collections
---

### Purpose
Often, SPARQL results need to be mapped to an Associations or Collection.

### Mapping a String Collection
	
	<resultMap id="governorsResultMap" resultClass="org.twinkql.it.association.Governor">
		<uniqueResult>s</uniqueResult>
		<rowMap  var="s" varType="localName" beanProperty="name" />
		<rowMap var="isa" varType="localName" beanProperty="isA[]"/>
	</resultMap>

A String Collection is mapped via a ```[]``` designation, shown as ```beanProperty="isA[]"``` above.

___NOTE:___ the ```uniqueResult``` element is important, as it identifies what defines a unique instances of the
```resultClass```. See [resultMap](resultMap.html) for more info.