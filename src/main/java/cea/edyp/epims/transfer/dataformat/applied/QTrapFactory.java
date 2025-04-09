package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.wiff.reader.AppliedAnalysis;
import cea.edyp.wiff.reader.WiffFile;
import org.slf4j.LoggerFactory;

public class QTrapFactory extends AbstractCacheFactory {

	private static Logger logger = LoggerFactory.getLogger(QTrapFactory.class);
	private static Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
	private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

	@Override
	public List<Analysis> createAnalysis(File file) {
		List<Analysis> analysis = new ArrayList<Analysis>();
		logger.info("QTrapFactory createAnalysis");
		if (file != null) {
			String msg = RSCS.getString("analysis.creation.info");
			Object[] args = { file.getAbsolutePath() };
			msg = MessageFormat.format(msg, args);
			mainLogger.info(msg);
//			logger.info(msg);

			WiffFile wiffFile = new WiffFile(file.getAbsolutePath());
			wiffFile.openFS();

			// One wiff file can contain many analysis
			int nbrAnalysis = wiffFile.getSamplesCount();
			for (int k = 1; k <= nbrAnalysis; k++) {
				AppliedAnalysis nextAppliedAnalysis = wiffFile.getAnalysis(k);
				if (nextAppliedAnalysis == null) {
					msg = RSCS.getString("analysis.creation.error");
					Object[] twoArgs = { file.getAbsolutePath() + " index " + k, RSCS.getString("unknown.error.message") };
					msg = MessageFormat.format(msg, twoArgs);
					mainLogger.info(msg);
					continue;
				}
				QTrapAnalysis epimsAnalysis = new QTrapAnalysis(file, (QTrapFormat) format);
				epimsAnalysis.setName(nextAppliedAnalysis.getAnalysisName());
				epimsAnalysis.setOperator(nextAppliedAnalysis.getOperator());
				epimsAnalysis.setSample(nextAppliedAnalysis.getSampleName());
				epimsAnalysis.setDescription(nextAppliedAnalysis.getDescription());
				analysis.add(epimsAnalysis);
			} // End go through all analysis of current wiff file
			wiffFile.closeFS();

		}

		return analysis;
	}

	@Override
	public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
		throw new UnsupportedOperationException("Not supported Operation");
	}
}
