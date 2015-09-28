package bg.smoc.model.web;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ResultsStyle {
	/*
	 * style - int 32bits
	 * 
	 * +--------------+-----------+--------------------+---+---+---+
	 * |....unused....|.alignment.|.....font-size......|.B.|.I.|.U.|
	 * |..............|....2b.....|.........5b.........|.1b|.1b|.1b|
	 * +--------------+-----------+--------------------+---+---+---+
	 * |.default:.....|....00.....|.........10.........|.0.|.0.|.0.|
	 * |...00-center..|...0-32....|....................|...|...|...|
	 * |...10-left....|...........|....................|...|...|...|
	 * |...01-right...|...........|....................|...|...|...|
	 * |...11-justify.|...........|....................|...|...|...|
	 * +--------------+-----------+--------------------+---+---+---+
	 * 
	 */

	// constants
	public final static int ALIGNMENT_CENTRE = 0x00;
	public final static int ALIGNMENT_LEFT = 0x10;
	public final static int ALIGNMENT_RIGHT = 0x01;
	public final static int ALIGNMENT_JUSTIFY = 0x11;

	// masks
	private final static int ALIGNENT_MASK = 0x300;
	private final static int FONT_SIZE_MASK = 0x0F8;
	private final static int IS_BOLD_MASK = 0x004;
	private final static int IS_ITALIC_MASK = 0x002;
	private final static int IS_UNDERLINED_MASK = 0x001;

	private int style;

	// set a default style
	public ResultsStyle() {
		this.style = 0;
		this.setFontSize(10);
	}

	public ResultsStyle(int fontSize) {
		this.style = 0;
		this.setFontSize(fontSize);
	}

	public ResultsStyle(ResultsStyle style) {
		this.style = style.style;
	}

	public void setStyle(ResultsStyle style) {
		this.style = style.style;
	}

	public void setAlignment(int alignentCode) {
		applyToStyle(alignentCode, ALIGNENT_MASK);
	}

	public int getAlignment() {
		return getFromStyle(ALIGNENT_MASK);
	}

	public void setFontSize(int fontSize) {
		applyToStyle(fontSize, FONT_SIZE_MASK);
	}

	public int getFontSize() {
		return getFromStyle(FONT_SIZE_MASK);
	}

	public boolean isBold() {
		return getFromStyle(IS_BOLD_MASK) == 1;
	}

	public boolean isItalic() {
		return getFromStyle(IS_ITALIC_MASK) == 1;
	}

	public boolean isUnderlined() {
		return getFromStyle(IS_UNDERLINED_MASK) == 1;
	}

	public void setBold(boolean value) {
		applyToStyle(value ? 1 : 0, IS_BOLD_MASK);
	}

	public void setItalic(boolean value) {
		applyToStyle(value ? 1 : 0, IS_BOLD_MASK);
	}

	public void setUnderlined(boolean value) {
		applyToStyle(value ? 1 : 0, IS_UNDERLINED_MASK);
	}

	// converters
	public String getStyleHTML() {
		String align = "centre";
		switch (this.getAlignment()) {
		case ALIGNMENT_JUSTIFY:
			align = "justify";
			break;
		case ALIGNMENT_LEFT:
			align = "left";
			break;
		case ALIGNMENT_RIGHT:
			align = "right";
			break;
		}
		return this.style
				+ "font-size:"
				+ this.getFontSize()
				+ "pt;"
				+ (this.isBold() ? "font-weight: bold;" : "")
				+ (this.isItalic() ? "font-style:italic;" : "")
				+ (this.isUnderlined() ? "text-decoration: underline;" : "")
				+ "text-align: "
				+ align
				+ ";";
	}

	public HSSFCellStyle getStyleHSSF(HSSFWorkbook wb) {
		HSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) this.getFontSize());
		font.setFontName("Courier New");

		if (this.isBold())
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		if (this.isItalic())
			font.setItalic(true);
		if (this.isUnderlined())
			font.setUnderline(HSSFFont.U_SINGLE);

		HSSFCellStyle style = wb.createCellStyle();
		style.setFont(font);

		switch (this.getAlignment()) {
		case ALIGNMENT_CENTRE:
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			break;
		case ALIGNMENT_JUSTIFY:
			style.setAlignment(HSSFCellStyle.ALIGN_JUSTIFY);
			break;
		case ALIGNMENT_LEFT:
			style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			break;
		case ALIGNMENT_RIGHT:
			style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			break;
		}

		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);

		return style;
	}

	// utils
	private int getLowestBit(int i) {
		return ((~i) + 1) & i;
	}

	private void applyToStyle(int value, int mask) {
		this.style = (this.style & (~mask)) ^ ((value * getLowestBit(mask)) & mask);
	}

	private int getFromStyle(int mask) {
		return (this.style & mask) / getLowestBit(mask);
	}
}
