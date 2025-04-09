/*
 * Created on 27 mars 2006
 *
 * $Id: RawInfoTest_.java,v 1.1 2007-09-14 09:37:51 dupierris Exp $
 */
package cea.edyp.epims.transfer.util;

import java.io.File;
import java.util.Objects;

import junit.framework.TestCase;
import cea.edyp.epims.transfer.dataformat.thermo.LTQFactory;
import cea.edyp.epims.transfer.dataformat.waters.MLAnalysis;
import cea.edyp.epims.transfer.dataformat.waters.MLFormat;
import cea.edyp.epims.transfer.dataformat.waters.MLRawInfo;
import cea.edyp.epims.transfer.model.Analysis;

/**
 * RawInfoTest_
 *
 * @author vdupierr
 */
public class RawInfoTest extends TestCase {

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testGetDuration(){
    File raw = new File(Objects.requireNonNull(this.getClass().getResource("/UF0856.raw")).getFile());

    MLFormat format = new MLFormat();
    MLAnalysis analysis = new MLAnalysis(raw, format);
    double duration = MLRawInfo.getAnalysisDuration(analysis);
//    System.out.println(" duration "+duration);
    assertEquals(59.97747, duration, 0.00001f);
  } 
  
  public void testGetInfusionDuration(){
    File raw = new File(Objects.requireNonNull(this.getClass().getResource("/GFPInfusion.raw")).getFile());
    MLFormat format = new MLFormat();
    MLAnalysis analysis = new MLAnalysis(raw, format);
    double duration = MLRawInfo.getAnalysisDuration(analysis);
//    System.out.println(" duration "+duration);
    assertEquals(3.10677 , duration, 0.00001f);
  }

  //Skip test for Jenkins (under linux) : LTQ use ThermoAccess dll
  public void _testReadThermoRaw(){
    File raw = new File(Objects.requireNonNull(this.getClass().getResource("/CAVEN10053.raw")).getFile());
    LTQFactory factory = new LTQFactory();
    Analysis analysis = factory.createAnalysis(raw).get(0);

//    float duration = analysis.getDuration();
//    System.out.println(" duration "+duration);
    assertEquals("HCD sur 951.5", analysis.getDescription());
  }
}
