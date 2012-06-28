---
layout: documentation
title: Setting up the TwinkqlTemplate using Spring
---

### Purpose
For applications that use the [Spring Framework](http://www.springsource.org), configuration is a snap.

### Example
	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:context="http://www.springframework.org/schema/context"
		xmlns:osgi="http://www.springframework.org/schema/osgi"
		xmlns:osgix="http://www.springframework.org/schema/osgi-compendium"
		xsi:schemaLocation="http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi-1.2.xsd
			http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
			http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
			http://www.springframework.org/schema/osgi-compendium http://www.springframework.org/schema/osgi-compendium/spring-osgi-compendium.xsd">
		
		<bean id="twinkqlContext" class="org.twinkql.context.SpringTwinkqlContextFactory">
			<!-- additional properties definitions here -->
		</bean>
		
		<bean class="org.twinkql.template.SpringTwinkqlTemplateFactory">
			<property name="twinkqlContext" ref="twinkqlContext"/>
		</bean>
	</beans>

A [SpringTwinkqlTemplateFactory](../maven-site/apidocs/org/twinkql/template/SpringTwinkqlTemplateFactory.html) is the Spring-enabled
Factory Bean for creating a TwinkqlTemplate.



