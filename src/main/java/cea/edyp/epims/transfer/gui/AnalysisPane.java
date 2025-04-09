/*
 * Created on Nov 26, 2004
 *
 * $Id: AnalysisPane.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.BackupParameters;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
@SuppressWarnings("serial")
public class AnalysisPane extends JPanel implements TableModelListener, PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisPane.class);
	private static final Logger fileLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
	private static final DecimalFormat df = new DecimalFormat("#,##0.###");
	
	private BackupParameters parameters;
	private final JTable table;
	private AnalysisTableModel model;
	private final JLabel nbrSelectionLB;
	private final JLabel sizeLB;
	private final JLabel refreshLB;
	private final JLabel nbAnalysesLB;
	private final JToolBar toolbar;
	private TableSorter sorter;

	private SwingWorker<Void, Integer> analysesLoader;
	
	public AnalysisPane(BackupParameters params) {
		setLayout(new GridBagLayout());
		parameters = params;
		parameters.addPropertyChangeListener(this);

		table = new JTable();
		table.setDefaultRenderer(Analysis.class, new AnalysisCellRenderer(params));
		table.setIntercellSpacing(new Dimension(5, 2));
		table.setCellSelectionEnabled(true);
		
		// add a horizontal scroll bar by disable the auto resize
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		new TableCellCopyPaste(table);
		KeyStroke select = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
		table.getInputMap().put(select, "SelectAnalysis");
		table.getActionMap().put("SelectAnalysis", new SelectAnalysisAction());

		int line = 0;
		toolbar = buildToolBar();
		int INSET = 5;
		this.add(toolbar, new GridBagConstraints(0, line++, 2, 1, 1.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(INSET, INSET, INSET, INSET), 0, 0));

		this.add(new JScrollPane(table), new GridBagConstraints(0, line++, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(INSET, INSET, INSET, INSET), 0, 0));

		refreshLB = new JLabel("refresh ...");
		this.add(refreshLB, new GridBagConstraints(0, line, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(INSET, INSET,
						INSET, INSET), 0, 0));

		sizeLB = new JLabel();
		this.add(sizeLB, new GridBagConstraints(0, line++, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(INSET, INSET,
						INSET, INSET), 0, 0));

		nbAnalysesLB = new JLabel();
		this.add(nbAnalysesLB, new GridBagConstraints(0, line, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(INSET, INSET,
						INSET, INSET), 0, 0));

		nbrSelectionLB = new JLabel();
		this.add(nbrSelectionLB, new GridBagConstraints(0, line++, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(INSET,
						INSET, INSET, INSET), 0, 0));

	}

	public void loadAnalysesFromSrcPath(final boolean update) {
		JProgressBar pg = new JProgressBar();
		pg.setIndeterminate(true);
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		JLabel dialogLabel = new JLabel("reading analysis files for instrument " + parameters.getConfigurationName());
		pane.add(dialogLabel);
		pane.add(pg);
		final JDialog dialog = new JDialog((Window)this.getTopLevelAncestor(), Dialog.ModalityType.APPLICATION_MODAL) {
			protected void processWindowEvent(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING) {
					super.processWindowEvent(e);
				} else {
					logger.info("Interrupt analysesLoader thread");
					fileLogger.info("Interruption du chargement des analyses.");
					analysesLoader.cancel(true);
				}
			}
		};
		dialog.getContentPane().add(pane);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		
		
		logger.debug(" - Start Load Analysis (SwingWorker)");
		analysesLoader = new SwingWorker<>() {

      @Override
      protected Void doInBackground() {
        if (update) {
          parameters.updateAnalysesFromSourcePath();
        } else {
          parameters.loadAnalysesFromSourcePath();
        }
        return null;
      }

      @Override
      protected void done() {
        dialog.setVisible(false);
        revalidate();
        repaint();
      }
    };

		analysesLoader.execute();
		dialog.setVisible(true);
		logger.info(" - End load Instrument Analyses");
		
	}

	public void setAnalyses(Analysis[] analyses) {
		logger.trace("display a new list of analyse in Analysis Table");
		if (model != null) {
			model.removeTableModelListener(this);
			model.stopRefresher();
		} 
		model = new AnalysisTableModel(analyses, parameters, refreshLB);
		model.addTableModelListener(this);
		sorter = new TableSorter(model);
		table.setModel(sorter);
		sorter.setTableHeader(table.getTableHeader());
		table.getColumnModel().getColumn(0).setMaxWidth(20);
		table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
		updateEstimatedSize();
		updateNbrSelectedAnalysis();
		updateTotalNbrAnalyses(analyses.length);
	}

	protected void updateTableModelContent() {
		if (model == null)
			return;
		model.updateModelContent();
		table.getColumnModel().getColumn(0).setMaxWidth(20);
		table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
		updateEstimatedSize();
		updateNbrSelectedAnalysis();
	}

	protected void removeFromTableModelContent(List<Analysis> analyses) {
		if (model == null)
			return;
		model.removeAnalyses(analyses.toArray(new Analysis[analyses.size()]));
	}



	public Analysis[] getSelectedAnalysis() {
		if (model != null)
			return model.getSelectedAnalysis();
		return new Analysis[0];
	}

	private void updateTotalNbrAnalyses(int count) {
		String msg = RSCS.getString("analysis.total.nbr");
		Object[] args = { count };
		msg = MessageFormat.format(msg, args);
		nbAnalysesLB.setText(msg);
	}
	
	private void updateEstimatedSize() {
		Analysis[] f = getSelectedAnalysis();
		float size = 0.0f;
		for (int i = 0; i < f.length; i++) {
			size += f[i].getEstimatedSize() / 1024.0f;
		}
		size = (size / 1024.0f) / 1024.0f; 
		sizeLB.setText(df.format(size) + " Go");
	}

	private void updateNbrSelectedAnalysis() {
		Analysis[] f = getSelectedAnalysis();
		String msg = RSCS.getString("analysis.selected.nbr");
		Object[] args = { f.length };
		msg = MessageFormat.format(msg, args);
		nbrSelectionLB.setText(msg);
	}


	/* 
	 * Called when Table model changes : re-estimate nb of selected analyses and totam size
	 * 
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		updateEstimatedSize();
		updateNbrSelectedAnalysis();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == parameters) {
			String propertyName = evt.getPropertyName();
			
			if (BackupParameters.BACKUP_RUNNING_PROPERTY.equals(propertyName)) {
				boolean isRunning = (Boolean)evt.getNewValue();
				logger.info("Transfert Running status changed to ..."+isRunning);				
				for(Component nextCmp :  toolbar.getComponents() ){
					if(JToggleButton.class.isInstance(nextCmp)|| JButton.class.isInstance(nextCmp) )
						nextCmp.setEnabled(!isRunning);
				}
				model.setTableEditable(!isRunning);							
			}
			
			if (BackupParameters.INSTRUMENT_CONFIGURATION_PROPERTY.equals(propertyName)) {
			logger.info("Instrument configuration changed ...");
				if (model != null) {
					model.stopRefresher();
					model = null;
					table.setModel(new DefaultTableModel());
				}
			}

			if (BackupParameters.TRANSFERT_MODE_PROPERTY.equals(propertyName) || BackupParameters.DATA_FORMAT_CONFIGURATION_PARAMETER.equals(propertyName)) {
				logger.info("Transfer mode changed ...");
				updateTableModelContent();
			}
			
			if (BackupParameters.ANALYSES_PROPERTY.equals(propertyName) && evt.getNewValue() != null) {
				logger.info("List of analyses changed ...");
				setAnalyses(parameters.getAnalyses());
			}
			
			if (BackupParameters.ANALYSES_ADDED_PROPERTY.equals(propertyName)) {
				logger.info("New analyses added to the initial list ...");
				Object[] values = (Object[])evt.getNewValue();
				Analysis[] addedAnalysis = new Analysis[values.length];
				for (int k = 0; k < values.length; k++) {
					addedAnalysis[k] = (Analysis)values[k];
				}
				model.addAnalyses(addedAnalysis);
			}

			if (BackupParameters.ANALYSES_REMOVED_PROPERTY.equals(propertyName)) {
				logger.info("Analyses removed from the initial list ...");
				Object[] values = (Object[])evt.getNewValue();
				Analysis[] removedAnalyses = new Analysis[values.length];
				for (int k = 0; k < values.length; k++) {
					removedAnalyses[k] = (Analysis)values[k];
				}
				model.removeAnalyses(removedAnalyses);
			}

		}
	}

	private JToolBar buildToolBar() {
		// JPanel toolbarPanel = new JPanel();

		// toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
		JToggleButton viewAnalysisBT = new JToggleButton(new ShowUnsavedAnalysisAction());
		viewAnalysisBT.setIcon((ImageIcon) RSCS.getObject("files.viewanalysis.icon"));
		viewAnalysisBT.setSelectedIcon((ImageIcon) RSCS.getObject("files.hideanalysis.icon"));
		viewAnalysisBT.setToolTipText(RSCS.getString("files.viewanalysis.tooltip"));
		viewAnalysisBT.setPreferredSize(new Dimension(28, 28));

		JToggleButton viewSavedAnalysisBT = new JToggleButton(new ShowSavedAnalysisAction());
		viewSavedAnalysisBT.setIcon((ImageIcon) RSCS.getObject("files.viewsavedanalysis.icon"));
		viewSavedAnalysisBT.setSelectedIcon((ImageIcon) RSCS.getObject("files.hidesavedanalysis.icon"));
		viewSavedAnalysisBT.setToolTipText(RSCS.getString("files.viewsavedanalysis.tooltip"));
		viewSavedAnalysisBT.setPreferredSize(new Dimension(28, 28));

		JButton selectAllBT = new JButton(new SelectAllAction());
		selectAllBT.setIcon((ImageIcon) RSCS.getObject("files.selectall.icon"));
		selectAllBT.setPreferredSize(new Dimension(28, 28));
		selectAllBT.setToolTipText(RSCS.getString("files.selectall.tooltip"));

		JButton selectNoneBT = new JButton(new SelectNoneAction());
		selectNoneBT.setIcon((ImageIcon) RSCS.getObject("files.selectnone.icon"));
		selectNoneBT.setPreferredSize(new Dimension(28, 28));
		selectNoneBT.setToolTipText(RSCS.getString("files.selectnone.tooltip"));

		JButton invertSelectionBT = new JButton(new InvertSelectionAction());
		invertSelectionBT.setIcon((ImageIcon) RSCS.getObject("files.invertselection.icon"));
		invertSelectionBT.setPreferredSize(new Dimension(28, 28));
		invertSelectionBT.setToolTipText(RSCS.getString("files.invertselection.tooltip"));

		JButton refreshBT = new JButton(new RefreshAnalysisFromSrcAction());
		refreshBT.setIcon((ImageIcon) RSCS.getObject("files.refresh.icon"));
		refreshBT.setPreferredSize(new Dimension(28, 28));
		refreshBT.setToolTipText(RSCS.getString("files.refresh.tooltip"));

		JButton fillRefreshBT = new JButton(new RefreshEPimsDataAction());
		fillRefreshBT.setIcon((ImageIcon) RSCS.getObject("files.epims.refresh.icon"));
		fillRefreshBT.setPreferredSize(new Dimension(28, 28));
		fillRefreshBT.setToolTipText(RSCS.getString("files.epims.refresh.tooltip"));

		JToolBar toolbar = new JToolBar();
		Dimension d = new Dimension(toolbar.getPreferredSize().width, 30);
		toolbar.setPreferredSize(d);
		toolbar.setBorderPainted(false);
		toolbar.add(viewAnalysisBT);
		toolbar.add(viewSavedAnalysisBT);

		toolbar.addSeparator();
		toolbar.add(selectAllBT);
		toolbar.add(selectNoneBT);
		toolbar.add(invertSelectionBT);
		toolbar.addSeparator();
		toolbar.add(refreshBT);
		toolbar.add(fillRefreshBT);
		toolbar.addSeparator();

		return toolbar;
	}

	class SelectAnalysisAction extends AbstractAction {

		public SelectAnalysisAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectall.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			for (int r : table.getSelectedRows()) {
				int idx = sorter.modelIndex(r);
				model.setValueAt(!((Boolean) model.getValueAt(idx, AnalysisTableModel.SELECT_COLUMN)), idx, AnalysisTableModel.SELECT_COLUMN);
			}
		}
	}

	class RefreshAnalysisFromSrcAction extends AbstractAction {

		public RefreshAnalysisFromSrcAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectall.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			loadAnalysesFromSrcPath(true);
		}
	}

	class RefreshEPimsDataAction extends AbstractAction {

		public RefreshEPimsDataAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectall.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			int[] selectedRows = table.getSelectedRows();
			if (selectedRows.length == 0) {
				// TODO : dialog : are you sure you want to update ePims data for all Analysis (this can take a while (3s*nb acq).. 
				model.refreshEPimsDataForAllRows();
			} else {
				for (int r : table.getSelectedRows()) {
					int idx = sorter.modelIndex(r);
					model.refreshEPimsDataForRow(idx);
				}
			}
			table.getColumnModel().getColumn(0).setMaxWidth(20);
			table.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
			updateEstimatedSize();
			updateNbrSelectedAnalysis();
		}
	}

	class ShowUnsavedAnalysisAction extends AbstractAction {

		public ShowUnsavedAnalysisAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectall.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			JToggleButton srcBT = (JToggleButton) arg0.getSource();
			model.showUnsavedAnalysis(!srcBT.isSelected());
		}
	}

	class ShowSavedAnalysisAction extends AbstractAction {

		public ShowSavedAnalysisAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectall.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			JToggleButton srcBT = (JToggleButton) arg0.getSource();
			model.showSavedAnalysis(!srcBT.isSelected());
		}
	}

	class SelectAllAction extends AbstractAction {

		public SelectAllAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectall.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			for (int row = 0; row < model.getRowCount(); row++) {
				model.setValueAt(Boolean.TRUE, row, AnalysisTableModel.SELECT_COLUMN);
			}
		}
	}

	class SelectNoneAction extends AbstractAction {

		public SelectNoneAction() {
			// putValue(Action.NAME, RSCS.getString("files.selectnone.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			for (int row = 0; row < model.getRowCount(); row++) {
				model.setValueAt(Boolean.FALSE, row, AnalysisTableModel.SELECT_COLUMN);
			}
		}
	}

	class InvertSelectionAction extends AbstractAction {

		public InvertSelectionAction() {
			// putValue(Action.NAME,
			// RSCS.getString("files.invertselection.label"));
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model == null)
				return;
			for (int row = 0; row < model.getRowCount(); row++) {
				if (((Boolean) model.getValueAt(row, 0)).booleanValue())
					model.setValueAt(Boolean.FALSE, row, 0);
				else
					model.setValueAt(Boolean.TRUE, row, 0);
			}
		}
	}

}

class AnalysisCellRenderer extends DefaultTableCellRenderer {

	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	private final BackupParameters parameter;

	public AnalysisCellRenderer(BackupParameters param) {
		parameter = param;
	}

	/**
    *
    */

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		Analysis analysis = (Analysis) value;
		String filename = analysis.getName();

		JLabel label = (JLabel) super.getTableCellRendererComponent(table, filename, isSelected, hasFocus, row, column);

		int status = analysis.getStatus();

		if (status == Analysis.ANALYSIS_STATUS_UNKNOWN) {
			label.setForeground((Color) RSCS.getObject("analysis.unselectable.foreground"));
			return label;
		}
		
		boolean noClean = (parameter.getTransferMode() == BackupParameters.TRANSFER_CLEAN_MODE)
				&& (Analysis.isAnalysisUnsaved(status) && !Analysis.AnalysisType.TEST.equals(analysis.getType()));
		boolean noTransfer = (parameter.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE)
				&& (Analysis.isAnalysisSaved(status) && !Analysis.AnalysisType.TEST.equals(analysis.getType()));

		if (noClean || noTransfer) {
			label.setForeground((Color) RSCS.getObject("analysis.unselectable.foreground"));
		} else {
			Font f = label.getFont();
			f = f.deriveFont(Font.BOLD);
			label.setForeground((Color) RSCS.getObject("analysis.selectable.foreground"));
			label.setFont(f);
		}
		return label;
	}

}

class StatusCellRenderer extends DefaultTableCellRenderer {

	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(StatusCellRenderer.class);

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color foregroundC = (Color) RSCS.getObject("analysis.status.NOK.foreground");
		
		String text = RSCS.getString("status.study.unknown");
		int status = (Integer) value;
		if(status == Analysis.ANALYSIS_STATUS_NOT_READABLE)
			text =  RSCS.getString("status.study.unreachable");
		if (status == Analysis.ANALYSIS_STATUS_UNKNOWN ||  status == Analysis.ANALYSIS_STATUS_NOT_READABLE) {
			label.setText(text);
			label.setForeground((Color) RSCS.getObject("analysis.unselectable.foreground"));
			return label;
		}

		if ((status & Analysis.ANALYSIS_EXIST_MASK) == Analysis.ANALYSIS_EXIST_MASK) {
			// Status is acquisition already exist
			text = RSCS.getString("status.study.acq.exist");
			foregroundC = (Color) RSCS.getObject("analysis.status.OK.foreground");

		} else if ((status & Analysis.ANALYSIS_STUDY_CLOSED_MASK) == Analysis.ANALYSIS_STUDY_CLOSED_MASK) {
			// Status is acquisition in closed study
			text = RSCS.getString("status.study.close");
			foregroundC = (Color) RSCS.getObject("analysis.status.NOK.foreground");

		} else if ((status & Analysis.ANALYSIS_INVALID_SAMPLE_MASK) == Analysis.ANALYSIS_INVALID_SAMPLE_MASK) {
			// Status is invalid sample
			text = RSCS.getString("status.study.invalid.sample");
			foregroundC = (Color) RSCS.getObject("analysis.status.NOK.foreground");

		} else if ((status & Analysis.ANALYSIS_OK_MASK) == Analysis.ANALYSIS_OK_MASK) {
			// Status is acquisition OK, not saved...
			text = RSCS.getString("status.study.ok");
			foregroundC = (Color) RSCS.getObject("analysis.status.OK.foreground");
		}

		label.setText(text);
		label.setForeground(foregroundC);
		return label;
	}

}