package cea.edyp.epims.transfer.dataformat.thermo;

import cea.edyp.epims.transfer.log.LogTextPanel;
import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Create Thermo Analysis from specified source path.
 * Analysis are created using ThermoAccess(based on ThermoFisher libraries) project
 *
 */
public class LTQFactory extends AbstractCacheFactory {

	protected static Logger logger = LoggerFactory.getLogger(LTQFactory.class);
  protected static Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  protected static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());


	public LTQFactory() {

	}

  @Override
  public boolean allowBatch() {
    return true;
  }


  @Override
  public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
    List<Analysis> filledAnalyses = createAnalysisFromList(files);
    Map<String, List<Analysis>> mappedFilledAnalyses = new HashMap<>();
    filledAnalyses.forEach(analysis -> mappedFilledAnalyses.put(analysis.getFile().getAbsolutePath(), Collections.singletonList(analysis) ));
    return mappedFilledAnalyses;
  }

  // Create a Single Analysis from specified file
  // TODO VDS Should be merge with createAnalysisFromList -> List with 1 element
	public List<Analysis> createAnalysis(File f) {
	  String msg = RSCS.getString("analysis.creation.info");
    Object[] args = {f.getAbsolutePath()};
    msg = MessageFormat.format(msg, args);
    mainLogger.info(msg);
    logger.info(msg);
    
    if (!f.exists() || f.isDirectory()){
      logger.debug("Invalid LTQ File specified");
      throw new IllegalArgumentException("Invalid LTQ File specified");
    }

    LTQAnalysis analysis = new LTQAnalysis(f, (LTQFormat)format);
    try {
      fillAnalysisInfo(f, analysis);
    } catch (Exception exp) {
      logger.error("erreur de lecture du fichier " + f.getAbsolutePath());
      msg = RSCS.getString("analysis.creation.error");
      Object[] twoArgs = {f.getAbsolutePath(), exp.getMessage()};           
      msg = MessageFormat.format(msg, twoArgs);
      mainLogger.info(msg);
      
      analysis = null;
    }
    
    List<Analysis> a = new ArrayList<>();
    if(analysis != null)
      a.add(analysis);
    return a;
	}

  protected void fillAnalysisInfo(File f, LTQAnalysis analysis)  {
    MetadataReader.getInstance().fillInfoFrom(f, analysis);
  }


  /**
   * Create an Analysis for each specified raw file.
   *
   * @param allFiles List of all files to read to create an anlysis for
   * @return list of analyses corresponding to specified files
   * @throws IllegalArgumentException if one file isn't a valid raw file
   */
  private List<Analysis> createAnalysisFromList(List<File> allFiles) {

    logger.debug(" CREATE Analysis for "+allFiles.size()+" files");
    for(File nextFile : allFiles) {
      String msg = RSCS.getString("analysis.creation.info");
      Object[] args = {nextFile};
      msg = MessageFormat.format(msg, args);
      mainLogger.info(msg);
      logger.info(msg);
    }

    allFiles.forEach( f -> { if (!f.exists() ){
      logger.debug("Invalid LTQ File specified");
      throw new IllegalArgumentException("Invalid LTQ File specified");
    }});

    List<LTQAnalysis> resultAnalysis = new ArrayList<>();
    for(File nextFile : allFiles){
      if(nextFile.isFile()){
        LTQAnalysis analysis = new LTQAnalysis(nextFile, (LTQFormat)format);
        resultAnalysis.add(analysis);
      }
    }

    try {
//      MetadataReader.getInstance().fillAnalysisInfos(resultAnalysis); //SingleThread implementation
      MetadataReader.getInstance().fillAnalysisInfosXThreaded(resultAnalysis,5);
    } catch (Exception exp) {
      logger.error("erreur de lecture depuis les " + allFiles.size()+" fichiers ");
      String msg = RSCS.getString("analysis.creation.error");
      allFiles.forEach(nextFile -> {
        Object[] twoArgs = {nextFile.getAbsolutePath(), exp.getMessage()};
        mainLogger.info(MessageFormat.format(msg, twoArgs));
      });

      resultAnalysis = null;
    }
    if(resultAnalysis != null){
      resultAnalysis.forEach(analysis->{
        if(analysis.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE) {
          String errMsg = RSCS.getString("analysis.creation.error");
          Object[] twoArgs = {analysis.getName(), "Invalid Analysis"};
          mainLogger.info(MessageFormat.format(errMsg, twoArgs));
        }
      });
      logger.debug(" - Analysis Create OK. Add "+resultAnalysis.size()+" analyses");
    } else
      logger.debug(" - Analysis Create ERROR. No analysis added");

    List<Analysis> a = new ArrayList<>();
    if(resultAnalysis != null)
      a.addAll(resultAnalysis);
    return a;
  }

  // --- Test Perf methods

//  public List<Analysis> createAnalysisFromDir(File f) {
//    String msg = RSCS.getString("analysis.creation.info");
//    Object[] args = {f.getAbsolutePath()};
//    msg = MessageFormat.format(msg, args);
//    mainLogger.info(msg);
//    logger.info(msg);
//
//    if (!f.exists() ){
//      logger.debug("Invalid LTQ File specified");
//      throw new IllegalArgumentException("Invalid LTQ File specified");
//    }
//    if(f.isFile())
//      return createAnalysis(f);
//    String[] exts = new String[]{"raw", "RAW"};
//    List<LTQAnalysis> resultAnalysis = new ArrayList<>();
//    List<File> allFiles =new ArrayList<>( FileUtils.listFiles(f, exts, true));//Arrays.asList(dirFile.listFiles());
//    for(File nextFile : allFiles){
//      if(nextFile.isFile()){
//        LTQAnalysis analysis = new LTQAnalysis(nextFile, (LTQFormat)format);
//        resultAnalysis.add(analysis);
//      }
//    }
//    try {
//      MetadataReader.getInstance().fillAnalysisInfos(resultAnalysis);
//    } catch (Exception exp) {
//      logger.error("erreur de lecture depuis " + f.getAbsolutePath());
//      msg = RSCS.getString("analysis.creation.error");
//      Object[] twoArgs = {f.getAbsolutePath(), exp.getMessage()};
//      msg = MessageFormat.format(msg, twoArgs);
//      mainLogger.info(msg);
//
//      resultAnalysis = null;
//    }
//
//
//    List<Analysis> a = new ArrayList<Analysis>();
//    if(resultAnalysis != null)
//      a.addAll(resultAnalysis);
//    return a;
//  }

}
