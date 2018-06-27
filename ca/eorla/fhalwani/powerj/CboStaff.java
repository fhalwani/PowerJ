package ca.eorla.fhalwani.powerj;

public class CboStaff extends CboMain {
	private static final long serialVersionUID = 8010704378419803735L;

	public CboStaff(PowerJ parent) {
		super(parent);
		setName("Staff");
		getData(parent);
		setModel();
	}

	void getData(PowerJ parent) {
		list.add(new DataItem(0, "* All *"));
		list.add(new DataItem(1, "Active"));
	}
}
