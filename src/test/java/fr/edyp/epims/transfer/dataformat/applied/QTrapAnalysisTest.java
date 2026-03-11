/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;

import junit.framework.TestCase;
import fr.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

public class QTrapAnalysisTest extends TestCase {
	
	private static Logger logger = LoggerFactory.getLogger(QTrapAnalysisTest.class);
	private QTrapFactory factory;
	
	protected void setUp() throws Exception {
		factory = new QTrapFactory();
	}
	
	public void testWiffScan() {
		URL resource = this.getClass().getResource("/QT6872.wiff");
		String filename = resource.getFile();
		long start = System.currentTimeMillis();
		List<Analysis> list = factory.createAnalysis(new File(filename));
		assertNotNull(list);
		assertEquals(1, list.size());
		Analysis analysis = list.get(0);
		assertEquals("QT6872", analysis.getName());
		assertEquals("CytCGFP", analysis.getDescription());
		assertEquals("adrait",analysis.getOperator());
		assertEquals("controlelc",analysis.getSample().toLowerCase());		
		logger.info("elapsed time = "+(System.currentTimeMillis() - start+" ms"));
	}

}
