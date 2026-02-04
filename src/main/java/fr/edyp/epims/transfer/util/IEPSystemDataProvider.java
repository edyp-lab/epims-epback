package fr.edyp.epims.transfer.util;

import java.io.File;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.BackupParameters;

public interface IEPSystemDataProvider {

	Boolean isPimsRootLocal();

	/**
	 * Return the Path location for PIMS_ROOT
	 * 
	 * @return absolute path to PIMS_ROOT
	 */
  String getPimsRootPath();

	/**
	 * Return the relative path (from PIMS_ROOT) to the directory representing
	 * PIMS_SYSTEM
	 * 
	 * @return relative path (from PIMS_ROOT) to PIMS_SYSTEM or null if
	 *         information is not accessible
	 */
  String getPimsSystemRelativePath();

	/**
	 * Return the nomenclature name of study containing specified sample, null if
	 * it does not exist
	 * 
	 * @param sampleName Get study nomenclature which defines the specified sample
	 * @return study nomenclature to which the specified sample belongs to
	 */
  String getStudyNameFor(String sampleName);

	/**
	 * Test if specified sample name exists in ePims
	 * 
	 * @param sampleName sample name to search for
	 * @return true if the sample is defined in ePims, false otherwise.
	 */
  boolean isSampleExist(String sampleName);

	/**
	 * Test if an acquisition with same name and on same instrument exists in ePims
	 * 
	 * @param acqName
	 *           : name of the acquisition to search for
	 * @param instrumentName
	 *           name of the instrument where were done the acquisition
	 * @return true if an acquisition with same name and on same instrument is
	 *         defined in ePims, false otherwise.
	 */
  boolean isAcquisitionExist(String acqName, String instrumentName);

	/**
	 * Test if the specified spectrometer exists in ePims
	 * 
	 * @param instrumentName name of the spectrometer to search for
	 * @return true if an instrument with specific name is defined in ePims, false otherwise.
	 */
  boolean isSpectrometerDefined(String instrumentName);

	/**
	 * Create ePims acquisition, acquisition result file and all associated file
	 * from a specified Analysis object
	 * 
	 * @param a
	 *            analysis to create ePims object for
	 * @param instrumentName
	 *           name of the spectrometer on which analysis was done
	 * @throws BackupException
	 *            on error
	 */
  void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException;

	/**
	 * Get analysis associated file destination directory in ePims. The file path is defined on ePims
	 * using the Pims Root Path, an absolute path is used.
	 * If ePims Root is not local an Exception will be thrown
	 * If the associated file is of type SPECTRA => destination = 'path to study'/path to
	 * spectra under study (depending on ePims configuration)
	 * 
	 * @param a
	 *           Analysis to get the destination file for an associated file
	 * @param f
	 *           associated file
	 * @param fileType
	 *           Associated File type
	 * @throws BackupException
	 *            if an error occurs while getting information
	 */
  File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException;

	/**
	 * Get the destination directory for specified analysis.
	 * If ePims Root is local the directory path is defined on ePims using the Pims Root Path, an absolute path is used
	 * and is saved with its associated relative path in the Analysis object.
	 * If the ePims Root is not local only relative path will be saved in the Analysis object.
	 * If the analysis is of type research => 'path to study'/'path to raws file' under study
	 * (depending on ePims configuration)
	 * If it is a shared analysis (blank or control) => 'path to shared path' for specified analysis
	 * properties (date, type, instrument ...)
	 * 
	 * @param a
	 *           Analysis to get the destination path for
	 * @return destination directory path (absolute or relative depending on ePims Root)
	 * @throws BackupException
	 *            if an error occurs while getting information
	 */
  String getDestinationDir(Analysis a, BackupParameters param) throws BackupException;

	/**
	 * Get analysis associated file destination path relative to the Pims Root Path.
	 *
	 * If the associated file is of type SPECTRA => destination = 'path to study'/path to
	 * spectra under study (depending on ePims configuration)
	 *
	 * @param a
	 *           Analysis to get the destination file for an associated file
	 * @param f
	 *           associated file
	 * @param fileType
	 *           Associated File type
	 * @throws BackupException
	 *            if an error occurs while getting information
	 */
	String getRelativeAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException;

//	/**
//	 * Get the destination directory path for specified analysis relative to the Pims Root Path.
//	 * If the analysis is of type research => 'path to study'/'path to raws file' under study
//	 * (depending on ePims configuration)
//	 * If it is a shared analysis (blank or control) => 'path to shared path' for specified analysis
//	 * properties (date, type, instrument ...)
//	 *
//	 * @param a
//	 *           Analysis to get the relative destination path for
//	 * @throws BackupException
//	 *            if an error occurs while getting information
//	 */
//	String getRelativeDestinationDir(Analysis a, BackupParameters param) throws BackupException;

	int getAnalysisStatus(Analysis analysis, BackupParameters params);

}