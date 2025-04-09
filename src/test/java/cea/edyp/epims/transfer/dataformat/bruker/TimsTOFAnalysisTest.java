package cea.edyp.epims.transfer.dataformat.bruker;

import cea.edyp.epims.transfer.model.Analysis;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;

public class TimsTOFAnalysisTest extends TestCase {
  private static final Logger logger = LoggerFactory.getLogger(TimsTOFAnalysisTest.class);

  private TimsTOFFactory tofFactory;

  protected void setUp() throws Exception {
    tofFactory = new TimsTOFFactory();
  }

  public void testReadAnalysis(){
    String filePath = "/FakeTimsTofData.d";
    URL resource = this.getClass().getResource(filePath);
    String filename = resource.getFile();
    File analysisFile = new File(filename);
    long start = System.currentTimeMillis();
    List<Analysis> list = tofFactory.createAnalysis(analysisFile);
    assertNotNull(list);
    assertEquals(1, list.size());
    Analysis analysis = list.get(0);
    assertEquals("FakeTimsTofData", analysis.getName());
    assertEquals("TTPro 00256",analysis.getOperator());
    assertEquals("2022002-1_eColi-10ng-30min",analysis.getSample());
    logger.info("elapsed time = "+(System.currentTimeMillis() - start+" ms"));
  }

}
