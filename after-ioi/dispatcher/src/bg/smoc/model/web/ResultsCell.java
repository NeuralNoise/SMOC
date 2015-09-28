package bg.smoc.model.web;

public class ResultsCell {
    private String value;
    private ResultsStyle style;
    private String href;

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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
