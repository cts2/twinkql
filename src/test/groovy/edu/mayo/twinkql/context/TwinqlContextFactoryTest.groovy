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
package edu.mayo.twinkql.context

import static org.junit.Assert.*

import java.io.IOException

import org.junit.Test
import org.springframework.core.io.ClassPathResource

import edu.mayo.twinkql.model.SparqlMap

/**
 * The Class TwinqlContextFactoryTest.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class TwinqlContextFactoryTest {
	
	@Test
	void testLoadSparqlMappings() throws IOException{
		TwinkqlContextFactory sparqlContextFactory = new TwinkqlContextFactory()
		
		def map = 
				sparqlContextFactory.loadSparqlMap(new ClassPathResource("/xml/test.xml"));
		
		assertNotNull map;
		
		assertEquals "myTestNamespace", map.getNamespace()
	}
	
	@Test
	public void testLoadSparqlMap() throws IOException{
		TwinkqlContextFactory twinkqlContextFactory = new TwinkqlContextFactory();
		
		SparqlMap map = 
				twinkqlContextFactory.loadSparqlMap(new ClassPathResource("/xml/test.xml"));
		
		
		def select = map.getSparqlMapSequence().getSelect().getAt(0);
		
		assertEquals "myTestQuery", select.getId()
		
		assertNotNull select.getContent()
		
	}

}
