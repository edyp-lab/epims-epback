/*
 * Created on Nov 26, 2004
 *
 * $Id: LTQFormat.java,v 1.1 2007-09-14 09:37:30 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.thermo;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.DefaultFileTransfertManager;
import cea.edyp.epims.transfer.util.ExtensionFileOnlyFilter;
import org.slf4j.LoggerFactory;

/**
 * For Thermo data, generated using "old" XCalibur (below 2.1)  (LTQ, LTQ-FT, LTQ Orbitrap)
 * 
 * @author CB205360
 */
public class LTQFormat extends JPanel implements DataFormat {

	private static final long serialVersionUID = 1L;

	protected static Logger logger = LoggerFactory.getLogger(LTQFormat.class);
	protected static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	protected static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	public static final String DESCRIPTION_PROP = "Description";
	public static final String USER_PROP = "Resp. instrument";
	public static final String MGF_PROP = "mgf";

	public static final String SPECTRA_BACKUP = "Traitement des spectres";

	private static final String[] FORMAT_PROPERTIES = { DESCRIPTION_PROP, USER_PROP };

	private FileFilter dataFilter;

	private boolean areSpectraHandled;
	private static int INSET = 5;
	protected LTQFactory analysisFactory;

	public LTQFormat() {
		String[] ext = { "raw" };
		dataFilter = new ExtensionFileOnlyFilter(ext);
		analysisFactory = new LTQFactory();

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
		logger.info("reading directory " + dir.getAbsolutePath());
		File[] files = dir.listFiles(dataFilter);

		// if the listFiles() return null the directory doesn't exists (or there
		// is an I/O error)
		if (files == null) {
			String msg = RSCS.getString("acq.dir.notexist");
			Object[] args = { dir };
			logPaneLogger.error(MessageFormat.format(msg, args));
			return new Analysis[0];
		}

		return this.analysisFactory.getAnalyses(this, Arrays.asList(files));
	}

	public int getPropertyCount() {
		return areSpectraHandled ? FORMAT_PROPERTIES.length + 1 : FORMAT_PROPERTIES.length;
	}

	@SuppressWarnings("rawtypes")
	public Class getPropertyClass(int propertyIdx) {
		return String.class;
	}

	public String getPropertyLabel(int propertyIdx) {
		if (propertyIdx < FORMAT_PROPERTIES.length)
			return FORMAT_PROPERTIES[propertyIdx];
		if (areSpectraHandled && propertyIdx == FORMAT_PROPERTIES.length)
			return MGF_PROP;
		return "";
	}

	public Object getProperty(int propertyIdx, Analysis analysis) {
		LTQAnalysis mlAnalysis = (LTQAnalysis) analysis;
		if (propertyIdx < FORMAT_PROPERTIES.length) {
			String propertyName = FORMAT_PROPERTIES[propertyIdx];
			if (propertyName.equals(DESCRIPTION_PROP))
				return mlAnalysis.getDescription();
			if (propertyName.equals(USER_PROP))
				return mlAnalysis.getOperator();
		}

		if (areSpectraHandled && propertyIdx == FORMAT_PROPERTIES.length)
			return (mlAnalysis.getAssociatedFiles().length == 0) ? "" : mlAnalysis.getAssociatedFiles()[0].getName();

		return null;
	}

	public JComponent getConfigurator() {
		return this;
	}

	// ////////////////
	// Implem methods
	// ///////////////

	private void setSpectraHandled(boolean b) {
		boolean oldValue = areSpectraHandled;
		areSpectraHandled = b;
		firePropertyChange(SPECTRA_BACKUP, oldValue, areSpectraHandled);
	}

	public boolean areSpectraHandled() {
		return areSpectraHandled;
	}

}
