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

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;

import edu.mayo.twinkql.model.CompositeResultMap;
import edu.mayo.twinkql.model.CompositeResultMapItem;
import edu.mayo.twinkql.model.ResultMapItem;

/**
 * The Class ExtendedCompositeResultMap.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
class ExtendedCompositeResultMap extends CompositeResultMap {

	private static final long serialVersionUID = -6125562115832012024L;

	private CompositeResultMap extending;

	/**
	 * Instantiates a new extended composite result map.
	 */
	ExtendedCompositeResultMap() {
		super();
	}

	/**
	 * Instantiates a new extended composite result map.
	 *
	 * @param extending the extending
	 */
	ExtendedCompositeResultMap(CompositeResultMap extending) {
		this.extending = extending;
	}

	public void setExtending(CompositeResultMap extending) {
		this.extending = extending;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.model.CompositeResultMap#enumerateCompositeResultMapItem()
	 */
	@Override
	public Enumeration<? extends CompositeResultMapItem> enumerateCompositeResultMapItem() {
		return Collections.enumeration(Arrays.asList(this
				.getCompositeResultMapItem()));
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.model.CompositeResultMap#getCompositeResultMapItem(int)
	 */
	@Override
	public CompositeResultMapItem getCompositeResultMapItem(int index)
			throws IndexOutOfBoundsException {
		return this.getCompositeResultMapItem()[index];
	}

	@Override
	public CompositeResultMapItem[] getCompositeResultMapItem() {
		return (CompositeResultMapItem[]) ArrayUtils.addAll(
				super.getCompositeResultMapItem(),
				this.extending.getCompositeResultMapItem());
	}

	@Override
	public int getCompositeResultMapItemCount() {
		return this.getCompositeResultMapItem().length;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.model.CompositeResultMap#iterateCompositeResultMapItem()
	 */
	@Override
	public Iterator<? extends CompositeResultMapItem> iterateCompositeResultMapItem() {
		return Arrays.asList(this.getCompositeResultMapItem()).iterator();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.model.ResultMap#enumerateResultMapItem()
	 */
	@Override
	public Enumeration<? extends ResultMapItem> enumerateResultMapItem() {
		return Collections.enumeration(Arrays.asList(this
				.getResultMapItem()));
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.model.ResultMap#getResultMapItem(int)
	 */
	@Override
	public ResultMapItem getResultMapItem(int index)
			throws IndexOutOfBoundsException {
		return this.getResultMapItem()[index];
	}

	@Override
	public ResultMapItem[] getResultMapItem() {
		return (ResultMapItem[]) ArrayUtils.addAll(
				super.getResultMapItem(),
				this.extending.getResultMapItem());
	}

	@Override
	public int getResultMapItemCount() {
		return this.getResultMapItem().length;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.twinkql.model.ResultMap#iterateResultMapItem()
	 */
	@Override
	public Iterator<? extends ResultMapItem> iterateResultMapItem() {
		return Arrays.asList(this.getResultMapItem()).iterator();
	}
	
	
	
}