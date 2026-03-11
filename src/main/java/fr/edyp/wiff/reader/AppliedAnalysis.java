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

public class AppliedAnalysis {

	private String analysisName;
	private String sampleName;
	private String operator;
	private String submitter;
	private String description;

	public AppliedAnalysis(String analysisName, String sampleName) {
		super();
		this.analysisName = analysisName;
		this.sampleName = sampleName;
	}

	public AppliedAnalysis(String analysisName, String sampleName,
			String operator, String description) {
		this(analysisName, sampleName);
		this.operator = operator;
		this.description = description;
	}

	public String getAnalysisName() {
		return analysisName;
	}

	public String getSampleName() {
		return sampleName;
	}

	public String getOperator() {
		return operator;
	}

	public String getSubmitter() {
		return submitter;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
