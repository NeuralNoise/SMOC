package bg.smoc.model.web;

import java.util.Vector;

public class ResultsRow {

	private Vector<ResultsCell> cells;

	public ResultsRow() {
		cells = new Vector<ResultsCell>();
	}

	public void addCell(String cellValue) {
		cells.add(new ResultsCell(cellValue));
	}

	public void addCell(ResultsCell cell) {
		cells.add(cell);
	}

	public void addCell(String cellValue, ResultsStyle style) {
		cells.add(new ResultsCell(cellValue, style));
	}

	public Vector<ResultsCell> getCells() {
		return cells;
	}

	public String getId() {
		return cells.get(0).getValue();
	}

	public void append(ResultsRow data) {
		this.cells.addAll(data.getCells());
	}
}
