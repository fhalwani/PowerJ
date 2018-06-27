package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class RendererDateTime extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 8213071360309130325L;
	private DateUtils dateUtils;

	RendererDateTime(PowerJ parent) {
		super();
		this.dateUtils = parent.dateUtils;
		setFont(Constants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if( value instanceof Calendar) {
			setText(dateUtils.formatter((Calendar) value, dateUtils.FORMAT_DATETIME));
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
