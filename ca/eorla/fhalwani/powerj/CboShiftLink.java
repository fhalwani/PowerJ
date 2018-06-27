package ca.eorla.fhalwani.powerj;

class CboShiftLink extends CboMain {
	private static final long serialVersionUID = 7775941624509344524L;

	public CboShiftLink(PowerJ parent) {
		super(parent);
		setName("cboShiftLink");
		strName ="ShiftLink";
		getData();
		setModel();
	}

	void getData() {
		list.add(new DataItem(1, "* No Link *"));
		list.add(new DataItem(2, "Specialties"));
		list.add(new DataItem(3, "Subspecialties"));
	}
}
