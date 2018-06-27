package ca.eorla.fhalwani.powerj;
import java.util.ArrayList;

class DataCaseFinal {
	boolean hasError = false;
	boolean hasLN = false;
	boolean codeSpecimens = false;	// Not molecular or FISH cases
	boolean needsFragments = false;
	byte specialtyID = 0;	// Surg, Cyto, Autopsy, other (molecular, FCM)
	byte subspecialtyID = 0;	// Breast, Cardiac, Derm, GI, GU, etc
	byte procedureID = 0;	// 1=Bx, 2=exc, 3=small, 4=large, 5=radical
	byte noSpecimens = 0;
	byte noFSSpecs = 0;
	byte noSynoptics = 0;
	byte mainSpecimenNo = 0;
	short facilityID = 0;
	short staffID = 0;
	short grossID = 0;
	short noBlocks = 0;
	short noSlides = 0;
	short noFSBlks = 0;
	short noFSSlds = 0;
	short mainSpecimenBlocks = 0;
	int grossTAT = 0;
	int routeTAT = 0;
	int finalTAT = 0;
	int totalTAT = 0;
	int masterSpecimen = 0;
	long caseID = 0;
	long accessionTime = 0;
	long grossTime = 0;
	long routeTime = 0;
	long scanTime = 0;
	long finalTime = 0;
	String caseNo = "";
	String comment = "";
	ArrayList<DataSpecimenFinal> lstSpecimens = new ArrayList<DataSpecimenFinal>();
}
