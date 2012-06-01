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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Class Tracker.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
class CompositeTracker extends Tracker {
	
	Set<String> requestedPredicateUris = new HashSet<String>();
	Map<String, Integer> collectionTracker = new HashMap<String, Integer>();

	/**
	 * Instantiates a new tracker.
	 *
	 * @param requestedPredicateUris the requested predicate uris
	 */
	public CompositeTracker(Set<String> requestedPredicateUris) {
		super();
		this.requestedPredicateUris = requestedPredicateUris;
	}

	/**
	 * Gets the requested predicate uris.
	 *
	 * @return the requested predicate uris
	 */
	public Set<String> getRequestedPredicateUris() {
		return requestedPredicateUris;
	}
	
	/**
	 * Gets the collection tracker.
	 *
	 * @return the collection tracker
	 */
	public Map<String, Integer> getCollectionTracker() {
		return collectionTracker;
	}
}