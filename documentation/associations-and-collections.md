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

### Mapping an Association

An Association is a property on a Bean that is another Bean. For example

	public class Parent {
	
		private String name;
	
		private Child child;
		
		...
	
	}
	
The attribute ```child```, in this case, is an _Association_, and could be mapped as such:
	
	<resultMap id="parentResultMap" resultClass="org.twinkql.Parent">
		<uniqueResult>s</uniqueResult>
		<rowMap  var="s" varType="literalValue" beanProperty="name" />
		<association beanProperty="child" isCollection="false" resultMap="childResultMap"/>
	</resultMap>

### Important ```Association``` attributes
* ```isCollection```: whether or not this is a _one-to-one_ relationship or a _one-to-many_.
* ```resultMap```: the nested ```resultMap``` id that describes this Assocation. 
If ```resultMap``` is omitted, it is assumed that the mapping will be inlined, as such:
	
		<resultMap id="parentResultMap" resultClass="org.twinkql.Parent">
			<uniqueResult>s</uniqueResult>
			<rowMap  var="s" varType="literalValue" beanProperty="name" />
			<association beanProperty="child" isCollection="false">
				<rowMap var="childName" 
					varType="literalValue" 
					beanProperty="childName"/>
			</association>
		</resultMap>

___NOTE:___ An ```association``` _cannot_ specify a ```resultMap``` attribute and also define inline ```rowMap```s.
