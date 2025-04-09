package cea.edyp.epims.transfer.dataformat.waters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

public class MLFactory extends AbstractCacheFactory {
	private static final String LOWERCASE_HEADER_FILENAME = "_header.txt";   
	
	private static final String acq_date_hdr = "$$ Acquired Date:";
	private static final String acq_time_hdr = "$$ Acquired Time:";
	private static final String job_code_hdr = "$$ Job Code:";
	private static final String user_name_hdr = "$$ User Name:";
	private static final String sample_hdr = "$$ Sample Description:";
	
	private static Logger logger = LoggerFactory.getLogger(MLFactory.class);
	
	@Override
	public List<Analysis> createAnalysis(File file) {
		MLAnalysis analysis = new MLAnalysis(file, (MLFormat)this.format);
		
		File header = new File(file, LOWERCASE_HEADER_FILENAME);
		if(!header.exists())
			header = new File(file, LOWERCASE_HEADER_FILENAME.toUpperCase());
		
		//Get HEADERS FILE INFO
		analysis.setDescription(getHeaderValue(sample_hdr, header));
		analysis.setOperator(getHeaderValue(user_name_hdr, header));
		StringBuffer date = new StringBuffer(getHeaderValue(acq_date_hdr, header));
		date.append(' ');
		date.append(getHeaderValue(acq_time_hdr, header));
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
		Date acqDate = df.parse(date.toString(), new ParsePosition(0));
		analysis.setDate(acqDate);  
		
		String sample = getHeaderValue(job_code_hdr, header);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    if ((sample != null) &&  (sample.equals(sdf.format(acqDate))))
    	sample = null;
		analysis.setSample(sample);
		
		List<Analysis> a = new ArrayList<Analysis>();
		a.add(analysis);
		return a;
	}
		
	private String getHeaderValue(String key, File header) {
		BufferedReader reader = null;
		String result = null;
		try {
			reader = new BufferedReader(new FileReader(header));
			String line = reader.readLine();
			while ((line != null) && !(line.startsWith(key))) {
				line = reader.readLine();
			}
			if ((line != null) && (line.startsWith(key))) {
				result = line.substring(key.length());
			}
			reader.close();
		} catch (IOException ioe) {
			logger.error("Impossible de lire le fichier header pour " + key);
			try {
				reader.close();
			} catch (Exception e) {
				logger.error("Impossible de germer le fichier header ", e);
			}
		}
		return result.trim();
	}

	@Override
	public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
		throw new UnsupportedOperationException("Not supported Operation");
	}
}
