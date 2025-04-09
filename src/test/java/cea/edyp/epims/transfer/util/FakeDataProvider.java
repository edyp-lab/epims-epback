package cea.edyp.epims.transfer.util;

import java.io.File;

import org.perf4j.StopWatch;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.BackupException;
import cea.edyp.epims.transfer.model.BackupParameters;
import org.perf4j.slf4j.Slf4JStopWatch;

public class FakeDataProvider implements IEPSystemDataProvider {

	
	public String getPimsRootPath() {
		return "d:/tmp/epims_root/a/";
	}

	public String getPimsSystemRelativePath() {
		return "system";
	}

	public String getStudyNameFor(String sampleName) {
		return sampleName.substring(0, 1);
	}

	public boolean isSampleExist(String sampleName) {
		return !sampleName.contains("wrong");
	}

	public boolean isAcquisitionExist(String acqName, String instrumentName) {
		return false;
	}

	public boolean isSpectrometerDefined(String instrumentName) {
		return true;
	}

	public void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException {
		// TODO Auto-generated method stub

	}

	public File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
		return new File(getDestinationDir(a, null), "Others");
	}

	public File getDestinationDir(Analysis a, BackupParameters param) throws BackupException {
		StopWatch stopWatch = new Slf4JStopWatch("fake getDestinationDir", a.getName());
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		File destination = new File(getPimsRootPath(), getStudyNameFor(a.getSample()));
		
		// Destination dir not exist => it must be created
		if (!destination.exists()) {
			boolean dirCreationSucceed = destination.mkdirs();

			// Directory creation failed => Error thrown
			if (!dirCreationSucceed) {
				throw new BackupException("Cannot create directory "+destination.getAbsolutePath());
			}
		}
		a.setDestination(destination.getAbsolutePath());
		stopWatch.stop();
		return destination;
	}

	public int getAnalysisStatus(Analysis analysis, BackupParameters params) {
		StopWatch stopWatch = new Slf4JStopWatch("fake getAnalysisStatus", analysis.getName());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopWatch.stop();
		int result = analysis.getSample().length() % 4;
		analysis.setStatus(result);
		return result;
	}

}
