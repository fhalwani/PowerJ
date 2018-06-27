package ca.eorla.fhalwani.powerj;
import javax.swing.JPanel;

class PnlMain extends JPanel {
	private static final long serialVersionUID = -8097239362423096070L;
	boolean altered = false;
	boolean programmaticChange = true;
	PowerJ parent;
	Numbers numbers;
	
	PnlMain(PowerJ parent) {
		super();
		this.parent = parent;
		this.numbers = parent.numbers;
        setBorder(Constants.borderEmpty);
	}

	boolean close() {
		parent.dbPowerJ.closeStms();
		return altered;
	}
	
	void pdf() {
	}
	
	void save() {
	}
	
	void xls() {
	}
}
