package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class RendererItem extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 9133214192277559743L;

	RendererItem() {
		super();
		setFont(Constants.APP_FONT);
		setOpaque(true);
		setToolTipText("Click to select from a list");
	}

    public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
    	setText(value.toString());
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
