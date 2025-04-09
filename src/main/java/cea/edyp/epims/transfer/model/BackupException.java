/*
 * Created on Nov 26, 2004
 *
 * $Id: BackupException.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.model;

/**
 * 
 * 
 * @author CB205360
 */
public class BackupException extends Exception {

   private Throwable nestedException;
   
   public BackupException(String s) {
      super(s);
   }
   
   public BackupException(String s, Throwable nested) {
      super(s);
      nestedException = nested;
   }
   
   
   public Throwable getNestedException() {
      return nestedException;
   }

}
