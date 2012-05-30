package edu.mayo.twinkql.template;

import javax.annotation.Resource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import edu.mayo.twinkql.context.TwinkqlContext;

public class TwinkqlTemplateFactory implements FactoryBean<TwinkqlTemplate> {
	
	@Resource
	private TwinkqlContext twinkqlContext;

	public TwinkqlTemplate getObject() throws Exception {
		
		DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
		
		parentBeanFactory.registerSingleton("twinkqlContext", this.twinkqlContext);

		GenericApplicationContext parentContext = 
		        new GenericApplicationContext(parentBeanFactory);
		
		parentContext.refresh();
		
		AnnotationConfigApplicationContext annotationConfigApplicationContext = 
			this.decorateContext(new AnnotationConfigApplicationContext());
        annotationConfigApplicationContext.setParent(parentContext);
        annotationConfigApplicationContext.scan("edu.mayo.twinkql");
        annotationConfigApplicationContext.refresh();

		TwinkqlTemplate template = annotationConfigApplicationContext.getBean(TwinkqlTemplate.class);
		
		return template;
	}

	protected AnnotationConfigApplicationContext decorateContext(
			AnnotationConfigApplicationContext applicationContext) {
		//for subclasses
		return applicationContext;
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
