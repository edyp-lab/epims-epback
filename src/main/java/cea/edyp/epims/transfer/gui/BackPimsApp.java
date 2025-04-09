/*
 * Created on Nov 25, 2004
 *
 * $Id: BackPimsApp.java,v 1.2 2008-02-20 06:57:03 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import cea.edyp.epims.transfer.preferences.EPBackPreferences;
import cea.edyp.epims.transfer.preferences.PreferencesKeys;
import cea.edyp.epims.transfer.task.ConnectTask;
import org.apache.commons.configuration2.ex.ConfigurationException;


import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.Backup;
import cea.edyp.epims.transfer.model.BackupParameters;
import cea.edyp.epims.transfer.model.CacheManager;
import cea.edyp.epims.transfer.model.InstrumentConfiguration;
import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
public class BackPimsApp extends JFrame implements Backup {

	private static final long serialVersionUID = -4003323048675629115L;
	private static final Logger logger = LoggerFactory.getLogger(BackPimsApp.class);
	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	private JComboBox<String> instrumentsCB;
	private AnalysisPane analysisPane;
	private LogTextPanel logPane;
	private ConfigurationPane configurationPane;
	private AnalysisOperationAction operationAction;

	private Map<String, InstrumentConfiguration> instrumentsConfig;
	private String currentInstrumentCfgName;

	private boolean backupRunning;
	private BackupParameters parameters;

	public BackPimsApp() {

		// initialize instruments

		try {
			instrumentsConfig = InstrumentConfiguration.readInstrumentsXMLConfiguration();
			parameters = new BackupParameters();
		} catch (ConfigurationException ce) {
			JOptionPane.showMessageDialog(this, RSCS.getString("getConf.instrument.error.msg"), RSCS.getString("getConf.instrument.error.title"),
					JOptionPane.ERROR_MESSAGE);
			logger.debug(RSCS.getString("getConf.instrument.error.msg") + " : " + InstrumentConfiguration.INSTRUMENT_CONFIGURATION_FILEPATH);
		} catch (InstantiationException ie) {
			JOptionPane.showMessageDialog(this, ie.getMessage(), RSCS.getString("error.panel.title"), JOptionPane.ERROR_MESSAGE);
		}

		String instrumentLabel = chooseInstrumentConfig();
		parameters.setInstrumentConfiguration(instrumentsConfig.get(currentInstrumentCfgName));

		if (instrumentLabel == null) {
			logger.error(RSCS.getString("getConf.instrument.error.msg"));
			JOptionPane.showMessageDialog(this, RSCS.getString("getConf.instrument.error.msg"), RSCS.getString("getConf.instrument.error.title"),
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		CacheManager.getInstance().setupForConfiguration(currentInstrumentCfgName);

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setSize((Integer) RSCS.getObject("app.start.width"), (Integer) RSCS.getObject("app.start.height"));
		this.setTitle(RSCS.getString("app.title"));
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					CacheManager.getInstance().save();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Impossible to save config cache ! ", RSCS.getString("error.panel.title"),
							JOptionPane.ERROR_MESSAGE);
				}
				exit();
			}
		});

		buildGUI();
		backupRunning = false;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				analysisPane.loadAnalysesFromSrcPath(false);
			}
		});
	}

	private String chooseInstrumentConfig() {
		String instrumentLabel = null;
		List<String> labelList = new ArrayList<String>(instrumentsConfig.keySet());
		JComboBox<String> jcb = new JComboBox<String>(labelList.toArray(new String[labelList.size()]));
		JOptionPane.showMessageDialog(this, jcb, RSCS.getString("getConf.instrument.label"), JOptionPane.QUESTION_MESSAGE);
		instrumentLabel = (String)jcb.getSelectedItem();
		this.currentInstrumentCfgName = instrumentLabel;
		return instrumentLabel;
	}

	private void buildGUI() {

		JTabbedPane tabs = new JTabbedPane();
		configurationPane = new ConfigurationPane(parameters);
		logPane = new LogTextPanel(parameters);
		analysisPane = new AnalysisPane(parameters);

		tabs.add(RSCS.getString("app.analysis"), analysisPane);
		tabs.add(RSCS.getString("app.log"), logPane);

		getContentPane().setLayout(new GridBagLayout());
		int row = 0;
		int col = 0;
		int INSET = 5;
		getContentPane().add(
				new JLabel(RSCS.getString("getConf.instrument.label")),
				new GridBagConstraints(col, row, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(INSET, INSET * 2, INSET,
								INSET), 0, 0));

		instrumentsCB = new JComboBox<String>(instrumentsConfig.keySet().toArray(new String[instrumentsConfig.size()]));
		instrumentsCB.setSelectedItem(currentInstrumentCfgName);
		instrumentsCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newInstrument = (String) instrumentsCB.getSelectedItem();
				setCurrentInstrumentConfiguration(newInstrument);
			}
		});

		getContentPane().add(
				instrumentsCB,
				new GridBagConstraints(++col, row, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(INSET, INSET, INSET,
								INSET), 0, 0));

		col = 0;
		getContentPane().add(
				configurationPane,
				new GridBagConstraints(col, ++row, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(INSET, INSET,
								INSET, INSET), 0, 0));

		getContentPane().add(
				tabs,
				new GridBagConstraints(col, ++row, 2, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(INSET, INSET, INSET,
								INSET), 0, 0));

		operationAction = new AnalysisOperationAction(this);

		JButton startBT = new JButton(operationAction);

		getContentPane().add(
				startBT,
				new GridBagConstraints(col, ++row, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(INSET, INSET, INSET,
								INSET), 0, 0));

		getContentPane().add(
				operationAction.getProgressBar(),
				new GridBagConstraints(col, ++row, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(INSET, INSET,
								INSET, INSET), 0, 0));


	}

	protected void setCurrentInstrumentConfiguration(String instrumentName) {
		if (!Objects.equals(instrumentName, currentInstrumentCfgName)) {
			parameters.setInstrumentConfiguration(instrumentsConfig.get(instrumentName));
			currentInstrumentCfgName = instrumentName;
			CacheManager.getInstance().setupForConfiguration(currentInstrumentCfgName);
			instrumentsCB.setSelectedItem(currentInstrumentCfgName);
			analysisPane.loadAnalysesFromSrcPath(false);
		}
	}

	public static void main(String[] args) {
//		URL log4jConfigFile = BackPimsApp.class.getResource("/cea/edyp/epims/transfer/log-config.xml");
//		DOMConfigurator.configure(log4jConfigFile);
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		logger.info("START BackPims App");

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler());
				}
			});
		} catch (Exception var5) {
		}

//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //VDS To change ... ? #25406
//		} catch (Exception ignored) {
//
//		}

		String user = EPBackPreferences.root().get(PreferencesKeys.SERVER_USER_KEY,"");
		String pwd = EPBackPreferences.root().get(PreferencesKeys.SERVER_PSWD_KEY,"");

		ConnectTask task = new ConnectTask(user, pwd);
		if (!task.fetchData()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, "No Connection", "Connection to server failed.", JOptionPane.ERROR_MESSAGE);
					}
				});
			} catch (Exception e) {

			}
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				BackPimsApp app = new BackPimsApp();
				app.setVisible(true);
			}
		});
	}

	public Analysis[] getAnalysisToBackup() {
		return analysisPane.getSelectedAnalysis();
	}

	public void transferBegin() {
		backupRunning = true;
		parameters.setIsRunning(backupRunning);
		instrumentsCB.setEnabled(false);
		
	}

	public void transferEnd(List<Analysis> processedFiles) {
		endTransfer(processedFiles);
	}

	public void transferAbort(List<Analysis> processedFiles) {
		endTransfer(processedFiles);
	}
	private void endTransfer(List<Analysis> processedFiles){
		backupRunning = false;
		parameters.setIsRunning(backupRunning);
		if ((parameters.getTransferMode() == BackupParameters.TRANSFER_CLEAN_MODE)  && processedFiles != null) {
			logger.debug("Need to remove transferred files from the list");
			analysisPane.removeFromTableModelContent(processedFiles);
		}
		analysisPane.updateTableModelContent();
		instrumentsCB.setEnabled(true);
	}

	public void exit() {
		int result = Integer.MIN_VALUE;
		if (backupRunning)
			result = JOptionPane.showConfirmDialog(this, RSCS.getString("exit.backup.running.dialog.text"), RSCS.getString("app.title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		else
			result = JOptionPane.showConfirmDialog(this, RSCS.getString("exit.dialog.text"), RSCS.getString("app.title"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			System.exit(0);
		}
	}

	public BackupParameters getBackupParameters() {
		return parameters;
	}

//	static {
//		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd_HH-mm_ss-SSS");
//		String dateString = sdf.format(new Date());
//		System.setProperty("log4jFileTime", dateString);
//	}

	public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
		public ExceptionHandler() {
		}

		public void uncaughtException(Thread thread, Throwable thrown) {
			BackPimsApp.logger.error("Uncaught exception in Thread" + thread.getName(), thrown);
		}
	}

}
