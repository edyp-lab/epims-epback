package cea.edyp.wiff.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.apache.poi.util.LittleEndian;

//import cea.commons.util.BinaryUtils;
import org.slf4j.LoggerFactory;

public class WiffFile {

	public static final String SCAN_EXT = "scan";
	public static final int ANALYSIS_NAME_IDX = 0;
	public static final int SAMPLE_NAME_IDX = 1;

	private static final String SAMPLESUBTREE = "SampleSubtree";
	private static final String SAMPLEDABE = "SampleSubtree\\Sample{0}\\SampleDABE";
	private static final String DATA = "DATA";
	private static final String SAMPLE_CUSTOM = "SampleSubtree\\Sample{0}\\SampleDABE\\SAMPLE_CUSTOM";
	private static final String COLNUMBER = "COLNUMBER";
	private static final String COLDATA = "COLDATA";
	private static final String COLNAMES = "COLNAMES";
	private static final String[] OPERATOR = {"operator","operateur"};
	private static final String DESCRIPTION = "description";
	private static final String SAMPLE_ENTRY_PREFIX = "Sample";
  private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
  
	Log logger = LogFactory.getLog(WiffFile.class);
  private static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  
	private String filename;
	private POIFSFileSystem POIfs;
	
	public WiffFile(String pathToFile) {
		filename = pathToFile;
	}
	
	public void openFS() {
		File file = new File(filename);
		try {
			POIfs = new POIFSFileSystem(file, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeFS() {
		if (POIfs != null) {
			try {
				POIfs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private DirectoryEntry getEntry(String path) {
		StringTokenizer tokenizer = new StringTokenizer(path, "\\");
		DirectoryEntry entry = POIfs.getRoot();
		try {
			while(tokenizer.hasMoreElements()) {  
				entry = (DirectoryEntry)entry.getEntry(tokenizer.nextToken());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entry;
	}
	
	@SuppressWarnings("unchecked")
  public int getSamplesCount() {
	  DirectoryEntry entry =  getEntry(SAMPLESUBTREE);
	  Iterator<Entry> it = entry.getEntries();
	  int splCount = 0; 
	  while(it.hasNext()){
	    String nextEntryName = it.next().getName();
	    if(nextEntryName.startsWith(SAMPLE_ENTRY_PREFIX)){
	      String suffix = nextEntryName.substring(SAMPLE_ENTRY_PREFIX.length());
	      try {
	        Integer.parseInt(suffix);
	        splCount++;
	      }catch (NumberFormatException nfe){
	      }
	    }
	  }
	    
	  return splCount;
	}

	private DirectoryEntry getSampleEntry(String pathTemplate, int idx) {
		String path = MessageFormat.format(pathTemplate, idx);
		return getEntry(path);
	}
	
	public AppliedAnalysis getAnalysis(int idx) {
		String[] analysis = new String[2];
		String[] columnNames = null;
		String[] columnValues = null;
		try {
			DirectoryEntry dir = getSampleEntry(SAMPLEDABE, idx);
			DocumentEntry entry = (DocumentEntry)dir.getEntry(DATA);
			DocumentInputStream dis = new DocumentInputStream(entry);
			dis.skip(36);
			analysis[ANALYSIS_NAME_IDX] = StringReader.readLine(dis);
			analysis[SAMPLE_NAME_IDX] = StringReader.readLine(dis);
			dis.close();
			logger.debug(" WIFF : ANALYSIS_NAME = "+analysis[ANALYSIS_NAME_IDX]+" ; SAMPLE_NAME "+analysis[SAMPLE_NAME_IDX]);
			dir = getSampleEntry(SAMPLE_CUSTOM, idx);
			entry = (DocumentEntry)dir.getEntry(COLNUMBER);
			dis = new DocumentInputStream(entry);
			dis.skip(32);
			int columnsCount = readShort(dis);
			dis.close();
			entry = (DocumentEntry)dir.getEntry(COLNAMES);
			dis = new DocumentInputStream(entry);
			dis.skip(32);
			columnNames = new String[columnsCount];
			for (int k = 0; k < columnsCount; k++)
				columnNames[k] = StringReader.readLine(dis);
			dis.close();
			columnValues = new String[columnsCount];
			entry = (DocumentEntry)dir.getEntry(COLDATA);
			dis = new DocumentInputStream(entry);
			dis.skip(32);
			for (int k = 0; k < columnsCount; k++)
				columnValues[k] = StringReader.readLine(dis);
			dis.close();
			
		} catch (FileNotFoundException e) {
			logger.warn("Invalid analysis in "+filename+" at index "+idx, e);
			return null;
			//e.printStackTrace();
			
		} catch (IOException e) {
			logger.warn("Invalid analysis in "+filename+" at index "+idx, e);
			return null;
			//e.printStackTrace();
		}
		return buildAnalysis(analysis, columnNames, columnValues);
	} 
	
	/**
	 * 
	 * Will get get AppliedAnalysis representing acquisition number idx in current File.
	 * The analysid properties will be created getting information from Sample List / Batch Columns:
	 * - property1 = Sample Name
	 * - property2 = Sample ID
	 * - property3 = Comment
	 * - property4 = DataFile
	 * 
	 * Optionally 2 custom columns could be defined 
	 *  - description : Acquisition description 
	 *  - operator/operateur : responsible of the instrument
	 * 
	 * IF property2 is not defined 
	 *  - ePims Sample name =  property1
	 *  - Analysis name =property4 without extension (In this case, their should be only ONE acquisition in file)
	 * ELSE 
	 *  - ePims Sample name =  property2
	 *  - Analysis name = property1
	 *  
	 * IF custom columns are defined
	 *    get these values from it (Acquisition description ,responsible of the instrument)
	 * ELSE 
	 *    retrieve information from comment columns using following rule :
	 *    d=Acquisition description
	 *    op=responsible of the instrument
	 *    If none d/op is found all value is set to description.
	 *    
	 *  
	 *  
	 * @param idx
	 * @return
	 */
	public AppliedAnalysis getAnalysisDeducingValues(int idx) {
		String[] analysis = new String[2];
		String[] columnNames = null;
		String[] columnValues = null;
		try {
			DirectoryEntry dir = getSampleEntry(SAMPLEDABE, idx);
			DocumentEntry entry = (DocumentEntry)dir.getEntry(DATA);
			DocumentInputStream dis = new DocumentInputStream(entry);
			dis.skip(36);
			//Read Sample Name Column
			String slpNameCol = StringReader.readLine(dis);
			//Read Sample ID Column
			String slpIDCol = StringReader.readLine(dis);			
			//Read Comment Column
			String comment = StringReader.readLine(dis);
	  	//Read Data File Column
			String fileName = StringReader.readLine(dis);

			if(slpIDCol == null || slpIDCol.isEmpty()){
				analysis[SAMPLE_NAME_IDX] = slpNameCol;
				int dotIndex = fileName.lastIndexOf('.');
				if(dotIndex != -1)
					analysis[ANALYSIS_NAME_IDX] = fileName.substring(0,dotIndex);
				else 
					analysis[ANALYSIS_NAME_IDX] = fileName;
			} else {
				analysis[ANALYSIS_NAME_IDX] = slpNameCol;
				analysis[SAMPLE_NAME_IDX] = slpIDCol;			
			}
			dis.close();
			logger.debug(" WIFF : ANALYSIS_NAME = "+analysis[ANALYSIS_NAME_IDX]+" ; SAMPLE_NAME "+analysis[SAMPLE_NAME_IDX]);
			dir = getSampleEntry(SAMPLE_CUSTOM, idx);
			entry = (DocumentEntry)dir.getEntry(COLNUMBER);
			dis = new DocumentInputStream(entry);
			dis.skip(32);
			int columnsCount = readShort(dis);
			dis.close();
			if(columnsCount >0){				
				//Read description / operator from custom columns
				
				entry = (DocumentEntry)dir.getEntry(COLNAMES);
				dis = new DocumentInputStream(entry);
				dis.skip(32);
				columnNames = new String[columnsCount];
				for (int k = 0; k < columnsCount; k++)
					columnNames[k] = StringReader.readLine(dis);
				dis.close();
				columnValues = new String[columnsCount];
				entry = (DocumentEntry)dir.getEntry(COLDATA);
				dis = new DocumentInputStream(entry);
				dis.skip(32);
				for (int k = 0; k < columnsCount; k++)
					columnValues[k] = StringReader.readLine(dis);
				dis.close();
				
				
			} else if(comment != null && !comment.isEmpty()) {
				//Read description / operator from comment column
				
				int opIndex = comment.indexOf("op=");
				int descIndex = comment.indexOf("d=");
				List<String> colNames = new ArrayList<String>();
				List<String> colVals = new ArrayList<String>();

				if(opIndex == -1 && descIndex == -1){
					colNames.add(DESCRIPTION);
					colVals.add(comment);
				} else {
					if(opIndex != -1){
						colNames.add(OPERATOR[0]);
						if(descIndex>opIndex)
							colVals.add(comment.substring(opIndex+3,descIndex));
						else
							colVals.add(comment.substring(opIndex+3));
					}
					
					if(descIndex != -1){				
						colNames.add(DESCRIPTION);
						if(opIndex>descIndex)
							colVals.add(comment.substring(descIndex+2,opIndex));
						else
							colVals.add(comment.substring(descIndex+2));
					}
				}
				columnNames = colNames.toArray(new String[colNames.size()]);
				columnValues = colVals.toArray(new String[colVals.size()]);

			}
		} catch (FileNotFoundException e) {
			logger.warn("Invalid analysis in "+filename+" at index "+idx);
			return null;
			//e.printStackTrace();
			
		} catch (IOException e) {
			logger.warn("Invalid analysis in "+filename+" at index "+idx);
			return null;
			//e.printStackTrace();
		}
		return buildAnalysis(analysis, columnNames, columnValues);
	} 
	
	private AppliedAnalysis buildAnalysis(String[] properties,
			String[] columnNames, String[] columnValues) {
		String operator = null;
		String description = null;
		if(columnNames != null){
			for (int k = 0; k < columnNames.length; k++) {
				if (columnNames[k].equals(DESCRIPTION)) {
					description = columnValues[k];
				} else {
					for(int nbrOp =0 ; nbrOp < OPERATOR.length ; nbrOp++){
						if (columnNames[k].equals(OPERATOR[nbrOp])) {
							operator = columnValues[k];
							break;
						}
					}
				}
			}
		}
		AppliedAnalysis analysis = new AppliedAnalysis(properties[ANALYSIS_NAME_IDX], properties[SAMPLE_NAME_IDX], operator, description);
		return analysis;
	}

	private short readShort(InputStream dis) throws IOException {
		byte[] w = new byte[2];
		dis.read(w);
		return LittleEndian.getShort(w);
	}
	
//	@SuppressWarnings("unused")
//  private void dumpContent(DocumentInputStream dis) throws IOException {
//		int count = dis.available();
//		byte[] w = new byte[count];
//		dis.read(w);
//		System.out.print(BinaryUtils.toHex(w));
//
//	}
	
	
	/**
	* Get all files associated to this acquisition WiffFile.
	* For simple acquisition, this will be a simple wiff file but for 
	* acquisition using "flat files for scan data" option it will be a wiff  
	* file and a wiff.scan file.
	* 
	*/
	public List<File> getWiffAcquisitionFiles() {
		File wiffFile = new File(filename);
		List<File> acqFiles = new ArrayList<File>();
//		File parentDir = wiffFile.getParentFile();
//		acqFiles.add(wiffFile);
//		File[] scansFiles = parentDir.listFiles(new FilenameFilter(){
//
//			
//			public boolean accept(File dir, String name) {
//				 int dotIndex = name.lastIndexOf('.');
//			    if(dotIndex == -1)
//			      return false;
//			    String extension = name.substring(dotIndex+1);
//			    if (extension != null) {
//			      return extension.equalsIgnoreCase(SCAN_EXT);
//			    }
//			    				
//				return false;
//			}
//			
//		});
//		
//		if(scansFiles != null && scansFiles.length >0){
//			String searchedFileName = wiffFile.getName()+"."+SCAN_EXT;
//			for(int i=0; i<scansFiles.length; i++){
//				File nextScanFile = scansFiles[i];
//				if(searchedFileName.equalsIgnoreCase(nextScanFile.getName())){
//					acqFiles.add(nextScanFile);
//				}				
//			}
//		}
		
		acqFiles.add(wiffFile);
		File scanFile = new File(wiffFile.getParentFile(), wiffFile.getName()+"."+SCAN_EXT);
		if (scanFile.exists()) {
			acqFiles.add(scanFile);
		}
		
		return acqFiles;
	}
	

  public File getZipFile(List<File> files) {
  	  	
    File currentFile;
    ZipOutputStream zipOutput;
    File zipAnalysisFile = null;
    try {
    
	    //test first if all the file in the dataFileList exist before creating the zip file
	    //if one file is missing set errorExists to true and put a error message
	    for(int j=0; j<files.size(); j++){
	    	currentFile = files.get(j);        	 
	    	//if a file doesn't exist
	    	if(!currentFile.exists()){
	    		String msg = RSCS.getString("datafile.not.found");
	    		Object[] args = {currentFile}; 
	    		logPaneLogger.error(MessageFormat.format(msg, args));
	    		throw new IOException(msg);
	    	}//else do nothing
	    }  	   
	    
	    Long start = System.currentTimeMillis();
	    logger.debug("******** Create ZIP ");
	    
	    zipAnalysisFile = new File(files.get(0).getParentFile(), files.get(0).getName()+".zip");
	    zipOutput = new ZipOutputStream(new FileOutputStream(zipAnalysisFile));
	
	    // Create a buffer for reading the files
	    byte[] buf = new byte[2048];    
	      
	    // Compress the files
	    for(int j=0; j<files.size(); j++) {
	    	currentFile = files.get(j);    
	    	FileInputStream inputStream = new FileInputStream(currentFile);
	    	logger.info("File to add to zip : "+currentFile.getName());
	    	// Add ZIP entry to output stream.
	    	zipOutput.putNextEntry(new ZipEntry(currentFile.getName()));
	    
	    	// Transfer bytes from the file to the ZIP file
	    	int len;
	    	while ((len = inputStream.read(buf)) > 0) {
	    		zipOutput.write(buf, 0, len);
	    	}
	    
	    	// Complete the entry
	    	zipOutput.closeEntry();
	    	inputStream.close();
	    }//END of for all files
	      
	    // Complete the ZIP file
	    zipOutput.close();
	    Long stop = System.currentTimeMillis();
	    logger.debug("******** STOP Create ZIP "+(stop-start)/1000);

    } catch (IOException e) {
    	logger.error("Error while compacting file into a zip. Trace : "+e);
    	return null;
    }
    zipAnalysisFile.setLastModified(files.get(0).lastModified());   
    return zipAnalysisFile;
    
  }
	
}
