package cea.edyp.epims.transfer.dataformat.bruker;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.DefaultFileTransfertManager;
import org.slf4j.LoggerFactory;

/**
 * @author Valentin Bouquet 1/09/09
 */

public class UltraFlexFormat extends JPanel implements DataFormat {        


	private static final long serialVersionUID = -2808405369156511537L;

	@SuppressWarnings("unused")
	//init log
	private static Logger logger = LoggerFactory.getLogger(UltraFlexFormat.class);

	//multi lanagage
	private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	//specific property titles        
	public static final String DESCRIPTION_PROP = "Description";
	public static final String USER_PROP = "Resp. instrument";
	public static final String AUTOX_METHOD_PROP = "AutoX_Method";
	public static final String NB_REPLICAT_PROP = "Nb replicats";        
	//titles for analysis type: MS or MSMS
	public static final String ACQ_TYPE = "Acq Type"; 
	public static final String NB_SHOOTS = "Nb Shots"; 
	public static final String NB_ACQ_PER_REP = "Nb Acq par Replica"; 
	public static final String ION_PARENT_MASS = "Masses des ions parents";
	
	//used in method setInfosDescriptionHandled
	public static final String SPECTRA_BACKUP = "Traitement des spectres";

	//list of properties that eP-Back will display
	//the description is not present
	private static final String[] FORMAT_PROPERTIES = { USER_PROP, ACQ_TYPE, NB_SHOOTS, NB_ACQ_PER_REP, AUTOX_METHOD_PROP, NB_REPLICAT_PROP, ION_PARENT_MASS };        

	//use in contructor
	private FileFilter dataFilter;               
	
	//boolean use with checkbox to catch action
	private boolean areDescriptionHandled;

	//nb of columns
	private static int INSET = 5;        

	//get the factory
	private UltraFlexFactory analysisFactory;
	
	public File fileDepot;
	
	
	//constructor
	public UltraFlexFormat() {                          
		
		dataFilter = new BrukerFileFilter();
		analysisFactory = new UltraFlexFactory();         
		
		//Add a checkbox  to hide or not the analysis description
		JCheckBox infoDescription = new JCheckBox(RSCS.getString("bruker.info.spectra.handle.checkbox.label.description"));
		this.add(
				infoDescription,
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
		
            //listener for the checkBox
            infoDescription.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                        setInfosDescriptionHandled( ((JCheckBox)e.getSource()).isSelected());
                }
            });           
	}
        

        ////////////////////
        // DATAFORMAT methods
        ////////////////////
	
	public IFileTransfertManager getFileTransfertManager() {
		return new DefaultFileTransfertManager(false );
	}
        
    /**
    * Because the class implements DataFormat interface, we have to 
    * implement this function<br>
    * The File dir represents the location of all the analysis
    * <br>
    * we use the factory method to retrieve the list of analysis.
    */
	public Analysis[] getAnalysis(File dir) {       
		fileDepot = dir;
		ArrayList<File> files = new ArrayList<File>();
		//we got just one src dir
		files.add(dir);
		return analysisFactory.getAnalyses(this, files);        	      	        	       
	}

        

	public int getPropertyCount() {
		return areDescriptionHandled ? FORMAT_PROPERTIES.length+1 : FORMAT_PROPERTIES.length;
	}

        

	@SuppressWarnings("rawtypes")
	public Class getPropertyClass(int propertyIdx) {
		return String.class;
	}
          
	public String getPropertyLabel(int propertyIdx) {
		if (propertyIdx < FORMAT_PROPERTIES.length)
			return FORMAT_PROPERTIES[propertyIdx];
		if(areDescriptionHandled && propertyIdx == FORMAT_PROPERTIES.length)
			return DESCRIPTION_PROP;            
		return "";
	}

        

	public Object getProperty(int propertyIdx, Analysis analysis) {
		//we have to cast the analysis to get the specific properties
		UltraFlexAnalysis uLAnalysis = (UltraFlexAnalysis) analysis;

		if (propertyIdx < FORMAT_PROPERTIES.length) {
			String propertyName = FORMAT_PROPERTIES[propertyIdx];
			if (propertyName.equals(DESCRIPTION_PROP))
				return uLAnalysis.getDescription();
			if (propertyName.equals(USER_PROP))
				return uLAnalysis.getOperator();
			if (propertyName.equals(ACQ_TYPE))
				return uLAnalysis.getAcqType();
			if (propertyName.equals(NB_SHOOTS))
				return uLAnalysis.getNbShoots();
			if (propertyName.equals(NB_ACQ_PER_REP))
				return uLAnalysis.getNbAcqPerReplicate();
			if (propertyName.equals(AUTOX_METHOD_PROP))
				return uLAnalysis.getAutoXMethod();
			if (propertyName.equals(NB_REPLICAT_PROP))
				return uLAnalysis.getNbReplicat();
			if (propertyName.equals(ION_PARENT_MASS))
				return uLAnalysis.getIonParentMasse();
		}                                    
		if( areDescriptionHandled && propertyIdx == FORMAT_PROPERTIES.length){
			return (uLAnalysis.getDescription());                
		} 
		return null;
	}

	public JComponent getConfigurator() {
		return this;
	}

        

        //////////////////
        // Implem methods
        /////////////////

        
        private void setInfosDescriptionHandled(boolean b) {  
        	boolean oldValue = areDescriptionHandled;
        	areDescriptionHandled = b;
        	firePropertyChange(SPECTRA_BACKUP, oldValue, areDescriptionHandled);
        }

}
