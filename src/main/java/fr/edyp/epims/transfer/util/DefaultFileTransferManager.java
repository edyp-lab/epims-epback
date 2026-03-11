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
