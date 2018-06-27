package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class RendererDouble extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = -3634027957214043016L;
	int noFractions = 0;
	private Numbers numbers;

    RendererDouble(PowerJ parent, int noFractions) {
		super();
		this.numbers = parent.numbers;
		this.noFractions = noFractions;
		setFont(Constants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
    }

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		try {
			if (value instanceof Double) {
				setText(numbers.formatDouble(noFractions, (Double) value));
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
