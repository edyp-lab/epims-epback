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
package fr.edyp.wiff.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.poifs.dev.POIFSViewer;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


public class MainPOIFS {


	// private static String DAB = "C:/Documents and Settings/christophe/Mes
	// documents/Sciences/spectrometry/Data/WiffFiles/Test Batch.dab";

	// WARNING Use test code instead: NOK with test file TTOF_01248...
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File file = new File(args[0]);
		try {
			FileInputStream inputStream = new FileInputStream(file);
			POIFSFileSystem POIfs = new POIFSFileSystem(inputStream);

			POIFSViewer.main(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		WiffFile wiffFile = new WiffFile(args[0]);
		wiffFile.openFS();
		
		int count = wiffFile.getSamplesCount();
		System.out.println("nb samples = " + count);
		for (int k = 1; k <= count; k++) {
			AppliedAnalysis analysis = wiffFile.getAnalysis(k);
			System.out.println("  - operateur : " + analysis.getOperator());
			System.out.println("  - submitter : "+analysis.getSubmitter());
			System.out.println("Sample #"+k+" : " + analysis.getAnalysisName()+" - "+analysis.getSampleName());
		}
		wiffFile.closeFS();
	}

}
