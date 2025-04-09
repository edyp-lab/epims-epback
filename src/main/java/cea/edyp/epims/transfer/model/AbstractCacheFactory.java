package cea.edyp.epims.transfer.model;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

import java.io.File;
import java.util.*;


public abstract class AbstractCacheFactory implements IFactory {

	protected DataFormat format;
	public AbstractCacheFactory(){
	}
	
	public Analysis[] getAnalyses(DataFormat format, List<File> files) {	
		this.format = format;
		
		//New map to save to cache
		Map<String, List<Analysis>> cachedAnalysis = new HashMap<>();

		//returned analysis
		List<Analysis> analysis = new ArrayList<>();

		StopWatch stopWatch = new Slf4JStopWatch(getClass().getSimpleName()+".analyses objects");

		boolean callBatch = allowBatch();
		List<File> filesInCreateBatch = new ArrayList<>();

		//Search for Analysis in all files
    for (File currentFile : files) {
      boolean needToCreate = false;

      //Search in cache
      Analysis[] result = CacheManager.getInstance().getAnalysis(currentFile.getAbsolutePath());

      if (result == null) {
        //Not in cache, so create them
        needToCreate = true;

      } else {
        //For all returned Analysis, try to set dataformat property
        for (int indexAnalysis = 0; indexAnalysis < result.length; indexAnalysis++) {
          Analysis nextA = result[indexAnalysis];
          if (nextA.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE) {
            needToCreate = true;
            break;
          }
          nextA.setDataFormat(this.format);
        }

        if (!needToCreate) {
          //Found in current cache, add them to final cache
          ArrayList<Analysis> currentAnalysis = new ArrayList<>(Arrays.asList(result));
          cachedAnalysis.put(currentFile.getAbsolutePath(), currentAnalysis);
          analysis.addAll(currentAnalysis);
        }
      }

      if (needToCreate) {
        if (callBatch)
          filesInCreateBatch.add(currentFile);
        else {
          List<Analysis> fileAnalysis = createAnalysis(currentFile);
          if (fileAnalysis != null && !fileAnalysis.isEmpty()) {
            //Add created analysis to final cache
            cachedAnalysis.put(currentFile.getAbsolutePath(), fileAnalysis);
            //Add top result
            analysis.addAll(fileAnalysis);
          }
        }
      }
      stopWatch.lap(getClass().getSimpleName() + ".analyses objects.iteration");
    } //end for all files

		// Create Analysis in Batch if possible
		if(!filesInCreateBatch.isEmpty()){
			Map<String, List<Analysis>> fileAnalysis = createBatchAnalysis(filesInCreateBatch);
			if (fileAnalysis != null && !fileAnalysis.isEmpty()) {
				//Add created analysis to final cache
				cachedAnalysis.putAll(fileAnalysis);
				//Add top result
				analysis.addAll(fileAnalysis.values().stream().flatMap(List::stream).toList());
			}
		}

		//Reset cache information
		CacheManager.getInstance().setAnalysis(cachedAnalysis);
		stopWatch.stop();
		return analysis.toArray(new Analysis[0]);
	}

	public abstract List<Analysis> createAnalysis(File file);
	public abstract Map<String, List<Analysis>> createBatchAnalysis(List<File> files);

	public  boolean allowBatch(){
		return false; //default
	}
}
