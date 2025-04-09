/*
 * Created on Nov 26, 2004
 *
 * $Id: DFConfiguratorPane.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import cea.edyp.epims.transfer.model.BackupParameters;

/**
 * 
 * 
 * @author CB205360
 */
public class DataFormatConfigurationPane extends JPanel implements PropertyChangeListener {

   private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

   private final BackupParameters parameters;

   public DataFormatConfigurationPane(BackupParameters params) {
      setLayout(new BorderLayout());
      parameters = params;
      createPanel();
      parameters.addPropertyChangeListener(BackupParameters.DATA_FORMAT_PARAMETER, this);      
   }

   public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(BackupParameters.DATA_FORMAT_PARAMETER)) {
         this.removeAll();
         createPanel();            
      }
   }
   
   private void createPanel(){     
     Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
     String title = RSCS.getString("dataformat.panel.title");
     TitledBorder tBorder = BorderFactory.createTitledBorder(loweredetched, title);
     tBorder.setTitleJustification(TitledBorder.RIGHT);

     this.add(parameters.getDataFormat().getConfigurator(), BorderLayout.CENTER);
     this.setBorder(tBorder);
   }
    
}
