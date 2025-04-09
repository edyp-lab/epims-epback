/*
 * Created on 24 mars 2006
 *
 * $Id: MLRawInfo.java,v 1.1 2007-09-14 09:37:31 dupierris Exp $
 */
package cea.edyp.epims.transfer.dataformat.waters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

/**
 * RawInfo
 *
 * @author vdupierr
 */
public class MLRawInfo {

  public static final String MSMS_function_data_file = "_FUNC00{0}.DAT";
  public static final String MSMS_function_idx_file = "_FUNC00{0}.IDX";
  public static final String MSMS_function_sts_file = "_FUNC00{0}.STS";
  
	protected static final int SCAN_LENGTH = 22;
		
	protected static Logger logger = LoggerFactory.getLogger(MLRawInfo.class);

	public static double getAnalysisDuration(MLAnalysis analysis) {

		
		int count = 1;
    File[] files = getFunctionFiles(analysis, count);
    double duration = 0;
    while ((files != null) && (files.length == 3)) {
       duration = Math.max(duration, getAnalysisDuration(files[1]));
       files = getFunctionFiles(analysis, ++count);
    }
    
		return duration;
	}

	private static double getAnalysisDuration(File indexFile) {
		try {
			FilterInputStream idx_is = new BufferedInputStream(new FileInputStream(
					indexFile), 1024);
			if (idx_is.available() < SCAN_LENGTH)
				return 0;

			//--- Read First Scan description
			byte[] w = new byte[8];
			long toSkip = 12;
			long skipped = idx_is.skip(toSkip);
			//Be sure to skip expected nbr bytes
			while (skipped != toSkip) {
				toSkip = toSkip - skipped;
				skipped = idx_is.skip(toSkip);
			}

			//read first scan retention time 
			idx_is.read(w, 0, 4);
			double start_retentionTime = readFloat(w);
			toSkip = 6;
			skipped = idx_is.skip(toSkip); // Go to the end of first Scan       
			while (skipped != toSkip) {
				toSkip = toSkip - skipped;
				skipped = idx_is.skip(toSkip);
			}

			//-- Go to last Scan
			int bytesToEnd = idx_is.available();
			if (bytesToEnd < SCAN_LENGTH)
				return 0;
			//       logger.debug(" Available "+bytesToEnd+" => nbrScan "+(bytesToEnd/22)+" % ? "+(bytesToEnd%22));
			toSkip = bytesToEnd - SCAN_LENGTH;
			skipped = idx_is.skip(toSkip);
			//Be sure to skip expected nbr bytes
			while (skipped != toSkip) {
				toSkip = toSkip - skipped;
				skipped = idx_is.skip(toSkip);
			}

			//--- Read Last Scan description
			w = new byte[8];
			toSkip = 12;
			skipped = idx_is.skip(toSkip);
			//Be sure to skip expected nbr bytes
			while (skipped != toSkip) {
				toSkip = toSkip - skipped;
				skipped = idx_is.skip(toSkip);
			}

			//Read RetentionTime
			idx_is.read(w, 0, 4);
			double end_retentionTime = readFloat(w);
			idx_is.close();

			double duration = end_retentionTime - start_retentionTime;
			//       logger.debug(" Analysis duration => "+duration);
			return duration;

		} catch (IOException e) {
			logger.error("error reading scans", e);
			return 0;
		}

	}

	private static long getAnalysisSize(File datFile) {
		return datFile.length();
	}
	
	public static long getAnalysisSize(MLAnalysis analysis) {
		// TODO Auto-generated method stub

		int count = 1;
    File[] files = getFunctionFiles(analysis, count);
    long size = 0;
    while ((files != null) && (files.length == 3)) {
       size+=getAnalysisSize(files[0]);
       size+=getAnalysisSize(files[1]);
       files = getFunctionFiles(analysis, ++count);
    }

		return size;
	}
	
  protected static File[] getFunctionFiles(Analysis a, int c) {
    File datFile = null;
    File idxFile = null;
    File stsFile = null;

    Object[] args = { c };
    String funcDatFilename = MessageFormat.format(MSMS_function_data_file, args);
    String funcIdxFilename = MessageFormat.format(MSMS_function_idx_file, args);
    String funcStsFilename = MessageFormat.format(MSMS_function_sts_file, args);
    
    datFile = new File(a.getFile(), funcDatFilename);
    idxFile = new File(a.getFile(), funcIdxFilename);
    stsFile = new File(a.getFile(), funcStsFilename);
    
    if ((!datFile.exists()) || (!idxFile.exists()))
       return null;
    File[] files = { datFile, idxFile, stsFile };
    return files;
 }
  
  public static float readFloat(byte[] w) {
  	return Float.intBitsToFloat(readInt(w));
  }
  
  public static int readInt(byte[] w) {
    return (w[3]) << 24 | (w[2] & 0xff) << 16 | (w[1] & 0xff) << 8 | (w[0] & 0xff);
 }
}




