package fr.edyp.epims.transfer.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;

public class DefaultFileTransferManager extends AbstractFileTransfertManager {

	public DefaultFileTransferManager(){
		super();
	}

	// ---- AbstractFileTransfertManager methods -----
	@Override
	protected boolean destinationExist(String destPath) {
		File f = new File(destPath);
		return f.exists();
	}

	@Override
	protected String getDestinationPath(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws IOException {
		File destination = new File(a.getDestination(), a.getFileName());
		return destination.getAbsolutePath();
	}

	@Override
	protected String getAssociatedDataDestinationPath(Analysis a, File associatedFile, String fileType, IEPSystemDataProvider ePimsDataProvider) throws IOException {
    try {
      File assocFile = ePimsDataProvider.getAssociatedFileDestinationDir(a, associatedFile, a.getAssociatedFileType(associatedFile));
			assocFile = new File(assocFile, associatedFile.getName());
			return assocFile.getAbsolutePath();
		} catch (BackupException ex) {
      throw new RuntimeException(ex);
    }
  }

	@Override
	protected void doAnalysisCopy(Analysis analysis, String destPath) throws IOException {
		FileUtils.secureCopy(analysis.getFileToTransfer(), new File(destPath), analysis.getContentFilter());
	}

	@Override
	protected void doAssociatedDataCopy(File src, String destPath) throws IOException {
		FileUtils.secureCopy(src, new File(destPath));
	}

	protected void afterCopy() {

	}
}
