package edu.mayo.twinkql.result;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mayo.twinkql.instance.BeanInstantiator;
import edu.mayo.twinkql.model.RowMap;
import edu.mayo.twinkql.model.types.BindingPart;
import edu.mayo.twinkql.result.callback.Modifier;

@Component
public class QuerySolutionResultExtractor {

	@Autowired
	private BeanInstantiator beanInstantiator;
	
	public QuerySolutionResultExtractor() {
		super();
	}
	
	public QuerySolutionResultExtractor(BeanInstantiator beanInstantiator) {
		super();
		this.beanInstantiator = beanInstantiator;
	}

	/**
	 * Gets the result from query solution.
	 *
	 * @param rdfNode the rdf node
	 * @param rowMap the row map
	 * @return the result from query solution
	 */
	public String getResultFromQuerySolution(RDFNode rdfNode, RowMap rowMap) {
		if (rdfNode == null) {
			return null;
		}

		BindingPart part = rowMap.getVarType();

		String result;

		switch (part) {
		case LOCALNAME: {
			result = rdfNode.asNode().getLocalName();
			if (StringUtils.isBlank(result)) {
				result = StringUtils.substringAfterLast(rdfNode.asNode()
						.getURI(), "/");
			}
			break;
		}
		case URI: {
			result = rdfNode.asNode().getURI();
			break;
		}
		case NAMESPACE: {
			result = rdfNode.asNode().getNameSpace();
			if (StringUtils.equals(result, rdfNode.asNode().getURI())) {
				result = StringUtils.substringBeforeLast(rdfNode.asNode()
						.getURI(), "/") + "/";
			}
			break;
		}
		case LITERALVALUE: {
			result = rdfNode.asLiteral().getString();
			break;
		}
		default: {
			throw new IllegalStateException();
		}
		}

		String modifier = rowMap.getModifier();

		if (StringUtils.isNotBlank(modifier)) {
			@SuppressWarnings("unchecked")
			Modifier<String> modifierObject = this.beanInstantiator
					.instantiateCallback(modifier, Modifier.class);

			result = modifierObject.beforeSetting(result);
		}
		return result;
	}
}
