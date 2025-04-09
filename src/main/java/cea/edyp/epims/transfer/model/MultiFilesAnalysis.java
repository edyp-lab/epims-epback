package cea.edyp.epims.transfer.model;

import java.io.File;
import java.util.List;

/***
 * Represent an Analysis which is associated to multiple Files.
 * In this case, an empty Zip file should by returned by the 
 * getFile/getFileName and should be created when transferring the analysis
 * as actually One File is associated to an acquisition.  
 *  
 * @author VD225637
 *
 */

public interface MultiFilesAnalysis extends Analysis {

  
	/**
	 * Return all the files of this Analysis.
	 * 
	 * 	@return
	 */
  List<File> getAllAcquisitionFile();

	boolean keepRelativePath();
}
