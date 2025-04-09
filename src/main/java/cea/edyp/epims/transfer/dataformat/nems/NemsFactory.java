package cea.edyp.epims.transfer.dataformat.nems;


import cea.edyp.epims.transfer.log.LogTextPanel;
import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

public class NemsFactory extends AbstractCacheFactory {

    private static Logger logger = LoggerFactory.getLogger(NemsFactory.class);
    private static Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
    private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());



    @Override
    public List<Analysis> createAnalysis(File parentFile) {
        String msg = RSCS.getString("analysis.creation.info");
        Object[] args = {parentFile.getAbsolutePath()};
        msg = MessageFormat.format(msg, args);
        mainLogger.info(msg);

        if (!parentFile.exists() || !parentFile.isDirectory()){
            logger.debug("Invalid Nems File specified");
            throw new IllegalArgumentException("Invalid Nems File specified");
        }

        List<Analysis> analysisList = new ArrayList<Analysis>();

        Date date = NemsFormat.parseDateName(parentFile.getName());
        if (date == null) {
            return analysisList;
        }


        HashMap<String, ArrayList<Mset>> sample2MsetListMap = new HashMap<>();
        HashMap<String, String> sample2Operator = new HashMap<>();
        HashMap<String, String> sample2Description = new HashMap<>();

        File[] listOfFiles = parentFile.listFiles();
        for (File f : listOfFiles) {
            String name  = f.getName();
            if (f.isFile() && name.endsWith(".properties")) {
                Properties prop = new Properties();
                try {
                    prop.load(new FileReader(f));

                    String operator = prop.getProperty("operator");
                    String description = prop.getProperty("description", "");


                    final String SAMPLE = "SAMPLE:";
                    String sample = searchValue(description, SAMPLE);

                    sample2Operator.put(sample, operator);
                    sample2Description.put(sample, description);

                    final String FOLDER = "FOLDER:";
                    String folders = searchValue(description, FOLDER);
                    StringTokenizer st = new StringTokenizer(folders, ";");
                    while (st.hasMoreTokens()) {
                        String folder = st.nextToken();
                        // check that the folder exists
                        String folderPath = parentFile.getAbsolutePath()+File.separator+folder;
                        File folderFile = new File(folderPath);
                        if (!folderFile.exists() || !folderFile.isDirectory()) {
                            String error = "Invalid Nems Sample Directory specified "+folderPath;
                            logger.error(error);
                            throw new IllegalArgumentException(error);
                        }

                        String mset;

                        int index2 = name.lastIndexOf('_');
                        if (index2 == -1) {
                            index2 = name.lastIndexOf('.');
                            if (index2 == -1) {
                                String error = "Problem occured for "+name+" in folder "+folderPath;
                                logger.error(error);
                                throw new IllegalArgumentException(error);
                            }
                            mset = name.substring(0, index2);
                        } else {
                            int index1 = name.substring(0, index2).lastIndexOf('_');
                            mset = name.substring(index1 + 1, index2);
                        }

                        if (!sample.isEmpty() && !mset.isEmpty()) {
                            ArrayList<Mset> msetList = sample2MsetListMap.get(sample);
                            if (msetList == null) {
                                msetList = new ArrayList<>();
                                sample2MsetListMap.put(sample, msetList);
                            }
                            msetList.add(new Mset(mset, folderFile));
                        }
                    }

                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        for (String sample : sample2MsetListMap.keySet()) {
            String operator = sample2Operator.get(sample);
            String description = sample2Description.get(sample);

            ArrayList<Mset> msetList = sample2MsetListMap.get(sample);

            NemsAnalysis analysis = new NemsAnalysis(parentFile, (NemsFormat) format, sample, msetList);
            try {
                //JPM.NEMS : .H5 https://support.hdfgroup.org/ftp/HDF5/hdf-java/current/src/
                analysis.setDate(date);
                /*m_sb.setLength(0);
                for (Mset mset : msetList) {
                    if (m_sb.length()>0) {
                        m_sb.append(" - ");
                    }
                    m_sb.append(mset.name);
                }*/

                analysis.setDescription(description);
                analysis.setOperator(operator);
                analysis.setDuration(0.0f);

                analysisList.add(analysis);
            } catch (Exception exp) {
                logger.error("erreur de lecture du fichier " + parentFile.getAbsolutePath());
                msg = RSCS.getString("analysis.creation.error");
                Object[] twoArgs = {parentFile.getAbsolutePath(), exp.getMessage()};
                msg = MessageFormat.format(msg, twoArgs);
                mainLogger.info(msg);

            }

        }



        return analysisList;
    }

    @Override
    public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
        throw new UnsupportedOperationException("Not supported Operation");
    }

    private static String searchValue(String text, String key) {
        int sampleIndex = text.indexOf(key);
        if (sampleIndex == -1) {
            // properties file have not the necessary informations
            String error = "Invalid Nems Properties File specified : "+text+"  searched key:"+key;
            logger.error(error);
            throw new IllegalArgumentException(error);
        }
        int endSampleIndex = text.indexOf(",", sampleIndex+key.length()+1);
        if (endSampleIndex == -1) {
            endSampleIndex = text.length();
        }
        return text.substring(sampleIndex+key.length(), endSampleIndex);
    }

    public class Mset {
        public String name;
        public File msetFile;

        public Mset(String name, File msetFile) {
            this.name = name;
            this.msetFile = msetFile;
        }
    }

}
