/*
 * Copyright: (c) 2004-2012 Mayo Foundation for Medical Education and 
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
package edu.mayo.twinkql.result;

import org.apache.commons.beanutils.BeanUtils;

import edu.mayo.twinkql.model.NamedResultMap;
import edu.mayo.twinkql.model.ResultMap;
import edu.mayo.twinkql.model.ResultMapChoice;
import edu.mayo.twinkql.model.ResultMapChoiceItem;

/**
 * The Class ExtendedCompositeResultMap.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
class ExtendableResultMap extends NamedResultMap {

	private static final long serialVersionUID = -6125562115832012024L;

	private ExtendableResultMap extending;

	
	public ExtendableResultMap getExtending() {
		return extending;
	}

	public void setExtending(ExtendableResultMap extending) {
		this.extending = extending;
	}

	/**
	 * Instantiates a new extended composite result map.
	 *
	 * @param extending the extending
	 */
	ExtendableResultMap(ResultMap decorated) {
		super();
		try {
			BeanUtils.copyProperties(this, decorated);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} 
	}

	@Override
	public ResultMapChoice getResultMapChoice() {
			if(this.extending != null){
			ResultMapChoice choice = new ResultMapChoice();
			
			for(ResultMapChoiceItem item : extending.getResultMapChoice().getResultMapChoiceItem()){
				choice.addResultMapChoiceItem(item);
			}
			
			for(ResultMapChoiceItem item : super.getResultMapChoice().getResultMapChoiceItem()){
				choice.addResultMapChoiceItem(item);
			}
			
			return choice;
		} else {
			return super.getResultMapChoice();
		}
	}

}