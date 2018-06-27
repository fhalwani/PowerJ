package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class RendererString extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -788073455037650991L;

    RendererString() {
		super();
		setFont(Constants.APP_FONT);
		setOpaque(true);
    }

    public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		if (value instanceof String) {
	    	setText((String)value);
		} else if (value != null) {
	    	setText(value.toString());
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
