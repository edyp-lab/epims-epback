package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;


import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

public class Maldi4800Factory extends AbstractCacheFactory {
	
	private static Logger logger = LoggerFactory.getLogger(Maldi4800Factory.class);
	
	public List<Analysis> createAnalysis(File file) {
	  Maldi4800XMLParser maldiXmlParser = new Maldi4800XMLParser();
	  maldiXmlParser.extractAnalysis(file, (Maldi4800Format)format);
	  
	  List<Maldi4800Analysis> maldiAnalList = maldiXmlParser.getAnalysisList(); 
	  logger.debug(maldiAnalList.toString());
	  
	  //because Java doesn't support a List<subclass> as a List<superclass>
	  //we have to recreate a List<Analysis> from the List<Maldi4800Analysis>
	  List<Analysis> analList = new ArrayList<Analysis>();
	  analList.addAll(maldiAnalList);
	  return analList; 
	}

	@Override
	public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
		throw new UnsupportedOperationException("Not supported Operation");
	}
}
