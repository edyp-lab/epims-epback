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
package fr.edyp.epims.transfer.gui;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.DataFormat;

public class DecoratedAnalysis implements Analysis {
	
	private final Analysis analysis;
	private Boolean isSelected;
	private String displayedDestinationPath;
	private int row;
	

	public DecoratedAnalysis(Analysis analysis, int row) {
		this.analysis = analysis;
		this.row = row;
	}
	
	
	public int getRow() {
		return row;
	}
	
	void setRow(int row) {
		this.row = row;
	}
	
	public Analysis getAnalysis() {
		return analysis;
	}


	public Boolean isSelected() {
		return isSelected;
	}

	public void setSelected(Boolean isSelected) {
		this.isSelected = isSelected;
	}


	public String getDisplayedDestinationPath() {
		return displayedDestinationPath;
	}


	public void setDisplayedDestinationPath(String displayedDestinationPath) {
		this.displayedDestinationPath = displayedDestinationPath;
	}


	public long getEstimatedSize() {
		return analysis.getEstimatedSize();
	}

	public void setEstimatedSize(long size) {
		analysis.setEstimatedSize(size);
	}

	public String getName() {
		return analysis.getName();
	}

	public void setName(String name) {
		analysis.setName(name);
	}

	public String getSample() {
		return analysis.getSample();
	}

	public void setSample(String sample) {
		analysis.setSample(sample);
	}

	public File getSourceFile() {
		return analysis.getSourceFile();
	}

	@Override
	public File getFileToTransfer() {
		return analysis.getFileToTransfer();
	}

	@Override
	public boolean isTransferFileTempo() {
		return analysis.isTransferFileTempo();
	}


	public String getFileName() {
		return analysis.getFileName();
	}

	public String getDestination() {
		return analysis.getDestination();
	}

	public void setDestination(String destinationDir) {
		analysis.setDestination(destinationDir);
	}

	public String getRelativeDestination() {
		return analysis.getRelativeDestination();
	}
	public void setRelativeDestination(String destinationDir) {
		analysis.setRelativeDestination(destinationDir);
	}

	public FileFilter getContentFilter() {
		return analysis.getContentFilter();
	}

	public AnalysisType getType() {
		return analysis.getType();
	}

	public void setType(AnalysisType type) {
		analysis.setType(type);
	}

	public int getStatus() {
		return analysis.getStatus();
	}

	public void setStatus(int status) {
		analysis.setStatus(status);
	}

	public File[] getAssociatedFiles() {
		return analysis.getAssociatedFiles();
	}

	public String getAssociatedFileType(File associatedFile) {
		return analysis.getAssociatedFileType(associatedFile);
	}

	public Date getDate() {
		return analysis.getDate();
	}

	public void setDate(Date date) {
		analysis.setDate(date);
	}

	public String getOperator() {
		return analysis.getOperator();
	}

	public void setOperator(String operator) {
		analysis.setOperator(operator);
	}

	public String getDescription() {
		return analysis.getDescription();
	}

	public void setDescription(String desc) {
		analysis.setDescription(desc);
	}

	public Float getDuration() {
		return analysis.getDuration();
	}

	public void setDuration(Float duration) {
		analysis.setDuration(duration);
	}

	public void setDataFormat(DataFormat dataFormat) {
		analysis.setDataFormat(dataFormat);
	}

	public String toString() {
		return analysis.toString();
	}
}
