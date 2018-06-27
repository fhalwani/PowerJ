package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class RendererDate extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = -8087396319271804554L;
	private DateUtils dateUtils;

	RendererDate(PowerJ parent) {
		super();
		this.dateUtils = parent.dateUtils;
		setFont(Constants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if( value instanceof Date) {
			setText(dateUtils.formatter((Date) value, dateUtils.FORMAT_DATE));
        } else {
			setText("-");
        }
		if (isSelected) {
            setBorder(Constants.borderEmpty);
			setBackground(Constants.COLOR_LIGHT_BLUE);
        } else {
            setBorder(null);
			setBackground(Constants.COLOR_EVEN_ODD[row % 2]);
        }
		return this;
	}
}
