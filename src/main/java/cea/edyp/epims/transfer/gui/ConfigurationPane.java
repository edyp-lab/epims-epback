/*
 * Created on Nov 26, 2004
 *
 * $Id: ConfigurationPane.java,v 1.2 2008-02-20 06:57:21 dupierris Exp $
 * ~VD 23/02/05 : Ventile toujours les analyses + deux mode de transfer
 */
package cea.edyp.epims.transfer.gui;

import cea.edyp.epims.transfer.model.BackupParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 
 * 
 * @author CB205360
 */
public class ConfigurationPane extends JPanel implements PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationPane.class);
	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
	private static final int INSET = 5;

//	private XMLConfiguration appConfig;
	private final BackupParameters parameters;
	
	private JTextField sourcePathTF;
	private JTextField destPathTF;
	private JCheckBox removeFilesCB;
	private JComboBox<String> transferModeCbB;

	public ConfigurationPane(BackupParameters parameters) {
		this.parameters = parameters;
		parameters.addPropertyChangeListener(this);
		setLayout(new GridBagLayout());
		JComponent pane = buildPanel();
		this.add(pane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
				new Insets(INSET, 3 * INSET, INSET, 3 * INSET), 0, 0));
		updateValues();
	}

	private void updateValues() {
		sourcePathTF.setText(parameters.getSourcePath().getAbsolutePath());
		destPathTF.setText(parameters.getDestinationRootPath().getAbsolutePath());
	}

	private JComponent buildPanel() {

		logger.debug("Create Instrument specific.");

		// Panel de configuration 'globale'
		JPanel leftPanel = buildLeftPanel();
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Object[] args = { parameters.getConfigurationName(), parameters.getDataFormat().getClass().getName() };
		String title = MessageFormat.format(RSCS.getString("configuration.title"), args);
		TitledBorder tBorder = BorderFactory.createTitledBorder(loweredetched, title);
		leftPanel.setBorder(tBorder);

		// Panel de configuration propre à l'instrument (au type) choisi
		JPanel rightPanel = new DataFormatConfigurationPane(parameters);

		// Create a split pane with the two scroll panes in it.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

		splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerSize(INSET);
		splitPane.setDividerLocation(((Integer) RSCS.getObject("app.start.width") / 2) - 20);

		return splitPane;
	}

	
	public void propertyChange(PropertyChangeEvent evt) {
		if (Objects.equals(evt.getPropertyName(), BackupParameters.TRANSFERT_MODE_PROPERTY)) {
			removeFilesCB.setEnabled(parameters.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE);
		} else if (Objects.equals(evt.getPropertyName(), BackupParameters.INSTRUMENT_CONFIGURATION_PROPERTY)) {
			updateValues();
		} else if (Objects.equals(evt.getPropertyName(), BackupParameters.BACKUP_RUNNING_PROPERTY)) {
			transferModeCbB.setEnabled(!(Boolean)evt.getNewValue());
			removeFilesCB.setEnabled( (!(Boolean)evt.getNewValue()) && (parameters.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE));
		}
	}


	
	// Panel de configuration 'globale'
	private JPanel buildLeftPanel() {

		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());

		//
		// ligne 0
		//
		pane.add(new JLabel(RSCS.getString("source.path.label")), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.VERTICAL, new Insets(2 * INSET, 2 * INSET, 0, 2 * INSET), 0, 0));

		//
		// ligne 1
		//
		sourcePathTF = new JTextField();
		sourcePathTF.setEnabled(false);
		pane.add(sourcePathTF, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,
				2 * INSET, INSET, 2 * INSET), 0, 0));

		//
		// ligne 2
		//
		pane.add(new JLabel(RSCS.getString("dest.path.label")), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.VERTICAL, new Insets(INSET, 2 * INSET, 0, INSET), 0, 0));

		//
		// ligne 3
		//
		destPathTF = new JTextField();
		destPathTF.setEnabled(false);
		pane.add(destPathTF, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,
				2 * INSET, 2 * INSET, 2 * INSET), 0, 0));

		//
		// ligne 4
		//
		pane.add(new JLabel(RSCS.getString("operation.type.label")), new GridBagConstraints(0, 4, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.VERTICAL, new Insets(INSET, 2 * INSET, 0, INSET), 0, 0));

		//
		// ligne 5
		//
		// VD ATTENTION
		transferModeCbB = new JComboBox<String>(RSCS.getStringArray("operation.type.options.labels"));
		transferModeCbB.setSelectedIndex(parameters.getTransferMode());
		pane.add(transferModeCbB, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,
				2 * INSET, INSET, 2 * INSET), 0, 0));

		transferModeCbB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(transferModeCbB)) {
					parameters.setTransferMode(((JComboBox<String>) e.getSource()).getSelectedIndex());
				}
			}
		});

		//
		// ligne 6
		//
		removeFilesCB = new JCheckBox(RSCS.getString("remove.files.checkbox.text"));
		removeFilesCB.setSelected(false);
		removeFilesCB.setEnabled(parameters.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE);
		pane.add(removeFilesCB, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(0,
				2 * INSET, INSET, 2 * INSET), 0, 0));

		removeFilesCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(removeFilesCB)) {
					parameters.setRemoveFilesAfterCopy(((JCheckBox) e.getSource()).isSelected());
				}
			}

		});

		return pane;
	}


//	public void saveParameters() {
//		try {
//			appConfig.save();
//		} catch (ConfigurationException e) {
//			logger.debug("Impossible d'ecrire le fichier de configuration", e);
//		}
//	}

}
