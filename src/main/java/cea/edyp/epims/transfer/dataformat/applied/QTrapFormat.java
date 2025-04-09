package cea.edyp.epims.transfer.dataformat.applied;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.DefaultFileTransfertManager;
import cea.edyp.epims.transfer.util.ExtensionFileFilterName;

public class QTrapFormat extends JPanel implements DataFormat {

	private static final long serialVersionUID = 1112220274553999409L;
	private static Log logger = LogFactory.getLog(QTrapFormat.class);
	private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	private static final String ANALYSIS_DIR = "DATA";
	// Protected attributs that could be redefined / used in sub classes
	protected FilenameFilter dataFilter;
	protected static final String ANALYSIS_FILE_EXT = "wiff";
	protected static final String DESCRIPTION_PROP = "Description";
	protected static final String USER_PROP = "Resp. instrument";
	protected static final String FILE_PROPERTY = "Fichier";
	public static final String MGF_PROP = "mgf";
	protected QTrapFactory analysisFactory;

	private static final String SPECTRA_BACKUP = "Traitement des spectres";

	protected static final String[] FORMAT_PROPERTIES = { DESCRIPTION_PROP, USER_PROP, FILE_PROPERTY };

	private boolean areSpectraHandled;
	private static int INSET = 5;

	public QTrapFormat() {
		String[] ext = { ANALYSIS_FILE_EXT };
		dataFilter = new ExtensionFileFilterName(ext);
		analysisFactory = new QTrapFactory();

		JCheckBox mgfCB = new JCheckBox(RSCS.getString("mgf.spectra.handle.checkbox.label"));
		this.add(mgfCB, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2 * INSET,
				2 * INSET, INSET, 2 * INSET), 0, 0));

		mgfCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSpectraHandled(((JCheckBox) e.getSource()).isSelected());
			}
		});
	}

	// //////////////////
	// DATAFORMAT methods
	// //////////////////
	public IFileTransfertManager getFileTransfertManager() {
		return new DefaultFileTransfertManager(false);
	}

	public Analysis[] getAnalysis(File dir) {

		logger.info("QTrapFormat : reading directory " + dir.getAbsolutePath());
		List<File> files = new ArrayList<File>();

		// -- Two directories hierarchy is allowed
		// 1. directory is the entry of a unique project
		// 2. directory contains many projects, each having its own instrument
		// hierarchy

		// 1. directory is the entry of a unique project
		boolean isUniqueProject = false;
		File[] subDirs = dir.listFiles();
		for (int nbrSubDirs = 0; nbrSubDirs < subDirs.length; nbrSubDirs++) {
			File nextSubDir = subDirs[nbrSubDirs];
			if (!nextSubDir.isDirectory())
				continue;

			if (nextSubDir.getName().equalsIgnoreCase(ANALYSIS_DIR)) {
				isUniqueProject = true;
				File[] analysisFile = nextSubDir.listFiles(dataFilter);
				files.addAll(Arrays.asList(analysisFile));
			}
		}

		// 2. directory contains many projects, each having its own instrument
		// hierarchy
		if (!isUniqueProject) {
			for (int nbrProjects = 0; nbrProjects < subDirs.length; nbrProjects++) {
				File projectDir = subDirs[nbrProjects];
				if (!projectDir.isDirectory())
					continue;

				// Search in next Project's directories for ANALYSIS_DIR directory
				File[] subProjectsDirs = projectDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
				for (int nbrSubDirs = 0; nbrSubDirs < subProjectsDirs.length; nbrSubDirs++) {
					File nextProjectDir = subProjectsDirs[nbrSubDirs];
					if (nextProjectDir.isDirectory() && nextProjectDir.getName().equalsIgnoreCase(ANALYSIS_DIR)) {
						File[] analysisFile = nextProjectDir.listFiles(dataFilter);
						files.addAll(Arrays.asList(analysisFile));
					}
				} // End search for ANALYSIS_DIR in one project dir
			}
		} // End case 2.

		logger.info("Found "+files.size()+" files in "+dir.getAbsolutePath());
		return analysisFactory.getAnalyses(this, files);
	}

	public JComponent getConfigurator() {
		return this;
	}

	public Object getProperty(int propertyIdx, Analysis analysis) {
		QTrapAnalysis qTrapAnalysis = (QTrapAnalysis) analysis;
		if (propertyIdx < FORMAT_PROPERTIES.length) {
			String propertyName = FORMAT_PROPERTIES[propertyIdx];
			if (propertyName.equals(DESCRIPTION_PROP))
				return qTrapAnalysis.getDescription();
			if (propertyName.equals(USER_PROP))
				return qTrapAnalysis.getOperator();
			if (propertyName.equals(FILE_PROPERTY))
				return qTrapAnalysis.getFileName();
		}

		if (areSpectraHandled && propertyIdx == FORMAT_PROPERTIES.length)
			return (qTrapAnalysis.getAssociatedFiles().length == 0) ? "" : qTrapAnalysis.getAssociatedFiles()[0].getName();

		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	public Class getPropertyClass(int propertyIdx) {
		return String.class;
	}

	public int getPropertyCount() {
		return areSpectraHandled ? FORMAT_PROPERTIES.length + 1 : FORMAT_PROPERTIES.length;
	}

	public String getPropertyLabel(int propertyIdx) {
		if (propertyIdx < FORMAT_PROPERTIES.length)
			return FORMAT_PROPERTIES[propertyIdx];
		if (areSpectraHandled && propertyIdx == FORMAT_PROPERTIES.length)
			return MGF_PROP;
		return "";
	}

	// ////////////////
	// Implem methods
	// ///////////////

	protected void setSpectraHandled(boolean b) {
		boolean oldValue = areSpectraHandled;
		areSpectraHandled = b;
		firePropertyChange(SPECTRA_BACKUP, oldValue, areSpectraHandled);
	}

	public boolean areSpectraHandled() {
		return areSpectraHandled;
	}

}
