---
layout: documentation
title: Introduction
---

### What is Twinkql?
Twinkql is a SPARQL to Object Mapping Framework that allows users to bind named SPARQL queries to ResultMaps, which in turn may be bound to beans.
<br/>
<br/>
_**Twinkql has a few simple goals**_
<br/>
1. Many times accessing a SPARQL endpoint is only one layer of a complex application. An application may use data from a SPARQL endpoint to power a REST or SOAP service, to validate parts of the system, or any number of different applications. In this case, we need to go from SPARQL query to application-specific beans as quickly as possible, and with the least amount of code.
2. SPARQL in your code is hard to maintain. In Java, for example, complex SPARQL in Java files is represented as large String concatenations. This limits the readability of your code. Moving the SPARQL queries to XML files let you have a clean separation between application logic and SPARQL queries.
3. Accessing a SPARQL endpoint should be no harder than a SQL datasource. In fact, your application should only be concerned with the query you want to execute and what parameters you need put in that query.
