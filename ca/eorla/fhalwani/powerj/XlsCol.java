package ca.eorla.fhalwani.powerj;

class XlsCol {
	int id = 0;
	int type = 0;	// 0 = string, 1 = int, 2 = double, 3 = date
	String name = "";
	
	XlsCol(int i, int j, String k) {
		id = i;
		type = j;
		name = k;
	}
}
