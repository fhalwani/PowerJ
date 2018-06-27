package ca.eorla.fhalwani.powerj;

class CboProcedures extends CboMain {
	private static final long serialVersionUID = 6489043203737713166L;
	
	CboProcedures(PowerJ parent, boolean isEditor) {
		super(parent);
		setName("cboProcedures");
		strName ="Procedures";
		getData(parent, isEditor);
		setModel();
	}

	void getData(PowerJ parent, boolean isEditor) {
		if (!isEditor) {
			list.add(new DataItem(0, "* All *"));
		}
		for (byte i = 0; i < DataProcedure.NAMES.length; i++) {
			list.add(new DataItem(i, DataProcedure.NAMES[i]));
		}
	}
}
