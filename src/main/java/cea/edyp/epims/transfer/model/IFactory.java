package cea.edyp.epims.transfer.model;

import java.io.File;
import java.util.List;


public interface IFactory {
	
	public Analysis[] getAnalyses(DataFormat format, List<File> files);
	
}
