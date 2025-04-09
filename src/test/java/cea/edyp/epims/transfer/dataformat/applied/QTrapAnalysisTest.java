package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;

import junit.framework.TestCase;
import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

public class QTrapAnalysisTest extends TestCase {
	
	private static Logger logger = LoggerFactory.getLogger(QTrapAnalysisTest.class);
	private QTrapFactory factory;
	
	protected void setUp() throws Exception {
		factory = new QTrapFactory();
	}
	
	public void testWiffScan() {
		URL resource = this.getClass().getResource("/QT6872.wiff");
		String filename = resource.getFile();
		long start = System.currentTimeMillis();
		List<Analysis> list = factory.createAnalysis(new File(filename));
		assertNotNull(list);
		assertEquals(1, list.size());
		Analysis analysis = list.get(0);
		assertEquals("QT6872", analysis.getName());
		assertEquals("CytCGFP", analysis.getDescription());
		assertEquals("adrait",analysis.getOperator());
		assertEquals("controlelc",analysis.getSample().toLowerCase());		
		logger.info("elapsed time = "+(System.currentTimeMillis() - start+" ms"));
	}

}
