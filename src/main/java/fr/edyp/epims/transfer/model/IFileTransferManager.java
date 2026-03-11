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
package fr.edyp.epims.transfer.model;

import fr.edyp.epims.transfer.util.IEPSystemDataProvider;

public interface IFileTransferManager {

	
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

