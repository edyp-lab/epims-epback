/*
 * Created on Nov 30, 2004
 *
 * $Id: Backup.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.model;

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
