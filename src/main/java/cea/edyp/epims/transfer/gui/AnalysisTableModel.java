/*
 * Created on Nov 30, 2004
 *
 * $Id: AnalysisTableModel.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.BackupException;
import cea.edyp.epims.transfer.model.BackupParameters;
import org.slf4j.LoggerFactory;

/**
 * TableModel used in AnalysisPane. The model is formed of an array of -
 * selected DecoratedAnalysis (status - analysis - destination path - date of
 * analysis (from analysis) ++ DataFormat properties)
 * 
 * It is possible to display only saved/unsaved analysis or all analysis.
 * 
 * WARNING: row specified in TableModel methods is the row depending on
 * displayed analysis... Not the same as model row if not all analysis displayed
 * !!!
 * 
 * @author CB205360
 */
public class AnalysisTableModel extends AbstractTableModel {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisTableModel.class);
	private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	// Nbr of column before dataformat specific properties columns
	private final static int COLUMN_COUNT = 9;

	// Column indexes for generic properties :
	// !!! WARNING: If order is change, Change case in switch !!!!!
	public final static int SELECT_COLUMN = 0;
	private final static int STATUS_COLUMN = 1;
	private final static int ANALYSIS_COLUMN = 2;
	private final static int STUDY_COLUMN = 3;
	private final static int SAMPLE_COLUMN = 4;
	private final static int DEST_COLUMN = 5;
	private final static int DATE_COLUMN = 6;
	private final static int SIZE_COLUMN = 7;
	private final static int DURATION_COLUMN = 8;

	private int displayedAnalysisMask;
	protected final static int DISPLAY_SAVED_ANALYSIS_MASK = 1; // 01
	protected final static int DISPLAY_UNSAVED_ANALYSIS_MASK = 2; // 10

	private DecoratedAnalysis[] analyses;
	private DecoratedAnalysis[] displayedAnalyses;
	
	private final BackupParameters parameters;
	private final EPimsDataRefresher refresher;
	private final BlockingDeque<DecoratedAnalysis> refresherAnalysisQueue;
	private JLabel refresherLB;
	private boolean isEditableEnable = true; //To disable selection or editing during operation
	
	class EPimsDataRefresher extends SwingWorker<Void, Integer> {

		BlockingDeque<DecoratedAnalysis> queue;

		public EPimsDataRefresher(BlockingDeque<DecoratedAnalysis> queue) {
			this.queue = queue;
		}

		@Override
		protected Void doInBackground() throws Exception {
			while (true) {
				try {
					Thread.sleep(10);
					final DecoratedAnalysis analysis = queue.take();
					if(analysis.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE){
						logger.debug("Refresher Cancelled. Analysis unreachable...");
						continue;
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							logger.debug("refreshing label");
							refresherLB.setText("refreshing "+analysis.getName());
						}
					});
					if (isCancelled()) {
						logger.debug("Refresher Cancelled");
						return null;
					}
				  updateAnalysisDestinationPath(analysis);
					parameters.getEPimsDataProvider().getAnalysisStatus(analysis, parameters);
					publish(analysis.getRow());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refresherLB.setText("");
						}
					});
				} catch (InterruptedException ie) {
					logger.info("Refresher interrupted");
				}
			}

		}

		@Override
		protected void process(List<Integer> chunks) {
			for (Integer row : chunks) {
				DecoratedAnalysis a = getAnalysisAtRow(row);
				if( updateIsSelectedStatus(a) ) {
					fireTableCellUpdated(row, SELECT_COLUMN);
				}
				fireTableCellUpdated(row, STATUS_COLUMN);
				fireTableCellUpdated(row, DEST_COLUMN);
			}
		}

	}

	public AnalysisTableModel(Analysis[] a, BackupParameters params, JLabel refresherLB) {
		this(a, params);
		this.refresherLB = refresherLB;
	}
	
	public AnalysisTableModel(Analysis[] a, BackupParameters params) {
		// Set Both saved and unsaved Analysis
		displayedAnalysisMask = DISPLAY_SAVED_ANALYSIS_MASK | DISPLAY_UNSAVED_ANALYSIS_MASK;
		parameters = params;
		refresherAnalysisQueue = new LinkedBlockingDeque<>();
		analyses = new DecoratedAnalysis[a.length];
		for (int i = 0; i < a.length; i++) {
			analyses[i] = new DecoratedAnalysis(a[i], i);
			analyses[i].setSelected(Boolean.FALSE);
			if ((analyses[i].getDestination() == null)
					|| (analyses[i].getStatus() & Analysis.ANALYSIS_EXIST_MASK) != Analysis.ANALYSIS_EXIST_MASK) {
			
				refresherAnalysisQueue.add(analyses[i]);
			}
		}
		buildDisplayedAnalysesArray();
		refresher = new EPimsDataRefresher(refresherAnalysisQueue);
		refresher.execute();

	}

	private void buildDisplayedAnalysesArray() {
		
		switch (displayedAnalysisMask) {
		case DISPLAY_SAVED_ANALYSIS_MASK | DISPLAY_UNSAVED_ANALYSIS_MASK:
			displayedAnalyses = new DecoratedAnalysis[analyses.length];
			System.arraycopy(analyses, 0, displayedAnalyses, 0, analyses.length);
			break;

		case DISPLAY_SAVED_ANALYSIS_MASK:
			List<DecoratedAnalysis> savedAnalysesList = new ArrayList<>(analyses.length);
			for (int i = 0; i < analyses.length; i++) {
				int status = analyses[i].getStatus();
				if(Analysis.isAnalysisSaved(status)){
					savedAnalysesList.add(analyses[i]);
				}
			}
			displayedAnalyses = savedAnalysesList.toArray(new DecoratedAnalysis[savedAnalysesList.size()]);
			break;

		case DISPLAY_UNSAVED_ANALYSIS_MASK:
			List<DecoratedAnalysis> unsavedAnalysesList = new ArrayList<>(analyses.length);
			for (int i = 0; i < analyses.length; i++) {
				int status = analyses[i].getStatus();
				if(Analysis.isAnalysisUnsaved(status)){
					unsavedAnalysesList.add(analyses[i]);
				}
			}
			displayedAnalyses = unsavedAnalysesList .toArray(new DecoratedAnalysis[unsavedAnalysesList .size()]);
			break;			
		}
		
	}
	
	public void removeAnalyses(Analysis[] a) {
		List<Analysis> list = Arrays.asList(a);
		// rebuild the entire list (much simpler)
		DecoratedAnalysis[] previousAnalyses = analyses;
		analyses = new DecoratedAnalysis[analyses.length - a.length];
		int i = 0;
		for (DecoratedAnalysis d : previousAnalyses) {
			if (!list.contains(d.getAnalysis())) {
				d.setRow(i);
				analyses[i] = d;
				i++;
			}
		}
		buildDisplayedAnalysesArray();
		fireTableRowsUpdated(0, displayedAnalyses.length);
		refresherAnalysisQueue.retainAll(Arrays.asList(analyses));

	}

	public void addAnalyses(Analysis[] a) {
		int previousRowCount = analyses.length;
		DecoratedAnalysis[] newAnalyses = new DecoratedAnalysis[previousRowCount+ a.length];
		System.arraycopy(analyses, 0, newAnalyses, 0, previousRowCount);
		for (int i = 0; i < a.length; i++) {
			newAnalyses[previousRowCount + i] = new DecoratedAnalysis(a[i], previousRowCount + i);
			newAnalyses[previousRowCount + i].setSelected(Boolean.FALSE);
			refresherAnalysisQueue.add(newAnalyses[previousRowCount + i]);
		}
		analyses = newAnalyses;
		buildDisplayedAnalysesArray();
		fireTableRowsInserted(previousRowCount, displayedAnalyses.length);
	}

	public void stopRefresher() {
		if (!refresher.isDone() && !refresher.isCancelled()) {
			refresher.cancel(true);
			while (!refresher.isDone()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.info("Thread interrupted while waiting for AnalysisTableModel to cancel");
				}
			}
		}
	}

	private void updateAnalysisDestinationPath(DecoratedAnalysis analysis) {
		File dest;
		try {
			dest = parameters.getEPimsDataProvider().getDestinationDir(analysis, parameters);
		} catch (BackupException e) {
			dest = null;
		}

		if (dest == null) {// No destination found : Study name error ....
			analysis.setDisplayedDestinationPath(RSCS.getString("file.invalid.dest.path"));
		} else {
			// display destination path
			analysis.setDisplayedDestinationPath(dest.getAbsolutePath());
		} // End Destination found
		analysis.setDestination(analysis.getDisplayedDestinationPath());

	}

	/**
	 * This method is called when data structure has changed and some values
	 * should be recalculated.
	 */
	protected void updateModelContent() {
		for (int i = 0; i < displayedAnalyses.length; i++) {
			if( updateIsSelectedStatus(displayedAnalyses[i]) ) {
				fireTableCellUpdated(displayedAnalyses[i].getRow(), SELECT_COLUMN);
			}
		}
		this.fireTableStructureChanged();
	}

	protected boolean updateIsSelectedStatus(DecoratedAnalysis a) {
		if (a.isSelected()) {
				if (((parameters.getTransferMode() == BackupParameters.TRANSFER_CLEAN_MODE) && Analysis.isAnalysisUnsaved(a.getStatus()))
				 || ((parameters.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE) && !Analysis.isAnalysisOK(a.getStatus())))
					logger.debug("is selected changed to false");
				a.setSelected(Boolean.FALSE);
			  return true;
		}
		return false;
	}

	public void refreshEPimsDataForRow(int row) {
		DecoratedAnalysis a = getAnalysisAtRow(row);
		if(a.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE){
			return; //Do not update until acq is correct
		}
		a.setStatus(Analysis.ANALYSIS_STATUS_UNKNOWN);
		a.setDestination(null);
		a.setDisplayedDestinationPath(RSCS.getString("file.invalid.dest.path"));
		if( updateIsSelectedStatus(a) ) {
			fireTableCellUpdated(row, SELECT_COLUMN);
		}
		fireTableCellUpdated(row, STATUS_COLUMN);
		fireTableCellUpdated(row, DEST_COLUMN);
		refresherAnalysisQueue.addFirst(a);
	}

	public void refreshEPimsDataForAllRows() {
		refresherAnalysisQueue.clear();
		for (DecoratedAnalysis a : analyses) {
			if(a.getStatus() == Analysis.ANALYSIS_STATUS_NOT_READABLE){
				continue; //Do not update until acq is correct
			}
			a.setStatus(Analysis.ANALYSIS_STATUS_UNKNOWN);
			a.setDestination(null);
			a.setDisplayedDestinationPath(RSCS.getString("file.invalid.dest.path"));
			if( updateIsSelectedStatus(a) ) {
				fireTableCellUpdated(a.getRow(), SELECT_COLUMN);
			}
			fireTableCellUpdated(a.getRow(), STATUS_COLUMN);
			fireTableCellUpdated(a.getRow(), DEST_COLUMN);
			refresherAnalysisQueue.add(a);			
		}
	}

	public Analysis[] getSelectedAnalysis() {
		List<Analysis> selection = new ArrayList<Analysis>(getRowCount());
		for (int row = 0; row < getRowCount(); row++) {
			DecoratedAnalysis analysis = getAnalysisAtRow(row);
			if (analysis.isSelected()) {
				selection.add(analysis.getAnalysis());
			}
		}
		Analysis[] f = new Analysis[selection.size()];
		return selection.toArray(f);
	}
	
	protected void showSavedAnalysis(boolean show) {
		if (show)
			displayedAnalysisMask = displayedAnalysisMask | DISPLAY_SAVED_ANALYSIS_MASK;
		else
			displayedAnalysisMask = displayedAnalysisMask & DISPLAY_UNSAVED_ANALYSIS_MASK;
		buildDisplayedAnalysesArray();
		this.fireTableDataChanged();
	}

	protected void showUnsavedAnalysis(boolean show) {
		if (show)
			displayedAnalysisMask = displayedAnalysisMask | DISPLAY_UNSAVED_ANALYSIS_MASK;
		else
			displayedAnalysisMask = displayedAnalysisMask & DISPLAY_SAVED_ANALYSIS_MASK;
		buildDisplayedAnalysesArray();
		this.fireTableDataChanged();
	}

	/**
	 * gets the Analysis displayed at the specified row in the table.
	 * 
	 * @param displayedRow
	 * @return
	 */
	private DecoratedAnalysis getAnalysisAtRow(int displayedRow) {
		return displayedAnalyses[displayedRow];
	}

	public Object getValueAt(int row, int col) {
		DecoratedAnalysis a = getAnalysisAtRow(row);
		switch (col) {
		case SELECT_COLUMN:
			return a.isSelected();

		case STATUS_COLUMN:
      return a.getStatus();

		case ANALYSIS_COLUMN:
			return a;

		case STUDY_COLUMN:
			return "  --  ";

		case SAMPLE_COLUMN:
			return a.getSample();

		case DEST_COLUMN:
			if (a.getDestination() != null)
				return a.getDestination();
			return a.getDisplayedDestinationPath();

		case DATE_COLUMN:
			return a.getDate();

		case SIZE_COLUMN:
			return (((float) a.getEstimatedSize()) / (1024.0 * 1024.0));

		case DURATION_COLUMN:
			return a.getDuration();

		default:
			break;
		}
		return parameters.getDataFormat().getProperty(col - COLUMN_COUNT, a.getAnalysis());
	}

	public int getColumnCount() {
		return COLUMN_COUNT + parameters.getDataFormat().getPropertyCount();
	}

	public int getRowCount() {
		return displayedAnalyses.length;
	}

	public String getColumnName(int col) {
		String s = " ";
		switch (col) {
		case SELECT_COLUMN:
			break;
		case STATUS_COLUMN:
			s = RSCS.getString("analysis.table.status.label");
			break;
		case ANALYSIS_COLUMN:
			s = RSCS.getString("analysis.table.label");
			break;
		case STUDY_COLUMN:
			s = RSCS.getString("analysis.table.study.label");
			break;
		case SAMPLE_COLUMN:
			s = RSCS.getString("analysis.table.sample.label");
			break;
		case DEST_COLUMN:
			s = RSCS.getString("analysis.table.destination.label");
			break;
		case DATE_COLUMN:
			s = RSCS.getString("analysis.table.date.label");
			break;
		case SIZE_COLUMN:
			s = RSCS.getString("analysis.table.size.label");
			break;
		case DURATION_COLUMN:
			s = RSCS.getString("analysis.table.duration.label");
			break;
		default:
			s = parameters.getDataFormat().getPropertyLabel(col - COLUMN_COUNT);
			break;
		}
		;
		return s;
	}

	public Class getColumnClass(int col) {
		switch (col) {
		case SELECT_COLUMN:
			return Boolean.class;
		case STATUS_COLUMN, STUDY_COLUMN, SAMPLE_COLUMN, DEST_COLUMN:
			return String.class;
		case ANALYSIS_COLUMN:
			return Analysis.class;
      case DATE_COLUMN:
			return Date.class;
		case SIZE_COLUMN, DURATION_COLUMN:
			return Float.class;
    }
		return parameters.getDataFormat().getPropertyClass(col - COLUMN_COUNT);
	}

	public boolean isCellEditable(int row, int col) {
		if ( isEditableEnable && ((col == SELECT_COLUMN) || (col == SAMPLE_COLUMN))) {
			return true;
		}
		return false;
	}

		
	public void setTableEditable(boolean isEditable){
		isEditableEnable = isEditable;
	}
	
	public void setValueAt(Object value, int row, int col) {
		DecoratedAnalysis a = getAnalysisAtRow(row);

		// Select Column
		if (col == SELECT_COLUMN) {

			// Get analysis status
			int status = a.getStatus();
			if (status == Analysis.ANALYSIS_STATUS_UNKNOWN || status == Analysis.ANALYSIS_STATUS_NOT_READABLE)
				return;

			// Don't Allow Selection if :
			// transfer_mode==copy AND (destination file is NOK OR status =
			// INVALID_STUDY_SAMPLE, INVALID_STUDY, IN_CLOSE_STUDY, DUPLICATE,
			// EXIST
			// OR (status = INVALID_SAMPLE AND ignoreSample is false))
			// OR transfer_mode==clean AND
			// (status != EXIST OR study != Test)
			//

			if ( (parameters.getTransferMode() == BackupParameters.TRANSFER_CLEAN_MODE && (Analysis.isAnalysisUnsaved(status) && !Analysis.AnalysisType.TEST.equals(a.getType())))
				|| (parameters.getTransferMode() == BackupParameters.TRANSFER_COPY_MODE && (Analysis.isAnalysisSaved(status)
							|| Analysis.isAnalysisIncorrect(status) || Objects.equals(a.getDisplayedDestinationPath(), RSCS.getString("file.invalid.dest.path"))))) {
				return;
			}

			a.setSelected((Boolean) value);
			fireTableCellUpdated(row, col);

			// Sample Column
		} else if (col == SAMPLE_COLUMN) {
			a.setSample((String) value);
			refreshEPimsDataForRow(row);
			fireTableCellUpdated(row, col);
		}
	}


}