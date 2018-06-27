package ca.eorla.fhalwani.powerj;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class RendererColor extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 399862827207151225L;
	private Numbers numbers;

	RendererColor(PowerJ parent) {
		super();
		this.numbers = parent.numbers;
		setFont(Constants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		try {
			if (value instanceof Integer) {
				setText(numbers.formatNumber((Integer) value));
			} else if (value instanceof Short) {
				setText(numbers.formatNumber((Short) value));
			} else if (value instanceof Byte) {
				setText(numbers.formatNumber((Byte) value));
			} else if (value instanceof Long) {
				setText(numbers.formatNumber((Long) value));
			} else {
				setText(value.toString());
			}
		} catch (IllegalArgumentException ignore) {}
		if (isSelected) {
            setBorder(Constants.borderEmpty);
        } else {
            setBorder(null);
        }
		if (value instanceof Short) {
	        Color bg = ((Short)value > 100 ? Color.RED : 
	        	(Short)value > 70 ? Constants.AMBER : Color.GREEN);
			setBackground(bg);
		}
		return this;
	}
}
