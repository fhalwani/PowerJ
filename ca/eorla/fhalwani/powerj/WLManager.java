package ca.eorla.fhalwani.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

class WLManager {
	private short noFrags = 0, noLesions = 0, number = 0;
	private int index = 0;
	private long lastUpdate = 0;
	private long maxDate = 0;
	private final String className = "Workload";
	private String string = "";
	private String comment = "";
	// Search strings for # of fragments in gross description
	private final String[] SEARCH_STRINGS = {"number of lesions removed",
		"number of excisions", "number of fragments", "number of pieces"};
	private Pattern pattern;
	private Matcher matcher;
	private MapFacilities facilities;
	private MapAccessions accessions;
	private MapOrders masterOrders;
	private MapTemplates masterSpecimens;
	private DataCaseFinal thisCase = new DataCaseFinal();
	private DataSpecimenFinal thisSpecimen = new DataSpecimenFinal();
	private DataSpecimenOrder thisOrder = new DataSpecimenOrder();
	private DataMasterSpecimen masterSpecimen = new DataMasterSpecimen();
	private WLCoder coder1, coder2, coder3, coder4;
	private PowerJ parent;
	private DbAPIS dbAP;
	private DbPowerJ dbPowerJ;

	WLManager(PowerJ parent) {
		new WLManager(parent, 0, "");
	}
	
	WLManager(PowerJ parent, long caseID, String caseNo) {
		this.parent = parent;
		parent.log(JOptionPane.INFORMATION_MESSAGE, className,
				parent.dateUtils.formatter(parent.dateUtils.FORMAT_DATETIME) +
				" - Workload Manager Started...");
		dbPowerJ = parent.dbPowerJ;
		dbAP = new DbAPIS(parent);
		if (!dbAP.connected) {
			return;
		}
		facilities = new MapFacilities(parent);
		if (!(parent.variables.hasError || parent.abort())) {
			masterSpecimens = new MapTemplates(parent);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			masterOrders = new MapOrders(parent);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			accessions = new MapAccessions(parent);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			coder1 = new WLCoder(parent, (byte)1);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			coder2 = new WLCoder(parent, (byte)2);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			coder3 = new WLCoder(parent, (byte)3);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			coder4 = new WLCoder(parent, (byte)4);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			dbAP.prepareWorkload();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			dbPowerJ.prepareWorkload();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			// Initialize the text parser that finds first number in a string
			String s = "String 17 String";
			pattern = Pattern.compile(".*?(\\d+).*");
			matcher = pattern.matcher(s);
			// Do the work
			if (caseID > 0) {
				// For Debug
				codeCase(caseID);
			} else if (caseNo.length() > 4) {
				// For Debug
				getCase(caseNo);
			} else {
				getLastUpdate();
				if (maxDate - lastUpdate > 7200000) {
					// Else, we are up to date (min run every 2 hours)
					if (!(parent.variables.hasError || parent.abort())) {
						getCases();
					}
					if (!(parent.variables.hasError || parent.abort())) {
						redo();
					}
					if (!(parent.variables.hasError || parent.abort())) {
						dbPowerJ.prepareAdditionals();
					}
					if (!(parent.variables.hasError || parent.abort())) {
						dbAP.prepareAmendments();
					}
					if (!(parent.variables.hasError || parent.abort())) {
						getAmendments();
					}
					if (!(parent.variables.hasError || parent.abort())) {
						getCorrelations();
					}
					if (!(parent.variables.hasError || parent.abort())) {
						getAdditional();
					}
				}
			}
		}
		close();
	}

	private void close() {
		if (facilities != null) {
			facilities.close();
		}
		if (accessions != null) {
			accessions.close();
		}
		if (masterSpecimens != null) {
			masterSpecimens.close();
		}
		if (masterOrders != null) {
			masterOrders.close();
		}
		if (coder1 != null) {
			coder1.close();
		}
		if (coder2 != null) {
			coder2.close();
		}
		if (coder3 != null) {
			coder3.close();
		}
		if (coder4 != null) {
			coder4.close();
		}
		dbPowerJ.closeStms();
		dbAP.closeStms();
		dbAP.close();
	}

	private boolean codeCase(long caseID) {
		boolean success = false;
		PreparedStatement stm = dbAP.getStatement(2);
		ResultSet rst = null;
		try {
			stm.setLong(1, caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (accessions.doWorkload(rst.getShort("acc_type_id"))
						&& facilities.doWorkload(rst.getShort("facility_id"))) {
					thisCase = new DataCaseFinal();
					thisCase.caseID = caseID;
					thisCase.specialtyID = accessions.getSpecialty();
					thisCase.facilityID = rst.getShort("facility_id");
					thisCase.codeSpecimens = accessions.codeSpecimen();
					thisCase.caseNo = rst.getString("accession_no");
					thisCase.staffID = rst.getShort("assigned_to_id");
					thisCase.finalTime = rst.getTimestamp("completed_date").getTime();
					thisCase.accessionTime = rst.getTimestamp("created_date").getTime();
					parent.log(JOptionPane.INFORMATION_MESSAGE, className,
							"Coding case " + thisCase.caseNo);
					// We need to know if the case is malignant before-hand
					getSynoptics();
					coder1.newCase(thisCase);
					coder2.newCase(thisCase);
					coder3.newCase(thisCase);
					coder4.newCase(thisCase);
					getGrossTime();
					getRouteTime();
					getSpecimens();
					if (thisCase.needsFragments) {
						// Extract # of fragments from gross description
						getGrossDescr();
					}
					coder1.codeCase();
					coder2.codeCase();
					coder3.codeCase();
					coder4.codeCase();
					success = true;
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.INFORMATION_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
		return success;
	}
	
	private boolean deleteError(long caseID) {
		int i = dbPowerJ.deleteError(caseID);
		return (i > 0);
	}
	
	private void getAdditional() {
		boolean isDuplicate = false, exists = false;
		short orderID = 0, qty = 0;
		int noRows = 0, noCases = 0;
		long startTime = System.nanoTime();
		double dValue1 = 0, dValue2 = 0, dValue3 = 0, dValue4 = 0;
		Calendar calCurrent = Calendar.getInstance();
		Calendar calPrevious = Calendar.getInstance();
		ResultSet rstGetOrders = null, rstGetPrevious = null, rstGetAddl = null;
		PreparedStatement stmGetAddl = null, stmGetPrevious = null, stmInsert = null;
		thisCase = new DataCaseFinal();
		try {
			// Still using prepareAdditionals()
			stmGetPrevious = dbPowerJ.getStatement(0);
			stmGetAddl = dbPowerJ.getStatement(1);
			stmInsert = dbPowerJ.getStatement(2);
			rstGetOrders = dbAP.getOrders(lastUpdate, maxDate);
			while (rstGetOrders.next()) {
				if (++noRows % 100 == 0) {
					if (parent.abort()) {
						break;
					}
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {}
				}
				if (thisCase.caseID != rstGetOrders.getLong("acc_id")) {
					thisCase = new DataCaseFinal();
					thisCase.caseID = rstGetOrders.getLong("acc_id");
					// Must exist in PowerJ in Cases Table
					stmGetPrevious.setLong(1, thisCase.caseID);
					rstGetPrevious = stmGetPrevious.executeQuery();
					exists = false;
					while (rstGetPrevious.next()) {
						thisCase.caseNo = rstGetPrevious.getString("CASENO");
						exists = true;
					}
					rstGetPrevious.close();
					if (!exists) {
						continue;
					}
				} else if (!exists) {
					// Case does not exist in PowerJ
					continue;
				}
				thisCase.finalTime = rstGetOrders.getTimestamp("created_date").getTime();
				thisCase.staffID = rstGetOrders.getShort("ordered_by_id");
				orderID = rstGetOrders.getShort("procedure_id");
				qty = rstGetOrders.getShort("quantity");
				calCurrent.setTimeInMillis(thisCase.finalTime);
				// Order must be be an additional rule
				if (masterOrders.matchOrder(orderID)) {
					// Order must be of category additional (OrderType in Groups)
					if (masterOrders.getOrderType() != DataOrderType.ADDITINAL) {
						continue;
					}
					dValue1 = coder1.getAddl(masterOrders.getCodeID(1), qty);
					dValue2 = coder2.getAddl(masterOrders.getCodeID(2), qty);
					dValue3 = coder3.getAddl(masterOrders.getCodeID(3), qty);
					dValue4 = coder4.getAddl(masterOrders.getCodeID(4), qty);
					if (dValue1 > 0.001
							|| dValue2 > 0.001
							|| dValue3 > 0.001
							|| dValue4 > 0.001) {
						// Cannot exist in PowerJ in Additional Table
						stmGetAddl.setLong(1, thisCase.caseID);
						rstGetAddl = stmGetAddl.executeQuery();
						isDuplicate = false;
						while (rstGetAddl.next()) {
							thisCase.caseNo = rstGetAddl.getString("CASENO");
							thisCase.accessionTime = rstGetAddl.getTimestamp("FINALED").getTime();
							calPrevious.setTimeInMillis(thisCase.accessionTime);
							if (thisCase.staffID == rstGetAddl.getShort("PERID")) {
								if (orderID == rstGetAddl.getShort("CODEID")) {
									if (calPrevious.get(Calendar.DAY_OF_YEAR) == calCurrent.get(Calendar.DAY_OF_YEAR)) {
										// No duplicates on the same day
										isDuplicate = true;
									}
								}
							}
						}
						rstGetAddl.close();
						if (!isDuplicate) {
							parent.log(JOptionPane.INFORMATION_MESSAGE, className,
									"Coding Additional on case " + thisCase.caseID);
							stmInsert.setLong(1, thisCase.caseID);
							stmInsert.setShort(2, thisCase.staffID);
							stmInsert.setShort(3, orderID);
							stmInsert.setDate(4, new java.sql.Date(thisCase.finalTime));
							stmInsert.setDouble(5, dValue1);
							stmInsert.setDouble(6, dValue2);
							stmInsert.setDouble(7, dValue3);
							stmInsert.setDouble(8, dValue4);
							stmInsert.executeUpdate();
							noCases++;
							try {
								Thread.sleep(Constants.SLEEP_TIME);
							} catch (InterruptedException e) {}
						}
					}
				}
			}
			long noSeconds = (System.nanoTime() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Workload Coded " + noCases + " additional orders in " +
		    			noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rstGetOrders);
		}
	}
	
	private void getAmendments() {
		boolean isDuplicate = true, exists = false;
		short orderID = 0, codeID = 0;
		int noRows = 0, noCases = 0;
		long startTime = System.nanoTime();
		double dValue1 = 0, dValue2 = 0, dValue3 = 0, dValue4 = 0;
		Calendar calAmend = Calendar.getInstance();
		Calendar calPrevious = Calendar.getInstance();
		String descr = "";
		ResultSet rstGetCases = null, rstGetPrevious = null, rstGetOrders = null;
		ResultSet rstGetAddl = null;
		PreparedStatement stmGetCases = null, stmGetPrevious = null, stmGetOrders = null;
		PreparedStatement stmGetAddl = null, stmInsert = null;
		try {
			// Still using prepareAdditionals()
			stmGetPrevious = dbPowerJ.getStatement(0);
			stmGetAddl = dbPowerJ.getStatement(1);
			stmInsert = dbPowerJ.getStatement(2);
			stmGetCases = dbAP.getStatement(0);
			stmGetOrders = dbAP.getStatement(1);
			stmGetCases.setTimestamp(1, new Timestamp(lastUpdate));
			stmGetCases.setTimestamp(2, new Timestamp(maxDate));
			rstGetCases = stmGetCases.executeQuery();
			while (rstGetCases.next()) {
				if (parent.abort()) {
					break;
				}
				if (++noRows % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {}
				}
				thisCase = new DataCaseFinal();
				thisCase.codeSpecimens = false;
				thisCase.caseID = rstGetCases.getLong("acc_id");
				thisCase.finalTime = rstGetCases.getTimestamp("completed_date").getTime();
				thisCase.staffID = rstGetCases.getShort("assigned_to_id");
				calAmend.setTimeInMillis(thisCase.finalTime);
				descr = rstGetCases.getString("description").trim().toLowerCase();
				if (descr.equals("amendment final")) {
					codeID = 1;
				} else if (descr.equals("addendum final")) {
					codeID = 2;
				} else {
					continue;
				}
				// Cannot exist in PowerJ in Additional Table
				isDuplicate = false;
				stmGetAddl.setLong(1, thisCase.caseID);
				rstGetAddl = stmGetAddl.executeQuery();
				isDuplicate = false;
				while (rstGetAddl.next()) {
					thisCase.caseNo = rstGetAddl.getString("CASENO");
					thisCase.accessionTime = rstGetAddl.getTimestamp("FINALED").getTime();
					calPrevious.setTimeInMillis(thisCase.accessionTime);
					if (thisCase.staffID == rstGetAddl.getShort("PERID")) {
						if (codeID == rstGetAddl.getShort("CODEID")) {
							if (calPrevious.get(Calendar.DAY_OF_YEAR) == calAmend.get(Calendar.DAY_OF_YEAR)) {
								// No duplicates on the same day
								isDuplicate = true;
							}
						}
					}
				}
				rstGetAddl.close();
				if (!isDuplicate) {
					// Must exist in PowerJ in Cases Table for table foreign key
					// Must also get a specimen for WLCoder
					stmGetPrevious.setLong(1, thisCase.caseID);
					rstGetPrevious = stmGetPrevious.executeQuery();
					exists = false;
					while (rstGetPrevious.next()) {
						thisCase.caseNo = rstGetPrevious.getString("CASENO");
						thisCase.accessionTime = rstGetPrevious.getTimestamp("FINALED").getTime();
						if (masterSpecimens.matchSpecimens(rstGetPrevious.getInt("MSID"))) {
							masterSpecimen = masterSpecimens.getItem();
							thisSpecimen = new DataSpecimenFinal();
							thisSpecimen.specimenID = rstGetPrevious.getLong("SPECID");
							thisSpecimen.masterID = rstGetPrevious.getShort("MSID");
							thisCase.lstSpecimens.add(thisSpecimen);
							thisCase.noSpecimens = 1;
							exists = true;
						}
					}
					rstGetPrevious.close();
					if (exists && thisCase.noSpecimens > 0) {
						parent.log(JOptionPane.INFORMATION_MESSAGE, "WLManager",
								"Coding " + descr + ": " +
								thisCase.caseNo);
						coder1.newCase(thisCase);
						coder2.newCase(thisCase);
						coder3.newCase(thisCase);
						coder4.newCase(thisCase);
						coder1.addSpecimen(thisSpecimen.procedureID,
							masterSpecimen.coder1B, masterSpecimen.coder1M,
							masterSpecimen.coder1R);
						coder2.addSpecimen(thisSpecimen.procedureID,
							masterSpecimen.coder2B, masterSpecimen.coder2M,
							masterSpecimen.coder2R);
						coder3.addSpecimen(thisSpecimen.procedureID,
							masterSpecimen.coder3B, masterSpecimen.coder3M,
							masterSpecimen.coder3R);
						coder4.addSpecimen(thisSpecimen.procedureID,
								masterSpecimen.coder4B, masterSpecimen.coder4M,
								masterSpecimen.coder4R);
						stmGetOrders.setLong(1, thisCase.caseID);
						stmGetOrders.setTimestamp(2, new Timestamp(thisCase.accessionTime));
						stmGetOrders.setTimestamp(3, new Timestamp(thisCase.finalTime));
						rstGetOrders = stmGetOrders.executeQuery();
						while (rstGetOrders.next()) {
							orderID = rstGetOrders.getShort("procedure_id");
							if (masterOrders.matchOrder(orderID)) {
								if (masterOrders.getOrderType()== DataOrderType.IGNORE) {
									continue;
								}
								thisOrder = thisSpecimen.lstOrders.get(masterOrders.getGroupID());
								if (thisOrder == null) {
									thisOrder = new DataSpecimenOrder();
									thisOrder.groupID = masterOrders.getGroupID();
									thisOrder.name = masterOrders.getOrderName();
									thisSpecimen.lstOrders.put(masterOrders.getGroupID(), thisOrder);
								}
								thisOrder.qty += rstGetOrders.getShort("quantity");
								coder1.addOrder(orderID, masterOrders.getGroupID(),
										masterOrders.getCodeID(1), thisOrder.qty, false, false, 0);
								coder2.addOrder(orderID, masterOrders.getGroupID(),
										masterOrders.getCodeID(2), thisOrder.qty, false, false, 0);
								coder3.addOrder(orderID, masterOrders.getGroupID(),
										masterOrders.getCodeID(3), thisOrder.qty, false, false, 0);
								coder4.addOrder(orderID, masterOrders.getGroupID(),
										masterOrders.getCodeID(4), thisOrder.qty, false, false, 0);
							}
						}
						coder1.codeCase();
						coder2.codeCase();
						coder3.codeCase();
						coder4.codeCase();
						dValue1 = coder1.getValue();
						dValue2 = coder2.getValue();
						dValue3 = coder3.getValue();
						dValue4 = coder4.getValue();
						if (dValue1 > 0.001
								|| dValue2 > 0.001
								|| dValue3 > 0.001
								|| dValue4 > 0.001) {
							stmInsert.setLong(1, thisCase.caseID);
							stmInsert.setShort(2, thisCase.staffID);
							stmInsert.setShort(3, codeID);
							stmInsert.setDate(4, new java.sql.Date(thisCase.finalTime));
							stmInsert.setDouble(5, dValue1);
							stmInsert.setDouble(6, dValue2);
							stmInsert.setDouble(7, dValue3);
							stmInsert.setDouble(8, dValue4);
							stmInsert.executeUpdate();
							noCases++;
							try {
								Thread.sleep(Constants.SLEEP_TIME);
							} catch (InterruptedException e) {}
						}
					}
				}
			}
			long noSeconds = (System.nanoTime() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Workload Coded " + noCases + " amend/addend in " +
		    			noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbPowerJ.closeRst(rstGetCases);
			dbPowerJ.closeRst(rstGetPrevious);
			dbPowerJ.closeRst(rstGetOrders);
			dbAP.closeStms();
		}
	}
	
	private void getCase(String caseNo) {
		long caseID = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(1);
			stm.setString(1, caseNo);
			rst = stm.executeQuery();
			while (rst.next()) {
				caseID = rst.getLong("id");
			}
			rst.close();
			if (caseID > 0) {
	            if (codeCase(caseID)) {
					if (!parent.variables.hasError) {
						saveCase(false);
					}
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void getCases() {
		int noCases = 0;
		long caseID = 0;
		long startTime = System.nanoTime();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = dbAP.getStatement(0);
			stm.setTimestamp(1, new Timestamp(lastUpdate));
			stm.setTimestamp(2, new Timestamp(maxDate));
			rst = stm.executeQuery();
			while (rst.next()) {
				caseID = rst.getLong("CaseID");
				// User-defined what cases to code or ignore (autopsy, cytology, etc)
				// or by facility
				if (accessions.doWorkload(rst.getShort("acc_type_id"))
					&& facilities.doWorkload(rst.getShort("facility_id"))) {
		            if (codeCase(caseID)) {
						if (!parent.variables.hasError) {
							saveCase(true);
							noCases++;
							try {
								Thread.sleep(Constants.SLEEP_TIME);
							} catch (InterruptedException e) {}
						}
					}
				}
				if (parent.variables.hasError) {
					break;
				}
				if (parent.abort()) {
					break;
				}
				if (noCases % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {}
				}
			}
			long noSeconds = (System.nanoTime() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Workload Coded " + noCases + " new cases in " +
		    			noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private void getCorrelations() {
		long startTime = System.nanoTime();
		double dValue1 = coder1.getCorrelations();
		double dValue2 = coder2.getCorrelations();
		double dValue3 = coder3.getCorrelations();
		double dValue4 = coder4.getCorrelations();
		if (!(dValue1 > 0.001 || dValue2 > 0.001
				|| dValue3 > 0.001 || dValue4 == 0.001)) {
			return;
		}
		final short codeID = 3;
		boolean isDuplicate = false, exists = false;
		int noRows = 0, noCases = 0;
		Calendar calCurrent = Calendar.getInstance();
		Calendar calPrevious = Calendar.getInstance();
		ResultSet rstGetCorr = null, rstGetPrevious = null, rstGetAddl = null;
		PreparedStatement stmGetAddl = null, stmGetPrevious = null, stmInsert = null;
		try {
			// Still using prepareAdditionals()
			stmGetPrevious = dbPowerJ.getStatement(0);
			stmGetAddl = dbPowerJ.getStatement(1);
			stmInsert = dbPowerJ.getStatement(2);
			rstGetCorr = dbAP.getCorrelations(lastUpdate, maxDate);
			while (rstGetCorr.next()) {
				if (parent.abort()) {
					break;
				}
				if (++noRows % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {}
				}
				thisCase = new DataCaseFinal();
				thisCase.caseID = rstGetCorr.getLong("acc_id");
				thisCase.finalTime = rstGetCorr.getTimestamp("correlation_date").getTime();
				thisCase.staffID = rstGetCorr.getShort("correlated_by_id");
				calCurrent.setTimeInMillis(thisCase.finalTime);
				// Cannot exist in PowerJ in Additional Table
				stmGetAddl.setLong(1, thisCase.caseID);
				rstGetAddl = stmGetAddl.executeQuery();
				isDuplicate = false;
				while (rstGetAddl.next()) {
					thisCase.caseNo = rstGetAddl.getString("CASENO");
					thisCase.accessionTime = rstGetAddl.getTimestamp("FINALED").getTime();
					calPrevious.setTimeInMillis(thisCase.accessionTime);
					if (thisCase.staffID == rstGetAddl.getShort("PERID")) {
						if (codeID == rstGetAddl.getShort("CODEID")) {
							if (calPrevious.get(Calendar.DAY_OF_YEAR) == calCurrent.get(Calendar.DAY_OF_YEAR)) {
								// No duplicates on the same day
								isDuplicate = true;
							}
						}
					}
				}
				rstGetAddl.close();
				if (!isDuplicate) {
					// Must exist in PowerJ in Cases Table
					stmGetPrevious.setLong(1, thisCase.caseID);
					rstGetPrevious = stmGetPrevious.executeQuery();
					exists = false;
					while (rstGetPrevious.next()) {
						thisCase.caseNo = rstGetPrevious.getString("CASENO");
						exists = true;
					}
					rstGetPrevious.close();
					if (exists) {
						parent.log(JOptionPane.INFORMATION_MESSAGE, className,
								"Coding Correlation on case " + thisCase.caseID);
						stmInsert.setLong(1, thisCase.caseID);
						stmInsert.setShort(2, thisCase.staffID);
						stmInsert.setShort(3, codeID);
						stmInsert.setDate(4, new java.sql.Date(thisCase.finalTime));
						stmInsert.setDouble(5, dValue1);
						stmInsert.setDouble(6, dValue2);
						stmInsert.setDouble(7, dValue3);
						stmInsert.setDouble(8, dValue4);
						stmInsert.executeUpdate();
						noCases++;
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {}
					}
				}
			}
			long noSeconds = (System.nanoTime() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 6000) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Workload Coded " + noCases + " correlations in " +
		    			noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rstGetCorr);
		}
	}
	
	private void getGrossDescr() {
		String grossDescr = "";
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(4);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				// There are 2 fields, one is null
				if (rst.getString("finding") != null) {
					grossDescr = rst.getString("finding");
				} else if (rst.getString("finding_text") != null) {
					grossDescr = rst.getString("finding_text");
				}
			}
			rst.close();
			if (grossDescr.length() == 0) {
				// Some cases have gross combined with other fields
				stm = dbAP.getStatement(3);
				stm.setLong(1, thisCase.caseID);
				rst = stm.executeQuery();
				while (rst.next()) {
					// There are 2 fields, one is null
					if (rst.getString("finding_text") != null) {
						grossDescr = rst.getString("finding_text");
					} else if (rst.getString("finding") != null) {
						grossDescr = rst.getString("finding");
					}
					if (grossDescr.length() > 5) {
						grossDescr = grossDescr.trim().toLowerCase();
						int i = grossDescr.indexOf("gross description:");
						if (i > -1) {
							grossDescr = grossDescr.substring(i, grossDescr.length());
							break;
						}
					}
				}
			}
			rst.close();
			grossDescr = grossDescr.trim().toLowerCase();
			if (grossDescr.length() > 5) {
				if (thisCase.noSpecimens > 1) {
					String[] grosses = grossDescr.split("the specimen is received in a container");
					// First element is empty
					if (grosses.length == thisCase.noSpecimens +1) {
						for (int i = 0; i < thisCase.noSpecimens; i++) {
							thisSpecimen = thisCase.lstSpecimens.get(i);
							if (coder1.needsFragments(i)
									|| coder2.needsFragments(i)
									|| coder3.needsFragments(i)
									|| coder4.needsFragments(i)) {
								thisSpecimen.noFragments = getNoFragments(grosses[i+1]);
							}
						}
					}
				} else {
					thisCase.lstSpecimens.get(0).noFragments = getNoFragments(grossDescr);
				}
			} else {
				for (int i = 0; i < thisCase.noSpecimens; i++) {
					thisSpecimen = thisCase.lstSpecimens.get(i);
					if (coder1.needsFragments(i)
							|| coder2.needsFragments(i)
							|| coder3.needsFragments(i)
							|| coder4.needsFragments(i)) {
						thisSpecimen.noFragments = 1;
					}
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void getGrossTime() {
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(5);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				thisCase.grossTime = rst.getTimestamp("completed_date").getTime();
				thisCase.grossID = rst.getShort("assigned_to_id");
				break;
			}
			rst.close();
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void getLastUpdate() {
		// Earliest date is May 1st, 2017
		long minimum = parent.variables.minWorkloadDate;
		Calendar calLastUpdate = Calendar.getInstance();
		calLastUpdate.setTimeInMillis(minimum);
		ResultSet rst = null;
		try {
			rst = dbPowerJ.getLastWorkload();
			while (rst.next()) {
				if (rst.getTimestamp("finaled") != null) {
					// Add 1 second to avoid duplicated 1st case
					lastUpdate = rst.getTimestamp("finaled").getTime() + 1000;
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbPowerJ.closeRst(rst);
			dbPowerJ.closeStm();
			if (lastUpdate < minimum) {
				lastUpdate = minimum;
			}
			parent.variables.lastUpdate = lastUpdate;
			// Maximum range is 15 days interval per run
			Calendar calMaxDate = Calendar.getInstance();
			calMaxDate.setTimeInMillis(lastUpdate);
			calMaxDate.add(Calendar.DAY_OF_YEAR, 15);
			calMaxDate.set(Calendar.HOUR_OF_DAY, 0);
			calMaxDate.set(Calendar.MINUTE, 0);
			calMaxDate.set(Calendar.SECOND, 0);
			calMaxDate.set(Calendar.MILLISECOND, 1);
			maxDate = calMaxDate.getTimeInMillis();
			// Maximum cannot be more than now
			long endDate = Calendar.getInstance().getTimeInMillis();
			if (maxDate > endDate) {
				maxDate = endDate;
			}
		}
	}
	
	private short getNoFragments(String gross) {
		noFrags = 0;
		noLesions = 0;
		for (int i = 0; i < SEARCH_STRINGS.length; i++) {
			index = gross.indexOf(SEARCH_STRINGS[i]);
			if (index > -1) {
				string = gross.substring(index);
				// use 1st line only
				String[] lines = string.split(Constants.NEW_LINE);
				// Extract first number (string + : (.? = match any char 0-1 times) + 0/many spaces (\\s*? = match 0 or more spaces)+ digit + 0-many characters
				pattern = Pattern.compile(SEARCH_STRINGS[i] + ".?\\s*?(\\d+).*");
				matcher = pattern.matcher(lines[0]);
				if (matcher.find()) {
					string = matcher.group(1);
					if (string.length() > 0) {
						number = Short.parseShort(string);
						if (number < 1) {
							// Nothing found (0) or malicious (-3)
							number = 1;
						} else if (number > 20) {
							// Max 20 to correct for fragmentation
							number = 20;
						}
						if (i < 2) {
							// No of lesions
							if (noLesions < number) {
								noLesions = number;
							}
						} else {
							// No of fragments
							if (noFrags < number) {
								noFrags = number;
								break;
							}
						}
					}
				} else if (i < 2) {
					// if lesions/excisions found, but no number next to it
					noLesions = 1;
				} else {
					// if fragments/pieces found, but no number next to it
					noFrags = 1;
				}
			}
		}
		if (noLesions > 0 && noFrags > noLesions) {
			// If both found, and both > 0, then use noLesions
			// Unless noLesions > noFragments (Montfort)
			noFrags = noLesions;
		} else if (noFrags < 1) {
			noFrags = 1;
		}
		return noFrags;
	}

	private void getOrders() {
		boolean isFS = false, isRoutine = false, isAddlBlock = false;
		short qty = 0, orderID = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(6);
			stm.setLong(1, thisSpecimen.specimenID);
			stm.setTimestamp(2, new Timestamp(thisCase.finalTime));
			rst = stm.executeQuery();
			while (rst.next()) {
				orderID = rst.getShort("procedure_id");
				if (masterOrders.matchOrder(orderID)) {
					// Some orders do not count if routine
					isRoutine = (rst.getTimestamp("created_date").getTime() < thisCase.routeTime);
					qty = rst.getShort("quantity");
					isAddlBlock = false;
					switch (masterOrders.getOrderType()) {
					case DataOrderType.IGNORE:
						continue;
					case DataOrderType.BLK:
						thisSpecimen.noBlocks += qty;
						thisCase.noBlocks += qty;
						isAddlBlock = !isRoutine;
						break;
					case DataOrderType.SLIDE:
					case DataOrderType.SS:
					case DataOrderType.IHC:
					case DataOrderType.FISH:
						thisSpecimen.noSlides += qty;
						thisCase.noSlides += qty;
						break;
					case DataOrderType.BLK_FS:
						thisSpecimen.noBlocks += qty;
						thisSpecimen.noFSBlks += qty;
						thisCase.noBlocks += qty;
						thisCase.noFSBlks += qty;
						isFS = true;
						break;
					case DataOrderType.SLD_FS:
						thisSpecimen.noFSSlds += qty;
						thisSpecimen.noSlides += qty;
						thisCase.noSlides += qty;
						thisCase.noFSSlds += qty;
						isFS = true;
						break;
					default:
						// EM, FCM, MOLEC, REV
					}
					thisOrder = thisSpecimen.lstOrders.get(masterOrders.getGroupID());
					if (thisOrder == null) {
						thisOrder = new DataSpecimenOrder();
						thisOrder.groupID = masterOrders.getGroupID();
						thisOrder.name = masterOrders.getOrderName();
						thisSpecimen.lstOrders.put(masterOrders.getGroupID(), thisOrder);
					}
					thisOrder.qty += qty;
					coder1.addOrder(orderID, masterOrders.getGroupID(),
							masterOrders.getCodeID(1), qty, isRoutine, isAddlBlock,
							(thisCase.noSpecimens -1));
					coder2.addOrder(orderID, masterOrders.getGroupID(),
							masterOrders.getCodeID(2), qty, isRoutine, isAddlBlock,
							(thisCase.noSpecimens -1));
					coder3.addOrder(orderID, masterOrders.getGroupID(),
							masterOrders.getCodeID(3), qty, isRoutine, isAddlBlock,
							(thisCase.noSpecimens -1));
					coder4.addOrder(orderID, masterOrders.getGroupID(),
							masterOrders.getCodeID(4), qty, isRoutine, isAddlBlock,
							(thisCase.noSpecimens -1));
				} else {
					thisCase.hasError = true;
					thisSpecimen.errorID = Constants.ERROR_ORDER_UNKNOWN;
					comment = "ERROR: getOrder, " + thisCase.caseNo + ", Specimen " +
							thisSpecimen.specimenID + ", Order " + orderID + ", Code " +
							rst.getString("code") + ", " + Constants.ERROR_STRINGS[thisSpecimen.errorID];
					thisCase.comment += comment + Constants.NEW_LINE;
					parent.log(JOptionPane.ERROR_MESSAGE, className, comment);
					break;
				}
			}
			if (isFS) {
				thisCase.noFSSpecs++;
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}

	private void getRouteTime() {
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(7);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (rst.getTimestamp("event_date") != null) {
					thisCase.routeTime = rst.getTimestamp("event_date").getTime();
					break;
				}
			}
			rst.close();
			stm = dbAP.getStatement(10);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (rst.getTimestamp("event_date") != null) {
					thisCase.scanTime = rst.getTimestamp("event_date").getTime();
					break;
				}
			}
			if (thisCase.routeTime > thisCase.scanTime) {
				// Use scan time (cytology does not route PAP/H&E, but routes SS/IHC)
				thisCase.routeTime = thisCase.scanTime;
			}
			if (thisCase.grossTime < thisCase.accessionTime) {
				thisCase.grossTime = thisCase.accessionTime;
			}
			if (thisCase.routeTime < thisCase.grossTime) {
				thisCase.routeTime = thisCase.grossTime;
			}
			if (thisCase.routeTime > thisCase.finalTime) {
				thisCase.routeTime = thisCase.finalTime;
			}
			thisCase.grossTAT = parent.dateUtils.getBusinessHours(
				thisCase.accessionTime, thisCase.grossTime);
			thisCase.routeTAT = parent.dateUtils.getBusinessHours(
				thisCase.grossTime, thisCase.routeTime);
			thisCase.finalTAT = parent.dateUtils.getBusinessHours(
				thisCase.routeTime, thisCase.finalTime);
			thisCase.totalTAT = parent.dateUtils.getBusinessHours(
				thisCase.accessionTime, thisCase.finalTime);
			if (thisCase.grossTAT > Short.MAX_VALUE) {
				thisCase.grossTAT = Short.MAX_VALUE;
			}
			if (thisCase.routeTAT > Short.MAX_VALUE) {
				thisCase.routeTAT = Short.MAX_VALUE;
			}
			if (thisCase.finalTAT > Short.MAX_VALUE) {
				thisCase.finalTAT = Short.MAX_VALUE;
			}
			if (thisCase.totalTAT > Short.MAX_VALUE) {
				thisCase.totalTAT = Short.MAX_VALUE;
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private void getSpecimens() {
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(8);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (masterSpecimens.matchSpecimens(rst.getInt("tmplt_profile_id"))) {
					masterSpecimen = masterSpecimens.getItem();
					thisSpecimen = new DataSpecimenFinal();
					thisSpecimen.specimenID = rst.getLong("SpecID");
					thisSpecimen.masterID = rst.getShort("tmplt_profile_id");
					thisSpecimen.description = rst.getString("description").trim();
					thisSpecimen.procedureID = masterSpecimen.procedureID;
					thisSpecimen.subspecialtyID = masterSpecimens.getSubspecialtyID(
						rst.getString("label_name"), rst.getString("description"));
					thisCase.lstSpecimens.add(thisSpecimen);
					thisCase.noSpecimens++;
					// Find best fit sub-specialty
					if (masterSpecimen.hasLN) {
						thisCase.hasLN = true;
						thisCase.procedureID = thisSpecimen.procedureID;
						thisCase.subspecialtyID = thisSpecimen.subspecialtyID;
						thisCase.masterSpecimen = thisSpecimen.masterID;
						thisCase.mainSpecimenBlocks = thisSpecimen.noBlocks;
					} else if (thisCase.procedureID < thisSpecimen.procedureID) {
						// Use this specimen if Malignant & for subspecialty
						thisCase.procedureID = thisSpecimen.procedureID;
						thisCase.subspecialtyID = thisSpecimen.subspecialtyID;
						thisCase.masterSpecimen = thisSpecimen.masterID;
						thisCase.mainSpecimenBlocks = thisSpecimen.noBlocks;
					} else if (thisCase.procedureID == thisSpecimen.procedureID) {
						if (thisCase.subspecialtyID < thisSpecimen.subspecialtyID) {
							thisCase.subspecialtyID = thisSpecimen.subspecialtyID;
							thisCase.masterSpecimen = thisSpecimen.masterID;
							thisCase.mainSpecimenBlocks = thisSpecimen.noBlocks;
						} else if (thisCase.mainSpecimenBlocks < thisSpecimen.noBlocks) {
							thisCase.subspecialtyID = thisSpecimen.subspecialtyID;
							thisCase.masterSpecimen = thisSpecimen.masterID;
							thisCase.mainSpecimenBlocks = thisSpecimen.noBlocks;
						}
					}
					coder1.addSpecimen(thisSpecimen.procedureID,
						masterSpecimen.coder1B, masterSpecimen.coder1M,
						masterSpecimen.coder1R);
					coder2.addSpecimen(thisSpecimen.procedureID,
						masterSpecimen.coder2B, masterSpecimen.coder2M,
						masterSpecimen.coder2R);
					coder3.addSpecimen(thisSpecimen.procedureID,
						masterSpecimen.coder3B, masterSpecimen.coder3M,
						masterSpecimen.coder3R);
					coder4.addSpecimen(thisSpecimen.procedureID,
							masterSpecimen.coder4B, masterSpecimen.coder4M,
							masterSpecimen.coder4R);
					getOrders();
				} else {
					thisCase.hasError = true;
					comment = "ERROR: getSpecimens, " + thisCase.caseNo + ", Specimen " +
							thisCase.noSpecimens + ", Template " + rst.getInt("tmplt_profile_id") +
							", Descr " + rst.getString("description") + ", " +
							Constants.ERROR_STRINGS[thisSpecimen.errorID];
					thisCase.comment += comment + Constants.NEW_LINE;
					parent.log(JOptionPane.ERROR_MESSAGE, className, comment);
				}
			}
			if (thisCase.noSpecimens < 1) {
				thisCase.hasError = true;
				comment = "ERROR: getSpecimens, " + thisCase.caseNo + ", " +
						Constants.ERROR_STRINGS[Constants.ERROR_SPECIMENS_COUNT_ZERO];
				thisCase.comment += comment + Constants.NEW_LINE;
				parent.log(JOptionPane.ERROR_MESSAGE, className, comment);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private void getSynoptics() {
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(9);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				thisCase.noSynoptics = rst.getByte("NoSynoptics");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private void redo() {
		int noRows = 0;
		long caseID = 0;
		long startTime = System.nanoTime();
		ResultSet rst = null;
		try {
			rst = parent.dbPowerJ.getErrors(0);
			while (rst.next()) {
				caseID = rst.getLong("CASEID");
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Re-Coding Error case " + caseID);
	            if (codeCase(caseID)) {
					if (!parent.variables.hasError) {
						if (deleteError(caseID)) {
							saveCase(false);
						}
					}
				}
				if (parent.variables.hasError) {
					break;
				}
				if (parent.abort()) {
					break;
				}
				if (++noRows % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {}
				}
			}
			rst.close();
			long noSeconds = (System.nanoTime() - startTime) / 1000000000;
			if (noRows > 0 && noSeconds > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Workload Coded " + noRows + " corrected errors in " +
		    			noSeconds + " seconds (" + (noRows * 60 / noSeconds) + "/min)");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeStms();
			dbPowerJ.closeStms();
		}
	}
	
	private void saveCase(boolean isNew) {
		byte errorID = 0;
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			comment = "";
			if (thisCase.comment.length() > 0) {
				comment = thisCase.comment + Constants.NEW_LINE +
						"--------------------------" + Constants.NEW_LINE;
			}
			if (coder1.hasComment()) {
				comment += coder1.getComment();
			}
			if (coder2.hasComment()) {
				comment += coder2.getComment();
			}
			if (coder3.hasComment()) {
				comment += coder3.getComment();
			}
			if (coder4.hasComment()) {
				comment += coder4.getComment();
			}
			if (comment.length() > 32600) {
				comment = comment.substring(0, 32600);
			}
			if (thisCase.hasError) {
				// Specimen Error
				errorID = 1;
			} else if (coder1.hasError()) {
				errorID = 2;
			} else if (coder2.hasError()) {
				errorID = 3;
			} else if (coder3.hasError()) {
				errorID = 4;
			} else if (coder4.hasError()) {
				errorID = 5;
			} else {
				errorID = 0;
			}
			if (errorID > 0) {
				saveError(errorID, comment);
			} else {
				if (isNew) {
					stm = dbPowerJ.getStatement(0);
				} else {
					stm = dbPowerJ.getStatement(1);
				}
				stm.setShort(1, thisCase.facilityID);
				stm.setInt(2, thisCase.masterSpecimen);
				stm.setShort(3, thisCase.grossID);
				stm.setShort(4, thisCase.staffID);
				stm.setByte(5, thisCase.specialtyID);
				stm.setByte(6, thisCase.subspecialtyID);
				stm.setByte(7, thisCase.procedureID);
				stm.setByte(8, thisCase.noSpecimens);
				stm.setShort(9, thisCase.noBlocks);
				stm.setShort(10, thisCase.noSlides);
				stm.setShort(11, thisCase.noSynoptics);
				stm.setShort(12, thisCase.noFSSpecs);
				stm.setInt(13, thisCase.grossTAT);
				stm.setInt(14, thisCase.routeTAT);
				stm.setInt(15, thisCase.finalTAT);
				stm.setInt(16, thisCase.totalTAT);
				stm.setDate(17, new java.sql.Date(thisCase.accessionTime));
				stm.setDate(18, new java.sql.Date(thisCase.grossTime));
				stm.setDate(19, new java.sql.Date(thisCase.routeTime));
				stm.setTimestamp(20, new Timestamp(thisCase.finalTime));
				stm.setDouble(21, coder1.getValue());
				stm.setDouble(22, coder2.getValue());
				stm.setDouble(23, coder3.getValue());
				stm.setDouble(24, coder4.getValue());
				stm.setString(25, thisCase.caseNo);
				stm.setLong(26, thisCase.caseID);
				noUpdates = stm.executeUpdate();
				if (noUpdates > 0) {
					saveSpecimens(isNew);
					saveComment(isNew, comment);
					if (thisCase.noFSSpecs > 0) {
						saveFrozens(isNew);
					}
				}
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void saveError(byte errorID, String comment) {
		PreparedStatement stm = null;
		try {
			stm = dbPowerJ.getStatement(10);
			stm.setLong(1, thisCase.caseID);
			stm.setByte(2, errorID);
			stm.setString(3, thisCase.caseNo);
			stm.setString(4, comment);
			stm.executeUpdate();
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void saveComment(boolean isNew, String comment) {
		PreparedStatement stm = null;
		try {
			if (isNew) {
				stm = dbPowerJ.getStatement(2);
			} else {
				stm = dbPowerJ.getStatement(3);
			}
			stm.setString(1, comment);
			stm.setLong(2, thisCase.caseID);
			stm.executeUpdate();
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void saveFrozens(boolean isNew) {
		PreparedStatement stm = null;
		try {
			if (isNew) {
				stm = dbPowerJ.getStatement(4);
			} else {
				stm = dbPowerJ.getStatement(5);
			}
			stm.setInt(1, 0);
			stm.setByte(2, thisCase.noFSSpecs);
			stm.setShort(3, thisCase.noFSBlks);
			stm.setShort(4, thisCase.noFSSlds);
			stm.setDouble(5, coder1.getFrozen());
			stm.setDouble(6, coder2.getFrozen());
			stm.setDouble(7, coder3.getFrozen());
			stm.setDouble(8, coder4.getFrozen());
			stm.setLong(9, thisCase.caseID);
			stm.executeUpdate();
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void saveOrders(boolean isNew, int i) {
		PreparedStatement stm = null;
		try {
			if (isNew) {
				stm = dbPowerJ.getStatement(8);
			} else {
				stm = dbPowerJ.getStatement(9);
			}
			// Save specimen i orders
			for (Entry<Short, DataSpecimenOrder> orderEntry : thisSpecimen.lstOrders.entrySet()) {
				thisOrder = orderEntry.getValue();
				if (thisOrder.qty > 0
						&& (coder1.getOrder(i, thisOrder.groupID) > 0.001
								|| coder2.getOrder(i, thisOrder.groupID) > 0.001
								|| coder3.getOrder(i, thisOrder.groupID) > 0.001
								|| coder4.getOrder(i, thisOrder.groupID) > 0.001)) {
		            stm.setShort(1, thisOrder.qty);
					stm.setDouble(2, coder1.getOrder(i, thisOrder.groupID));
					stm.setDouble(3, coder2.getOrder(i, thisOrder.groupID));
					stm.setDouble(4, coder3.getOrder(i, thisOrder.groupID));
					stm.setDouble(5, coder4.getOrder(i, thisOrder.groupID));
		            stm.setShort(6, thisOrder.groupID);
		            stm.setLong(7, thisSpecimen.specimenID);
					stm.executeUpdate();
				}
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
	
	private void saveSpecimens(boolean isNew) {
		PreparedStatement stm = null;
		try {
			if (isNew) {
				stm = dbPowerJ.getStatement(6);
			} else {
				stm = dbPowerJ.getStatement(7);
			}
			for (int i = 0; i < thisCase.noSpecimens; i++) {
				thisSpecimen = thisCase.lstSpecimens.get(i);
				if (thisSpecimen.description.length() > 64) {
					thisSpecimen.description = thisSpecimen.description.substring(0, 64);
				}
				stm.setLong(1, thisCase.caseID);
				stm.setShort(2, thisSpecimen.masterID);
				stm.setShort(3, thisSpecimen.noBlocks);
				stm.setShort(4, thisSpecimen.noSlides);
				stm.setShort(5, thisSpecimen.noFragments);
				stm.setDouble(6, coder1.getValue(i));
				stm.setDouble(7, coder2.getValue(i));
				stm.setDouble(8, coder3.getValue(i));
				stm.setDouble(9, coder4.getValue(i));
				stm.setString(10, thisSpecimen.description);
				stm.setLong(11, thisSpecimen.specimenID);
				stm.executeUpdate();
				saveOrders(isNew, i);
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}
}
