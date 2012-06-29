![Twinkql Logl](http://twinkql.org/img/twinkql-logo.png)
### What is Twinkql?
[Twinkql](http://twinql.org) is a SPARQL to Object Mapping Framework that allows users to bind named SQARQL queries to ResultMaps, which in turn may be bound to beans.
<br/>
_**Twinkql has a few simple goals**_
<br/>

1. Many times accessing a SPARQL endpoint is only one layer of a complex application. An application may use data from a SPARQL endpoint to power a REST or SOAP service, to validate parts of the system, or any number of different applications. In this case, we need to go from SPARQL query to application-specific beans as quickly as possible, and with the least amount of code.

2. SPARQL in your code is hard to maintain. In Java, for example, complex SPARQL in in Java files is represented as large String concatenations. This limits the readability of your code. Moving the SPARQL queries to XML files let you have a clean separation between application logic and SPARQL queries.

3. Accessing a SPARQL endpoint should be no harder than a SQL datasource. In fact, your application should only be concerned with the query you want to execute and what parameters you need put in that query.

### A Simple Select 
For example, say I want to find all of the labels and comments from all the owl Ontologies. In Twinkql, I can specify that query in XML, keeping it separate from the rest of my program code.

```xml
<select id="myTestQuery">
SELECT ?label ?note
    WHERE {
        ?s a <http://www.w3.org/2002/07/owl#Ontology>;
            rdfs:label ?label ;
            rdfs:comment ?comment .
    }
</select>
```

### Passing in a variable 
What if there is a query parameter in my search? Twinkql can handle that with Parameter Substitution.

```xml
<select id="myTestQuery">
SELECT ?label ?note
    WHERE {
        ?s a <http://www.w3.org/2002/07/owl#Ontology>;
            rdfs:label ?label ;
            rdfs:comment ?comment ; 
            <http://my/ns#myproperty> #{myQueryParam}
    }
</select>
```
In this case, when I execute my query, I simply pass in a Map with a key of 'myQueryParam' - and whatever value is associated with that key will be injected into my query.


### Binding Results to a Bean 
In many applications, the SPARQL query is just part of the architecture. Results from a SPARQL query may need to be manipulated further, passed into a messaging queue, processed into a different format, etc. Twinql allows you to bind beans to SPARQL query results via 'ResultMaps'. Result maps simply take a variable from the SPARQL query (the 'var' attribute) and map it to a property on your result bean (the 'beanProperty' attribute).

```xml
<resultMap id="myTestResultMap" 
    resultClass="com.sample.twinkql.Bean">
    <rowMap var="label" varType="literalValue" beanProperty="name"/>
    <rowMap var="comment" varType="literalValue" beanProperty="node"/>
</resultMap>
```
In this example, a bean of class 'com.sample.twinkql.Bean' will be instantiated, and for each row of the SPARQL result set returned, the variables will be bound. The user will be returned a Collectin of the resulting beans.

### License
[Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 license)

### Inspirations
Twinkql has been heavily inspired by the (MyBatis Project)[http://mybatis.org], and we thank them for a great project. Twinkql aims to be the _MyBatis_ for the SPARQL community.

### More Info
For more info, please see the [Project Page](http://twinkql.org) and [follow us on Twitter](https://twitter.com/Twinkql).