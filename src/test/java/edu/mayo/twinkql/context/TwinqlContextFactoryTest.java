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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jibx.runtime.JiBXException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import edu.mayo.sparqler.model.SparqlMap;
import edu.mayo.sparqler.model.SparqlMappings;
import edu.mayo.twinkql.context.TwinkqlContextFactory;

/**
 * The Class TwinqlContextFactoryTest.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class TwinqlContextFactoryTest {
	
	@Test
	public void testLoadSparqlMappings() throws JiBXException, IOException{
		TwinkqlContextFactory sparqlContextFactory = new TwinkqlContextFactory();
		
		SparqlMappings mappings = 
				sparqlContextFactory.loadSparqlMappings(new ClassPathResource("/xml/test.xml"));
		
		assertNotNull(mappings);
		
		assertEquals("myTestNamespace", mappings.getNamespace());
		
		assertEquals(1, mappings.getSparqlMapList().size());
	}
	
	@Test
	public void testLoadSparqlMapp() throws JiBXException, IOException{
		TwinkqlContextFactory twinkqlContextFactory = new TwinkqlContextFactory();
		
		SparqlMappings mappings = 
				twinkqlContextFactory.loadSparqlMappings(new ClassPathResource("/xml/test.xml"));
		
		
		SparqlMap map = mappings.getSparqlMapList().get(0);
		
		assertEquals("myTestQuery", map.getId());
		
		assertNotNull(map.getString());
		
	}

}
