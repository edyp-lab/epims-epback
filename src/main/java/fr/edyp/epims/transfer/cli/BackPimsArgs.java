package fr.edyp.epims.transfer.cli;

import com.beust.jcommander.Parameter;

public class BackPimsArgs {

	@Parameter(names = "-f", description = "Chemin absolu ou relatif vers le fichier de l'analyse", required = true)
	private String filename;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
