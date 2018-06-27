package ca.eorla.fhalwani.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.swing.JOptionPane;

class StatsManager {
	private long lastUpdate = 0;
	private long maxDate = 0;
	private final String className = "Cases";
	private DataCase thisCase = new DataCase();
	private DataSpecimenFinal thisSpecimen = new DataSpecimenFinal();
	private DataMasterSpecimen masterSpecimen = new DataMasterSpecimen();
	private MapFacilities facilities;
	private MapAccessions accessions;
	private MapOrders masterOrders;
	private MapTemplates masterSpecimens;
	private PowerJ parent;
	private DbAPIS dbAP;
	private DbPowerJ dbPowerJ;
	private PreparedStatement stmInsert = null;

	StatsManager(PowerJ parent) {
		this.parent = parent;
		parent.log(JOptionPane.INFORMATION_MESSAGE, className,
				parent.dateUtils.formatter(parent.dateUtils.FORMAT_DATETIME) +
				" - Case Manager Started...");
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
			dbAP.prepareWorkload();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			dbPowerJ.prepareStats();
			stmInsert = dbPowerJ.getStatement(0);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getLastUpdate();
			if (maxDate - lastUpdate > 7200000) {
				// Else, we are up to date (min run every 2 hours)
				getCases();
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
					thisCase = new DataCase();
					thisCase.caseID = caseID;
					thisCase.specialtyID = accessions.getSpecialty();
					thisCase.facilityID = rst.getShort("facility_id");
					thisCase.caseNo = rst.getString("accession_no");
					thisCase.staffID = rst.getShort("assigned_to_id");
					thisCase.finalTime = rst.getTimestamp("completed_date").getTime();
					thisCase.accessionTime = rst.getTimestamp("created_date").getTime();
					parent.log(JOptionPane.INFORMATION_MESSAGE, className,
							"Coding case " + thisCase.caseNo);
					// We need to know if the case is malignant before-hand
					getSynoptics();
					getGrossTime();
					getRouteTime();
					getSpecimens();
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
			System.out.println("From: " + 
			parent.dateUtils.formatter(lastUpdate, parent.dateUtils.FORMAT_DATETIME) +
					" - " + parent.dateUtils.formatter(maxDate, parent.dateUtils.FORMAT_DATETIME));
			while (rst.next()) {
				caseID = rst.getLong("CaseID");
				// User-defined what cases to code or ignore (autopsy, cytology, etc)
				// or by facility
				if (accessions.doWorkload(rst.getShort("acc_type_id"))
					&& facilities.doWorkload(rst.getShort("facility_id"))) {
		            if (codeCase(caseID)) {
						if (!parent.variables.hasError) {
							saveCase();
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
						"Case Manager Coded " + noCases + " new cases in " +
		    			noSeconds + " seconds (" + (noCases * 60 / noSeconds) + "/min)");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
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
		Calendar calLastUpdate = Calendar.getInstance();
		// Earliest date is Jan 1st, 2011
		calLastUpdate.set(Calendar.YEAR, 2011);
		calLastUpdate.set(Calendar.DAY_OF_YEAR, 1);
		calLastUpdate.set(Calendar.HOUR_OF_DAY, 0);
		calLastUpdate.set(Calendar.MINUTE, 0);
		calLastUpdate.set(Calendar.SECOND, 0);
		calLastUpdate.set(Calendar.MILLISECOND, 1);
		long minimum = calLastUpdate.getTimeInMillis();
		ResultSet rst = null;
		try {
			rst = dbPowerJ.getLastStats();
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

	private void getOrders() {
		boolean isFS = false;
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
					qty = rst.getShort("quantity");
					switch (masterOrders.getOrderType()) {
					case DataOrderType.IGNORE:
						continue;
					case DataOrderType.BLK:
						thisCase.noBlocks += qty;
						break;
					case DataOrderType.SLIDE:
						thisCase.noSlides += qty;
						thisCase.noHE += qty;
						break;
					case DataOrderType.SS:
						thisCase.noSlides += qty;
						thisCase.noSS += qty;
						break;
					case DataOrderType.IHC:
						thisCase.noSlides += qty;
						thisCase.noIHC += qty;
						break;
					case DataOrderType.FISH:
						thisCase.noSlides += qty;
						thisCase.noMOL += qty;
					case DataOrderType.BLK_FS:
						thisCase.noBlocks += qty;
						thisCase.noFSBlks += qty;
						isFS = true;
						break;
					case DataOrderType.SLD_FS:
						thisCase.noSlides += qty;
						thisCase.noHE += qty;
						thisCase.noFSSlds += qty;
						isFS = true;
						break;
					default:
						// EM, FCM, MOLEC, REV
					}
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
					thisSpecimen.procedureID = masterSpecimen.procedureID;
					thisSpecimen.subspecialtyID = masterSpecimens.getSubspecialtyID(
						rst.getString("label_name"), rst.getString("description"));
					thisCase.noSpecimens++;
					// Find best fit sub-specialty
					if (masterSpecimen.hasLN) {
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
					getOrders();
				}
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

	private void saveCase() {
		try {
			stmInsert.setShort(1, thisCase.facilityID);
			stmInsert.setShort(2, thisCase.grossID);
			stmInsert.setShort(3, thisCase.staffID);
			stmInsert.setByte(4, thisCase.specialtyID);
			stmInsert.setByte(5, thisCase.subspecialtyID);
			stmInsert.setByte(6, thisCase.procedureID);
			stmInsert.setByte(7, thisCase.noSpecimens);
			stmInsert.setShort(8, thisCase.noBlocks);
			stmInsert.setShort(9, thisCase.noSlides);
			stmInsert.setShort(10, thisCase.noHE);
			stmInsert.setShort(11, thisCase.noSS);
			stmInsert.setShort(12, thisCase.noIHC);
			stmInsert.setShort(13, thisCase.noMOL);
			stmInsert.setShort(14, thisCase.noFSSpecs);
			stmInsert.setShort(15, thisCase.noFSBlks);
			stmInsert.setShort(16, thisCase.noFSSlds);
			stmInsert.setShort(17, thisCase.noSynoptics);
			stmInsert.setInt(18, thisCase.grossTAT);
			stmInsert.setInt(19, thisCase.routeTAT);
			stmInsert.setInt(20, thisCase.finalTAT);
			stmInsert.setInt(21, thisCase.totalTAT);
			stmInsert.setInt(22, thisCase.masterSpecimen);
			stmInsert.setTimestamp(23, new Timestamp(thisCase.accessionTime));
			stmInsert.setTimestamp(24, new Timestamp(thisCase.grossTime));
			stmInsert.setTimestamp(25, new Timestamp(thisCase.routeTime));
			stmInsert.setTimestamp(26, new Timestamp(thisCase.finalTime));
			stmInsert.setString(27, thisCase.caseNo);
			stmInsert.setLong(28, thisCase.caseID);
			stmInsert.executeUpdate();
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
	}

	class DataCase {
		byte specialtyID = 0;
		byte subspecialtyID = 1;
		byte procedureID = 0;
		byte noSpecimens = 0;
		byte noFSSpecs = 0;
		byte noSynoptics = 0;
		short facilityID = 0;
		short staffID = 0;
		short grossID = 0;
		short noBlocks = 0;
		short noHE = 0;
		short noSS = 0;
		short noIHC = 0;
		short noMOL = 0;
		short noSlides = 0;
		short noFSBlks = 0;
		short noFSSlds = 0;
		short noFSSpcs = 0;
		short mainSpecimenBlocks = 0;
		int grossTAT = 0;
		int routeTAT = 0;
		int finalTAT = 0;
		int totalTAT = 0;
		int masterSpecimen = 1424;	// Toes are missing
		long caseID = 0;
		long accessionTime = 0;
		long grossTime = 0;
		long routeTime = 0;
		long scanTime = 0;
		long finalTime = 0;
		String caseNo = "";
	}
}
