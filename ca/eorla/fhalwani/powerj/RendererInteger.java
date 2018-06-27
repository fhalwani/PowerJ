package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class RendererInteger extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 3768788637609187311L;
	private Numbers numbers;

	RendererInteger(PowerJ parent) {
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
			setBackground(Constants.COLOR_LIGHT_BLUE);
        } else {
            setBorder(null);
			setBackground(Constants.COLOR_EVEN_ODD[row % 2]);
        }
		return this;
	}
}
