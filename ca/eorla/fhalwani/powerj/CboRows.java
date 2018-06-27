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

class CboRows extends JComboBox {
	private static final long serialVersionUID = -3382989927218642203L;
	private final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
	private final ViewRows viewRows;
	private final JPopupMenu popupMenu = new JPopupMenu();

	public CboRows(int[] value) {
		int[] selected = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		viewRows = new ViewRows(selected);
		comboModel.addElement(selected);
		setModel(comboModel);
		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -3850983808453689124L;
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				int[] results = (int[]) comboModel.getElementAt(0);
				String display = "";
				for (byte i = 0; i < results.length; i++) {
					switch (results[i]) {
					case Constants.ROW_FACILITY:
						display += ":FAC";
						break;
					case Constants.ROW_SPECIALTY:
						display += ":SPE";
						break;
					case Constants.ROW_SUBSPECIALTY:
						display += ":SUB";
						break;
					case Constants.ROW_STAFF:
						display += ":STA";
						break;
					case Constants.ROW_PROCEDURE:
						display += ":PRO";
						break;
					default:
						// 0 = ignore
					}
					if (display.length() > 2) {
						setText(display.substring(1));
					}
				}
				return this;
			}
		});
		popupMenu.add(viewRows);
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				final boolean popupShown = popupMenu.isShowing();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hidePopup();
						if (popupShown) {
							popupMenu.setVisible(false);
						} else {
							viewRows.setValue(getValue());
							popupMenu.show(CboRows.this, 0, getHeight());
						}
					}
				});
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {}
			public void popupMenuCanceled(PopupMenuEvent pme) {}
		});
		viewRows.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Confirm".equals(property)) {
					popupMenu.setVisible(false);
				}
			}
		});
		viewRows.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Cancel".equals(property)) {
					popupMenu.setVisible(false);
					setValue((int[]) e.getNewValue());
				}
			}
		});
		viewRows.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Value".equals(property)) {
					setValue((int[]) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
				}
			}
		});
	}
	
	public int[] getValue() {
		return (int[]) comboModel.getElementAt(0);
	}

	public void setValue(int[] value) {
		int[] selected = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		setSelectedItem(selected);
		comboModel.removeAllElements();
		comboModel.addElement(selected);
		if (!viewRows.getValue().equals(selected)) {
			viewRows.setValue(selected);
		}
	}
}
