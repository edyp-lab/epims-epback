package cea.edyp.epims.transfer.model;

import junit.framework.TestCase;

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
  }

}
