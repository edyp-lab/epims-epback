package cea.edyp.epims.transfer.dataformat.applied;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;

import junit.framework.TestCase;
import cea.edyp.epims.transfer.model.Analysis;
import org.slf4j.LoggerFactory;

public class WiffScanAnalysisTest extends TestCase {
	
	private static Logger logger = LoggerFactory.getLogger(WiffScanAnalysisTest.class);
	
	private WiffScanFactory factory;
	
	protected void setUp() throws Exception {
		factory = new WiffScanFactory();
	}
	
	public void testWiffScan() {
		URL resource = this.getClass().getResource("/TTOF_01248.wiff");
		String filename = resource.getFile();
		long start = System.currentTimeMillis();
		List<Analysis> list = factory.createAnalysis(new File(filename));
		assertNotNull(list);
		assertEquals(1, list.size());
		Analysis analysis = list.get(0);
		assertEquals("TTOF_01248", analysis.getName());
		assertEquals("1D 25cm", analysis.getDescription());
		assertNull(analysis.getOperator());
		assertEquals("controlelc",analysis.getSample().toLowerCase());
		logger.info("elapsed time = "+(System.currentTimeMillis() - start+" ms"));
	}

}
