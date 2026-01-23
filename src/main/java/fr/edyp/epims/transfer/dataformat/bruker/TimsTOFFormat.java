package fr.edyp.epims.transfer.dataformat.bruker;

import fr.edyp.epims.transfer.log.LogTextPanel;
import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.DataFormat;
import fr.edyp.epims.transfer.model.IFileTransferManager;
import fr.edyp.epims.transfer.preferences.EPBackPreferences;
import fr.edyp.epims.transfer.preferences.PreferencesKeys;
import fr.edyp.epims.transfer.util.DefaultFileTransferManager;
import fr.edyp.epims.transfer.util.FTPFileTransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class TimsTOFFormat extends JPanel implements DataFormat {

  private static final long serialVersionUID = 1L;
  private final static Logger logger = LoggerFactory.getLogger(TimsTOFFormat.class);
  private final static Logger fileLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  private final static ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());
  private final TimsTOFFactory analysisFactory;
  private final FileFilter dataFilter;

//  private static final String[] FORMAT_PROPERTIES = { /*USER_PROP, ACQ_TYPE, NB_SHOOTS, NB_ACQ_PER_REP, AUTOX_METHOD_PROP, NB_REPLICAT_PROP, ION_PARENT_MASS */};


  public TimsTOFFormat(){
    analysisFactory = new TimsTOFFactory();
    dataFilter = new TimsTOFFormat.TimsTOFDirFilter();
  }

  @Override
  public Analysis[] getAnalysis(File dir) {
    logger.info("reading TimsTOF analysis from " + dir.getAbsolutePath());
    File[] analysisFiles =  dir.listFiles(dataFilter);
    if (analysisFiles == null) {
      String msg = RSCS.getString("acq.dir.notexist");
      Object[] args = { dir };
      fileLogger.error(MessageFormat.format(msg, args));
      return new Analysis[0];
    }

    return this.analysisFactory.getAnalyses(this, Arrays.asList(analysisFiles));
  }

  @Override
  public IFileTransferManager getFileTransfertManager() {
    Preferences preferences = EPBackPreferences.root();
    String transferMode = preferences.get(PreferencesKeys.TRANSFER_MODE,PreferencesKeys.DEFAULT_TRANSFER_MODE);
    if(transferMode.equals(PreferencesKeys.DEFAULT_TRANSFER_MODE))
      return new DefaultFileTransferManager( );
    else
      return new FTPFileTransferManager( );
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

 private static class TimsTOFDirFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      if (!pathname.isDirectory()) {
        return false;
      }

      if(!pathname.getName().endsWith(".d"))
        return false;

      int nbFound =0;
      File[] listOfFiles = pathname.listFiles();
      for(File nextFile : listOfFiles){
        if(nextFile.getName().equals("analysis.tdf_bin"))
          nbFound++;
        else if(nextFile.getName().equals("analysis.tdf"))
          nbFound++;
        else if(nextFile.getName().equals("SampleInfo.xml"))
          nbFound++;
      }

      return  nbFound==3;
    }

  }

  public static class TimsTOFFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      return true;
    }

  }
}
