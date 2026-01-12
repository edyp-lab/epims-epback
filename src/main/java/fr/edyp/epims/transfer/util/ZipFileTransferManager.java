package fr.edyp.epims.transfer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.MultiFilesAnalysis;

/***
 * IFileTransferManager for MultiFilesAnalysis which create a Zip in
 * destination directory.
 * 
 * @author VD225637
 * 
 */

public class ZipFileTransferManager extends DefaultFileTransferManager {

	public ZipFileTransferManager() {
		super();
	}

	@Override
	public void copyOnly(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException {

		if ((!MultiFilesAnalysis.class.isAssignableFrom(a.getClass())) || ((MultiFilesAnalysis) a).getAllAcquisitionFile().size() == 1) {
			super.copyOnly(a, ePimsDataProvider);
			return;
		}

		MultiFilesAnalysis ma = (MultiFilesAnalysis) a;

		// TODO VD : REMOVE copied file in case of error
		try {
			long start = System.currentTimeMillis();
			File destination = new File(a.getDestination(), a.getFileName());

			boolean skipCopy = false;
			if (destination.exists()) {
          logger.warn("File for Analysis {} already exist ! Can't copy acquisition ", a.getName());
					throw new BackupException("Analysis " + a.getName() + " already exist on PIMS-ROOT");
			}

			for (File nextFile : ma.getAllAcquisitionFile()) {
				if (nextFile == null || !nextFile.exists()) {
          logger.warn("File for analysis {} can't be find ! Can't copy acquisition", a.getName());
					throw new BackupException("Problem on analysisFile " + nextFile + " for analysis " + a.getName()
							+ ". The file can't be reached or is null");
				}
			}

			if (!skipCopy) {
        logger.debug(" Copy only in zip {} to {}", a.getName(), destination.getAbsolutePath());
				boolean copySucess = false;
				if( destination.createNewFile())
					copySucess = FileUtils.copyFilesToOneZip(destination, ma.getAllAcquisitionFile(),a.getFile(), ma.keepRelativePath());
				long end = System.currentTimeMillis();
				long duration = (end - start) / 1000;
				if (copySucess ) {
					String msg = RSCS.getString("copy.success");
					Object[] args = { a.getFileName(), destination.getAbsolutePath(), duration };
					msg = MessageFormat.format(msg, args);
					fileLogger.info(msg);

					File[] associatedFiles = a.getAssociatedFiles();
          for (File associatedFile : associatedFiles) {
            start = System.currentTimeMillis();
            destination = ePimsDataProvider.getAssociatedFileDestinationDir(a, associatedFile, a.getAssociatedFileType(associatedFile));
            destination = new File(destination, associatedFile.getName());
            FileUtils.secureCopy(associatedFile, destination);
            end = System.currentTimeMillis();
            duration = (end - start) / 1000;
            String assMsg = RSCS.getString("copy.success");
            Object[] assArgs = {associatedFile.getName(), destination.getAbsolutePath(), duration};
            assMsg = MessageFormat.format(assMsg, assArgs);
            fileLogger.info(assMsg);
          }
				} else {
					String msg = RSCS.getString("copy.error");
					Object[] args = { a.getFileName() };
					msg = MessageFormat.format(msg, args);
					fileLogger.info(msg);
					throw new BackupException("Error while creating zip File " + a.getFileName() + " for analysis " + a.getName());
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			String msg = RSCS.getString("copy.error");
			Object[] args = { a.getFileName() };
			msg = MessageFormat.format(msg, args);
			fileLogger.info(msg);

			throw new BackupException("impossible de trouver le fichier pour l'analyse " + a.getName(), e);
		} catch (IOException e) {
			e.printStackTrace();
			String msg = RSCS.getString("copy.error");
			Object[] args = { a.getFileName() };
			msg = MessageFormat.format(msg, args);
			fileLogger.info(msg);
			throw new BackupException("impossible d'ecrire le fichier de l'analyse " + a.getName(), e);
		}

	}

	@Override
	public void move(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws BackupException {

		if ((!MultiFilesAnalysis.class.isAssignableFrom(a.getClass())) || ((MultiFilesAnalysis) a).getAllAcquisitionFile().size() == 1) {
			super.move(a, ePimsDataProvider);
			return;
		}

		MultiFilesAnalysis ma = (MultiFilesAnalysis) a;

		try {

			long start = System.currentTimeMillis();
			File destination = new File(a.getDestination(), a.getFileName());

			boolean skipCopy = false;
			if (destination.exists()) {
				logger.warn("File for Analysis {} already exist ! Can't copy acquisition ", a.getName());
				throw new BackupException("Analysis " + a.getName() + " already exist on PIMS-ROOT");
			}

			for (File nextFile : ma.getAllAcquisitionFile()) {
				if (nextFile == null || !nextFile.exists()) {
          logger.warn("File for analysis {} can't be find ! Can't copy acquisition", a.getName());
					throw new BackupException("Problem on analysisFile " + nextFile + " for analysis " + a.getName()
							+ ". The file can't be reached or is null");
				}
			}

			if (!skipCopy) {
				ArrayList<File> filesToDel = new ArrayList<>();
        logger.debug(" Move {} to {}", destination, destination.getAbsolutePath());
				boolean copySucess = FileUtils.copyFilesToOneZip(destination, ma.getAllAcquisitionFile(), ma.getFile(), ma.keepRelativePath());
				long end = System.currentTimeMillis();
				long duration = (end - start) / 1000;
				if (copySucess) {
					String msg = RSCS.getString("copy.success");
					Object[] args = { a.getFileName(), destination.getAbsolutePath(), duration };
					msg = MessageFormat.format(msg, args);
					fileLogger.info(msg);

					filesToDel.addAll(ma.getAllAcquisitionFile());

					File[] associatedFiles = a.getAssociatedFiles();
          for (File associatedFile : associatedFiles) {
            start = System.currentTimeMillis();
            destination = ePimsDataProvider.getAssociatedFileDestinationDir(a, associatedFile, a.getAssociatedFileType(associatedFile));
            destination = new File(destination, associatedFile.getName());
            FileUtils.secureCopy(associatedFile, destination);
            end = System.currentTimeMillis();
            duration = (end - start) / 1000;
            String assMsg = RSCS.getString("copy.success");
            Object[] assArgs = {associatedFile.getName(), destination.getAbsolutePath(), duration};
            assMsg = MessageFormat.format(assMsg, assArgs);
            fileLogger.info(assMsg);
            filesToDel.add(associatedFile);
          }

					boolean result = delete(filesToDel);
					if (!result) {
						String delMsg = RSCS.getString("delete.error");
						Object[] delArgs = { a.getName() };
						delMsg = MessageFormat.format(delMsg, delArgs);
						logger.debug(delMsg);
						fileLogger.info(delMsg);
					}
				} else {
					String msg = RSCS.getString("copy.error");
					Object[] args = { a.getFileName() };
					msg = MessageFormat.format(msg, args);
					fileLogger.info(msg);
					throw new BackupException("Error while creating zip File " + a.getFileName() + " for analysis " + a.getName());
				}

			}
		} catch (FileNotFoundException e) {
			throw new BackupException("impossible de trouver le fichier pour l'analyse " + a.getName(), e);
		} catch (IOException e) {
			throw new BackupException("impossible d'ecrire le fichier de l'analyse " + a.getName(), e);
		}

	}

	@Override
	public void clean(Analysis a) throws BackupException {

		if ((!MultiFilesAnalysis.class.isAssignableFrom(a.getClass())) || ((MultiFilesAnalysis) a).getAllAcquisitionFile().size() == 1) {
			super.clean(a);
			return;
		}

		MultiFilesAnalysis ma = (MultiFilesAnalysis) a;

    logger.info(" Suppression de {}", a.getName());
		long start = System.currentTimeMillis();
		List<File> analysisSrc = ma.getAllAcquisitionFile();
    logger.debug(" Clear analysis Files including {} ... ", analysisSrc.get(0).getAbsolutePath());
		boolean delresult = delete(analysisSrc);
		long end = System.currentTimeMillis();
		long duration = (end - start) / 1000;
		if (delresult) {
			String msg = RSCS.getString("clean.success");
			Object[] args = { a.getName(), duration };
			msg = MessageFormat.format(msg, args);
			fileLogger.info(msg);
		} else {
			String msg = RSCS.getString("clean.error");
			Object[] args = { a.getName() };
			msg = MessageFormat.format(msg, args);
			fileLogger.info(msg);
		}

		File[] associatedFiles = a.getAssociatedFiles();
		start = System.currentTimeMillis();
		delresult = delete(Arrays.asList(associatedFiles));
		end = System.currentTimeMillis();
		duration = (end - start) / 1000;
		if (delresult) {
			String msg = RSCS.getString("clean.success");
			Object[] args = { a.getName(), duration };
			msg = MessageFormat.format(msg, args);
			fileLogger.info(msg);
		} else {
			String msg = RSCS.getString("clean.error");
			Object[] args = { a.getName() };
			msg = MessageFormat.format(msg, args);
			fileLogger.info(msg);
		}

	}

	private boolean delete(List<File> files) {
		boolean allDeletable = true;
    for (File f : files) {
      allDeletable = allDeletable && checkDeletable(f);
      logger.debug(" Result check File {} deletable {}", f.getName(), allDeletable);
    }

		if (!allDeletable) {
			return false;
		}

		logger.debug(" Start DELETE ");

		boolean succes = true;
    for (File file : files) {
      logger.debug(" Delete file {}", file.getName());
      succes = succes && deleteFile(file);
    }

		return succes;
	}

	private boolean deleteFile(File file) {
		boolean result = true;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if(files != null) {
				for (File value : files) {
					result = result && deleteFile(value);
					logger.debug("{} delete result {}", value.getName(), result);
				}
			}
		}

		result = result && file.delete();
    logger.debug("{} delete result {}", file.getName(), result);

		return result;
	}

	private boolean checkDeletable(File f) {
		boolean deletable = true;
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			if(files != null) {
				for (File file : files) {
					deletable = deletable && checkDeletable(file);
				}
			}
		} else
			deletable = f.canWrite();
		return deletable;
	}

}
