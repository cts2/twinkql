---
layout: documentation
title: A resultMap
---

### Purpose
A ```resultMap``` is where SPARQL results are bound to Object attributes.

### Example
	
	<resultMap id="novelResultMap" resultClass="org.twinkql.it.param.Novel">
		<uniqueResult>novel</uniqueResult>
		<rowMap var="novel" varType="uri" beanProperty="novel"/>
		<rowMap var="author" varType="uri" beanProperty="author"/>
	</resultMap>

### Attributes

* ```id```: The the unique name of the query. The __id__ value, when paired with
the containing __namespace__, must be unique.

* ```resultClass```: The full class name of the desired result (or _alias_, if an _alias_ has been registered).

### Elements

* ```uniqueResult```: The SPARQL variable that indicates a unique result.

* ```rowMap```: A [rowMap](rowMap.html) is a binding of a SPARQL result row to a Bean property.

* ```association```: A [association](associations-and-collections.html) is the binding of a property on a Bean that is another Bean.

___Example SPARQL Results:___ 

<table>
	<tr>
	 	<th>Variable: novel</th>
        <th>Variable: label</th>
	</tr>
    <tr>
        <td>http://dbpedia.org/resource/The_Lord_of_the_Rings</td>
        <td>The Lord of the Rings</td>
    </tr>
    <tr>
        <td>http://dbpedia.org/resource/The_Lord_of_the_Rings</td>
        <td>Der Herr der Ringe</td>
    </tr>    
    <tr>
        <td>http://dbpedia.org/resource/The_Lord_of_the_Rings</td>
        <td>El Senor de los Anillos</td>
    </tr>
</table>

In this example, we have 3 rows returned from a SPARQL result. There are two ways Twinkql can deal with this result:
* Treat each individual row as a new instance of the ```resultClass```.
* Group the results based on a ```uniqueResult``` variable.

When grouping, instead of getting multiple Objects, you can group results and populate lists.

__For Example:__

	public class Novel
		
		private String uri;
		private List<String> names;
		
		...
		
if the above result where grouped by the ```novel``` variable (```<uniqueResult>novel</uniqueResult>```), there would be one
```Novel``` Object created, and all names would be added to the ```names``` list.
