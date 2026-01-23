package fr.edyp.epims.transfer.util;

import java.io.File;
import java.io.IOException;
import java.util.List;


import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.MultiFilesAnalysis;

/***
 * IFileTransferManager for MultiFilesAnalysis which create a Zip in
 * destination directory.
 * 
 * @author VD225637
 * 
 */

public class ZipFileTransferManager extends DefaultFileTransferManager {

	public ZipFileTransferManager() {
		super();
	}

	// ---- AbstractFileTransfertManager methods -----

	@Override
	protected List<File> getAnalysisFilesToDelete(Analysis a) {
		if(MultiFilesAnalysis.class.isAssignableFrom(a.getClass()))
			return ((MultiFilesAnalysis)a).getAllAcquisitionFile();
		else
			return List.of(a.getSourceFile());
	}

	@Override
	protected void doAnalysisCopy(Analysis analysis, String destPath) throws IOException {
		File destination = new File(destPath);
		boolean copySucess = false;
		if(MultiFilesAnalysis.class.isAssignableFrom(analysis.getClass())) {
			//First check all acq files exist
			for (File nextFile : ((MultiFilesAnalysis)analysis).getAllAcquisitionFile()) {
				if (nextFile == null || !nextFile.exists()) {
					logger.warn("File for analysis {} can't be find ! Can't copy acquisition", analysis.getName());
					throw new IOException("Problem on analysisFile " + nextFile + " for analysis " + analysis.getName()
									+ ". The file can't be reached or is null");
				}
			}
			if( destination.createNewFile())
				copySucess = FileUtils.copyFilesToOneZip(destination,  ((MultiFilesAnalysis)analysis).getAllAcquisitionFile(),analysis.getFile(),  ((MultiFilesAnalysis)analysis).keepRelativePath());
			if(!copySucess) {
				throw new IOException("Error while creating zip File " + analysis.getFileName() + " for analysis " + analysis.getName());
			}
		} else
			FileUtils.secureCopy(analysis.getFile(), new File(destPath), analysis.getContentFilter());
	}

}
