package bg.smoc.model;

import java.math.BigDecimal;

import bg.smoc.model.web.ResultsRow;

public class UserContestData {
	private ResultsRow data;
	
	private BigDecimal totalPoints;

	public ResultsRow getData() {
		return data;
	}

	public void setData(ResultsRow data) {
		this.data = data;
	}

	public BigDecimal getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(BigDecimal totalPoints) {
		this.totalPoints = totalPoints;
	}
}
