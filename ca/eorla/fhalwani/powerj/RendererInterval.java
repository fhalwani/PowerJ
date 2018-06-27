package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class RendererInterval extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 3768788637609187311L;
	private long millis = 0, seconds = 0, minutes = 0, hours = 0;
	private String interval = "";
	
	RendererInterval() {
		super();
		setFont(Constants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		interval = "";
		try {
			millis = (Long)value;
			if (millis < 28800000) {
				seconds = (millis / 1000) % 60;
				minutes = (millis / 60000) % 60;
				hours = millis / 3600000;
				if (hours > 0) {
					if (hours > 99) {
						hours = 99;
					}
					interval += hours + ":";
					if (minutes > 9) {
						interval += minutes + ":";
					} else {
						interval += "0" + minutes + ":";
					}
				} else if (minutes > 0) {
					interval += minutes + ":";
				}
				if (seconds > 9) {
					interval += seconds;
				} else if (hours > 0 || minutes > 0) {
					interval += "0" + seconds;
				} else {
					interval += seconds;
				}
			} else {
				// Next work shift (>8hours) marker
				interval = "--";
			}
			setText(interval);
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
