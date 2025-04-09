package cea.edyp.epims.transfer.dataformat.bruker;

import cea.edyp.epims.transfer.log.LogTextPanel;
import cea.edyp.epims.transfer.model.AbstractCacheFactory;
import cea.edyp.epims.transfer.model.Analysis;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

public class TimsTOFFactory extends AbstractCacheFactory  {

  private static final Logger logger = LoggerFactory.getLogger(TimsTOFFactory.class);
  private static final Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);

  private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

  private static final String SAMPLEXML_FILENAME="SampleInfo.xml";
  private static final String SAMPLEXML_PATH="//SampleTable//Sample";
  private static final String SAMPLEXML_SAMPLEID_KEY="SampleID";
  private static final String SAMPLEXML_OPERATOR_KEY="Submitter_Name";

  /**
   * Create one analysis from timsTOF analysis directory.
   * Only one analysis from each .d folder
   * @param file folder to read analysis from. Only 1 analysis will be created
   * @return a List of 1 TimsTOFAnalysis
   */
  @Override
  public List<Analysis> createAnalysis(File file) {
    String msg = RSCS.getString("analysis.creation.info");
    Object[] args = {file.getAbsolutePath()};
    msg = MessageFormat.format(msg, args);
    mainLogger.info(msg);


    if (!file.exists() || !file.isDirectory()){
      logger.debug("Invalid timsTOF File specified");
      throw new IllegalArgumentException("Invalid timsTOF File specified");
    }

    List<Analysis> analysisList = new ArrayList<Analysis>();

    long lastModified = file.lastModified();
    Date date = new Date(lastModified);
    TimsTOFAnalysis analysis = new TimsTOFAnalysis(file, (TimsTOFFormat) format);
    analysis.setDate(date);
    analysis.setDescription("");
    analysis.setDuration(0.0f);
    analysis.setOperator("No-operator-found");
    analysis.setSample("No-Sample-found");
    analysis.setEstimatedSize(0L);
    readAndSetDataForAnalysis(analysis);
    analysisList.add(analysis);
    return analysisList;
  }

  private void readAndSetDataForAnalysis(TimsTOFAnalysis a) {
    try {
      File dir = a.getParentDirFile();
      File[] splFile = dir.listFiles((FileFilter) new NameFileFilter(SAMPLEXML_FILENAME));
      if (splFile.length > 0) {
        FileInputStream fis = new FileInputStream(splFile[0]);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(fis);
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node sample = (Node) xPath.compile(SAMPLEXML_PATH).evaluate(xmlDocument, XPathConstants.NODE);
        NamedNodeMap att = sample.getAttributes();
        Node aNode = att.getNamedItem(SAMPLEXML_SAMPLEID_KEY);
        a.setSample(aNode.getNodeValue());
        aNode = att.getNamedItem(SAMPLEXML_OPERATOR_KEY);
        a.setOperator(aNode.getNodeValue());
      }
    } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<String, List<Analysis>> createBatchAnalysis(List<File> files) {
    throw new UnsupportedOperationException("Not supported Operation");
  }

}
