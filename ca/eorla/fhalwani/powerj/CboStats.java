package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class CboStats extends JComboBox {
	private static final long serialVersionUID = -6144885642388099790L;
	private final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
	private final ViewStats colsView;
	private final JPopupMenu popupMenu = new JPopupMenu();

	CboStats(boolean[] value, PowerJ parent) {
		final int arrLength = value.length;
		final String[] cNames = {"CA", "SP", "BL", "SL", "HE", "SS",
				"IH", "MO", "FS", "FB", "FL", "SN", "GR", "HI", "RE", "TO"};
		// Make sure display is wide enough
		boolean[] selected = new boolean[arrLength];
		for (byte i = 0; i < arrLength; i++) {
			selected[i] = value[i];
		}
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		colsView = new ViewStats(selected, parent);
		comboModel.addElement(selected);
		setModel(comboModel);
		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 33771457556963313L;
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				boolean[] results = (boolean[]) comboModel.getElementAt(0);
				String display = "";
				for (int i = 0; i < arrLength; i++) {
					if (results[i]) {
						if (i < 6) {
							display += cNames[i] + ":";
							break;
						}
					}
				}
				if (display.length() > 2) {
					display = display.substring(1);
				}
				setText(display);
				return this;
			}
		});
		popupMenu.add(colsView);
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				final boolean popupShown = popupMenu.isShowing();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hidePopup();
						if (popupShown) {
							popupMenu.setVisible(false);
						} else {
							colsView.setValue(getValue());
							popupMenu.show(CboStats.this, 0, getHeight());
						}
					}
				});
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {}
			public void popupMenuCanceled(PopupMenuEvent pme) {}
		});
		colsView.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Confirm".equals(property)) {
					popupMenu.setVisible(false);
				}
			}
		});
		colsView.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Value".equals(property)) {
					setValue((boolean[]) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
				}
			}
		});
	}

	public boolean[] getValue() {
		return (boolean[]) comboModel.getElementAt(0);
	}

	public void setValue(boolean[] value) {
		boolean[] selected = new boolean[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		setSelectedItem(selected);
		comboModel.removeAllElements();
		comboModel.addElement(selected);
		if (!colsView.getValue().equals(selected)) {
			colsView.setValue(selected);
		}
	}
}
