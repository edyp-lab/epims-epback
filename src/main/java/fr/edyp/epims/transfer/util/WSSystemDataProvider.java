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
/*
 * Created on 14 mars 2005
 *
 * $Id: BackPimsUtil.java,v 1.2 2008-02-20 07:00:50 dupierris Exp $
 */
package fr.edyp.epims.transfer.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import fr.edyp.epims.json.*;
import fr.edyp.epims.json.AcquisitionFileDescriptorJson;
import fr.edyp.epims.json.AcquisitionFileMessageJson;
import fr.edyp.epims.transfer.preferences.EPBackPreferences;
import fr.edyp.epims.transfer.preferences.PreferencesKeys;
import fr.edyp.epims.transfer.task.AcquisitionServices;
import fr.edyp.epims.transfer.task.SystemServices;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.perf4j.StopWatch;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.BackupParameters;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * @author VDUPIERR
 */
public class WSSystemDataProvider implements IEPSystemDataProvider {

	private static final ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());
	private static final Logger logger = LoggerFactory.getLogger(WSSystemDataProvider.class);

	private String ePimsRootPath;
	private Boolean isRootLocal;

	/**
	 * Map used in function getDestinationDir, to reduce the number of
	 * WS requests that slows down eP-Back, the map keep the relation
	 * sample-AcquPath
	 */
	private final Map<String, String> sampleNameToAcquPath = new HashMap<>();

	public WSSystemDataProvider() throws InstantiationException {
		// Get Properties
		ePimsRootPath = null;

		try {
			ePimsRootPath = EPBackPreferences.root().get(PreferencesKeys.SERVER_ROOT_KEY,"<FTP>");
			String transferMode = EPBackPreferences.root().get(PreferencesKeys.TRANSFER_MODE,PreferencesKeys.DEFAULT_TRANSFER_MODE);
			isRootLocal = transferMode.equals(PreferencesKeys.DIRECT_TRANSFER_MODE);
			if(ePimsRootPath.equals("<FTP>") && isRootLocal) {
				ePimsRootPath = null;
				throw new InstantiationException(RSCS.getString("epims.root.webservices.error"));
			}

		} catch (Exception e) {
			throw new InstantiationException(RSCS.getString("webservices.error"));
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.edyp.epims.transfer.util.IEPSystemDataProvider#getPimsRootPath()
	 */
	public String getPimsRootPath() {
		return ePimsRootPath;
	}

	@Override
	public Boolean isPimsRootLocal() {
		return isRootLocal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.edyp.epims.transfer.util.IEPSystemDataProvider#getPimsSystemRelativePath
	 * ()
	 */
	public String getPimsSystemRelativePath() {
		return SystemServices.getPimsSystemRelativePathTask();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.edyp.epims.transfer.util.IEPSystemDataProvider#getStudyNameFor(java
	 * .lang.String)
	 */
	public String getStudyNameFor(String sampleName) {

		StudyJson studyJson = AcquisitionServices.getStudyPathJson(sampleName);
		if (studyJson != null) {
			return studyJson.getNomenclatureTitle();
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.edyp.epims.transfer.util.IEPSystemDataProvider#isSampleExist(java.lang
	 * .String)
	 */
	public boolean isSampleExist(String sampleName) {
		SampleJson splDecr = AcquisitionServices.getSampleJson(sampleName);
		return (splDecr != null);
	}

	public boolean isAcquisitionExist(String acqName, String instrumentName) {
		try {
			ProtocolApplicationJson protocolApplicationJson = AcquisitionServices.getAcquisitionsDescriptors(acqName, instrumentName);

			return (protocolApplicationJson != null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.edyp.epims.transfer.util.IEPSystemDataProvider#isSpectrometerDefined
	 * (java.lang.String)
	 */
	public boolean isSpectrometerDefined(String instrumentName) {
		InstrumentJson instDesc = AcquisitionServices.getInstrumentJson(instrumentName);
		return (instDesc != null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.edyp.epims.transfer.util.IEPSystemDataProvider#
	 * createAcquisitionAndFilesFor (fr.edyp.epims.transfer.model.Analysis,
	 * java.lang.String)
	 */
	public void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException {
		SampleJson splDesc = null;
		if (a.getType() == Analysis.AnalysisType.RESEARCH) {
			splDesc = AcquisitionServices.getSampleJson(a.getSample());
			if (splDesc == null) {
				throw new BackupException(RSCS.getString("sample.invalid"));
			}
			if (a.getCategory() == null && splDesc.getCategory() != null) {
				a.setCategory(splDesc.getCategory().name());
			}
		}

		// Instrument description
		InstrumentJson instrDescriptor = AcquisitionServices.getInstrumentJson(instrumentName);
		if (instrDescriptor == null) {
			instrDescriptor = new InstrumentJson();
			instrDescriptor.setName(instrumentName);
		}

		AcquisitionFileDescriptorJson acqFileDesc = createAcquisitionFileDescriptor(a);
		ProtocolApplicationJson protocolApplicationJson = acqFileDesc.getAcquisition();
		AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();
		acquisitionJson.setInstrumentId(instrDescriptor.getId());
		acquisitionJson.setInstrumentName(instrumentName);


		AcquisitionFileMessageJson acqFileMessage = new AcquisitionFileMessageJson();

		acqFileMessage.setAcquisitionFileDescriptor(acqFileDesc);
		acqFileMessage.setSampleDescriptor(splDesc);

		boolean success = AcquisitionServices.createAcquisition(acqFileMessage);
		if (!success ) {
			logger.debug("Error saving Acquisition");
			throw new BackupException("Error saving Acquisition");
		}
	}

	/**
	 * Create an AcquisitionFileDescriptor with the information of the given
	 * Analysis. BEWARE : this AcquisitionFileDescriptor will not have an
	 * instrumentDescriptor!
	 *
	 * @return : AcquisitionFileDescriptor
	 * @param : Analysis on which the AcquisitionFileDescriptor will be created
	 */
	private AcquisitionFileDescriptorJson createAcquisitionFileDescriptor(Analysis analysis) {
		// Acquisition and associated file descriptions
		AcquisitionFileDescriptorJson acquFileDesc = new AcquisitionFileDescriptorJson();
		ProtocolApplicationJson protocolApplicationJson = new ProtocolApplicationJson();
		AcquisitionJson acquisitionJson = new AcquisitionJson();
		protocolApplicationJson.setAcquisitionJson(acquisitionJson);

		protocolApplicationJson.setName(analysis.getName());
		switch (analysis.getType()) {
		case BLANK:
			acquisitionJson.setNature("Blanc");
			break;
		case CONTROL_INSTRUMENT:
			acquisitionJson.setNature("ControleInstrument");
			break;
		case CONTROL_LC:
			acquisitionJson.setNature("ControleLC");
			break;
		default:
			acquisitionJson.setNature("Recherche");
			break;
		}
		acquisitionJson.setDurationMin(analysis.getDuration());
		acquisitionJson.setMethodName(analysis.getMethodName());
		acquisitionJson.setInjectionVolume(analysis.getInjectionVolume());
		acquisitionJson.setVialInformation(analysis.getVialInformation());
		if (analysis.getCategory() != null) {
			acquisitionJson.setCategory(Category.valueOf(analysis.getCategory()));
		}
		protocolApplicationJson.setActor(analysis.getOperator());
		protocolApplicationJson.setComment(analysis.getDescription());

		long size = analysis.getEstimatedSize();
		Double sizeInMo = ((double) size) / (1024 * 1024);
		acquFileDesc.setFileSize(sizeInMo);
		acquFileDesc.setFileName(analysis.getFileName());

		if (analysis.getDate() != null) {
			protocolApplicationJson.setDate(analysis.getDate());
			acquFileDesc.setDate(analysis.getDate());
		} else {
			protocolApplicationJson.setDate(new Date());
			acquFileDesc.setDate(new Date());
		}

		acquFileDesc.setAcquisition(protocolApplicationJson);

		return acquFileDesc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.edyp.epims.transfer.util.IEPSystemDataProvider#
	 * getAssociatedFileDestinationFile(fr.edyp.epims.transfer.model.Analysis,
	 * java.io.File, java.lang.String,
	 * fr.edyp.epims.transfer.model.BackupParameters)
	 */
	public File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
		File destination = null;
		boolean dirCreationSucceed;
		if(!isRootLocal){
			String msg = RSCS.getString("not.local.root");
			throw new BackupException(msg);
		}

		if (Analysis.SPECTRA_FILETYPE.equals(fileType)) {
			// Only valid for research analysis
			if (!a.getType().equals(Analysis.AnalysisType.RESEARCH)) {
				String msg = RSCS.getString("analysis.invalid.associated.file.type");
				Object[] args = { a.getName() };
				throw new BackupException(MessageFormat.format(msg, args));
			}

			SampleJson splDescr = AcquisitionServices.getSampleJson(a.getSample());
			if (splDescr == null) {
				String msg = RSCS.getString("analysis.invalid.sample.description");
				Object[] args = { a.getName() };
				throw new BackupException(MessageFormat.format(msg, args));
			}

			StudyPathJson studyPathJson = AcquisitionServices.getStudyPathJson(a.getSample());

			try {
				File studyFullPath = studyPathJson != null ? new File(getPimsRootPath(),studyPathJson.getPath()): null;

				if (studyFullPath != null && studyFullPath.exists() ) {
					String spectraRelativePath = SystemServices.getSpectraRelativePath();
					destination = spectraRelativePath !=null ? new File(studyFullPath, SystemServices.getSpectraRelativePath()) : null;

					// Destination dir not exist => it must be created
					if(destination == null ){
						String msg =" No folder specified for SPECTRA !! Contact your administrator ";
						throw new BackupException(msg);
					}
					if (!destination.exists()) {
						dirCreationSucceed = destination.mkdirs();

						// Directory creation failed => Error thrown
						if (!dirCreationSucceed) {
							String msg = RSCS.getString("cant.create.associated.file.directory");
							Object[] args = { destination.getAbsolutePath() };
							throw new BackupException(MessageFormat.format(msg, args));
						}
					}

				} else {
					if (studyFullPath==null)
						throw new BackupException("Can't define Study Path ");
						// Study's full path doesn't exist
					String msg = RSCS.getString("study.dir.notexist");
					Object[] args = { studyFullPath.getAbsolutePath() };
					throw new BackupException(MessageFormat.format(msg, args));
				}
			} catch (BackupException epce2) {
				String msg = RSCS.getString("epims.getinfo.error");
				Object[] args = { epce2.getMessage() };
				throw new BackupException(MessageFormat.format(msg, args));
			}
		}
		return destination;
	}

	@Override
	public String getRelativeAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
		String assocAnalysePath = "";
		if (Analysis.SPECTRA_FILETYPE.equals(fileType)) {
			// Only valid for research analysis
			if (!a.getType().equals(Analysis.AnalysisType.RESEARCH)) {
				String msg = RSCS.getString("analysis.invalid.associated.file.type");
				Object[] args = { a.getName() };
				throw new BackupException(MessageFormat.format(msg, args));
			}

			SampleJson splDescr = AcquisitionServices.getSampleJson(a.getSample());
			if (splDescr == null) {
				String msg = RSCS.getString("analysis.invalid.sample.description");
				Object[] args = { a.getName() };
				throw new BackupException(MessageFormat.format(msg, args));
			}

			StudyPathJson studyPathJson = AcquisitionServices.getStudyPathJson(a.getSample());

			try {

				if (studyPathJson != null ) {
					String spectraRelativePath = SystemServices.getSpectraRelativePath();
					assocAnalysePath = spectraRelativePath !=null ?  studyPathJson+"/" +spectraRelativePath : null;

					// Destination dir not exist => it must be created
					if(assocAnalysePath == null ){
						String msg =" No folder specified for SPECTRA !! Contact your administrator ";
						throw new BackupException(msg);
					}

				} else {
						throw new BackupException("Can't define Study Path ");
				}
			} catch (BackupException epce2) {
				String msg = RSCS.getString("epims.getinfo.error");
				Object[] args = { epce2.getMessage() };
				throw new BackupException(MessageFormat.format(msg, args));
			}
		}
		return assocAnalysePath;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.edyp.epims.transfer.util.IEPSystemDataProvider#getDestinationFile(cea
	 * .edyp.epims.transfer.model.Analysis,
	 * fr.edyp.epims.transfer.model.BackupParameters)
	 */
	public String getDestinationDir(Analysis analysis, BackupParameters param) throws BackupException {

		StopWatch stopWatch = new Slf4JStopWatch("ePims getDestinationDir", analysis.getName());
		String tempAcqPath = null;
		String destination;

		AcquisitionFileMessageJson acqFileMsg = new AcquisitionFileMessageJson();

		InstrumentJson instrDescriptor = AcquisitionServices.getInstrumentJson(param.getInstrumentName());
		Integer instrId = instrDescriptor != null ? instrDescriptor.getId() : -1;
		AcquisitionFileDescriptorJson acqFileDesc = createAcquisitionFileDescriptor(analysis);

		ProtocolApplicationJson protocolApplicationJson = acqFileDesc.getAcquisition();
		AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();
		acquisitionJson.setInstrumentId(instrId);
		acquisitionJson.setInstrumentName(param.getInstrumentName());

		acqFileMsg.setAcquisitionFileDescriptor(acqFileDesc);

		if (analysis.getType() == Analysis.AnalysisType.RESEARCH) {
			SampleJson splDescr = new SampleJson();
			splDescr.setName(analysis.getSample());
			acqFileMsg.setSampleDescriptor(splDescr);

			// Try to get temporary the acqPath from the research sample (if it's
			// not a research sample the path must be retrieved from the server)
			tempAcqPath = sampleNameToAcquPath.get(analysis.getSample());
		}

		if (tempAcqPath == null) {

			if (analysis.getType() == Analysis.AnalysisType.RESEARCH)
        logger.debug("{} not found in the cache", analysis.getSample());

			// So we must request threw WS to get the sample name and then add this
			// result in the hash map

			String acqPath = AcquisitionServices.getAcquisitionDestinationPath(acqFileMsg);
			if (acqPath == null) {
				String msg = RSCS.getString("epims.getinfo.error");
				//Object[] args = { epce2.getMessage() };
				throw new BackupException(MessageFormat.format(msg, ""));
			} else {

        logger.debug(" Get Destination for {} => {}", analysis.getName(), acqPath);
				destination = getDestLocalFile(analysis.getName(), acqPath);
				tempAcqPath = acqPath;
				// add this result in the HashMap
				sampleNameToAcquPath.put(analysis.getSample(), acqPath);
				stopWatch.lap("ePims getDestinationDir.from WS");
			}

		} else {// we found the sampleName in the map so take the acqPath
					// associated
      logger.debug(" Get Destination from the hashMap {} => {}", analysis.getName(), tempAcqPath);
			destination = getDestLocalFile(analysis.getName(), tempAcqPath); // new File(getPimsRootPath(), tempAcqPath);
			stopWatch.lap("ePims getDestinationDir.from Cache");
		}

		analysis.setDestination(destination);
		analysis.setRelativeDestination(tempAcqPath);
		stopWatch.stop();
		return destination;
	}

	@Nullable
	private String getDestLocalFile(String analyseName, String acqPath) throws BackupException {
		String destination;
		if(isRootLocal){
			File targetFile;
			try {
				File baseFile = new File(getPimsRootPath());
				targetFile = new File(baseFile, acqPath).getCanonicalFile();
				if (!targetFile.getPath().startsWith(baseFile.getCanonicalPath())) {
					throw new BackupException("Invalid file path for analysis " + analyseName );
				}
			} catch (IOException e){
				throw new BackupException("Invalid file path for analysis " + analyseName , e);
			}
			destination = targetFile.getAbsolutePath();
		} else {
			destination = acqPath;//new File(acqPath);
		}
		return destination;
	}

//	@Override
//	public String getRelativeDestinationDir(Analysis analysis, BackupParameters param) throws BackupException {
//
//		StopWatch stopWatch = new Slf4JStopWatch("ePims getRelativeDestinationDir", analysis.getName());
//		String tempAcqPath = null;
//		String acqPath="";
//
//		AcquisitionFileMessageJson acqFileMsg = new AcquisitionFileMessageJson();
//
//		InstrumentJson instrDescriptor = AcquisitionServices.getInstrumentJson(param.getInstrumentName());
//		Integer instrId = instrDescriptor != null ? instrDescriptor.getId() : -1;
//		AcquisitionFileDescriptorJson acqFileDesc = createAcquisitionFileDescriptor(analysis);
//
//		ProtocolApplicationJson protocolApplicationJson = acqFileDesc.getAcquisition();
//		AcquisitionJson acquisitionJson = protocolApplicationJson.getAcquisitionJson();
//		acquisitionJson.setInstrumentId(instrId);
//		acquisitionJson.setInstrumentName(param.getInstrumentName());
//
//		acqFileMsg.setAcquisitionFileDescriptor(acqFileDesc);
//
//		if (analysis.getType() == Analysis.AnalysisType.RESEARCH) {
//			SampleJson splDescr = new SampleJson();
//			splDescr.setName(analysis.getSample());
//			acqFileMsg.setSampleDescriptor(splDescr);
//
//			// Try to get temporary the acqPath from the research sample (if it's
//			// not a research sample the path must be retrieved from the server)
//			tempAcqPath = sampleNameToAcquPath.get(analysis.getSample());
//		}
//
//		if (tempAcqPath == null) {
//
//			if (analysis.getType() == Analysis.AnalysisType.RESEARCH)
//				logger.debug("{} not found in the cache", analysis.getSample());
//
//			// So we must request threw WS to get the sample name and then add this
//			// result in the hash map
//
//			acqPath = AcquisitionServices.getAcquisitionDestinationPath(acqFileMsg);
//			if (acqPath == null) {
//				String msg = RSCS.getString("epims.getinfo.error");
//				//Object[] args = { epce2.getMessage() };
//				throw new BackupException(MessageFormat.format(msg, ""));
//			} else {
//				logger.debug(" Get Destination for {} => {}", analysis.getName(), acqPath);
//				sampleNameToAcquPath.put(analysis.getSample(), acqPath);
//				stopWatch.lap("ePims getDestinationDir.from WS");
//			}
//
//		} else {// we found the sampleName in the map so take the acqPath
//			// associated
//			logger.debug(" Get Destination from the hashMap {} => {}", analysis.getName(), tempAcqPath);
//			stopWatch.lap("ePims getDestinationDir.from Cache");
//		}
//
////		analysis.setDestination(destination.getAbsolutePath());
//		stopWatch.stop();
//		return acqPath;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.edyp.epims.transfer.util.IEPSystemDataProvider#getAnalysisStatus(cea
	 * .edyp.epims.transfer.model.Analysis,
	 * fr.edyp.epims.transfer.model.BackupParameters)
	 */
	public int getAnalysisStatus(Analysis analysis, BackupParameters params) {

    logger.info("request analysis status for sample {}", analysis.getName());
		StopWatch stopWatch = new Slf4JStopWatch("ePims getAnalysisStatus", analysis.getName());

		if(analysis.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE)
			return Analysis.ANALYSIS_STATUS_NOT_READABLE; // Do not change status until the acq. file is readable !

		// Test if already saved in DB
		int result = testAcquisitionExist(analysis, params.getInstrumentName());

		Analysis.AnalysisType analysisType = analysis.getType();

		SampleJson splDescriptor  = AcquisitionServices.getSampleJson(analysis.getSample());


		if (analysisType.equals(Analysis.AnalysisType.RESEARCH)) {

			if (splDescriptor == null)
				result = result | Analysis.ANALYSIS_INVALID_SAMPLE_MASK;
			else {

				StudyPathJson stdDescriptor = AcquisitionServices.getStudyPathJson(analysis.getSample());

        assert stdDescriptor != null;
        if (!stdDescriptor.isRunningStatus())
					result = result | Analysis.ANALYSIS_STUDY_CLOSED_MASK;
			} // END Sample valid name

		} // end analysis type = research

		analysis.setStatus(result);
		stopWatch.stop();
		return result;
	}

	private int testAcquisitionExist(Analysis analysis, String instrumentName) {
		StopWatch stopWatch = new Slf4JStopWatch("ePims testAcquisitionExists");
		int result = Analysis.ANALYSIS_OK_MASK;

		try {
			ProtocolApplicationJson protocolApplicationJson = AcquisitionServices.getAcquisitionsDescriptors(analysis.getName(), instrumentName);


			if (protocolApplicationJson != null) {
				result = Analysis.ANALYSIS_EXIST_MASK;
			} // End acquisition exist

		} catch (Exception e) {
			String msg = RSCS.getString("acq.more.than.one.exist");
			Object[] args = {analysis.getName()};
			logger.warn(MessageFormat.format(msg, args));
			logger.error(e.getMessage());
		}

		stopWatch.stop();
		return result;
	}

}
