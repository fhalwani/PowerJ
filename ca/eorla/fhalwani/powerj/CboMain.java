package ca.eorla.fhalwani.powerj;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

class CboMain extends JComboBox {
	private static final long serialVersionUID = -3252507179536480231L;
	String strName = "CoderName";
	protected ArrayList<DataItem> list = new ArrayList<DataItem>();
	
	CboMain(PowerJ parent) {
		super();
		setFont(Constants.APP_FONT);
		setEditable(false);
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
	}
	
	/** Clears the list. */
	void close() {
		list.clear();
	}
	
	/** Get the Item ID of a selected JComboBox. */
	int getIndex() {
		int index = getSelectedIndex();
		if (index > -1) {
			index = list.get(index).getValue();
		} else {
			index = 0;
		}
		return index;
	}
	
	/** Sets JComboBox selected index to an Item. */
	void setIndex(DataItem item) {
		setSelectedIndex(-1);
		for (int i = 0; i < getItemCount(); i++) {
			if (item.equals((DataItem) list.get(i))) {
				setSelectedIndex(i);
				break;
			}
		}
	}
	
	/** Sets JComboBox selected index to an Item Index. */
	void setIndex(int index) {
		setSelectedIndex(-1);
		for (int i = 0; i < getItemCount(); i++) {
			if (list.get(i).getValue() == index) {
				setSelectedIndex(i);
				break;
			}
		}
	}
	
	void setModel() {
		DefaultComboBoxModel model = new DefaultComboBoxModel(list.toArray());
		setModel(model);
	}
}
