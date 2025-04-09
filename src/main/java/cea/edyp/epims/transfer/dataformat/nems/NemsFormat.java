package cea.edyp.epims.transfer.dataformat.nems;

import cea.edyp.epims.transfer.log.LogTextPanel;
import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.DataFormat;
import cea.edyp.epims.transfer.model.IFileTransfertManager;
import cea.edyp.epims.transfer.util.DefaultFileTransfertManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class NemsFormat extends JPanel implements DataFormat {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(NemsFormat.class);
    private static Logger logPaneLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
    private static ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd-MM-yyyy", Locale.FRENCH);
    public static final SimpleDateFormat FORMATTER_SHORT = new SimpleDateFormat("ddMMyyyy", Locale.FRENCH);

    private FileFilter dataFilter;
    private NemsFactory analysisFactory;

    public NemsFormat() {
        analysisFactory = new NemsFactory();
        dataFilter = new NemsFileFilter();
    }

    @Override
    public Analysis[] getAnalysis(File dir) {
        logger.info("reading directory " + dir.getAbsolutePath());
        File[] files = dir.listFiles(dataFilter);

        // if the listFiles() return null the directory doesn't exists (or there
        // is an I/O error)
        if (files == null) {
            String msg = RSCS.getString("acq.dir.notexist");
            Object[] args = { dir };
            logPaneLogger.error(MessageFormat.format(msg, args));
            return new Analysis[0];
        }

        return this.analysisFactory.getAnalyses(this, Arrays.asList(files));
    }

    @Override
    public IFileTransfertManager getFileTransfertManager() {
        return new DefaultFileTransfertManager(false);
    }

    @Override
    public int getPropertyCount() {
        return 0;
    }

    @Override
    public Class getPropertyClass(int propertyIdx) {
        return null;
    }

    @Override
    public String getPropertyLabel(int propertyIdx) {
        return null;
    }

    @Override
    public Object getProperty(int propertyIdx, Analysis analysis) {
        return null;
    }

    @Override
    public JComponent getConfigurator() {
        return this;
    }

    public static Date parseDateName(String name) {
        try {
            Date date = FORMATTER.parse(name);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public class NemsFileFilter implements FileFilter {

        @Override
        public boolean accept(File parentFile) {
            if (!parentFile.isDirectory()) {
                return false;
            }
            Date date = parseDateName(parentFile.getName());
            if (date == null) {
                return false;
            }

            String dateShort = FORMATTER_SHORT.format(date);

            // Search :
            // - SPLx_MSETy_DATE directories
            // - .h5 file
            // - .m file (matlab)
            // - Unlocked directory
            File h5File = null;
            File matlabFile = null;
            File unlockedDirectory = null;

            boolean foundSampleMset = false;

            File[] listOfFiles = parentFile.listFiles();
            for (File f : listOfFiles) {
                String name  = f.getName();
                if (f.isDirectory()) {
                    if (name.equals("Unlocked")) {
                        unlockedDirectory = f;
                        continue;
                    }
                    int index1 = name.indexOf('_');
                    int index2 = name.lastIndexOf('_');
                    if ((index1!=-1) && (index2!=-1) && (index1!=index2)) {
                        String sample = name.substring(0,index1);
                        String mset = name.substring(index1+1, index2);
                        String msetDate = name.substring(index2+1);
                        // check date
                        if (!msetDate.equals(dateShort)) {
                            return false;
                        }
                        foundSampleMset = !sample.isEmpty() && !mset.isEmpty();
                    }
                } else {
                    if (name.endsWith(".h5")) {
                        h5File = f;
                    } else if (name.endsWith(".m")) {
                        matlabFile = f;
                    }
                }
            }

            return (h5File != null) && (matlabFile != null) && foundSampleMset;

        }



    }


}
