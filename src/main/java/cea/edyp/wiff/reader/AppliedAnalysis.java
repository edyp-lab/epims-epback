package cea.edyp.wiff.reader;

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
