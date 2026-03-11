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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;


import fr.edyp.epims.transfer.model.AbstractCacheFactory;
import fr.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

public class Maldi4800Factory extends AbstractCacheFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(Maldi4800Factory.class);
	
	public List<Analysis> createAnalysis(File file) {
	  Maldi4800XMLParser maldiXmlParser = new Maldi4800XMLParser();
	  maldiXmlParser.extractAnalysis(file, (Maldi4800Format)format);
	  
	  List<Maldi4800Analysis> maldiAnalList = maldiXmlParser.getAnalysisList(); 
	  logger.debug(maldiAnalList.toString());
	  
	  //because Java doesn't support a List<subclass> as a List<superclass>
	  //we have to recreate a List<Analysis> from the List<Maldi4800Analysis>
    return new ArrayList<>(maldiAnalList);
	}

	@Override
	public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
		throw new UnsupportedOperationException("Not supported Operation");
	}
}
