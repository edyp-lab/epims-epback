/*
 * Created on Nov 26, 2004
 *
 * $Id: DataFormat.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.model;

import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JComponent;


/**
 * 
 * 
 * @author CB205360
 */
public interface DataFormat {

   /**
    * Return an Array of all the Analysis associated to the specified
    * directory. This array may be cached in order to improve the performance. 
    *  
    * @param dir Search for Analysis into the specified directory
    * @return an Array, may be empty, of all found analysis
    */
   public Analysis[] getAnalysis(File dir);
   
   /**
    * Return the IFileTransfertManager appropriated for this
    * DataFormat.
    * 
    */
   public IFileTransfertManager getFileTransfertManager();
   
   
   /**
    * Returns the number of properties available for this data format object.
    * 
    * @return the number of properties available for this data format object.
    */
   public int getPropertyCount();

    /**
     * Return the Class typing Objects of specified property (using its index).
     *   
     * @param propertyIdx The property index 
     * @return The property Objects Class or null if no property is associated to the 
     * specified index.
     */      
   public Class getPropertyClass(int propertyIdx);
   
  /**
   * Return the Label for the specified property (using its index).
   *   
   * @param propertyIdx The property index 
   * @return The property Label or null if no property is associated to the 
   * specified index.
   */      
   public String getPropertyLabel(int propertyIdx);

  /**
   * Return the property, specified by its index, value for the specified Analysis
   *   
   * @param propertyIdx The property index 
   * @param analysis The Analysis to look for the property's value
   * @return The property value for the specified Analysis or null if no 
   * property is associated to the specified index or if the Analysis doesn't 
   * belong to this DataFormat
   */      
   public Object getProperty(int propertyIdx, Analysis analysis);
      
   /**
    * Returns the configuration pane for this data format object.
    * 
    * @return a JComponent object
    */
   public JComponent getConfigurator();
   
   public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

   public void addPropertyChangeListener(PropertyChangeListener listener);

   public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

   public void removePropertyChangeListener(PropertyChangeListener listener);   
   
}
