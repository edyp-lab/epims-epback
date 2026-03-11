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

import org.perf4j.StopWatch;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.BackupParameters;
import org.perf4j.slf4j.Slf4JStopWatch;

public class FakeDataProvider implements IEPSystemDataProvider {

	
	public String getPimsRootPath() {
		return "d:/tmp/epims_root/a/";
	}

	@Override
	public Boolean isPimsRootLocal() {
		return true;
	}

	public String getPimsSystemRelativePath() {
		return "system";
	}

	public String getStudyNameFor(String sampleName) {
		return sampleName.substring(0, 1);
	}

	public boolean isSampleExist(String sampleName) {
		return !sampleName.contains("wrong");
	}

	public boolean isAcquisitionExist(String acqName, String instrumentName) {
		return false;
	}

	public boolean isSpectrometerDefined(String instrumentName) {
		return true;
	}

	public void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException {

	}

	public File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
		return new File(getDestinationDir(a, null), "Others");
	}

	public String getDestinationDir(Analysis a, BackupParameters param) throws BackupException {
		StopWatch stopWatch = new Slf4JStopWatch("fake getDestinationDir", a.getName());
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		File destination = new File(getPimsRootPath(), getStudyNameFor(a.getSample()));
		
		// Destination dir not exist => it must be created
		if (!destination.exists()) {
			boolean dirCreationSucceed = destination.mkdirs();

			// Directory creation failed => Error thrown
			if (!dirCreationSucceed) {
				throw new BackupException("Cannot create directory "+destination.getAbsolutePath());
			}
		}
		a.setDestination(destination.getAbsolutePath());
		stopWatch.stop();
		return destination.getAbsolutePath();
	}

	@Override
	public String getRelativeAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
		return getRelativeDestinationDir(a, null)+File.pathSeparator+"Others";
	}

//	@Override
	public String getRelativeDestinationDir(Analysis a, BackupParameters param) throws BackupException {
		StopWatch stopWatch = new Slf4JStopWatch("fake getRelativeDestinationDir", a.getName());
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		String destination = getStudyNameFor(a.getSample());

//		a.setDestination(destination);
		stopWatch.stop();
		return destination;
	}

	public int getAnalysisStatus(Analysis analysis, BackupParameters params) {
		StopWatch stopWatch = new Slf4JStopWatch("fake getAnalysisStatus", analysis.getName());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stopWatch.stop();
		int result = analysis.getSample().length() % 4;
		analysis.setStatus(result);
		return result;
	}

}
