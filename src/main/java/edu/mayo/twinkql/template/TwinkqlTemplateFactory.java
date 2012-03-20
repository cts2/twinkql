package edu.mayo.twinkql.template;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.mayo.twinkql.context.TwinkqlContext;
import edu.mayo.twinkql.model.SparqlMap;

public class TwinkqlTemplateFactory implements FactoryBean<TwinkqlTemplate> {
	
	private TwinkqlContext twinkqlContext;

	public TwinkqlTemplate getObject() throws Exception {
		
		this.twinkqlContext.getSparqlMaps().add(this.createInteralSparqlMap());
		
		DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
		
		parentBeanFactory.registerSingleton("twinkqlContext", this.twinkqlContext);

		GenericApplicationContext parentContext = 
		        new GenericApplicationContext(parentBeanFactory);
		
		parentContext.refresh();
		
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.setParent(parentContext);
        annotationConfigApplicationContext.scan("edu.mayo.twinkql");
        annotationConfigApplicationContext.refresh();

		TwinkqlTemplate template = annotationConfigApplicationContext.getBean(TwinkqlTemplate.class);
		
		return template;
	}
	
	public SparqlMap createInteralSparqlMap() throws IOException, MarshalException, ValidationException  {
		Resource resource = new ClassPathResource("internal/reasoning.xml");
		String xml = IOUtils.toString(resource.getInputStream());
		
		SparqlMap map = SparqlMap.unmarshalSparqlMap(new StringReader(xml));
		
		return map;
	}

	public Class<?> getObjectType() {
		return TwinkqlTemplate.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public TwinkqlContext getTwinkqlContext() {
		return twinkqlContext;
	}

	public void setTwinkqlContext(TwinkqlContext twinkqlContext) {
		this.twinkqlContext = twinkqlContext;
	}

}
