package ca.eorla.fhalwani.powerj;

class CboPriorities extends CboMain {
	private static final long serialVersionUID = 3664266758563471009L;

	public CboPriorities(PowerJ parent) {
		super(parent);
		setName("cboProcedures");
		strName ="Procedures";
		getData();
		setModel();
	}

	void getData() {
		list.add(new DataItem(1, "Low"));
		list.add(new DataItem(2, "Medium"));
		list.add(new DataItem(3, "High"));
		list.add(new DataItem(4, "Critical"));
	}
}
