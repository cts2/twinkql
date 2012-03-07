/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.twinkql.context;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * The Class Qname.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class Qname {
	private String namespace;
	private String localName;
	
	/**
	 * To qname.
	 *
	 * @param qualifiedName the qualified name
	 * @param defaultNamespace the default namespace
	 * @return the qname
	 */
	public static Qname toQname(String qualifiedName, String defaultNamespace){
		String[] names = StringUtils.split(qualifiedName, ":");
		
		if(names.length == 1){
			return new Qname(defaultNamespace, names[0]);
		} else {
			return new Qname(names[0], names[1]);
		}	
	}
	
	/**
	 * To qname.
	 *
	 * @param qualifiedName the qualified name
	 * @return the qname
	 */
	public static Qname toQname(String qualifiedName){
		String[] names = StringUtils.split(qualifiedName, ":");
		
		Assert.isTrue(names.length == 2, 
				"Error parsing Qualified Name: " + qualifiedName + ". A QualifiedName" +
						" must be in the format '[namespace]:[localname].");

		return new Qname(names[0], names[1]);
	}
	
	/**
	 * Instantiates a new qname.
	 *
	 * @param namespace the namespace
	 * @param localName the local name
	 */
	public Qname(String namespace, String localName){
		super();
		this.namespace = namespace;
		this.localName = localName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localName == null) ? 0 : localName.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Qname other = (Qname) obj;
		if (localName == null) {
			if (other.localName != null)
				return false;
		} else if (!localName.equals(other.localName))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "Namespace: " + this.namespace + ", LocalName: " + this.localName;
	}
}