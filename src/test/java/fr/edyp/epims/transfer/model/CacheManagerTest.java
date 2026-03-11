/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
