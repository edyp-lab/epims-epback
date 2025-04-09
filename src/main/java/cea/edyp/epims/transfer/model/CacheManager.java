package cea.edyp.epims.transfer.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cea.edyp.epims.transfer.dataformat.applied.QTrapAnalysis;
import cea.edyp.epims.transfer.dataformat.bruker.TimsTOFAnalysis;
import cea.edyp.epims.transfer.dataformat.nems.NemsAnalysis;
import cea.edyp.epims.transfer.dataformat.thermo.LTQAnalysis;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.perf4j.StopWatch;

import cea.edyp.epims.transfer.dataformat.applied.Maldi4800Analysis;
import cea.edyp.epims.transfer.dataformat.applied.WiffScanAnalysis;
import cea.edyp.epims.transfer.dataformat.bruker.UltraFlexAnalysis;
import cea.edyp.epims.transfer.dataformat.waters.MLAnalysis;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.LoggerFactory;

public class CacheManager {
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
	private final HashMap<String, Map<String, List<Analysis>>> analysisPerConfig;
	
	private String currentConfiguration;
	private Map<String, List<Analysis>> currentAnalysisMap;
	private static CacheManager instance;
	private final XStream xstream;
	
	private CacheManager(){
		analysisPerConfig = new HashMap<String, Map<String, List<Analysis>>>();
		xstream = new XStream(new DomDriver());
//		xstream.registerConverter(new AnalysisConverter());
		initializeXStream();
	}
	
	private void initializeXStream() {
		Class[] typeClasses = new Class[8];
		typeClasses[0] = QTrapAnalysis.class;
		typeClasses[1] = LTQAnalysis.class;
		typeClasses[2] = MLAnalysis.class;
		typeClasses[3] = Maldi4800Analysis.class;
		typeClasses[4] = UltraFlexAnalysis.class;
		typeClasses[5] = WiffScanAnalysis.class;
		typeClasses[6] = NemsAnalysis.class;
		typeClasses[7] = TimsTOFAnalysis.class;
		xstream.allowTypes(typeClasses);

		xstream.omitField(QTrapAnalysis.class, "dataFormat");
		xstream.omitField(QTrapAnalysis.class, "associatedFiles");
		xstream.aliasField("destinationPath", cea.edyp.epims.transfer.dataformat.applied.QTrapAnalysis.class,"analysisDestination");
		xstream.aliasField("analysisName", cea.edyp.epims.transfer.dataformat.applied.QTrapAnalysis.class,"name");
		xstream.aliasField("user", cea.edyp.epims.transfer.dataformat.applied.QTrapAnalysis.class,"operator");

		xstream.omitField(cea.edyp.epims.transfer.dataformat.thermo.LTQAnalysis.class, "dataFormat");		
		xstream.omitField(cea.edyp.epims.transfer.dataformat.thermo.LTQAnalysis.class, "associatedFiles");
		xstream.aliasField("destinationPath", cea.edyp.epims.transfer.dataformat.thermo.LTQAnalysis.class,"analysisDestination");
		xstream.aliasField("user", cea.edyp.epims.transfer.dataformat.thermo.LTQAnalysis.class,"operator");

		xstream.omitField(TimsTOFAnalysis.class, "dataFormat");
		xstream.omitField(TimsTOFAnalysis.class, "associatedFiles");
		xstream.omitField(TimsTOFAnalysis.class, "allAcqFiles");
		xstream.omitField(TimsTOFAnalysis.class, "zipFile");

		xstream.omitField(MLAnalysis.class, "dataFormat");
		xstream.omitField(MLAnalysis.class, "associatedFiles");
		
		xstream.omitField(Maldi4800Analysis.class, "dataFormat");   
		xstream.omitField(Maldi4800Analysis.class, "associatedFiles");
		
		xstream.omitField(UltraFlexAnalysis.class, "dataFormat");   
		xstream.omitField(UltraFlexAnalysis.class, "associatedFiles");
		
		xstream.omitField(WiffScanAnalysis.class, "dataFormat");   
		xstream.omitField(WiffScanAnalysis.class, "associatedFiles");

		xstream.omitField(NemsAnalysis.class, "dataFormat");
		xstream.omitField(NemsAnalysis.class, "associatedFiles");
		xstream.omitField(NemsAnalysis.class, "msetArrayList");
		xstream.omitField(NemsAnalysis.class, "files");
		xstream.omitField(NemsAnalysis.class, "statusMask");//test ?!
		xstream.omitField(NemsAnalysis.class, "fileSize");
		xstream.omitField(NemsAnalysis.class, "nemsFilter");
		xstream.aliasField("analysisDirectoryFile", cea.edyp.epims.transfer.dataformat.nems.NemsAnalysis.class,"analysisFile");
		xstream.aliasField("destinationPath", cea.edyp.epims.transfer.dataformat.nems.NemsAnalysis.class,"analysisDestination");

	}

	
	public static CacheManager getInstance(){
		if(instance == null)
			instance = new CacheManager();
		return instance;
	}

	public void setupForConfiguration(String config) {
		StopWatch stopWatch = new Slf4JStopWatch("Cache configuration");
		if(currentConfiguration != null){
			analysisPerConfig.put(currentConfiguration, currentAnalysisMap);
			try {
				save();
			}catch(IOException ioe){
				logger.warn("Configuration "+currentConfiguration+" NOT SAVED");
			}
		}
		this.currentConfiguration = config;
		if(analysisPerConfig.get(config) == null){
			try {
				initializeCache(config);
			} catch (IOException e) {				
				logger.warn("Impossible de lire le fichier pour la config "+config+" : "+e.getMessage());
				analysisPerConfig.put(currentConfiguration, new HashMap<String, List<Analysis>>());
			}
		}
		currentAnalysisMap = analysisPerConfig.get(currentConfiguration);
		stopWatch.stop();
	}
	
	private void initializeCache(String config) throws IOException {

		File f = new File(config+".xml");
		try {
			Map<String,List<Analysis>> analysisInFile =(Map<String,List<Analysis>>) xstream.fromXML(new FileReader(f));			
			if(analysisInFile == null)
				analysisInFile = new HashMap<>();
			analysisPerConfig.put(config, analysisInFile);
		} catch (IOException e) {
			logger.debug("IOE "+e.getMessage());
			analysisPerConfig.put(config, new HashMap<>());
			throw e;
		}		
	}

	public String getConfiguration() {
		return this.currentConfiguration;
	}
	
	/**
	 * Return all the analysis associated to the specified file. The 
	 * file absolute path should be specified. 
	 *    
	 * @param acqFileAbsolutePath String representation of the absolute path of the Analysis file
	 * @return all analysis associated to specified file
	 */
	public Analysis[] getAnalysis(String acqFileAbsolutePath) {
		List<Analysis> all = currentAnalysisMap.get(acqFileAbsolutePath);
		if(all == null)
			return null;
		return all.toArray(new Analysis[all.size()]);
	}
	
	/**
	 * Set specified File Absolute Path / Analysis Map  for current configuration. This will 
	 * replace all previously saved analysis. 
	 *    
	 * @param analysisPerFile String representation of the absolute path of the file associated to Analysis
	 * @return all analysis associated to specified file
	 */
	public void setAnalysis(Map<String, List<Analysis>> analysisPerFile) {
		currentAnalysisMap.clear();
		currentAnalysisMap.putAll(analysisPerFile);
	}
	
	
	/**
	 * Add all specified analysis to current configuration.
	 *   
	 * @param analysisPerFile  path, Analysis map to add.
	 */	
	public void addAnalysis(Map<String, List<Analysis>> analysisPerFile) {
		
	  for(String currentAbsPath : analysisPerFile.keySet()){
	    List<Analysis> analList = currentAnalysisMap.get(currentAbsPath);
	    if(analList == null)
	       analList = analysisPerFile.get(currentAbsPath);
	    else
	      analList.addAll(analysisPerFile.get(currentAbsPath));
	    
	    currentAnalysisMap.put(currentAbsPath,analList);
	    
	  }

	}
	
	/**
	 * Remove all the analysis associated to specified file. The 
	 * file absolute path should be specified. 
	 * 
	 * @param acqFileAbsolutePath Absolute path of acquisition file to remove from cache.
	 */
	public void removeAnalysis(String acqFileAbsolutePath) {
		currentAnalysisMap.remove(acqFileAbsolutePath);
	}
		
	public void save(String cfg) throws IOException{
		
		File f = new File(cfg+".xml");
		try {
			FileWriter writer = new FileWriter(f);
			xstream.toXML(currentAnalysisMap, writer);
//			Iterator<String> keys = currentAnalysisMap.keySet().iterator();
//			while(keys.hasNext()){
//				String nextKey = keys.next();
//				List<Analysis> a = currentAnalysisMap.get(nextKey);
//				xstream.toXML(nextKey, writer);
//				for(int i=0; i<a.size(); i++){
//					xstream.toXML((Analysis)a.get(i), writer);
//				}
//			}
		} catch (IOException e) {
			logger.debug("IOE "+e.getMessage());
			e.printStackTrace();
			throw e;
		}		
	}
	
	public void save() throws IOException{
		save(currentConfiguration);
	}
	
}
