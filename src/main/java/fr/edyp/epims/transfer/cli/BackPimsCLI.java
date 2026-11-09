package fr.edyp.epims.transfer.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.AnalysisOperationMgr;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.BackupParameters;
import fr.edyp.epims.transfer.model.CacheManager;
import fr.edyp.epims.transfer.model.InstrumentConfiguration;
import fr.edyp.epims.transfer.preferences.EPBackPreferences;
import fr.edyp.epims.transfer.preferences.PreferencesKeys;
import fr.edyp.epims.transfer.task.ConnectTask;

public class BackPimsCLI {

	private static final Logger logger = LoggerFactory.getLogger(BackPimsCLI.class);

	public static void main(String[] args) {
		try {
			boolean success = execute(args);
			if (!success) {
				System.exit(1);
			}
		} catch (Exception e) {
			logger.error("Erreur lors de l'exécution en mode CLI: {}", e.getMessage(), e);
			System.exit(1);
		}
	}

	public static boolean execute(String[] args) throws Exception {
		String analysisParam = parseAnalysisParam(args);
		logger.info("Exécution en mode CLI pour le transfert de l'analyse: {}", analysisParam);

		Map<String, InstrumentConfiguration> instrumentsConfig = InstrumentConfiguration.readInstrumentsXMLConfiguration();
		InstrumentConfiguration instrumentConfig = validateSingleInstrumentConfig(instrumentsConfig);

		BackupParameters parameters = new BackupParameters();
		parameters.setInstrumentConfiguration(instrumentConfig);
		CacheManager.getInstance().setupForConfiguration(instrumentConfig.getLabel());

		String user = EPBackPreferences.root().get(PreferencesKeys.SERVER_USER_KEY, "");
		String pwd = EPBackPreferences.root().get(PreferencesKeys.SERVER_PSWD_KEY, "");
		ConnectTask task = new ConnectTask(user, pwd);
		if (!task.fetchData()) {
			throw new BackupException("Échec de connexion au serveur ePims: " + task.getTaskError());
		}

		return transferAnalysis(analysisParam, parameters);
	}

	public static InstrumentConfiguration validateSingleInstrumentConfig(Map<String, InstrumentConfiguration> instrumentsConfig) throws BackupException {
		if (instrumentsConfig == null || instrumentsConfig.isEmpty()) {
			throw new BackupException("Aucune configuration d'instrument trouvée dans " + InstrumentConfiguration.INSTRUMENT_CONFIGURATION_FILEPATH);
		}
		if (instrumentsConfig.size() > 1) {
			throw new BackupException("Configuration de multiples instruments détectée (" + instrumentsConfig.size() + " instruments trouvés). Le mode CLI nécessite la configuration d'un unique instrument.");
		}
		return instrumentsConfig.values().iterator().next();
	}

	public static String parseAnalysisParam(String[] args) throws BackupException {
		if (args == null || args.length == 0) {
			throw new BackupException("Aucune analyse spécifiée en paramètre. Utilisez l'option -f \"filename\".");
		}
		BackPimsArgs cliArgs = new BackPimsArgs();
		JCommander jCommander = JCommander.newBuilder().addObject(cliArgs).build();
		try {
			jCommander.parse(args);
		} catch (ParameterException pe) {
			throw new BackupException("Paramètres de ligne de commande invalides: " + pe.getMessage(), pe);
		}
		if (cliArgs.getFilename() == null || cliArgs.getFilename().trim().isEmpty()) {
			throw new BackupException("Le nom de fichier d'analyse (-f) ne peut pas être vide.");
		}
		return cliArgs.getFilename().trim();
	}

	public static boolean transferAnalysis(String analysisParam, BackupParameters parameters) throws BackupException {
		Analysis targetAnalysis = findAnalysis(analysisParam, parameters);
		if (targetAnalysis == null) {
			throw new BackupException("Analyse non trouvée pour le paramètre: " + analysisParam);
		}

		String dest;
		try {
			dest = parameters.getEPimsDataProvider().getDestinationDir(targetAnalysis, parameters);
		} catch (BackupException e) {
			logger.warn("Erreur lors de la récupération du répertoire de destination pour {}: {}", targetAnalysis.getName(), e.getMessage());
			dest = null;
		}

		int status = parameters.getEPimsDataProvider().getAnalysisStatus(targetAnalysis, parameters);

		boolean isOk = Analysis.isAnalysisOK(status);
		if (!isOk || dest == null) {
			String statusDesc = getStatusDescription(status);
			String destPath = (dest != null) ? dest : (targetAnalysis.getDestination() != null ? targetAnalysis.getDestination() : "Invalide / Non déterminé");
			logger.warn("Transfert impossible pour l'analyse '{}' car l'analyse n'est pas valide. Statut: {} ({}), Destination path: {}", targetAnalysis.getName(), status, statusDesc, destPath);
			return false;
		}

		logger.info("Démarrage du transfert de l'analyse '{}' vers '{}'...", targetAnalysis.getName(), dest);
		AnalysisOperationMgr operationMgr = new AnalysisOperationMgr(parameters);
		operationMgr.transfer(targetAnalysis);
		logger.info("Transfert terminé avec succès pour l'analyse '{}'.", targetAnalysis.getName());

		try {
			CacheManager.getInstance().save();
		} catch (IOException e) {
			logger.warn("Impossible de sauvegarder le cache: {}", e.getMessage());
		}

		return true;
	}

	public static File findPath(String filename, File sourcePath) {
		if (filename == null || filename.trim().isEmpty()) {
			return null;
		}
		File file = new File(filename.trim());
		if (file.isAbsolute()) {
			return file;
		}
		if (sourcePath != null) {
			return new File(sourcePath, filename.trim());
		}
		return file;
	}

	public static Analysis findAnalysis(String analysisParam, BackupParameters parameters) throws BackupException {
		File file = findPath(analysisParam, parameters.getSourcePath());
		if (file == null || !file.exists()) {
			logger.warn("Fichier d'analyse non trouvé : {}", file != null ? file.getAbsolutePath() : analysisParam);
			return null;
		}

		Analysis[] analyses = parameters.getDataFormat().createAnalyses(file);
		if (analyses != null ) {
			if ( analyses.length > 1)
				throw new  BackupException("En mode CLI, un fichier doit correspondre à une seule analyse ");
			return analyses[0];
		}

		return null;
	}


	public static String getStatusDescription(int status) {
		if (status == Analysis.ANALYSIS_STATUS_NOT_READABLE) {
			return "Inaccessible / Fichier illisible";
		}
		if (status == Analysis.ANALYSIS_STATUS_UNKNOWN) {
			return "Inconnu";
		}
		if (status == Analysis.ANALYSIS_OK_MASK) {
			return "OK (non sauvegardée)";
		}
		List<String> reasons = new ArrayList<>();
		if ((status & Analysis.ANALYSIS_EXIST_MASK) == Analysis.ANALYSIS_EXIST_MASK) {
			reasons.add("Acquisition déjà existante dans ePims");
		}
		if ((status & Analysis.ANALYSIS_STUDY_CLOSED_MASK) == Analysis.ANALYSIS_STUDY_CLOSED_MASK) {
			reasons.add("Etude close");
		}
		if ((status & Analysis.ANALYSIS_INVALID_SAMPLE_MASK) == Analysis.ANALYSIS_INVALID_SAMPLE_MASK) {
			reasons.add("Echantillon invalide ou non défini");
		}
		return String.join(", ", reasons);
	}
}
