package ca.eorla.fhalwani.powerj;

class CboStatus extends CboMain {
	private static final long serialVersionUID = 4446774773973570744L;

	CboStatus(PowerJ parent, int flags) {
		super(parent);
		setName("cboStatus");
		strName ="Status";
		getData(parent, flags);
		setModel();
	}

	void getData(PowerJ parent, int flags) {
		if (flags > 4 || flags == 3) {
			list.add(new DataItem(Constants.STATUS_All, 
					Constants.STATUS_NAMES[Constants.STATUS_All - 1]));
		}
		if ((flags & 1) != 0) {
			// Gross
			list.add(new DataItem(Constants.STATUS_Accession, 
					Constants.STATUS_NAMES[Constants.STATUS_Accession]));
		}
		if ((flags & 2) != 0) {
			// Histology
			list.add(new DataItem(Constants.STATUS_Histology, 
					Constants.STATUS_NAMES[Constants.STATUS_Histology-1]));
			list.add(new DataItem(Constants.STATUS_Gross, 
					Constants.STATUS_NAMES[Constants.STATUS_Gross]));
			list.add(new DataItem(Constants.STATUS_Embed, 
					Constants.STATUS_NAMES[Constants.STATUS_Embed]));
			list.add(new DataItem(Constants.STATUS_Microtomy, 
					Constants.STATUS_NAMES[Constants.STATUS_Microtomy]));
			list.add(new DataItem(Constants.STATUS_Slides, 
					Constants.STATUS_NAMES[Constants.STATUS_Slides]));
		}
		if ((flags & 4) != 0) {
			// Diagnosis
			list.add(new DataItem(Constants.STATUS_Routed, 
					Constants.STATUS_NAMES[Constants.STATUS_Routed]));
		}
	}
}
