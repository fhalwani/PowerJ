package ca.eorla.fhalwani.powerj;
import java.util.HashMap;

class DataSpecimenFinal {
	byte errorID = 0;
	byte subspecialtyID = 0;
	byte procedureID = 0;
	short noFragments = 1;
	short noBlocks = 0;
	short noFSBlks = 0;
	short noSlides = 0;
	short noFSSlds = 0;
	short masterID = 0;
	long specimenID = 0;
	String description = "";
	HashMap<Short, DataSpecimenOrder> lstOrders = new HashMap<Short, DataSpecimenOrder>();
}
