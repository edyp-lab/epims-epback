package cea.edyp.epims.transfer.dataformat.thermo;

import cea.edyp.epims.transfer.model.Analysis;

import cea.edyp.epims.transfer.util.ListUtils;
import fr.profi.msdata.serialization.SerializationCallback;
import fr.profi.msdata.thermoreader.MetaDataReaderThread;
import fr.profi.msdata.thermoreader.MetaDataListReaderTask;
import fr.profi.msdata.thermoreader.MetaDataReaderTask;

import fr.profi.thermoreader.model.RunMetaData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MetadataReader {

  protected static Logger logger = LoggerFactory.getLogger(MetadataReader.class);
  private static MetadataReader m_instance;

  //Thread used to read raw files information
  private final MetaDataReaderThread m_readerThread;
  private static int nextTaskId = 0;

  // Will be called back with RunMetaData information
  private final MySerializationCallback m_callback;

  // List to trace analysis to process and state (ONGOING / FINISH)
  protected static HashMap<String, LTQAnalysis> analysisByPath;
  protected static HashMap<String, Integer> stateAnalysisByPath;
  protected static int ON_GOING_STATE= 0;
  protected static int FINISHED_STATE= 1;
  protected static int ERROR_STATE= 2;

  private MetadataReader(){
    analysisByPath = new HashMap<>();
    stateAnalysisByPath = new HashMap<>();
    m_callback = new MySerializationCallback();
    m_readerThread = MetaDataReaderThread.getInstance();
    m_readerThread.start(); // start reader
  }

  public static MetadataReader getInstance(){
    if(m_instance==null) {
      m_instance = new MetadataReader();
    }
    return m_instance;
  }

  //Method to trace analysis processing
  private synchronized void addAnalyse(LTQAnalysis analysis, File file){
    logger.trace("ADD Analyse "+analysis.getName());
    analysisByPath.put(file.getAbsolutePath(), analysis);
    stateAnalysisByPath.put(file.getAbsolutePath(), ON_GOING_STATE);
  }

  private synchronized boolean isAnalyseFilled(String filepath){
   return stateAnalysisByPath.get(filepath) != ON_GOING_STATE;
  }

  protected static synchronized void setAnalyseState(String filepath, int state){
    stateAnalysisByPath.put(filepath, state);
  }

  // Process a single Analysis
  public Analysis fillInfoFrom(File file, LTQAnalysis analysis){
    logger.debug(" --- Register task for file " + file.getName());
    addAnalyse(analysis, file);
    String filePath = file.getAbsolutePath();
    MetaDataReaderTask task = new MetaDataReaderTask(String.valueOf(++nextTaskId), file, m_callback);

    boolean analyseFilled = false;
    m_readerThread.addTask(task);
    int loopCount = 0;
    while (!analyseFilled) {
      try {
        Thread.sleep(100);
        loopCount++;
        if(isAnalyseFilled(filePath))
          analyseFilled = true;
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    logger.debug( " ------------ CALLED isAnalyseFilled "+loopCount+" times ....  ");
    return analysis;
  }


  public void fillAnalysisInfosXThreaded( List<LTQAnalysis> analysis, int nbThreads) {
    logger.info("   --- Register task for "+analysis.size()+"; files " );

    List<List<LTQAnalysis>> analysisByThread = ListUtils.splitInto(analysis, nbThreads);
    List<List<File>> analysisFilesByThread = new ArrayList<>(nbThreads);
    List<List<String>> analysisFilePathsByThread = new ArrayList<>(nbThreads);
    int nbAnalysis =0;
    for(List<LTQAnalysis> eachList : analysisByThread){
      if(!eachList.isEmpty()) {
        List<File> analysisFiles = new ArrayList<>(eachList.size());
        List<String> analysisFilePaths = new ArrayList<>(eachList.size());
        for (LTQAnalysis eachAnalysis : eachList) {
          addAnalyse(eachAnalysis, eachAnalysis.getFile());
          analysisFiles.add(eachAnalysis.getFile());
          analysisFilePaths.add(eachAnalysis.getFile().getAbsolutePath());
          nbAnalysis++;
        }
        analysisFilesByThread.add(analysisFiles);
        analysisFilePathsByThread.add(analysisFilePaths);
      }
    }

    logger.debug(" Created "+analysisFilesByThread.size()+" Threads. With "+analysisByThread.get(0).size()+" or "+analysisByThread.get(analysisByThread.size()-1).size()+" analysis. "+nbAnalysis+" added ");
    for (List<File> files : analysisFilesByThread) {
      m_readerThread.addTask(new MetaDataListReaderTask(String.valueOf(++nextTaskId), files, m_callback));
    }

    boolean allAnalyseFilled = false;
    int loopCount = 0;
    while (!allAnalyseFilled) {
      try {
        Thread.sleep(500);
        loopCount++;
        List<String> pathToRem = new ArrayList<>();
        for (List<String> analysisFilePaths : analysisFilePathsByThread) {
          for (String nextPath : analysisFilePaths) {
            if (isAnalyseFilled(nextPath)) {
              pathToRem.add(nextPath);
            }
          }
          analysisFilePaths.removeAll(pathToRem);
        }

        boolean allEmpty =true;
        for (List<String> analysisFilePaths : analysisFilePathsByThread) {
          if (!analysisFilePaths.isEmpty()) {
            allEmpty = false;
            break;
          }
        }

        allAnalyseFilled = allEmpty;

      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    logger.debug( " ------------ CALLED isAnalyseFilled {} times ....  ",loopCount);
  }

  public static class MySerializationCallback extends SerializationCallback {

    @Override
    public void run(String acqFilePath, List<RunMetaData> list, boolean success) {
      if(!success && StringUtils.isNotEmpty(acqFilePath)){
        cea.edyp.epims.transfer.dataformat.thermo.LTQAnalysis analysis = analysisByPath.get(acqFilePath);
        if(analysis == null){
          logger.warn("\n\n !!!! NO Analysis found for {}  .... MAY fall in Dead wait !!!! \n\n", acqFilePath);
        } else {
          logger.info("-- UNABLE to read info for analysis {} !!!!", analysis.getName());
            analysis.setStatus(Analysis.ANALYSIS_STATUS_NOT_READABLE);
            setAnalyseState(acqFilePath, ERROR_STATE);
        }
      }
      if(success) {
        list.forEach(l -> {
          LTQAnalysis analysis = analysisByPath.get(l.getFilePath());
          if (analysis == null) {
            logger.warn(" !!!! NO Analysis found for {}", l.getAcqName());
          } else {
            analysis.setDate(l.getAcqTime());
            analysis.setSample(l.getSampleName());
            analysis.setDescription(l.getAcqDescription());
            analysis.setOperator(l.getOperator());
            analysis.setDuration((float) l.getDurationMin());
            logger.info("-- Read info for analysis {}", analysis.getName());
            setAnalyseState(acqFilePath, FINISHED_STATE);
          }
        });
      }
    }
  }//End SerializationCallback

}
