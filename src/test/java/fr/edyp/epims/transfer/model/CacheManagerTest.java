package fr.edyp.epims.transfer.model;

import fr.edyp.epims.transfer.dataformat.applied.WiffScanAnalysis;
import junit.framework.TestCase;
import org.apache.commons.io.FilenameUtils;

public class CacheManagerTest extends TestCase  {
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testReadConfig(){
    CacheManager.getInstance().setupForConfiguration("Exploris2");
    Analysis[] all = CacheManager.getInstance().getAnalysis("C:\\Temp\\Vero\\epims_instrum_data\\hf1\\HF1_023192.raw");
    assertEquals(all.length, 1);
    assertEquals(all[0].getName(), "HF1_023192");
    assertEquals(all[0].getDuration(), 0.0f);
    assertEquals(all[0].getOperator(), "hesse");


    CacheManager.getInstance().setupForConfiguration("Nems");
    all = CacheManager.getInstance().getAnalysis("C:\\Temp\\Vero\\epims_instrum_data\\NEMS\\Data_2023\\03-08-2021");
    assertEquals(all.length, 1);
    assertEquals(all[0].getName(), "MATV_AB_03-08-2021");
    assertEquals(all[0].getDuration(), 0.0f);
    assertEquals(all[0].getOperator(), "masselon");

    CacheManager.getInstance().setupForConfiguration("TripleTof");
    all = CacheManager.getInstance().getAnalysis("C:\\Temp\\Vero\\epims_instrum_data\\ttof\\DATA\\TTOF_01479.wiff");
    assertEquals(1,all.length);
    assertEquals( "TTOF_01479", all[0].getName());
    assertEquals("NCS1 DCMSlink", all[0].getDescription() );
    assertEquals(1,all[0].getStatus());
    assertEquals("TTOF_01479.wiff.zip", ((WiffScanAnalysis)all[0]).getZipFilename());
//    assertEquals("TTOF_01479.wiff.zip", all[0].getFileToTransfer().getName()); Only locally !

  }

}
