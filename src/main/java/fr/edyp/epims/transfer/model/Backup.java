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
 * Created on Nov 30, 2004
 *
 * $Id: Backup.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package fr.edyp.epims.transfer.model;

import java.util.List;

/**
 * 
 * 
 * @author CB205360
 */
public interface Backup {

   public BackupParameters getBackupParameters();
   
   /**
    * Return an array if all the Analysis to backup. The returned
    * array may be empty. 
    * @return
    */
   public Analysis[] getAnalysisToBackup();
   
   /**
    * This method should be called before a transfer is started
    */
   public void transferBegin();
   
  /**
   * This method should be called after a transfer is finished
   */
   public void transferEnd(List<Analysis> processedFiles);
   
  /**
   * This method should be called when a transfer is aborted
   */
   public void transferAbort(List<Analysis> processedFiles);
}
