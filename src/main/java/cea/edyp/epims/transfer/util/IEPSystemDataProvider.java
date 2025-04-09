package cea.edyp.epims.transfer.util;

import java.io.File;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.BackupException;
import cea.edyp.epims.transfer.model.BackupParameters;

public interface IEPSystemDataProvider {

	/**
	 * Return the Path location for PIMS_ROOT
	 * 
	 * @return absolute path to PIMS_ROOT
	 */
	public String getPimsRootPath();

	/**
	 * Return the relative path (from PIMS_ROOT) to directory representing
	 * PIMS_SYSTEM
	 * 
	 * @return relative path (from PIMS_ROOT) to PIMS_SYSTEM or null if
	 *         information is not accessible
	 */
	public String getPimsSystemRelativePath();

	/**
	 * Return the nomenclature name of study containing specified sample, null if
	 * does not exist
	 * 
	 * @param sampleName
	 * @return study nomenclature to which specified sample belongs to
	 */
	public String getStudyNameFor(String sampleName);

	/**
	 * Test if specified sample name exist in ePims system
	 * 
	 * @param sampleName
	 * @return true if sample is defined in ePims system, false otherwise.
	 */
	public boolean isSampleExist(String sampleName);

	/**
	 * Test if an acquisition with same name on same instrument exist in ePims
	 * system
	 * 
	 * @param acqName
	 *           : name of the acquisition to search for
	 * @param instrumentName
	 *           name of the instrument where were done the acquisition
	 * @return true if an acquisition with same name on same instrument is
	 *         defined in ePims system, false otherwise.
	 */
	public boolean isAcquisitionExist(String acqName, String instrumentName);

	/**
	 * Test if specified spectrometer exist in ePims system
	 * 
	 * @param instrumentName
	 * @return true if instrument is defined in ePims system, false otherwise.
	 */
	public boolean isSpectrometerDefined(String instrumentName);

	/**
	 * Create ePims acquisition, acquisition result file and all associated file
	 * from specified Analysis object
	 * 
	 * @param a
	 *            analysis to create ePims object for
	 * @param instrumentName
	 *           name of the spectrometer on which analysis was done
	 * @throws BackupException
	 *            on error
	 */
	public void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException;

	/**
	 * Get analysis associated file destination directory on ePims System If
	 * associated file is SPECTRA => destination = 'path to study'/path to
	 * spectra under study (depending on ePims configuration)
	 * 
	 * @param a
	 *           Analysis to get destination file for associated file
	 * @param f
	 *           associated file
	 * @param fileType
	 *           Associated File type
	 * @throws BackupException
	 *            if an error occurs while getting information
	 */
	public File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException;

	/**
	 * Get destination directory for specified analysis. if research analysis =>
	 * 'path to study'/'path to raws file' under study (depending on ePims
	 * configuration) if shared (blank or control) => path to shared path for
	 * specified analysis properties (date, type, instrument ...)
	 * 
	 * @param a
	 *           Analysis to get destination file for
	 * @throws BackupException
	 *            if an error occurs while getting information
	 */
	public File getDestinationDir(Analysis a, BackupParameters param) throws BackupException;

	public int getAnalysisStatus(Analysis analysis, BackupParameters params);

}