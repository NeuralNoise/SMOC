package bg.smoc.model.web;

import java.util.Vector;

public class ResultsTable {
	
	private ResultsRow header;
	private Vector<ResultsRow> rows;

	public ResultsTable() {
		header = null;
		rows = new Vector<ResultsRow>();
	}

	public ResultsRow addRow() {
		ResultsRow row = new ResultsRow();
		rows.add(row);
		return row;
	}

	public Vector<ResultsRow> getRows() {
		return rows;
	}

	public ResultsRow addHeader() {
		header = new ResultsRow();
		return header;
	}

	public ResultsRow getHeader() {
		return header;
	}

	public void appendRow(ResultsRow row) {
		rows.add(row);
	}

	public void setRows(Vector<ResultsRow> newRows) {
		this.rows = newRows;
	}
}
