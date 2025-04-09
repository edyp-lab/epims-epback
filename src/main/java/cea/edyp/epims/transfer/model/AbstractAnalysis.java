package cea.edyp.epims.transfer.model;

import cea.edyp.epims.transfer.log.LogTextPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

abstract public class AbstractAnalysis implements  Analysis {

  protected static final Logger fileLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
  protected static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());
  protected static final Logger logger = LoggerFactory.getLogger(AbstractAnalysis.class);

  protected String name;
  protected String sample;
  protected File analysisFile;   //and the fileName included
  protected String analysisDestination;
  protected Analysis.AnalysisType analyseType;
  protected int status;
  protected Date acqDate;
  protected String operator;
  protected String description;
  protected Float duration;
  protected long estimatedSize;
  protected List<File> associatedFiles;

  protected void determineType() {
    if (CTRL_INST_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_INSTRUMENT;
    else if (CTRL_LC_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.CONTROL_LC;
    else if (BLANK_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.BLANK;
    else if (TEST_ANALYSIS_CODE.equalsIgnoreCase(sample))
      analyseType = Analysis.AnalysisType.TEST;
    else if (sample == null || sample.trim().isEmpty())
      analyseType = Analysis.AnalysisType.UNKNOWN;
    else
      analyseType = Analysis.AnalysisType.RESEARCH;
  }

  @Override
  public long getEstimatedSize() {
    return estimatedSize;
  }

  @Override
  public void setEstimatedSize(long size) {
    estimatedSize = size;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name= name;
  }

  @Override
  public String getSample() {
    return sample;
  }

  @Override
  public void setSample(String spl) {
    sample=spl;
    determineType();
  }

  @Override
  public boolean removeTemporaryZipFile() {
    return false;
  }

  @Override
  public String getDestination() {
    return this.analysisDestination;
  }

  @Override
  public void setDestination(String destinationDir) {
    this.analysisDestination= destinationDir;
  }

  @Override
  public AnalysisType getType() {
    return analyseType;
  }

  @Override
  public void setType(AnalysisType type) {
    analyseType = type;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
  }

  @Override
  public Date getDate() {
    return acqDate;
  }

  @Override
  public void setDate(Date date) {
    this.acqDate = date;
  }

  @Override
  public String getOperator() {
    return operator;
  }

  @Override
  public void setOperator(String op) {
    this.operator=op;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String desc) {
    this.description = desc;
  }

  @Override
  public Float getDuration() {
    return duration;
  }

  @Override
  public void setDuration(Float duration) {
    this.duration = duration;
  }

  @Override
  abstract public void setDataFormat(DataFormat dataFormat);

  @Override
  abstract public File getFile();

  @Override
  abstract public String getFileName();

  @Override
  abstract public FileFilter getContentFilter();

  @Override
  abstract public File[] getAssociatedFiles() ;

  @Override
  abstract public String getAssociatedFileType(File associatedFile);

}
