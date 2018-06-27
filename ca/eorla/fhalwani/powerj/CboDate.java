package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Calendar;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class CboDate extends JComboBox {
	private static final long serialVersionUID = -5579161016242689323L;
	private final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
	private DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
	private Calendar min;
	private Calendar max;
	private final ViewMonths viewMonths;
	private final JPopupMenu popupMenu = new JPopupMenu();

	public CboDate() {
		this(null, null, null, null);
	}

	public CboDate(DateFormat formatter) {
		this(null, null, null, formatter);
	}

	public CboDate(Calendar value) {
		this(value, null, null, null);
	}

	public CboDate(Calendar value, DateFormat formatter) {
		this(value, null, null, formatter);
	}

	public CboDate(Calendar value, Calendar minDate, Calendar maxDate) {
		this(value, minDate, maxDate, null);
	}

	/**
	 * Constructs a LocalDateCombo with the date, lower (earliest) and upper (latest) limits provided,
	 * formatted according to the provided formatter.
	 * <P>
	 * Dates outside the specified range are not displayed.
	 * <P>
	 * This class does not attempt to verify that minDate <= value <= maxDate. It is the
	 * responsibility of client code to supply sane values.
	 *
	 * @param value The initial value
	 * @param minDate The minimum value (earliest date); <CODE>null</CODE> for no limit.
	 * @param maxDate The maximum value (latest date); <CODE>null</CODE> for no limit.
	 * @param formatter Formats the date for display
	 */
	public CboDate(Calendar value, Calendar minDate, Calendar maxDate, DateFormat df) {
		min = minDate;
		max = maxDate;
		if (value == null) {
			value = Calendar.getInstance();
		}
		if (df != null) {
			formatter = df;
		}
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		viewMonths = new ViewMonths(value, minDate, maxDate);
		comboModel.addElement(value);
		setModel(comboModel);
		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 8363455710196915231L;

			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				setText(formatter.format(((Calendar)value).getTime()));
				return this;
			}
		});
		popupMenu.add(viewMonths);
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				final boolean popupShown = popupMenu.isShowing();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hidePopup();
						if (popupShown) {
							popupMenu.setVisible(false);
						} else {
							viewMonths.setValue(getValue());
							popupMenu.show(CboDate.this, 0, getHeight());
						}
					}
				});
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {}
			public void popupMenuCanceled(PopupMenuEvent pme) {}
		});
		viewMonths.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Confirm".equals(property)) {
					setValue((Calendar) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
					popupMenu.setVisible(false);
				} else if ("Value".equals(property)) {
					setValue((Calendar) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
				}
			}
		});
	}

	public Calendar getValue() {
		return (Calendar) comboModel.getElementAt(0);
	}

	public void setMax(Calendar value) {
		max = value;
		viewMonths.setMax(value);
	}

	public void setMin(Calendar value) {
		min = value;
		viewMonths.setMin(value);
	}

	public void setValue(Calendar value) {
		if (getSelectedItem() != null && getSelectedItem().equals(value)) {
			return;
		}
		if (min != null && value.getTimeInMillis() < min.getTimeInMillis()) {
			value = min;
		}
		if (max != null && value.getTimeInMillis() > max.getTimeInMillis()) {
			value = max;
		}
		setSelectedItem(value);
		comboModel.removeAllElements();
		comboModel.addElement(value);
		if (!viewMonths.getValue().equals(value)) {
			viewMonths.setValue(value);
		}
	}
}
