package fr.edyp.epims.transfer.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fr.edyp.epims.transfer.model.AbstractAnalysis;
import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;
import fr.edyp.epims.transfer.model.BackupParameters;
import fr.edyp.epims.transfer.model.DataFormat;
import fr.edyp.epims.transfer.model.IFileTransferManager;
import fr.edyp.epims.transfer.model.InstrumentConfiguration;
import fr.edyp.epims.transfer.util.IEPSystemDataProvider;
import junit.framework.TestCase;

public class BackPimsCLITest extends TestCase {

	private File tempFile;
	private File tempDir;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tempDir = new File(System.getProperty("java.io.tmpdir"), "backpims_test_" + System.currentTimeMillis());
		tempDir.mkdirs();
		tempFile = new File(tempDir, "Sample_01.raw");
		tempFile.createNewFile();
	}

	@Override
	protected void tearDown() throws Exception {
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}
		if (tempDir != null && tempDir.exists()) {
			tempDir.delete();
		}
		super.tearDown();
	}

	public void testParseAnalysisParam() throws Exception {
		assertEquals("MyAnalysis.raw", BackPimsCLI.parseAnalysisParam(new String[]{"-f", "MyAnalysis.raw"}));
		assertEquals("C:/path/MyAnalysis.raw", BackPimsCLI.parseAnalysisParam(new String[]{"-f", "C:/path/MyAnalysis.raw"}));

		try {
			BackPimsCLI.parseAnalysisParam(new String[]{"MyAnalysis"});
			fail("Should throw BackupException when -f option is missing");
		} catch (BackupException e) {
			// Expected
		}

		try {
			BackPimsCLI.parseAnalysisParam(new String[]{"-a", "MyAnalysis.raw"});
			fail("Should throw BackupException for unsupported -a option");
		} catch (BackupException e) {
			// Expected
		}

		try {
			BackPimsCLI.parseAnalysisParam(new String[]{});
			fail("Should throw BackupException for empty args");
		} catch (BackupException e) {
			// Expected
		}

		try {
			BackPimsCLI.parseAnalysisParam(null);
			fail("Should throw BackupException for null args");
		} catch (BackupException e) {
			// Expected
		}

		try {
			BackPimsCLI.parseAnalysisParam(new String[]{"-f", "   "});
			fail("Should throw BackupException for blank arg");
		} catch (BackupException e) {
			// Expected
		}
	}

	public void testFindPath() {
		File sourceDir = new File("C:/test_source");
		File absFile = new File("C:/other_dir/test.raw");
		assertEquals(absFile.getAbsolutePath(), BackPimsCLI.findPath(absFile.getAbsolutePath(), sourceDir).getAbsolutePath());

		File relResult = BackPimsCLI.findPath("test.raw", sourceDir);
		assertEquals(new File(sourceDir, "test.raw").getAbsolutePath(), relResult.getAbsolutePath());

		File noSourceResult = BackPimsCLI.findPath("test.raw", null);
		assertEquals(new File("test.raw").getAbsolutePath(), noSourceResult.getAbsolutePath());

		assertNull(BackPimsCLI.findPath(null, sourceDir));
		assertNull(BackPimsCLI.findPath("   ", sourceDir));
	}

	public void testValidateSingleInstrumentConfig() throws Exception {
		// Test empty config
		Map<String, InstrumentConfiguration> emptyMap = new HashMap<>();
		try {
			BackPimsCLI.validateSingleInstrumentConfig(emptyMap);
			fail("Should throw BackupException for empty config");
		} catch (BackupException e) {
			// Expected
		}

		// Test multiple instruments
		Map<String, InstrumentConfiguration> multiMap = new HashMap<>();
		String instrSourcePath  = System.getProperty("java.io.tmpdir")+"/backpims_test";
		multiMap.put("Inst1", new InstrumentConfiguration("Inst1", "Inst1", "LTQ Thermo", false, 0, instrSourcePath));
		multiMap.put("Inst2", new InstrumentConfiguration("Inst2", "Inst2", "NEMS", false, 0, instrSourcePath));
		try {
			BackPimsCLI.validateSingleInstrumentConfig(multiMap);
			fail("Should throw BackupException for multiple instruments config");
		} catch (BackupException e) {
			assertTrue(e.getMessage().contains("multiples instruments"));
		}

		// Test single instrument
		Map<String, InstrumentConfiguration> singleMap = new HashMap<>();
		InstrumentConfiguration singleInst = new InstrumentConfiguration("SingleInst", "SingleInst", "LTQ Thermo", false, 0, instrSourcePath);
		singleMap.put("SingleInst", singleInst);
		InstrumentConfiguration result = BackPimsCLI.validateSingleInstrumentConfig(singleMap);
		assertNotNull(result);
		assertEquals("SingleInst", result.getLabel());
	}

	public void testGetStatusDescription() {
		assertEquals("OK (non sauvegardée)", BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_OK_MASK));
		assertEquals("Inaccessible / Fichier illisible", BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_STATUS_NOT_READABLE));
		assertEquals("Inconnu", BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_STATUS_UNKNOWN));

		String existDesc = BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_EXIST_MASK);
		assertTrue(existDesc.contains("Acquisition déjà existante"));

		String closedDesc = BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_STUDY_CLOSED_MASK);
		assertTrue(closedDesc.contains("Etude close"));

		String invalidSplDesc = BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_INVALID_SAMPLE_MASK);
		assertTrue(invalidSplDesc.contains("Echantillon invalide"));

		String combinedDesc = BackPimsCLI.getStatusDescription(Analysis.ANALYSIS_EXIST_MASK | Analysis.ANALYSIS_STUDY_CLOSED_MASK);
		assertTrue(combinedDesc.contains("Acquisition déjà existante"));
		assertTrue(combinedDesc.contains("Etude close"));
	}

	public void testTransferAnalysisValid() throws Exception {
		TestAnalysis mockAnalysis = new TestAnalysis("Sample_01", Analysis.ANALYSIS_OK_MASK, tempFile);
		TestDataProvider testDataProvider = new TestDataProvider(mockAnalysis, Analysis.ANALYSIS_OK_MASK, "C:/epims_dest/Sample_01");
		BackupParameters params = createTestParameters(mockAnalysis, testDataProvider);

		boolean transferred = BackPimsCLI.transferAnalysis(tempFile.getAbsolutePath(), params);
		assertTrue("Transfer should succeed when analysis is OK and not already existing", transferred);
		assertTrue("Transfer manager copyOnly should be called", testDataProvider.transferCalled);
	}

	public void testTransferAnalysisAlreadyExists() throws Exception {
		TestAnalysis mockAnalysis = new TestAnalysis("Sample_01", Analysis.ANALYSIS_EXIST_MASK, tempFile);
		TestDataProvider testDataProvider = new TestDataProvider(mockAnalysis, Analysis.ANALYSIS_EXIST_MASK, "C:/epims_dest/Sample_01");
		BackupParameters params = createTestParameters(mockAnalysis, testDataProvider);

		boolean transferred = BackPimsCLI.transferAnalysis(tempFile.getAbsolutePath(), params);
		assertFalse("Transfer should NOT occur when analysis already exists in ePims", transferred);
		assertFalse("Transfer manager should not be called", testDataProvider.transferCalled);
	}

	public void testTransferAnalysisInvalidSample() throws Exception {
		TestAnalysis mockAnalysis = new TestAnalysis("Sample_01", Analysis.ANALYSIS_INVALID_SAMPLE_MASK, tempFile);
		TestDataProvider testDataProvider = new TestDataProvider(mockAnalysis, Analysis.ANALYSIS_INVALID_SAMPLE_MASK, "C:/epims_dest/Sample_01");
		BackupParameters params = createTestParameters(mockAnalysis, testDataProvider);

		boolean transferred = BackPimsCLI.transferAnalysis(tempFile.getAbsolutePath(), params);
		assertFalse("Transfer should NOT occur when analysis has invalid sample", transferred);
		assertFalse("Transfer manager should not be called", testDataProvider.transferCalled);
	}

	public void testTransferAnalysisStudyClosed() throws Exception {
		TestAnalysis mockAnalysis = new TestAnalysis("Sample_01", Analysis.ANALYSIS_STUDY_CLOSED_MASK, tempFile);
		TestDataProvider testDataProvider = new TestDataProvider(mockAnalysis, Analysis.ANALYSIS_STUDY_CLOSED_MASK, "C:/epims_dest/Sample_01");
		BackupParameters params = createTestParameters(mockAnalysis, testDataProvider);

		boolean transferred = BackPimsCLI.transferAnalysis(tempFile.getAbsolutePath(), params);
		assertFalse("Transfer should NOT occur when study is closed", transferred);
		assertFalse("Transfer manager should not be called", testDataProvider.transferCalled);
	}

	public void testTransferAnalysisInvalidDestination() throws Exception {
		TestAnalysis mockAnalysis = new TestAnalysis("Sample_01", Analysis.ANALYSIS_OK_MASK, tempFile);
		TestDataProvider testDataProvider = new TestDataProvider(mockAnalysis, Analysis.ANALYSIS_OK_MASK, null);
		BackupParameters params = createTestParameters(mockAnalysis, testDataProvider);

		boolean transferred = BackPimsCLI.transferAnalysis(tempFile.getAbsolutePath(), params);
		assertFalse("Transfer should NOT occur when destination cannot be determined", transferred);
		assertFalse("Transfer manager should not be called", testDataProvider.transferCalled);
	}

	public void testTransferAnalysisNotFound() throws Exception {
		TestAnalysis mockAnalysis = new TestAnalysis("Sample_01", Analysis.ANALYSIS_OK_MASK, tempFile);
		TestDataProvider testDataProvider = new TestDataProvider(mockAnalysis, Analysis.ANALYSIS_OK_MASK, "C:/epims_dest/Sample_01");
		BackupParameters params = createTestParameters(mockAnalysis, testDataProvider);

		try {
			BackPimsCLI.transferAnalysis("NonExistentAnalysis_random_12345.raw", params);
			fail("Should throw BackupException when analysis is not found");
		} catch (BackupException e) {
			assertTrue(e.getMessage().contains("non trouvée"));
		}
	}

	private BackupParameters createTestParameters(TestAnalysis analysis, TestDataProvider dataProvider) throws Exception {
		File sourceTempDir = new File(System.getProperty("java.io.tmpdir"), "instr_source" );
		InstrumentConfiguration config = new InstrumentConfiguration("TestInst", "TestInst", "LTQ Thermo", false, BackupParameters.TRANSFER_COPY_MODE, sourceTempDir.getAbsolutePath());
		BackupParameters params = new BackupParameters(dataProvider);
		params.setInstrumentConfiguration(config);

		TestDataFormat dataFormat = new TestDataFormat(analysis, dataProvider);
		params.setDataFormat(dataFormat);
		params.setSourcePath(tempDir);
		return params;
	}

	static class TestAnalysis extends AbstractAnalysis {
		public TestAnalysis(String name, int status, File file) {
			this.name = name;
			this.status = status;
			this.sample = "Sample_01";
			this.analyseType = AnalysisType.RESEARCH;
			this.analysisFile = file;
		}

		@Override
		public String getFileName() {
			return analysisFile.getName();
		}

		@Override
		public File getFileToTransfer() {
			return analysisFile;
		}

		@Override
		public File getSourceFile() {
			return analysisFile;
		}

		@Override
		public FileFilter getContentFilter() {
			return null;
		}

		@Override
		public File[] getAssociatedFiles() {
			return new File[0];
		}

		@Override
		public String getAssociatedFileType(File associatedFile) {
			return null;
		}

		@Override
		public void setDataFormat(DataFormat format) {
		}
	}

	static class TestDataFormat implements DataFormat {
		private final Analysis analysis;
		private final TestTransferManager transferManager;

		public TestDataFormat(Analysis analysis, TestDataProvider dataProvider) {
			this.analysis = analysis;
			this.transferManager = new TestTransferManager(dataProvider);
		}

		@Override
		public Analysis[] getAnalysis(File dir) {
			return new Analysis[]{analysis};
		}

		@Override
		public Analysis[] createAnalyses(File file) {
			return new Analysis[]{analysis};
		}

		@Override
		public IFileTransferManager getFileTransfertManager() {
			return transferManager;
		}

		@Override
		public int getPropertyCount() {
			return 0;
		}

		@Override
		public Class getPropertyClass(int propertyIdx) {
			return String.class;
		}

		@Override
		public String getPropertyLabel(int propertyIdx) {
			return "";
		}

		@Override
		public Object getProperty(int propertyIdx, Analysis analysis) {
			return null;
		}

		@Override
		public javax.swing.JComponent getConfigurator() {
			return null;
		}

		@Override
		public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		}

		@Override
		public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		}

		@Override
		public void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		}

		@Override
		public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		}
	}

	static class TestTransferManager implements IFileTransferManager {
		private final TestDataProvider dataProvider;

		public TestTransferManager(TestDataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public void copyOnly(Analysis a, IEPSystemDataProvider dataProvider) throws BackupException {
			this.dataProvider.transferCalled = true;
		}

		@Override
		public void move(Analysis a, IEPSystemDataProvider dataProvider) throws BackupException {
			this.dataProvider.transferCalled = true;
		}

		@Override
		public void clean(Analysis a) throws BackupException {
		}
	}

	static class TestDataProvider implements IEPSystemDataProvider {
		private final Analysis analysis;
		private final int statusToReturn;
		private final String destToReturn;
		public boolean transferCalled = false;

		public TestDataProvider(Analysis analysis, int statusToReturn, String destToReturn) {
			this.analysis = analysis;
			this.statusToReturn = statusToReturn;
			this.destToReturn = destToReturn;
		}

		@Override
		public String getPimsRootPath() {
			return "C:/epims_root";
		}

		@Override
		public Boolean isPimsRootLocal() {
			return true;
		}

		@Override
		public String getPimsSystemRelativePath() {
			return "system";
		}

		@Override
		public String getStudyNameFor(String sampleName) {
			return "Study1";
		}

		@Override
		public boolean isSampleExist(String sampleName) {
			return (statusToReturn & Analysis.ANALYSIS_INVALID_SAMPLE_MASK) != Analysis.ANALYSIS_INVALID_SAMPLE_MASK;
		}

		@Override
		public boolean isAcquisitionExist(String acqName, String instrumentName) {
			return (statusToReturn & Analysis.ANALYSIS_EXIST_MASK) == Analysis.ANALYSIS_EXIST_MASK;
		}

		@Override
		public boolean isSpectrometerDefined(String instrumentName) {
			return true;
		}

		@Override
		public void createAcquisitionAndFilesFor(Analysis a, String instrumentName) throws BackupException {
		}

		@Override
		public File getAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
			return new File("Others");
		}

		@Override
		public String getRelativeAssociatedFileDestinationDir(Analysis a, File f, String fileType) throws BackupException {
			return "Others";
		}

		@Override
		public String getDestinationDir(Analysis a, BackupParameters param) throws BackupException {
			if (destToReturn == null) {
				throw new BackupException("Destination invalide");
			}
			a.setDestination(destToReturn);
			a.setRelativeDestination("Study1");
			return destToReturn;
		}

		@Override
		public int getAnalysisStatus(Analysis analysis, BackupParameters params) {
			analysis.setStatus(statusToReturn);
			return statusToReturn;
		}
	}
}
