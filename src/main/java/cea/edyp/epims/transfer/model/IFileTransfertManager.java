package cea.edyp.epims.transfer.model;

import cea.edyp.epims.transfer.util.IEPSystemDataProvider;

public interface IFileTransfertManager {

	
	/**
	 * Copy analysis, a, associated Files to destination path without other cleaning operation.
	 * 
	 * @param a Analysis to copy
	 * @param ePimsDataProvider  IEPSystemDataProvider providing methods to access data from ePims System
	 * @throws BackupException
	 */
	public void copyOnly(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException;
	
	
	/**
	 * Move analysis, a, associated Files to destination path and clean source path .
	 * 
	 * @param a Analysis to move
	 * @param ePimsDataProvider  IEPSystemDataProvider providing methods to access data from ePims System
	 * @throws BackupException
	 */
	public void move(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException;

	
	/**
	 * Clean source path of analysis, a, associated Files.
	 * 
	 * @param a Analysis to clean source path for
	 * @throws BackupException
	 */
	public void clean(Analysis a) throws BackupException;

}

