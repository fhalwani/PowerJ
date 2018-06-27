package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JOptionPane;

class WLCoder {
	private final short RULE_SYNOPTICS = 2;
	private final short RULE_FROZENS = 3;
	private final short RULE_FROZENS_BLOCKS = 4;
	private final short RULE_CORRELATIONS = 5;
	private boolean isActive = true;
	private double dValue = 0;
	private String comment = "";
	String coderName = "";
	private DataCaseFinal thisCase = new DataCaseFinal();
	private CaseCoder caseCoder = new CaseCoder();
	private DataSpecimenFinal thisSpecimen = new DataSpecimenFinal();
	private SpecimenCoder specimenCoder = new SpecimenCoder();
	private OrdersCoder ordersCoder = new OrdersCoder();
	private MasterCode masterCode = new MasterCode();
	private HashMap<Short, MasterCode> masterCodes = new HashMap<Short, MasterCode>();
	private ArrayList<SpecimenCoder> lstSpecimenCoders = new ArrayList<SpecimenCoder>();
	private Numbers numbers;
	private PowerJ parent;

	WLCoder(PowerJ parent, byte coderID) {
		this.parent = parent;
		this.numbers = parent.numbers;
		isActive = parent.variables.codersActive[coderID -1];
		readTables(coderID);
	}

	void addOrder(short orderID, short groupID, short codeID, short qty,
			boolean isRoutine, boolean isAddlBlock, int specimenNo) {
		if (caseCoder.inclusive) {
			return;
		}
		masterCode = masterCodes.get(codeID);
		if (masterCode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_ORDER_UNKNOWN;
			comment = "ERROR: addOrder, " + thisCase.caseNo + ", Specimen " +
					specimenNo + ", Order " + codeID + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			return;
		}
		switch (masterCode.rule) {
		case DataRules.RULE_UNIQUE_CASE_INCLUSIVE:
		case DataRules.RULE_GROUP_CASE_INCLUSIVE:
		case DataRules.RULE_AFTER_CASE_INCLUSIVE:
			thisSpecimen = thisCase.lstSpecimens.get(0);
			specimenCoder = lstSpecimenCoders.get(0);
			break;
		default:
			thisSpecimen = thisCase.lstSpecimens.get(specimenNo);
			specimenCoder = lstSpecimenCoders.get(specimenNo);
		}
		if (specimenCoder.inclusive) {
			return;
		}
		if (isAddlBlock && 
				(!specimenCoder.codeAddBlocks ||
						!caseCoder.codeAddBlocks)) {
			return;
		}
		ordersCoder = specimenCoder.lstOrders.get(groupID);
		if (ordersCoder == null) {
			ordersCoder = new OrdersCoder();
			ordersCoder.codeID = codeID;
			ordersCoder.isAddlBlock = isAddlBlock;
			ordersCoder.name = masterCode.name;
			specimenCoder.lstOrders.put(groupID, ordersCoder);
		} else if (isAddlBlock) {
			// Avoid changing it to false
			ordersCoder.isAddlBlock = true;
		}
		switch (masterCode.rule) {
		case DataRules.RULE_CASE_INCLUSIVE:
		case DataRules.RULE_GROUP_CASE_INCLUSIVE:
		case DataRules.RULE_SPECIMEN_INCLUSIVE:
		case DataRules.RULE_GROUP_SPECIMEN_INCLUSIVE:
			ordersCoder.qty = 1;
			break;
		case DataRules.RULE_UNIQUE_CASE_INCLUSIVE:
		case DataRules.RULE_UNIQUE_SPECIMEN_INCLUSIVE:
			if (ordersCoder.isUnique(orderID)) {
				ordersCoder.qty += 1;
			}
			break;
		case DataRules.RULE_UNIQUE_EVERY_X_MIN_MAX:
		case DataRules.RULE_UNIQUE_1_2_3PLUS:
		case DataRules.RULE_UNIQUE_1_2_X:
			// CPT
			if (ordersCoder.isUnique(orderID)) {
				ordersCoder.qty += qty;
			}
			break;
		case DataRules.RULE_AFTER_CASE_INCLUSIVE:
		case DataRules.RULE_AFTER_SPECIMEN_INCLUSIVE:
			// Once per case if ordered after routing
			if (!isRoutine) {
				ordersCoder.qty = 1;
			}
			break;
		case DataRules.RULE_AFTER_EVERY_X_MIN_MAX:
		case DataRules.RULE_AFTER_1_2_3PLUS:
		case DataRules.RULE_AFTER_1_2_X:
			// CAP
			if (!isRoutine) {
				ordersCoder.qty += qty;
			}
			break;
		case DataRules.RULE_GROUP_EVERY_X_MIN_MAX:
		case DataRules.RULE_GROUP_1_2_3PLUS:
		case DataRules.RULE_GROUP_1_2_X:
			// W2Q
			ordersCoder.qty += qty;
			break;
		default:
			// Ignore RCP
			ordersCoder.qty = 0;
		}
	}

	void addSpecimen(byte procedureID, short codeBenign, short codeMalignant, short codeRadical) {
		thisSpecimen = thisCase.lstSpecimens.get(thisCase.noSpecimens -1);
		specimenCoder = new SpecimenCoder();
		specimenCoder.procID = procedureID;
		specimenCoder.coderID = codeBenign;
		lstSpecimenCoders.add(specimenCoder);
		if (thisCase.noSynoptics > 0) {
			specimenCoder.coderMalig = codeMalignant;
			specimenCoder.coderRadical = codeRadical;
		}
		if (!thisCase.codeSpecimens) {
			// Molecular
			specimenCoder.codeAddBlocks = false;
			return;
		}
		masterCode = masterCodes.get(specimenCoder.coderID);
		if (masterCode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_SPECIMEN_UNKNOWN;
			comment = "ERROR: addSpecimen, " + thisCase.caseNo + ", Specimen " +
					thisCase.noSpecimens + ", Coder " + specimenCoder.coderID + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			return;
		}
		switch (masterCode.rule) {
		case DataRules.RULE_IGNORE:
			// Nothing for specimen (molecular, Derm IF, Iron Quant)
			break;
		case DataRules.RULE_CASE_INCLUSIVE:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			caseCoder.codeAddBlocks = false;
			caseCoder.inclusive = true;
			break;
		case DataRules.RULE_CASE_FRAGS_X_MIN_MAX:
		case DataRules.RULE_CASE_FRAGS_1_2_3PLUS:
		case DataRules.RULE_CASE_FRAGS_1_2_X:
		case DataRules.RULE_CASE_FRAGS_BLOCKS:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			caseCoder.codeAddBlocks = false;
			thisCase.needsFragments = true;
			break;
		case DataRules.RULE_CASE_GROSS_MICRO:
		case DataRules.RULE_CASE_BLOCKS_X_MIN_MAX:
		case DataRules.RULE_CASE_BLOCKS_1_2_3PLUS:
		case DataRules.RULE_CASE_BLOCKS_1_2_X:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			caseCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_CASE_FIXED:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			break;
		case DataRules.RULE_SPECIMEN_INCLUSIVE:
		case DataRules.RULE_LINKED_INCLUSIVE:
			specimenCoder.inclusive = true;
			specimenCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
		case DataRules.RULE_SPECIMEN_FRAGS_1_2_3PLUS:
		case DataRules.RULE_SPECIMEN_FRAGS_1_2_X:
		case DataRules.RULE_SPECIMEN_FRAGS_BLOCKS:
		case DataRules.RULE_LINKED_FRAGS_X_MIN_MAX:
		case DataRules.RULE_LINKED_FRAGS_1_2_3PLUS:
		case DataRules.RULE_LINKED_FRAGS_1_2_X:
		case DataRules.RULE_LINKED_FRAGS_BLOCKS:
			// CAP GI Polyps, skin resections
			thisCase.needsFragments = true;
			specimenCoder.needsFragments = true;
			specimenCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
		case DataRules.RULE_SPECIMEN_BLOCKS_1_2_3PLUS:
		case DataRules.RULE_SPECIMEN_BLOCKS_1_2_X:
		case DataRules.RULE_LINKED_BLOCKS_X_MIN_MAX:
		case DataRules.RULE_LINKED_BLOCKS_1_2_3PLUS:
		case DataRules.RULE_LINKED_BLOCKS_1_2_X:
			specimenCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_SPECIMEN_FIXED:
		case DataRules.RULE_SPECIMEN_GROSS_MICRO:
		case DataRules.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case DataRules.RULE_SPECIMEN_1_2_3PLUS:
		case DataRules.RULE_SPECIMEN_1_2_X:
		case DataRules.RULE_LINKED_FIXED:
		case DataRules.RULE_LINKED_EVERY_X_MIN_MAX:
		case DataRules.RULE_LINKED_1_2_3PLUS:
		case DataRules.RULE_LINKED_1_2_X:
			break;
		default:
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: addSpecimen, " + thisCase.caseNo + ", Specimen " +
					thisCase.noSpecimens + ", Rule " + masterCode.rule + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
		}
		caseCoder.comment += "Specimen " + thisCase.noSpecimens + ", Rule " +
				masterCode.rule + ", Name: " + masterCode.name +
				", AddlBlk: " + (specimenCoder.codeAddBlocks ? "T" : "F") +
				", Frags: " + (specimenCoder.needsFragments ? "T" : "F") +
				Constants.NEW_LINE;
	}

	void checkSpecimens() {
		for (int i = 0; i < thisCase.noSpecimens; i++) {
			thisSpecimen = thisCase.lstSpecimens.get(i);
			specimenCoder = lstSpecimenCoders.get(i);
			if (thisCase.noSynoptics > 0) {
				// Malignant or radical case
				if (thisCase.noSynoptics > 1 || thisCase.hasLN) {
					// Radical is 2 synoptics or 1 synoptic + node dissection
					if (!caseCoder.foundRadical) {
						masterCode = masterCodes.get(specimenCoder.coderRadical);
						if (masterCode != null) {
							if (masterCode.rule > DataRules.RULE_IGNORE) {
								// Use this specimen as first choice for coding malignancy
								caseCoder.mainSpecimenNo = thisCase.noSpecimens;
								thisCase.procedureID = specimenCoder.procID;
								thisCase.masterSpecimen = thisSpecimen.masterID;
								specimenCoder.coderID = specimenCoder.coderRadical;
								caseCoder.foundRadical = true;
								break;
							}
						}
					}
				}
				if (!caseCoder.foundRadical && !caseCoder.foundMalignant) {
					masterCode = masterCodes.get(specimenCoder.coderMalig);
					if (masterCode != null) {
						if (masterCode.rule > DataRules.RULE_IGNORE) {
							// Use this specimen as first choice for coding malignancy
							caseCoder.mainSpecimenNo = thisCase.noSpecimens;
							thisCase.procedureID = specimenCoder.procID;
							thisCase.masterSpecimen = thisSpecimen.masterID;
							specimenCoder.coderID = specimenCoder.coderMalig;
							caseCoder.foundMalignant = true;
							break;
						}
					}
				}
			}
		}
		masterCode = masterCodes.get(specimenCoder.coderID);
		if (masterCode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_SPECIMEN_UNKNOWN;
			comment = "ERROR: addSpecimen, " + thisCase.caseNo + ", Specimen " +
					thisCase.noSpecimens + ", Coder " + specimenCoder.coderID + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			return;
		}
		switch (masterCode.rule) {
		case DataRules.RULE_IGNORE:
			// Nothing for specimen (molecular, Derm IF, Iron Quant)
			break;
		case DataRules.RULE_CASE_INCLUSIVE:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			caseCoder.codeAddBlocks = false;
			caseCoder.inclusive = true;
			break;
		case DataRules.RULE_CASE_FRAGS_X_MIN_MAX:
		case DataRules.RULE_CASE_FRAGS_1_2_3PLUS:
		case DataRules.RULE_CASE_FRAGS_1_2_X:
		case DataRules.RULE_CASE_FRAGS_BLOCKS:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			caseCoder.codeAddBlocks = false;
			thisCase.needsFragments = true;
			break;
		case DataRules.RULE_CASE_GROSS_MICRO:
		case DataRules.RULE_CASE_BLOCKS_X_MIN_MAX:
		case DataRules.RULE_CASE_BLOCKS_1_2_3PLUS:
		case DataRules.RULE_CASE_BLOCKS_1_2_X:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			caseCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_CASE_FIXED:
			caseCoder.coderID = specimenCoder.coderID;
			caseCoder.mainSpecimenNo = thisCase.noSpecimens;
			break;
		case DataRules.RULE_SPECIMEN_INCLUSIVE:
		case DataRules.RULE_LINKED_INCLUSIVE:
			specimenCoder.inclusive = true;
			specimenCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
		case DataRules.RULE_SPECIMEN_FRAGS_1_2_3PLUS:
		case DataRules.RULE_SPECIMEN_FRAGS_1_2_X:
		case DataRules.RULE_SPECIMEN_FRAGS_BLOCKS:
		case DataRules.RULE_LINKED_FRAGS_X_MIN_MAX:
		case DataRules.RULE_LINKED_FRAGS_1_2_3PLUS:
		case DataRules.RULE_LINKED_FRAGS_1_2_X:
		case DataRules.RULE_LINKED_FRAGS_BLOCKS:
			// CAP GI Polyps, skin resections
			thisCase.needsFragments = true;
			specimenCoder.needsFragments = true;
			specimenCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
		case DataRules.RULE_SPECIMEN_BLOCKS_1_2_3PLUS:
		case DataRules.RULE_SPECIMEN_BLOCKS_1_2_X:
		case DataRules.RULE_LINKED_BLOCKS_X_MIN_MAX:
		case DataRules.RULE_LINKED_BLOCKS_1_2_3PLUS:
		case DataRules.RULE_LINKED_BLOCKS_1_2_X:
			specimenCoder.codeAddBlocks = false;
			break;
		case DataRules.RULE_SPECIMEN_FIXED:
		case DataRules.RULE_SPECIMEN_GROSS_MICRO:
		case DataRules.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case DataRules.RULE_SPECIMEN_1_2_3PLUS:
		case DataRules.RULE_SPECIMEN_1_2_X:
		case DataRules.RULE_LINKED_FIXED:
		case DataRules.RULE_LINKED_EVERY_X_MIN_MAX:
		case DataRules.RULE_LINKED_1_2_3PLUS:
		case DataRules.RULE_LINKED_1_2_X:
			break;
		default:
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: addSpecimen, " + thisCase.caseNo + ", Specimen " +
					thisCase.noSpecimens + ", Rule " + masterCode.rule + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
		}
		caseCoder.comment += "Malignant/Radical Specimen " + thisCase.noSpecimens + ", Rule " +
				masterCode.rule + ", " + ", Name: " + masterCode.name +
				", AddlBlk: " + (specimenCoder.codeAddBlocks ? "T" : "F") +
				", Frags: " + (specimenCoder.needsFragments ? "T" : "F") +
				Constants.NEW_LINE;
		for (int i = 0; i < thisCase.noSpecimens; i++) {
			thisSpecimen = thisCase.lstSpecimens.get(i);
			specimenCoder = lstSpecimenCoders.get(i);
			for (Entry<Short, OrdersCoder> orderEntry : specimenCoder.lstOrders.entrySet()) {
				ordersCoder = orderEntry.getValue();
				if (ordersCoder.isAddlBlock && 
						(!specimenCoder.codeAddBlocks ||
								!caseCoder.codeAddBlocks)) {
					ordersCoder.qty = 0;
				} else if (caseCoder.inclusive || specimenCoder.inclusive) {
					ordersCoder.qty = 0;
				}
			}
		}
	}

	void close() {
		masterCodes.clear();
		lstSpecimenCoders.clear();
	}

	void codeCase() {
		if (thisCase.noSynoptics > 0) {
			checkSpecimens();
		}
		if (caseCoder.coderID > 0) {
			codeMain();
		} else if (thisCase.codeSpecimens) {
			// Else, Molecular case, only code orders
			codeSpecimens();
		}
		codeOrders();
		if (thisCase.noSynoptics > 0) {
			// W2Q adds workload for actual synoptics
			codeSynoptics();
		}
		if (thisCase.noFSSpecs > 0) {
			codeFrozen();
		}
	}

	private void codeFrozen() {
		boolean caseFixed = false;
		boolean codeBlocks = true;
		double dLinks = 0, dExpect = 0;
		short noLinks = 0;
		masterCode = masterCodes.get(RULE_FROZENS);
		if (masterCode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: codeFrozen, " + thisCase.caseNo + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			return;
		}
		if (masterCode.rule == DataRules.RULE_IGNORE) {
			return;
		}
		for (int i = 0; i < thisCase.noSpecimens; i++) {
			if (caseFixed) break;
			thisSpecimen = thisCase.lstSpecimens.get(i);
			if (thisSpecimen.noFSBlks < 1 && thisSpecimen.noFSSlds < 1) {
				continue;
			}
			dValue = 0;
			switch (masterCode.rule) {
			case DataRules.RULE_CASE_INCLUSIVE:
			case DataRules.RULE_LINKED_INCLUSIVE:
				specimenCoder.frozenValue = masterCode.valueA;
				caseFixed = true;
				codeBlocks = false;
				break;
			case DataRules.RULE_CASE_FIXED:
			case DataRules.RULE_LINKED_FIXED:
				specimenCoder.frozenValue = masterCode.valueA;
				caseFixed = true;
				break;
			case DataRules.RULE_CASE_GROSS_MICRO:
				if (thisCase.noFSSlds > 0) {
					specimenCoder.frozenValue = masterCode.valueB;
				} else {
					specimenCoder.frozenValue = masterCode.valueA;
				}
				caseFixed = true;
				codeBlocks = false;
				break;
			case DataRules.RULE_LINKED_EVERY_X_MIN_MAX:
			case DataRules.RULE_SPECIMEN_EVERY_X_MIN_MAX:
				noLinks++;
				dExpect = masterCode.valueA * numbers.ceiling(noLinks, masterCode.count);
				dValue = dExpect - dLinks;
				specimenCoder.frozenValue = setMinMax(dValue, dLinks, dExpect, masterCode.valueB, masterCode.valueC);
				dLinks += specimenCoder.frozenValue;
				break;
			case DataRules.RULE_LINKED_1_2_3PLUS:
			case DataRules.RULE_SPECIMEN_1_2_3PLUS:
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.frozenValue = masterCode.valueA;
				} else if (noLinks == 2) {
					specimenCoder.frozenValue = masterCode.valueB;
				} else {
					specimenCoder.frozenValue = masterCode.valueC;
				}
				break;
			case DataRules.RULE_LINKED_1_2_X:
			case DataRules.RULE_SPECIMEN_1_2_X:
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.frozenValue = masterCode.valueA;
				} else if (noLinks == 2) {
					specimenCoder.frozenValue = masterCode.valueB;
				} else if (noLinks == masterCode.count) {
					specimenCoder.frozenValue = masterCode.valueC;
				}
				break;
			case DataRules.RULE_CASE_BLOCKS_X_MIN_MAX:
			case DataRules.RULE_LINKED_BLOCKS_X_MIN_MAX:
				noLinks += thisSpecimen.noFSBlks;
				dExpect = masterCode.valueA * numbers.ceiling(noLinks, masterCode.count);
				dValue = dExpect - dLinks;
				specimenCoder.frozenValue = setMinMax(dValue, dLinks, dExpect, masterCode.valueB, masterCode.valueC);
				dLinks += specimenCoder.frozenValue;
				codeBlocks = false;
				break;
			case DataRules.RULE_CASE_BLOCKS_1_2_3PLUS:
			case DataRules.RULE_LINKED_BLOCKS_1_2_3PLUS:
				noLinks += thisSpecimen.noFSBlks;
				if (noLinks >= 1 && dLinks < masterCode.valueA) {
					specimenCoder.frozenValue += masterCode.valueA;
				}
				if (noLinks >= 2 && dLinks < masterCode.valueA + masterCode.valueB) {
					specimenCoder.frozenValue += masterCode.valueB;
				}
				if (noLinks >= 3 && dLinks < masterCode.valueA + masterCode.valueB + (masterCode.valueC * (noLinks -2))) {
					specimenCoder.frozenValue += (masterCode.valueC * (noLinks -2));
				}
				dLinks += specimenCoder.frozenValue;
				codeBlocks = false;
				break;
			case DataRules.RULE_CASE_BLOCKS_1_2_X:
			case DataRules.RULE_LINKED_BLOCKS_1_2_X:
				noLinks += thisSpecimen.noFSBlks;
				if (noLinks >= 1 && dLinks < masterCode.valueA) {
					specimenCoder.frozenValue += masterCode.valueA;
				}
				if (noLinks >= 2 && dLinks < masterCode.valueA + masterCode.valueB) {
					specimenCoder.frozenValue += masterCode.valueB;
				}
				if (noLinks >= masterCode.count && dLinks < masterCode.valueA + masterCode.valueB + masterCode.valueC) {
					specimenCoder.frozenValue += masterCode.valueC;
				}
				dLinks += specimenCoder.frozenValue;
				codeBlocks = false;
				break;
			case DataRules.RULE_SPECIMEN_INCLUSIVE:
				specimenCoder.frozenValue = masterCode.valueA;
				codeBlocks = false;
				break;
			case DataRules.RULE_SPECIMEN_FIXED:
				specimenCoder.frozenValue = masterCode.valueA;
				break;
			case DataRules.RULE_SPECIMEN_GROSS_MICRO:
				if (thisSpecimen.noFSSlds > 0) {
					specimenCoder.frozenValue = masterCode.valueB;
				} else {
					specimenCoder.frozenValue = masterCode.valueA;
				}
				break;
			case DataRules.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
				dValue = masterCode.valueA * numbers.ceiling(thisSpecimen.noFSBlks, masterCode.count);
				specimenCoder.frozenValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
				codeBlocks = false;
				break;
			case DataRules.RULE_SPECIMEN_BLOCKS_1_2_3PLUS:
				if (thisSpecimen.noFSBlks >= 1) {
					specimenCoder.frozenValue = masterCode.valueA;
				}
				if (thisSpecimen.noFSBlks >= 2) {
					specimenCoder.frozenValue += masterCode.valueB;
				}
				if (thisSpecimen.noFSBlks  >= 3) {
					specimenCoder.frozenValue += (masterCode.valueC * (thisSpecimen.noFSBlks -2));
				}
				codeBlocks = false;
				break;
			case DataRules.RULE_SPECIMEN_BLOCKS_1_2_X:
				if (thisSpecimen.noFSBlks >= 1) {
					specimenCoder.frozenValue = masterCode.valueA;
				}
				if (thisSpecimen.noFSBlks >= 2) {
					specimenCoder.frozenValue += masterCode.valueB;
				}
				if (thisSpecimen.noFSBlks >= masterCode.count) {
					specimenCoder.frozenValue += masterCode.valueC;
				}
				codeBlocks = false;
				break;
			default:
				caseCoder.hasError = true;
				caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
				comment = "ERROR: codeFrozen, " + thisCase.caseNo + ", " +
						Constants.ERROR_STRINGS[caseCoder.errorID];
				caseCoder.comment += comment + Constants.NEW_LINE;
				parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			}
			caseCoder.frozenValue += specimenCoder.frozenValue;
			if (codeBlocks && thisSpecimen.noFSBlks > 0) {
				codeBlocks = codeFrozenBlocks();
				masterCode = masterCodes.get(RULE_FROZENS);
			}
			caseCoder.comment += "Frozen Sections: Specimen " + (i+1) +
					", Blocks " + thisSpecimen.noFSBlks +
					", Value: " + numbers.formatDouble(3, specimenCoder.frozenValue) +
					", Case: " + numbers.formatDouble(3, caseCoder.frozenValue) +
					Constants.NEW_LINE;
		}
	}

	private boolean codeFrozenBlocks() {
		boolean codeBlocks = true;
		// Same specimen as codeFrozen
		dValue = 0;
		masterCode = masterCodes.get(RULE_FROZENS_BLOCKS);
		if (masterCode == null) {
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: codeFrozenBlocks, " + thisCase.caseNo + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			codeBlocks = false;
		} else {
			switch (masterCode.rule) {
			case DataRules.RULE_IGNORE:
				codeBlocks = false;
				dValue = 0;
				break;
			case DataRules.RULE_CASE_BLOCKS_X_MIN_MAX:
			case DataRules.RULE_LINKED_BLOCKS_X_MIN_MAX:
			case DataRules.RULE_LINKED_EVERY_X_MIN_MAX:
				dValue = masterCode.valueA * numbers.ceiling(thisCase.noFSBlks, masterCode.count);
				dValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
				codeBlocks = false;
				break;
			case DataRules.RULE_CASE_BLOCKS_1_2_3PLUS:
			case DataRules.RULE_LINKED_BLOCKS_1_2_3PLUS:
			case DataRules.RULE_LINKED_1_2_3PLUS:
				if (thisCase.noFSBlks >= 1) {
					dValue = masterCode.valueA;
				}
				if (thisCase.noFSBlks >= 2) {
					dValue += masterCode.valueB;
				}
				if (thisCase.noFSBlks >= 3) {
					dValue += (masterCode.valueC * (thisCase.noFSBlks -2));
				}
				codeBlocks = false;
				break;
			case DataRules.RULE_CASE_BLOCKS_1_2_X:
			case DataRules.RULE_LINKED_BLOCKS_1_2_X:
			case DataRules.RULE_LINKED_1_2_X:
				if (thisCase.noFSBlks >= 1) {
					dValue = masterCode.valueA;
				}
				if (thisCase.noFSBlks >= 2) {
					dValue += masterCode.valueB;
				}
				if (thisCase.noFSBlks >= masterCode.count) {
					dValue += masterCode.valueC;
				}
				codeBlocks = false;
				break;
			case DataRules.RULE_SPECIMEN_INCLUSIVE:
			case DataRules.RULE_SPECIMEN_FIXED:
				dValue = masterCode.valueA;
				break;
			case DataRules.RULE_SPECIMEN_EVERY_X_MIN_MAX:
			case DataRules.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
			case DataRules.RULE_GROUP_EVERY_X_MIN_MAX:
				dValue = masterCode.valueA * numbers.ceiling(thisSpecimen.noFSBlks, masterCode.count);
				dValue += setMinMax(dValue, masterCode.valueB, masterCode.valueC);
				break;
			case DataRules.RULE_SPECIMEN_1_2_3PLUS:
			case DataRules.RULE_SPECIMEN_BLOCKS_1_2_3PLUS:
			case DataRules.RULE_GROUP_1_2_3PLUS:
				if (thisSpecimen.noFSBlks >= 1) {
					dValue = masterCode.valueA;
				}
				if (thisSpecimen.noFSBlks >= 2) {
					dValue += masterCode.valueB;
				}
				if (thisSpecimen.noFSBlks  >= 3) {
					dValue += (masterCode.valueC * (thisSpecimen.noFSBlks -2));
				}
				break;
			case DataRules.RULE_SPECIMEN_1_2_X:
			case DataRules.RULE_SPECIMEN_BLOCKS_1_2_X:
			case DataRules.RULE_GROUP_1_2_X:
				if (thisSpecimen.noFSBlks >= 1) {
					dValue = masterCode.valueA;
				}
				if (thisSpecimen.noFSBlks >= 2) {
					dValue += masterCode.valueB;
				}
				if (thisSpecimen.noFSBlks >= masterCode.count) {
					dValue += masterCode.valueC;
				}
				break;
			default:
				caseCoder.hasError = true;
				caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
				comment = "ERROR: codeFrozenBlocks, " + thisCase.caseNo + ", " +
						Constants.ERROR_STRINGS[caseCoder.errorID];
				caseCoder.comment += comment + Constants.NEW_LINE;
				parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
			}
			if (dValue > 0) {
				specimenCoder.frozenValue += dValue;
				caseCoder.frozenValue += dValue;
				caseCoder.comment += "Frozen Blocks: " + dValue +
						", Specimen: " + numbers.formatDouble(3, specimenCoder.frozenValue) +
						", Case: " + numbers.formatDouble(3, caseCoder.frozenValue) +
						Constants.NEW_LINE;
			}
		}
		return codeBlocks;
	}
	
	private void codeMain() {
		specimenCoder = lstSpecimenCoders.get(caseCoder.mainSpecimenNo -1);
		masterCode = masterCodes.get(caseCoder.coderID);
		switch (masterCode.rule) {
		case DataRules.RULE_CASE_INCLUSIVE:
		case DataRules.RULE_CASE_FIXED:
			specimenCoder.codeValue = masterCode.valueA;
			break;
		case DataRules.RULE_CASE_GROSS_MICRO:
			if (thisCase.noSlides > 0) {
				specimenCoder.codeValue = masterCode.valueB;
			} else {
				specimenCoder.codeValue = masterCode.valueA;
			}
			break;
		case DataRules.RULE_CASE_BLOCKS_X_MIN_MAX:
			dValue = masterCode.valueA * numbers.ceiling(thisCase.noBlocks, masterCode.count);
			specimenCoder.codeValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
			break;
		case DataRules.RULE_CASE_BLOCKS_1_2_3PLUS:
			specimenCoder.codeValue = masterCode.valueA;
			if (thisCase.noBlocks > 1) {
				specimenCoder.codeValue += masterCode.valueB;
				if (thisCase.noBlocks > 2) {
					specimenCoder.codeValue += (masterCode.valueC * (thisCase.noBlocks -2));
				}
			}
			break;
		case DataRules.RULE_CASE_BLOCKS_1_2_X:
			specimenCoder.codeValue = masterCode.valueA;
			if (thisCase.noBlocks > 1) {
				specimenCoder.codeValue += masterCode.valueB;
				if (thisCase.noBlocks >= masterCode.count) {
					specimenCoder.codeValue += masterCode.valueC;
				}
			}
			break;
		default:
			// Case Fragments
			short counter = 0;
			for (int i = 0; i < thisCase.noSpecimens; i++) {
				counter += thisCase.lstSpecimens.get(i).noFragments;
			}
			switch (masterCode.rule) {
			case DataRules.RULE_CASE_FRAGS_X_MIN_MAX:
				specimenCoder.codeValue = masterCode.valueA * numbers.ceiling(counter, masterCode.count);
				specimenCoder.codeValue = setMinMax(specimenCoder.codeValue, 0d, specimenCoder.codeValue, masterCode.valueB, masterCode.valueC);
				break;
			case DataRules.RULE_CASE_FRAGS_1_2_3PLUS:
				specimenCoder.codeValue = masterCode.valueA;
				if (counter > 1) {
					specimenCoder.codeValue += masterCode.valueB;
					if (counter > 2) {
						specimenCoder.codeValue += (masterCode.valueC * (counter -2));
					}
				}
				break;
			case DataRules.RULE_CASE_FRAGS_1_2_X:
				specimenCoder.codeValue = masterCode.valueA;
				if (counter > 1) {
					specimenCoder.codeValue += masterCode.valueB;
					if (counter >= masterCode.count) {
						specimenCoder.codeValue += masterCode.valueC;
					}
				}
				break;
			default:
				// RULE_CASE_FRAGS_BLOCKS
				specimenCoder.codeValue = masterCode.valueA * numbers.ceiling(counter, masterCode.count);
				dValue = masterCode.valueB * numbers.ceiling(thisCase.noBlocks, masterCode.count);
				caseCoder.comment += "Fragments: " + counter +
						" - " + numbers.formatDouble(3, specimenCoder.codeValue) +
						", Blocks: " + thisCase.noBlocks +
						" - "+ numbers.formatDouble(3, dValue) +
						Constants.NEW_LINE;
				if (specimenCoder.codeValue < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.codeValue = dValue;
				}
			}
		}
		caseCoder.codeValue = specimenCoder.codeValue;
		caseCoder.comment += "Case Rule: " + masterCode.rule +
				", Value: " + numbers.formatDouble(3, caseCoder.codeValue) +
				Constants.NEW_LINE;
	}

	private void codeOrders() {
		if (caseCoder.inclusive) {
			return;
		}
		for (int i = 0; i < thisCase.noSpecimens; i++) {
			specimenCoder = lstSpecimenCoders.get(i);
			if (specimenCoder.inclusive) {
				continue;
			}
			for (Entry<Short, OrdersCoder> orderEntry : specimenCoder.lstOrders.entrySet()) {
				ordersCoder = orderEntry.getValue();
				if (ordersCoder.qty == 0) {
					continue;
				}
				if (ordersCoder.isAddlBlock && 
						(!specimenCoder.codeAddBlocks ||
								!caseCoder.codeAddBlocks)) {
					continue;
				}
				masterCode = masterCodes.get(ordersCoder.codeID);
				switch (masterCode.rule) {
				case DataRules.RULE_AFTER_EVERY_X_MIN_MAX:
				case DataRules.RULE_GROUP_EVERY_X_MIN_MAX:
				case DataRules.RULE_UNIQUE_EVERY_X_MIN_MAX:
					ordersCoder.value = masterCode.valueA *
					numbers.ceiling(ordersCoder.qty, masterCode.count);
					ordersCoder.value = setMinMax(ordersCoder.value,
							masterCode.valueB, masterCode.valueC);
					break;
				case DataRules.RULE_AFTER_1_2_3PLUS:
				case DataRules.RULE_GROUP_1_2_3PLUS:
				case DataRules.RULE_UNIQUE_1_2_3PLUS:
					ordersCoder.value = masterCode.valueA;
					if (ordersCoder.qty > 1) {
						ordersCoder.value += masterCode.valueB;
					}
					if (ordersCoder.qty > 2) {
						ordersCoder.value += (masterCode.valueC * (ordersCoder.qty - 2));
					}
					break;
				case DataRules.RULE_AFTER_1_2_X:
				case DataRules.RULE_GROUP_1_2_X:
				case DataRules.RULE_UNIQUE_1_2_X:
					ordersCoder.value = masterCode.valueA;
					if (ordersCoder.qty > 1) {
						ordersCoder.value += masterCode.valueB;
					}
					if (ordersCoder.qty >= masterCode.count) {
						ordersCoder.value += masterCode.valueC;
					}
					break;
				default:
					ordersCoder.value = masterCode.valueA * ordersCoder.qty;
				}
				specimenCoder.codeValue += ordersCoder.value;
				caseCoder.codeValue += ordersCoder.value;
				caseCoder.comment += "codeOrders: Specimen " + (i+1) +
						", Orders: " + ordersCoder.name + ", Qty: " + ordersCoder.qty +
						", Value: " + numbers.formatDouble(3, ordersCoder.value) +
						", Case Value: " + numbers.formatDouble(3, caseCoder.codeValue) +
						Constants.NEW_LINE;
			}
		}
	}

	private void codeSpecimens() {
		short masterID = 0, noSpecs = 0, noLinks = 0, noFrags = 0, noBlocks = 0, prevRule = 0;
		double dExpect = 0, dSpecs = 0, dLinks = 0, dFrags = 0, dBlocks = 0;
		for (int i = 0; i < thisCase.noSpecimens; i++) {
			specimenCoder = lstSpecimenCoders.get(i);
			thisSpecimen = thisCase.lstSpecimens.get(i);
			masterCode = masterCodes.get(specimenCoder.coderID);
			switch (masterCode.rule) {
			case DataRules.RULE_IGNORE:
				// Nothing for this particular specimen (molecular, Derm IF, Iron Quant)
				break;
			case DataRules.RULE_SPECIMEN_INCLUSIVE:
			case DataRules.RULE_SPECIMEN_FIXED:
				// Example, autopsy, refer-out
				specimenCoder.codeValue = masterCode.valueA;
				break;
			case DataRules.RULE_SPECIMEN_EVERY_X_MIN_MAX:
				if (masterID != thisSpecimen.masterID || prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					masterID = thisSpecimen.masterID;
					dSpecs = 0;
					noSpecs = 0;
				}
				noSpecs++;
				dExpect = masterCode.valueA * numbers.ceiling(noSpecs, masterCode.count);
				dValue = dExpect - dSpecs;
				specimenCoder.codeValue = setMinMax(dValue, dSpecs, dExpect, masterCode.valueB, masterCode.valueC);
				dSpecs += specimenCoder.codeValue;
				break;
			case DataRules.RULE_SPECIMEN_1_2_3PLUS:
				if (masterID != thisSpecimen.masterID || prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					masterID = thisSpecimen.masterID;
					dSpecs = 0;
					noSpecs = 0;
				}
				noSpecs++;
				if (noSpecs == 1) {
					specimenCoder.codeValue = masterCode.valueA;
				} else if (noSpecs == 2) {
					specimenCoder.codeValue = masterCode.valueB;
				} else {
					specimenCoder.codeValue = masterCode.valueC;
				}
				break;
			case DataRules.RULE_SPECIMEN_1_2_X:
				if (masterID != thisSpecimen.masterID || prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					masterID = thisSpecimen.masterID;
					noSpecs = 0;
				}
				noSpecs++;
				if (noSpecs == 1) {
					specimenCoder.codeValue = masterCode.valueA;
				} else if (noSpecs == 2) {
					specimenCoder.codeValue = masterCode.valueB;
				} else if (noLinks == masterCode.count) {
					specimenCoder.codeValue = masterCode.valueC;
				}
				break;
			case DataRules.RULE_SPECIMEN_GROSS_MICRO:
				// Femoral heads
				if (thisSpecimen.noSlides > 0) {
					specimenCoder.codeValue = masterCode.valueB;
				} else {
					specimenCoder.codeValue = masterCode.valueA;
				}
				break;
			case DataRules.RULE_SPECIMEN_FRAGS_X_MIN_MAX:
				// CAP Medical GI biopsies
				dValue = masterCode.valueA * numbers.ceiling(thisSpecimen.noFragments, masterCode.count);
				specimenCoder.codeValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
				break;
			case DataRules.RULE_SPECIMEN_FRAGS_1_2_3PLUS:
				specimenCoder.codeValue = masterCode.valueA;
				if (thisSpecimen.noFragments > 1) {
					specimenCoder.codeValue += masterCode.valueB;
					if (thisSpecimen.noFragments > 2) {
						specimenCoder.codeValue += (masterCode.valueC * (thisSpecimen.noFragments -2));
					}
				}
				break;
			case DataRules.RULE_SPECIMEN_FRAGS_1_2_X:
				specimenCoder.codeValue = masterCode.valueA;
				if (thisSpecimen.noFragments > 1) {
					specimenCoder.codeValue += masterCode.valueB;
					if (thisSpecimen.noFragments >= masterCode.count) {
						specimenCoder.codeValue += masterCode.valueC;
					}
				}
				break;
			case DataRules.RULE_SPECIMEN_FRAGS_BLOCKS:
				// CAP GI Polyps, skin resections
				specimenCoder.codeValue = masterCode.valueA * numbers.ceiling(thisSpecimen.noFragments, masterCode.count);
				dValue = masterCode.valueB * numbers.ceiling(thisSpecimen.noBlocks, masterCode.count);
				if (specimenCoder.codeValue < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.codeValue = dValue;
				}
				break;
			case DataRules.RULE_SPECIMEN_BLOCKS_X_MIN_MAX:
				dValue = masterCode.valueA * numbers.ceiling(thisSpecimen.noBlocks, masterCode.count);
				specimenCoder.codeValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
				break;
			case DataRules.RULE_SPECIMEN_BLOCKS_1_2_3PLUS:
				specimenCoder.codeValue = masterCode.valueA;
				if (thisSpecimen.noBlocks > 1) {
					specimenCoder.codeValue += masterCode.valueB;
					if (thisSpecimen.noBlocks > 2) {
						specimenCoder.codeValue += (masterCode.valueC * (thisSpecimen.noBlocks -2));
					}
				}
				break;
			case DataRules.RULE_SPECIMEN_BLOCKS_1_2_X:
				specimenCoder.codeValue = masterCode.valueA;
				if (thisSpecimen.noBlocks > 1) {
					specimenCoder.codeValue += masterCode.valueB;
					if (thisSpecimen.noBlocks >= masterCode.count) {
						specimenCoder.codeValue += masterCode.valueC;
					}
				}
				break;
			case DataRules.RULE_LINKED_INCLUSIVE:
			case DataRules.RULE_LINKED_FIXED:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.codeValue = masterCode.valueA;
				}
				break;
			case DataRules.RULE_LINKED_FRAGS_X_MIN_MAX:
				// CAP Breast/Prostate Bx (max 20 cores per case = 10 L4E)
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.noFragments;
				dExpect = masterCode.valueA * numbers.ceiling(noFrags, masterCode.count);
				dValue = dExpect - dFrags;
				specimenCoder.codeValue = setMinMax(dValue, dLinks, dExpect, masterCode.valueB, masterCode.valueC);
				dFrags += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_FRAGS_1_2_3PLUS:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.noFragments;
				if (noFrags >= 1
						&& dFrags < masterCode.valueA) {
					specimenCoder.codeValue += masterCode.valueA;
				}
				if (noFrags >= 2
						&& dFrags < masterCode.valueA + masterCode.valueB) {
					specimenCoder.codeValue += masterCode.valueB;
				}
				if (noFrags >= 3
						&& dFrags < masterCode.valueA + masterCode.valueB +
						(masterCode.valueC * (noFrags -2))) {
					specimenCoder.codeValue += (masterCode.valueC * (noFrags -2));
				}
				dFrags += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_FRAGS_1_2_X:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dFrags = 0;
					noFrags = 0;
				}
				noFrags += thisSpecimen.noFragments;
				if (noFrags >= 1
						&& dFrags < masterCode.valueA) {
					specimenCoder.codeValue += masterCode.valueA;
				}
				if (noFrags >= 2
						&& dFrags < masterCode.valueA + masterCode.valueB) {
					specimenCoder.codeValue += masterCode.valueB;
				}
				if (noFrags >= masterCode.count
						&& dFrags < masterCode.valueA + masterCode.valueB + masterCode.valueC) {
					specimenCoder.codeValue += masterCode.valueC;
				}
				dFrags += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_FRAGS_BLOCKS:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dBlocks = 0;
					dFrags = 0;
					noFrags = 0;
					noBlocks = 0;
				}
				noFrags += thisSpecimen.noFragments;
				noBlocks += thisSpecimen.noBlocks;
				specimenCoder.codeValue = (masterCode.valueA * numbers.ceiling(noFrags, masterCode.count)) -dFrags;
				dValue = (masterCode.valueB * numbers.ceiling(noBlocks, masterCode.count)) -dBlocks;
				dFrags += specimenCoder.codeValue;
				dBlocks += dValue;
				if (specimenCoder.codeValue < dValue) {
					// Use # blocks to get the higher value
					specimenCoder.codeValue = dValue;
				}
			case DataRules.RULE_LINKED_BLOCKS_X_MIN_MAX:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.noBlocks;
				dExpect = masterCode.valueA * numbers.ceiling(noBlocks, masterCode.count);
				dValue = dExpect - dBlocks;
				specimenCoder.codeValue = setMinMax(dValue, dBlocks, dExpect, masterCode.valueB, masterCode.valueC);
				dBlocks += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_BLOCKS_1_2_3PLUS:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.noBlocks;
				if (noBlocks >= 1 && dBlocks < masterCode.valueA) {
					specimenCoder.codeValue += masterCode.valueA;
				}
				if (noBlocks >= 2 && dBlocks < masterCode.valueA + masterCode.valueB) {
					specimenCoder.codeValue += masterCode.valueB;
				}
				if (noBlocks >= 3 && dBlocks < masterCode.valueA + masterCode.valueB + (masterCode.valueC * (noBlocks -2))) {
					specimenCoder.codeValue += (masterCode.valueC * (noBlocks -2));
				}
				dBlocks += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_BLOCKS_1_2_X:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dBlocks = 0;
					noBlocks = 0;
				}
				noBlocks += thisSpecimen.noBlocks;
				if (noBlocks >= 1 && dBlocks < masterCode.valueA) {
					specimenCoder.codeValue += masterCode.valueA;
				}
				if (noBlocks >= 2
						&& dBlocks < masterCode.valueA + masterCode.valueB) {
					specimenCoder.codeValue += masterCode.valueB;
				}
				if (noBlocks >= masterCode.count && dBlocks < masterCode.valueA + masterCode.valueB + masterCode.valueC) {
					specimenCoder.codeValue += masterCode.valueC;
				}
				dBlocks += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_EVERY_X_MIN_MAX:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					dLinks = 0;
					noLinks = 0;
				}
				noLinks++;
				dExpect = masterCode.valueA * numbers.ceiling(noLinks, masterCode.count);
				dValue = dExpect - dLinks;
				specimenCoder.codeValue = setMinMax(dValue, dLinks, dExpect, masterCode.valueB, masterCode.valueC);
				dLinks += specimenCoder.codeValue;
				break;
			case DataRules.RULE_LINKED_1_2_3PLUS:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.codeValue = masterCode.valueA;
				} else if (noLinks == 2) {
					specimenCoder.codeValue = masterCode.valueB;
				} else {
					specimenCoder.codeValue = masterCode.valueC;
				}
				break;
			case DataRules.RULE_LINKED_1_2_X:
				if (prevRule != masterCode.rule) {
					prevRule = masterCode.rule;
					noLinks = 0;
				}
				noLinks++;
				if (noLinks == 1) {
					specimenCoder.codeValue = masterCode.valueA;
				} else if (noLinks == 2) {
					specimenCoder.codeValue = masterCode.valueB;
				} else if (noLinks == masterCode.count) {
					specimenCoder.codeValue = masterCode.valueC;
				}
				break;
			default:
				caseCoder.hasError = true;
				caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
			}
			caseCoder.codeValue += specimenCoder.codeValue;
			caseCoder.comment += "Specimen " + (i+1) +
					": Links " + noLinks + ", Rule " + masterCode.rule +
					", Value " + numbers.formatDouble(3, specimenCoder.codeValue) +
					", Case " + numbers.formatDouble(3, caseCoder.codeValue) +
					Constants.NEW_LINE;
		}
	}

	private void codeSynoptics() {
		// How to code the Synoptic Report itself (not the malignant specimen)
		if (thisCase.mainSpecimenNo > 0) {
			thisSpecimen = thisCase.lstSpecimens.get(thisCase.mainSpecimenNo -1);
			specimenCoder = lstSpecimenCoders.get(thisCase.mainSpecimenNo -1);
		} else {
			thisSpecimen = thisCase.lstSpecimens.get(0);
			specimenCoder = lstSpecimenCoders.get(0);
		}
		dValue = 0;
		masterCode = masterCodes.get(RULE_SYNOPTICS);
		switch (masterCode.rule) {
		case DataRules.RULE_IGNORE:
			// RCP-UK
			break;
		case DataRules.RULE_CASE_INCLUSIVE:
		case DataRules.RULE_GROUP_CASE_INCLUSIVE:
		case DataRules.RULE_UNIQUE_CASE_INCLUSIVE:
		case DataRules.RULE_AFTER_CASE_INCLUSIVE:
			dValue = masterCode.valueA;
			break;
		case DataRules.RULE_CASE_FIXED:
			dValue = caseCoder.codeValue * thisCase.noSynoptics;
			break;
		case DataRules.RULE_SPECIMEN_INCLUSIVE:
		case DataRules.RULE_SPECIMEN_FIXED:
		case DataRules.RULE_LINKED_INCLUSIVE:
		case DataRules.RULE_LINKED_FIXED:
		case DataRules.RULE_GROUP_SPECIMEN_INCLUSIVE:
		case DataRules.RULE_UNIQUE_SPECIMEN_INCLUSIVE:
		case DataRules.RULE_AFTER_SPECIMEN_INCLUSIVE:
			if (thisCase.noSpecimens > thisCase.noSynoptics) {
				dValue = caseCoder.codeValue * thisCase.noSynoptics;
			} else {
				dValue = caseCoder.codeValue * thisCase.noSpecimens;
			}
			break;
		case DataRules.RULE_SPECIMEN_EVERY_X_MIN_MAX:
		case DataRules.RULE_LINKED_EVERY_X_MIN_MAX:
		case DataRules.RULE_GROUP_EVERY_X_MIN_MAX:
		case DataRules.RULE_UNIQUE_EVERY_X_MIN_MAX:
		case DataRules.RULE_AFTER_EVERY_X_MIN_MAX:
			// W2Q
			dValue = masterCode.valueA * numbers.ceiling(thisCase.noSynoptics, masterCode.count);
			dValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
			break;
		case DataRules.RULE_SPECIMEN_1_2_3PLUS:
		case DataRules.RULE_LINKED_1_2_3PLUS:
		case DataRules.RULE_GROUP_1_2_3PLUS:
		case DataRules.RULE_UNIQUE_1_2_3PLUS:
		case DataRules.RULE_AFTER_1_2_3PLUS:
			dValue = masterCode.valueA;
			if (thisCase.noSynoptics > 1) {
				// 2nd synoptic report (0=ignore)
				dValue += masterCode.valueB;
				if (thisCase.noSynoptics > 2) {
					// 3+ synoptic reports
					dValue += (masterCode.valueC * (thisCase.noSynoptics - 2));
				}
			}
			break;
		case DataRules.RULE_SPECIMEN_1_2_X:
		case DataRules.RULE_LINKED_1_2_X:
		case DataRules.RULE_GROUP_1_2_X:
		case DataRules.RULE_UNIQUE_1_2_X:
		case DataRules.RULE_AFTER_1_2_X:
			dValue = masterCode.valueA;
			if (thisCase.noSynoptics > 1) {
				// 2nd synoptic report (0=ignore)
				dValue += masterCode.valueB;
				if (thisCase.noSynoptics >= masterCode.count) {
					// synoptic report X
					dValue += masterCode.valueC;
				}
			}
			break;
		default:
			caseCoder.hasError = true;
			caseCoder.errorID = Constants.ERROR_CODING_RULE_UNKNOWN;
			comment = "ERROR: codeSynoptics, " + thisCase.caseNo + ", " +
					Constants.ERROR_STRINGS[caseCoder.errorID];
			caseCoder.comment += comment + Constants.NEW_LINE;
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, comment);
		}
		caseCoder.codeValue += dValue;
		caseCoder.comment += "Synoptic: Rule " + masterCode.rule +
				", Value " + numbers.formatDouble(3, dValue) +
				", Case " + numbers.formatDouble(3, caseCoder.codeValue) +
				Constants.NEW_LINE;
	}

	double getAddl(short codeID, short qty) {
		masterCode = masterCodes.get(codeID);
		switch (masterCode.rule) {
		case DataRules.RULE_ADDL_CASE_INCLUSIVE:
		case DataRules.RULE_ADDL_SPECIMEN_INCLUSIVE:
			dValue = masterCode.valueA;
			break;
		case DataRules.RULE_ADDL_EVERY_X_MIN_MAX:
			dValue = masterCode.valueA *
			numbers.ceiling(qty, masterCode.count);
			dValue = setMinMax(dValue, masterCode.valueB, masterCode.valueC);
			break;
		case DataRules.RULE_ADDL_1_2_3PLUS:
			dValue = masterCode.valueA;
			if (qty > 1) {
				dValue += masterCode.valueB;
			}
			if (qty > 2) {
				dValue += (masterCode.valueC * (qty - 2));
			}
			break;
		case DataRules.RULE_ADDL_1_2_X:
			dValue = masterCode.valueA;
			if (qty > 1) {
				dValue += masterCode.valueB;
			}
			if (qty >= masterCode.count) {
				dValue += masterCode.valueC;
			}
			break;
		default:
			dValue = 0;
		}
		if (dValue > 99.9) {
			dValue = 99.9;
		}
		return dValue;
	}

	String getComment() {
		return (coderName + Constants.NEW_LINE +
				caseCoder.comment + "--------------------------" + Constants.NEW_LINE);
	}

	double getCorrelations() {
		dValue = 0;
		masterCode = masterCodes.get(RULE_CORRELATIONS);
		if (masterCode != null) {
			dValue = masterCode.valueA;
		}
		return dValue;
	}

	double getFrozen() {
		return caseCoder.frozenValue;
	}

	double getFrozen(int specimenNo) {
		return lstSpecimenCoders.get(specimenNo).frozenValue;
	}

	double getOrder(int specimenNo, short groupID) {
		dValue = 0;
		ordersCoder = lstSpecimenCoders.get(specimenNo).lstOrders.get(groupID);
		if (ordersCoder != null) {
			dValue = ordersCoder.value;
			if (dValue > 99.9) {
				dValue = 99.9;
			}
		}
		return dValue;
	}

	double getValue() {
		dValue = caseCoder.codeValue;
		if (dValue > 99.9) {
			dValue = 99.9;
		}
		return dValue;
	}

	double getValue(int specimenNo) {
		dValue = lstSpecimenCoders.get(specimenNo).codeValue;
		if (dValue > 99.9) {
			dValue = 99.9;
		}
		return dValue;
	}

	boolean hasComment() {
		return (caseCoder.comment.length() > 0);
	}

	boolean hasError() {
		if (!isActive) return false;
		return caseCoder.hasError;
	}

	boolean needsFragments(int specimenNo) {
		return lstSpecimenCoders.get(specimenNo).needsFragments;
	}

	void newCase(DataCaseFinal thisCase) {
		lstSpecimenCoders.clear();
		caseCoder = new CaseCoder();
		thisSpecimen = new DataSpecimenFinal();
		specimenCoder = new SpecimenCoder();
		ordersCoder = new OrdersCoder();
		this.thisCase = thisCase;
	}

	private void readTables(byte coderID) {
		ResultSet rst = parent.dbPowerJ.getCoder(coderID);
		try {
			coderName = parent.variables.codersName[coderID -1];
			while (rst.next()) {
				masterCode = new MasterCode();
				masterCode.rule = rst.getShort("RULEID");
				masterCode.count = rst.getShort("COUNT");
				masterCode.valueA = rst.getDouble("VALUE1");
				masterCode.valueB = rst.getDouble("VALUE2");
				masterCode.valueC = rst.getDouble("VALUE3");
				masterCode.name = rst.getString("NAME");
				masterCodes.put(rst.getShort("ID"), masterCode);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, coderName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private double setMinMax(double value, double min, double max) {
		// Unlinked: Min <= value <= Max
		double result = value;
		if (result < min) {
			result = min;
		}
		if (max > 0 && result > max) {
			result = max;
		}
		return result;
	}

	private double setMinMax(double value, double accumulated,
			double expected, double min, double max) {
		// Linked: Min <= value+accumulated <= Max
		double result = 0;
		if (max > 0 && value + accumulated > max) {
			// Max
			result = max - accumulated;
		} else if (value + accumulated < min) {
			// Min
			result = min - accumulated;
		} else if (value + accumulated > expected) {
			// Adjustment for Min
			result = expected - accumulated;
			if (result < 0) result = 0;
		} else {
			result = value;
		}
		return result;
	}
	
	private class CaseCoder {
		boolean hasError = false;
		boolean foundMalignant = false;
		boolean foundRadical = false;
		boolean inclusive = false;
		boolean codeAddBlocks = true;
		byte errorID = 0;
		byte mainSpecimenNo = 0;
		short coderID = 0;
		double codeValue = 0;
		double frozenValue = 0;
		String comment = "";
	}

	private class MasterCode {
		short rule = 0;
		short count = 0;
		double valueA = 0;
		double valueB = 0;
		double valueC = 0;
		String name = "";
	}

	private class OrdersCoder {
		boolean isAddlBlock = false;
		short codeID = 0;
		short qty = 0;
		double value = 0;
		String name = "";
		private ArrayList<Short> orders = new ArrayList<Short>();
		
		boolean isUnique(short orderID) {
			for (int i = 0; i < orders.size(); i++) {
				if (orders.get(i) == orderID) {
					return false;
				}
			}
			orders.add(orderID);
			return true;
		}
	}

	private class SpecimenCoder {
		boolean codeAddBlocks = true;
		boolean inclusive = false;
		boolean needsFragments = false;
		byte procID = 0;
		short coderID = 0;
		short coderMalig = 0;
		short coderRadical = 0;
		double codeValue = 0;
		double frozenValue = 0;
		HashMap<Short, OrdersCoder> lstOrders = new HashMap<Short, OrdersCoder>();
	}
}
