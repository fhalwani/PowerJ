package ca.eorla.fhalwani.powerj;

class DataMasterSpecimen {
	boolean hasLN = false;
	// Surg, Cyto, Autopsy, Molecular, Consult
	byte specialtyID = 0;
	// Derm, GI, GU, Gyne, etc
	byte subspecialtyID = 0;
	// NA, Bx, excision (polyp, lipoma), Minor (small organ,appendix), 
	// Major (large organ, breast, colon), Radical (Whipple)
	byte procedureID = 0;
	// 3 coding systems & each has 3 values (benign, malignant, radical)
	short coder1B = 0;
	short coder1M = 0;
	short coder1R = 0;
	short coder2B = 0;
	short coder2M = 0;
	short coder2R = 0;
	short coder3B = 0;
	short coder3M = 0;
	short coder3R = 0;
	short coder4B = 0;
	short coder4M = 0;
	short coder4R = 0;
}
