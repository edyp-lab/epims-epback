/*
 * Created on Nov 26, 2004
 *
 * $Id: MLFormat.java,v 1.1 2007-09-14 09:37:31 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.waters;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.DefaultFileTransfertManager;
import cea.edyp.epims.transfer.util.ExtensionFileFilter;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
public class MLFormat extends JPanel implements DataFormat {


	private static final long serialVersionUID = 5806319793310558296L;
	
	private static Logger logger = LoggerFactory.getLogger(MLFormat.class);
	private static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);

	private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	private static final String DESCRIPTION_PROP = "Description";
	private static final String USER_PROP = "Resp. instrument";
	private static final String PKL_PROP = "pkl";
	private static final String PRP_PROP = "prp";
	
	public static final String SPECTRA_BACKUP = "spectra handled";
	public static final String PKL_PATH = "pkl path";
   
	private static final String[] PROPERTIES = { DESCRIPTION_PROP, USER_PROP };

	private static int INSET = 5;

	private int pkl_id;
	private int prp_id;
	private boolean areSpectraHandled;
	private File pklPath;
   
	private JTextField pklPathTF;
	private JLabel pklDirLabel;
	private JButton pklBrowse;
      
	private FileFilter dataFilter;
	private MLFactory analysisFactory;
   
	public MLFormat() {
		String[] ext = { "raw" };
		dataFilter = new ExtensionFileFilter(ext);
		analysisFactory = new MLFactory();
		
		setLayout(new GridBagLayout());
      
		JCheckBox pklCB = new JCheckBox(RSCS.getString("pkl.spectra.handle.checkbox.label"));
		this.add(
               pklCB,
               new GridBagConstraints(
                  0,
                  0,
                  2,
                  1,
                  1.0,
                  1.0,
                  GridBagConstraints.NORTHWEST,
                  GridBagConstraints.HORIZONTAL,
                  new Insets(2*INSET, 2*INSET, INSET, 2*INSET),                 
                  0,
                  0));

     pklCB.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  setAreSpectraHandled( ((JCheckBox)e.getSource()).isSelected());
               }
            });

     pklDirLabel = new JLabel(RSCS.getString("pkl.path.textfield.label"));     
     this.add(
              pklDirLabel,
              new GridBagConstraints(
                 0,
                 1,
                 2,
                 1,
                 0.0,
                 0.0,
                 GridBagConstraints.WEST,
                 GridBagConstraints.NONE,
                 new Insets(0, 2*INSET, INSET, 2*INSET),                 
                 0,
                 0));

      
     pklPathTF = new JTextField(); 
     pklPathTF.setEnabled(false);
     this.add(
         pklPathTF,
         new GridBagConstraints(
            0,
            2,
            1,
            1,
            1.0,
            1.0,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL,
            new Insets(INSET, 5*INSET, INSET, INSET),
            0,
            0));

     pklBrowse = new JButton(RSCS.getString("action.browse.label"));
     pklBrowse.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(MLFormat.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
              File file = fc.getSelectedFile();
              pklPathTF.setText(file.getAbsolutePath());
              updatePKLPath();               
            }
         }      
      });
      
     pklBrowse.setPreferredSize(new Dimension(pklBrowse.getPreferredSize().width, pklPathTF.getPreferredSize().height)); 
     this.add(
         pklBrowse,
         new GridBagConstraints(
            1,
            2,
            1,
            1,
            0.0,
            0.0,
            GridBagConstraints.NORTHEAST,
            GridBagConstraints.NONE,
            new Insets(INSET, INSET, INSET, 2*INSET),
            0,
            0));


     pklPathTF.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            updatePKLPath();
         }
            
      });
      
     pklPathTF.addFocusListener(new FocusListener() {

         public void focusGained(FocusEvent e) {         }

         public void focusLost(FocusEvent e) {
            updatePKLPath();
         }
      
      });
     pklCB.setSelected(false);
     setAreSpectraHandled(false);
     prp_id = -1;
     pkl_id = -1;
   }

   ////////////////////
   // DATAFORMAT methods
   ////////////////////	
	public IFileTransfertManager getFileTransfertManager() {
		return new DefaultFileTransfertManager(false );
	}

	public Analysis[] getAnalysis(File dir) {
    logger.info("reading directory "+dir.getAbsolutePath());
		File[] files = dir.listFiles(dataFilter);
		
		//if the listFiles() return null the directory doesn't exists (or there is an I/O error)
    if(files == null){
      String msg = RSCS.getString("acq.dir.notexist");
      Object[] args = {dir}; 
      logPaneLogger.error(MessageFormat.format(msg, args));
      return new Analysis[0];
    }
		
		return analysisFactory.getAnalyses(this, Arrays.asList(files));
	}

	public int getPropertyCount() {    
		int incr = (areSpectraHandled) ? 2 : 0;
		return PROPERTIES.length + incr;
	}

	@SuppressWarnings("rawtypes")
	public Class getPropertyClass(int propertyIdx) {
		return String.class;
	}

   public String getPropertyLabel(int propertyIdx) {
      if (propertyIdx < PROPERTIES.length)
         return PROPERTIES[propertyIdx];
      if(propertyIdx == pkl_id)
        return PKL_PROP;
      else if(propertyIdx == prp_id)
        return PRP_PROP;
      return "";
   }

   public Object getProperty(int propertyIdx, Analysis analysis) {
  	 MLAnalysis mlAnalysis = (MLAnalysis) analysis;
  	 if (propertyIdx < PROPERTIES.length) {
  		 String propertyName = PROPERTIES[propertyIdx];
  		 if (propertyName.equals(DESCRIPTION_PROP))
  			 return mlAnalysis.getDescription();
  		 if(propertyName.equals(USER_PROP))
  			 return mlAnalysis.getOperator();
  	 } else if(propertyIdx == pkl_id){
  		 return (mlAnalysis.getPKL() == null) ? "" : mlAnalysis.getPKL().getName();
  	 } else if(propertyIdx == prp_id){
  		 return (mlAnalysis.getPRP() == null) ? "" : mlAnalysis.getPRP().getName();
  	 }
  	 return null;
   }

   public JComponent getConfigurator() {
      return this;
   }
 
  //////////////////
  // Implem methods
  /////////////////
   
   private void updatePKLPath() {
      String text = pklPathTF.getText();
      File dir = new File(text);
      if (dir.exists() && dir.canRead() && dir.isDirectory()) {
         setPKLPath(dir);
      } else {
         pklPathTF.setText(RSCS.getString("pkl.path.invalid.message"));
      }      
   }
   
   private void setAreSpectraHandled(boolean b) {
      if(b){
        pkl_id = getPropertyCount();
        prp_id = pkl_id+1;
      }else {
        pkl_id = -1;
        prp_id = -1;
      }      
      boolean oldValue = areSpectraHandled;
      areSpectraHandled = b;
      pklPathTF.setEnabled(areSpectraHandled);  
      pklDirLabel.setEnabled(areSpectraHandled);
      pklBrowse.setEnabled(areSpectraHandled);
      firePropertyChange(SPECTRA_BACKUP, oldValue, areSpectraHandled);
   }
   
   public boolean areSpectraHandled() {
      return areSpectraHandled;
   }

   public File getPKLPath() {
      return (areSpectraHandled) ? pklPath : null;
   }

   public void setPKLPath(File file) {
      File oldPath = pklPath;
      pklPath = file;
      firePropertyChange(PKL_PATH, oldPath, pklPath);
   }

}
