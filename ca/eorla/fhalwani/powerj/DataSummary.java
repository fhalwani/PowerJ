package ca.eorla.fhalwani.powerj;

class DataSummary {
	short id = 0;
	short leaf = 0;
	int noCases = 0;
	int noSlides = 0;
	double prcntCases = 0;
	double prcntSlides = 0;
	double value1 = 0;
	double value2 = 0;
	double value3 = 0;
	double value4 = 0;
	double prcnt1 = 0;
	double prcnt2 = 0;
	double prcnt3 = 0;
	double prcnt4 = 0;
	String name = "";
	Object[] children; 
	
	public DataSummary(String name) { 
		this.name = name; 
	}

	protected Object[] getChildren() {
		return children; 
	}

	public String toString() {
		return name;
	}
}
