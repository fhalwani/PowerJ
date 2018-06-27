package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class RendererBoolean extends JCheckBox implements TableCellRenderer {
	private static final long serialVersionUID = -1196649717665832029L;

	RendererBoolean() {
		super();
		setFont(Constants.APP_FONT);
		setOpaque(true);
		setHorizontalAlignment(JLabel.CENTER);
	}

    public Component getTableCellRendererComponent(
    		JTable table, Object value, boolean isSelected,
    		boolean hasFocus, int row, int column) {
    	if (value instanceof Boolean) {
    		setSelected((Boolean) value);
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
