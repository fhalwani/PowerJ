package ca.eorla.fhalwani.powerj;

class DataProcedure {
	// Specimen type/procedure
	static final byte UNKNOWN = 0;
	static final byte BIOPSY = 1;	// Surgical
	static final byte EXCISION = 2;
	static final byte SMALL = 3;
	static final byte LARGE = 4;
	static final byte RADICAL = 5;
	static final byte SMEAR = 6;	// Cytology
	static final byte FLUID = 7;
	static final byte WASH = 8;
	static final byte FNA = 9;
	static final byte FULL = 10;	// Autopsy
	static final byte REGION = 11;
	static final byte ORGAN = 12;
	static final String[] NAMES = {"Unknown", "Biopsy", "Excision", "Minor", 
		"Major", "Radical", "Smear", "Fluid", "Wash", "FNA", "Full", "Region", "Organ"};
}
