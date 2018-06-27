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

class CboColumns extends JComboBox {
	private static final long serialVersionUID = 6365919400470070521L;
	private final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
	private final ViewColumns colsView;
	private final JPopupMenu popupMenu = new JPopupMenu();

	CboColumns(boolean[] value, PowerJ parent) {
		boolean[] selected = new boolean[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		colsView = new ViewColumns(selected, parent);
		comboModel.addElement(selected);
		setModel(comboModel);
		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 33771457556963313L;
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				boolean[] results = (boolean[]) comboModel.getElementAt(0);
				String display = "";
				for (byte i = 0; i < 9; i++) {
					if (results[i]) {
						switch (i) {
						case 0:
							display += ":CA";
							break;
						case 1:
							display += ":SL";
							break;
						case 2:
							display += ":C1";
							break;
						case 3:
							display += ":C2";
							break;
						case 4:
							display += ":C3";
							break;
						case 5:
							display += ":C4";
							break;
						default:
							// ignore rest
						}
					}
				}
				if (display.length() > 2) {
					setText(display.substring(1));
				}
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
							popupMenu.show(CboColumns.this, 0, getHeight());
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
