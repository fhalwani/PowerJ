package ca.eorla.fhalwani.powerj;

class CboTypes extends CboMain {
	private static final long serialVersionUID = 8471833235367264849L;

	CboTypes(PowerJ parent) {
		super(parent);
		setName("cboTypes");
		strName = "Types";
		getData();
		setModel();
	}

	void getData() {
		for (byte i = 0; i < DataOrderType.TYPES.length; i++) {
			list.add(new DataItem(i, DataOrderType.TYPES[i]));
		}
	}
}
