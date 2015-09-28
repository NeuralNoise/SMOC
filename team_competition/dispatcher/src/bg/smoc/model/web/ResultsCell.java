package bg.smoc.model.web;

public class ResultsCell {
	private String value;
	private ResultsStyle style;

	public ResultsCell(String value) {
		this.value = value;
		this.style = new ResultsStyle();
	}

	public ResultsStyle getStyle() {
		return style;
	}

	public void setStyle(ResultsStyle style) {
		this.style = style;
	}

	public ResultsCell(String value, ResultsStyle style) {
		this.value = value;
		this.style = style;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
