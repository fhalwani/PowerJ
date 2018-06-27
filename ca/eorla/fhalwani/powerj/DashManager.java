package ca.eorla.fhalwani.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.JOptionPane;

class DashManager {
	private int noCases = 0;
	private final String className = "Workload";
	private MapFacilities facilities;
	private MapAccessions accessions;
	private MapOrders masterOrders;
	private MapTemplates masterSpecimens;
	private PowerJ parent;
	private DbAPIS dbAP;
	private DbPowerJ dbPowerJ;
	private Numbers numbers;
	private DataMasterSpecimen masterSpecimen = new DataMasterSpecimen();
	private DataCase thisCase = new DataCase();
	private ArrayList<DataCase> list = new ArrayList<DataCase>();

	DashManager(PowerJ parent) {
		this.parent = parent;
		this.numbers = parent.numbers;
		long startTime = System.nanoTime();
		parent.log(JOptionPane.INFORMATION_MESSAGE, className,
				parent.dateUtils.formatter(parent.dateUtils.FORMAT_DATETIME) +
				" - Dashboard Manager Started...");
		dbPowerJ = parent.dbPowerJ;
		dbAP = new DbAPIS(parent);
		if (!dbAP.connected) {
			return;
		}
		masterSpecimens = new MapTemplates(parent);
		if (!(parent.variables.hasError || parent.abort())) {
			masterOrders = new MapOrders(parent);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			accessions = new MapAccessions(parent);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			facilities = new MapFacilities(parent);
		}
		if (!(parent.variables.hasError || parent.abort())) {
			dbPowerJ.prepareDash();
			dbAP.prepareDash();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getLastUpdate();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getAccessions();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getCases();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getCanceled();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getGrossed();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getEmbeded();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getMicrotomed();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getStained();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getRouted();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			getFinal();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			deleteComplete();
		}
		if (!(parent.variables.hasError || parent.abort())) {
			long noSeconds = (System.nanoTime() - startTime) / 1000000000;
			if (noCases > 0 && noSeconds > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE,
					 className, "Updated the status of " + numbers.formatNumber(noCases) +
					" cases in " + numbers.formatNumber(noSeconds) + " seconds (" + 
					numbers.formatNumber((noCases * 60 / noSeconds)) + "/min)");
			}
		}
		close();
	}

	private void close() {
		list.clear();
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

	/** Sync the cache database when a case is deleted in PowerPath. **/
	private int deleteCase(long caseID) {
		return dbPowerJ.deleteDashboard(caseID);
	}

	/** Keep completed Cases from last week to measure workload/workflow. **/
	private int deleteComplete() {
		Calendar calDate = Calendar.getInstance();
		calDate.add(Calendar.DAY_OF_YEAR, -10);
		calDate.set(Calendar.HOUR_OF_DAY, 0);
		calDate.set(Calendar.MINUTE, 0);
		calDate.set(Calendar.SECOND, 0);
		calDate.set(Calendar.MILLISECOND, 1);
		int noUpdates = dbPowerJ.deleteCompleted(calDate.getTimeInMillis());
		if (noUpdates > 0) {
			parent.log(JOptionPane.INFORMATION_MESSAGE, className,
					"Deleted " + numbers.formatNumber(noUpdates) + " completed cases.");
		}
		return noUpdates;
	}

	/** Retrieve new cases added since last run. */
	private void getAccessions() {
		int noUpdates = 1;
		ResultSet rst = dbAP.getAccessions();
		try {
			while (rst.next()) {
				// User-defined what cases to code or ignore (autopsy, cytology, etc)
				// or by facility
				if (accessions.doDashboard(rst.getShort("acc_type_id"))
					&& facilities.doDashboard(rst.getShort("facility_id"))) {
					thisCase = new DataCase();
					thisCase.caseID = rst.getLong("id");
					thisCase.specialtyID = accessions.getSpecialty();
					thisCase.facilityID = rst.getShort("facility_id");
					thisCase.caseNo = rst.getString("accession_no");
					thisCase.accesTime = rst.getTimestamp("created_date").getTime();
					getSpecimens();
					if (thisCase.noSpecimens > 0) {
						if (insertCase() > 0) {
							noUpdates++;
						}
					}
				}
				if (parent.abort()) {
					break;
				}
				if (noUpdates % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 1) {
				noUpdates--;
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Added " + numbers.formatNumber(noUpdates) + " new cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
			dbAP.closeStm();
		}
	}
	
	/** Delete Cancelled cases. */
	private void getCanceled() {
		int noUpdates = 0;
		long caseID = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(4);
			for (int i = 0; i < noCases; i++) {
				thisCase = list.get(i);
				caseID = thisCase.caseID;
				stm.setLong(1, caseID);
				rst = stm.executeQuery();
				while (rst.next()) {
					if (rst.getString("description") != null) {
						if (rst.getString("description").contains("Cancel Test")) {
							if (deleteCase(caseID) > 0) {
								thisCase.cancelled = true;
								noUpdates++;
							}
						}
					}
				}
				rst.close();
				if (parent.abort()) {
					break;
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Deleted " + numbers.formatNumber(noUpdates) + " cancelled cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	/** Get all pending cases from PowerJ. */
	private void getCases() {
		ResultSet rst = dbPowerJ.getPending();
		try {
			while (rst.next()) {
				if (parent.abort()) {
					break;
				}
				thisCase = new DataCase();
				thisCase.caseID = rst.getLong("CASEID");
				thisCase.statusID = rst.getByte("STATUS");
				thisCase.specialtyID = rst.getByte("SPYID");
				thisCase.subspecialtyID = rst.getByte("SUBID");
				thisCase.procedureID = rst.getByte("PROID");
				thisCase.noSpecimens = rst.getByte("NOSPECS");
				thisCase.facilityID = rst.getShort("FACID");
				thisCase.noBlocks = rst.getShort("NOBLOCKS");
				thisCase.noSlides = rst.getShort("NOSLIDES");
				thisCase.masterSpecimen = rst.getLong("MSID");
				thisCase.caseNo = rst.getString("CASENO");
				thisCase.accesTime = rst.getTimestamp("ACCESSED").getTime();
				if (thisCase.statusID > Constants.STATUS_Accession) {
					thisCase.grossTime = rst.getTimestamp("GROSSED").getTime();
					thisCase.grossID = rst.getShort("GROSSID");
					thisCase.grossTAT = rst.getShort("GROSSTAT");
				}
				if (thisCase.statusID > Constants.STATUS_Gross) {
					thisCase.embedTime = rst.getTimestamp("EMBEDED").getTime();
					thisCase.embedID = rst.getShort("EMBEDID");
					thisCase.embedTAT = rst.getShort("EMBEDTAT");
				}
				if (thisCase.statusID > Constants.STATUS_Embed) {
					thisCase.microTime = rst.getTimestamp("MICROED").getTime();
					thisCase.microID = rst.getShort("MICROID");
					thisCase.microTAT = rst.getShort("MICROTAT");
				}
				if (thisCase.statusID > Constants.STATUS_Microtomy) {
					thisCase.stainTime = rst.getTimestamp("STAINED").getTime();
					thisCase.stainTAT = rst.getShort("STAINTAT");
					thisCase.stainID = rst.getShort("STAINID");
				}
				if (thisCase.statusID > Constants.STATUS_Slides) {
					thisCase.routeTime = rst.getTimestamp("ROUTED").getTime();
					thisCase.routeID = rst.getShort("ROUTEID");
					thisCase.routeTAT = rst.getShort("ROUTETAT");
				}
				if (thisCase.statusID > Constants.STATUS_Routed) {
					thisCase.finalTime = rst.getTimestamp("FINALED").getTime();
					thisCase.finalID = rst.getShort("FINALID");
					thisCase.finalTAT = rst.getShort("FINALTAT");
				}
				list.add(thisCase);
			}
			noCases = list.size();
			parent.log(JOptionPane.INFORMATION_MESSAGE, className,
					"Scanning " + numbers.formatNumber(noCases) + " pending cases.");
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
	
	/** Update embedding status of grossed cases. */
	private void getEmbeded() {
		short noBlocks = 0;
		int noUpdates = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(1);
			for (int i = 0; i < noCases; i++) {
				if (parent.abort()) {
					break;
				}
				thisCase = list.get(i);
				if (!thisCase.cancelled &&
					thisCase.statusID < Constants.STATUS_Embed) {
					getSpecimens();
					if (thisCase.noSpecimens > 0) {
						stm.setLong(1, thisCase.caseID);
						rst = stm.executeQuery();
						noBlocks = 0;
						while (rst.next()) {
							noBlocks++;
							if (noBlocks >= thisCase.noBlocks) {
								if (thisCase.statusID < Constants.STATUS_Gross) {
									thisCase.grossTime = rst.getTimestamp("event_date").getTime();
									thisCase.statusID = Constants.STATUS_Gross;
									updateGrossed();
								}
								thisCase.statusID = Constants.STATUS_Embed;
								thisCase.embedTime = rst.getTimestamp("event_date").getTime();
								thisCase.embedID = rst.getShort("personnel_id");
								thisCase.updated = true;
							}
						}
						rst.close();
						if (thisCase.updated) {
							thisCase.noBlocks = noBlocks;
							if (updateEmbeded() > 0) {
								noUpdates++;
							}
						}
					} else if (deleteCase(thisCase.caseID) > 0) {
						// Case has been cancelled
						thisCase.cancelled = true;
					}
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Updated " + numbers.formatNumber(noUpdates) + " embeded cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	/** Update final status of routed cases. */
	private void getFinal() {
		int noUpdates = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(4);
			for (int i = 0; i < noCases; i++) {
				if (parent.abort()) {
					break;
				}
				thisCase = list.get(i);
				if (!thisCase.cancelled) {
					stm.setLong(1, thisCase.caseID);
					rst = stm.executeQuery();
					while (rst.next()) {
						if (rst.getString("description") == null) {
							continue;
						} else if (rst.getString("description").equals("Microscopic/Diagnostic Dictation")
							|| rst.getString("description").equals("Microscopic Dictation")
							|| rst.getString("description").equals("Pathologist Review")) {
							if (rst.getTimestamp("completed_date") == null) {
								if (rst.getShort("assigned_to_id") > 0) {
									if (thisCase.finalID != rst.getShort("assigned_to_id")) {
										thisCase.finalID = rst.getShort("assigned_to_id");
										updateAssigned();
									}
								}
								continue;
							}
							if (thisCase.statusID < Constants.STATUS_Gross) {
								thisCase.grossTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Gross;
								updateGrossed();
							}
							if (thisCase.statusID < Constants.STATUS_Embed) {
								thisCase.embedTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Embed;
								updateEmbeded();
							}
							if (thisCase.statusID < Constants.STATUS_Microtomy) {
								thisCase.microTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Microtomy;
								updateMicrotomed();
							}
							if (thisCase.statusID < Constants.STATUS_Slides) {
								thisCase.stainTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Slides;
								updateStained();
							}
							if (thisCase.statusID < Constants.STATUS_Routed) {
								thisCase.routeTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Routed;
								updateRouted();
							}
							if (thisCase.statusID < Constants.STATUS_Diagnosis) {
								thisCase.statusID = Constants.STATUS_Diagnosis;
								thisCase.finalTime = rst.getTimestamp("completed_date").getTime();
								thisCase.finalID = rst.getShort("assigned_to_id");
								thisCase.updated = true;
							}
						} else if (rst.getString("description").equals("Final")) {
							if (thisCase.statusID < Constants.STATUS_Gross) {
								thisCase.grossTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Gross;
								updateGrossed();
							}
							if (thisCase.statusID < Constants.STATUS_Embed) {
								thisCase.embedTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Embed;
								updateEmbeded();
							}
							if (thisCase.statusID < Constants.STATUS_Microtomy) {
								thisCase.microTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Microtomy;
								updateMicrotomed();
							}
							if (thisCase.statusID < Constants.STATUS_Slides) {
								thisCase.stainTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Slides;
								updateStained();
							}
							if (thisCase.statusID < Constants.STATUS_Routed) {
								thisCase.routeTime = rst.getTimestamp("completed_date").getTime();
								thisCase.statusID = Constants.STATUS_Routed;
								updateRouted();
							}
							thisCase.statusID = Constants.STATUS_Final;
							thisCase.finalTime = rst.getTimestamp("completed_date").getTime();
							thisCase.finalID = rst.getShort("assigned_to_id");
							thisCase.noSlides = getNoSlides();
							thisCase.updated = true;
						}
					}
					rst.close();
					if (thisCase.updated) {
						if (updateFinal() > 0) {
							noUpdates++;
						}
					}
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Updated " + numbers.formatNumber(noUpdates) + " final cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	/** Update grossing status of accessioned cases. */
	private void getGrossed() {
		int noUpdates = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(4);
			for (int i = 0; i < noCases; i++) {
				if (parent.abort()) {
					break;
				}
				thisCase = list.get(i);
				if (!thisCase.cancelled &&
					thisCase.statusID < Constants.STATUS_Gross) {
					getSpecimens();
					if (thisCase.noSpecimens > 0) {
						stm.setLong(1, thisCase.caseID);
						rst = stm.executeQuery();
						while (rst.next()) {
							if (rst.getString("description") == null) {
								continue;
							}
							if (rst.getTimestamp("completed_date") == null) {
								continue;
							}
							if (rst.getString("description").equals("Gross Dictation")) {
								thisCase.statusID = Constants.STATUS_Gross;
								thisCase.grossTime = rst.getTimestamp("completed_date").getTime();
								thisCase.grossID = rst.getShort("assigned_to_id");
								if (updateGrossed() > 0) {
									noUpdates++;
								}
								break;
							}
						}
						rst.close();
					}
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Updated " + numbers.formatNumber(noUpdates) + " grossed cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private void getLastUpdate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		parent.variables.lastUpdate = cal.getTimeInMillis();
		ResultSet rst = dbPowerJ.getLastDash();
		try {
			long accession = 0;
			while (rst.next()) {
				if (rst.getTimestamp("accession") != null) {
					accession = rst.getTimestamp("accession").getTime();
				}
			}
			if (parent.variables.lastUpdate < accession) {
				parent.variables.lastUpdate = accession;
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	/** Update microtomy status of embeded cases. */
	private void getMicrotomed() {
		short noBlocks = 0;
		int noUpdates = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(2);
			for (int i = 0; i < noCases; i++) {
				if (parent.abort()) {
					break;
				}
				thisCase = list.get(i);
				if (!thisCase.cancelled &&
					thisCase.statusID < Constants.STATUS_Microtomy) {
					stm.setLong(1, thisCase.caseID);
					rst = stm.executeQuery();
					noBlocks = 0;
					while (rst.next()) {
						noBlocks++;
						if (noBlocks >= thisCase.noBlocks) {
							if (thisCase.statusID < Constants.STATUS_Gross) {
								thisCase.grossTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Gross;
								updateGrossed();
							}
							if (thisCase.statusID < Constants.STATUS_Embed) {
								thisCase.embedTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Embed;
								updateEmbeded();
							}
							thisCase.statusID = Constants.STATUS_Microtomy;
							thisCase.microTime = rst.getTimestamp("event_date").getTime();
							thisCase.microID = rst.getShort("personnel_id");
							thisCase.updated = true;
						}
					}
					rst.close();
					if (thisCase.updated) {
						thisCase.noBlocks = noBlocks;
						if (updateMicrotomed() > 0) {
							noUpdates++;
						}
					}
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Updated " + numbers.formatNumber(noUpdates) + " microtomed cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	/** Update Number of blocks for a specimen. */
	private short getNoBlocks(long specID) {
		short noBlocks = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(0);
			stm.setLong(1, specID);
			rst = stm.executeQuery();
			while (rst.next()) {
				noBlocks += rst.getShort("Blocks");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
		return noBlocks;
	}

	/** Update Number of stained slides for a case. */
	private short getNoSlides() {
		short noSlides = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(3);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (masterOrders.matchOrder(rst.getShort("procedure_id"))) {
					switch (masterOrders.getOrderType()) {
					case DataOrderType.SLIDE:
					case DataOrderType.SS:
					case DataOrderType.IHC:
					case DataOrderType.FISH:
					case DataOrderType.SLD_FS:
						noSlides += rst.getInt("quantity");
						break;
					default:
						// Ignore
					}
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
		return noSlides;
	}
	
	/** Update Routing status of cases. */
	private void getRouted() {
		short noSlides = 0;
		int noUpdates = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(5);
			for (int i = 0; i < noCases; i++) {
				if (parent.abort()) {
					break;
				}
				thisCase = list.get(i);
				if (!thisCase.cancelled &&
					thisCase.statusID < Constants.STATUS_Routed) {
					stm.setLong(1, thisCase.caseID);
					rst = stm.executeQuery();
					noSlides = 0;
					while (rst.next()) {
						noSlides++;
						if (noSlides >= thisCase.noSlides) {
							if (thisCase.statusID < Constants.STATUS_Gross) {
								thisCase.grossTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Gross;
								updateGrossed();
							}
							if (thisCase.statusID < Constants.STATUS_Embed) {
								thisCase.embedTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Embed;
								updateEmbeded();
							}
							if (thisCase.statusID < Constants.STATUS_Microtomy) {
								thisCase.microTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Microtomy;
								updateMicrotomed();
							}
							if (thisCase.statusID < Constants.STATUS_Slides) {
								thisCase.stainTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Slides;
								updateStained();
							}
							thisCase.statusID = Constants.STATUS_Routed;
							thisCase.routeTime = rst.getTimestamp("event_date").getTime();
							thisCase.routeID = rst.getShort("personnel_id");
							thisCase.updated = true;
						}
					}
					rst.close();
					if (thisCase.updated) {
						thisCase.noSlides = noSlides;
						if (updateRouted() > 0) {
							noUpdates++;
						}
					}
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Updated " + numbers.formatNumber(noUpdates) + " routed cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	/** Update Slides Stained status of microtomed cases. */
	private void getStained() {
		short noSlides = 0;
		int noUpdates = 0;
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(7);
			for (int i = 0; i < noCases; i++) {
				if (parent.abort()) {
					break;
				}
				thisCase = list.get(i);
				if (!thisCase.cancelled &&
					thisCase.statusID < Constants.STATUS_Slides) {
					stm.setLong(1, thisCase.caseID);
					rst = stm.executeQuery();
					noSlides = 0;
					while (rst.next()) {
						noSlides++;
						if (noSlides >= thisCase.noBlocks) {
							if (thisCase.statusID < Constants.STATUS_Gross) {
								thisCase.grossTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Gross;
								updateGrossed();
							}
							if (thisCase.statusID < Constants.STATUS_Embed) {
								thisCase.embedTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Embed;
								updateEmbeded();
							}
							if (thisCase.statusID < Constants.STATUS_Microtomy) {
								thisCase.microTime = rst.getTimestamp("event_date").getTime();
								thisCase.statusID = Constants.STATUS_Microtomy;
								updateMicrotomed();
							}
							thisCase.statusID = Constants.STATUS_Slides;
							thisCase.stainTime = rst.getTimestamp("event_date").getTime();
							thisCase.stainID = rst.getShort("personnel_id");
							thisCase.updated = true;
						}
					}
					rst.close();
					if (thisCase.updated) {
						thisCase.noSlides = noSlides;
						if (updateStained() > 0) {
							noUpdates++;
						}
					}
				}
				if (i % 100 == 0) {
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException ignore) {}
				}
			}
			if (noUpdates > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, className,
						"Updated " + numbers.formatNumber(noUpdates) + " stained cases.");
			}
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private void getSpecimens() {
		final byte SPECIALTY_GENERAL = 1;
		short noBlocks = 0;
		String strLabel = "", strDescr = "";
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			// May be called from Grossing or Histology
			// re-validate specimens & specialty
			thisCase.noBlocks = 0;
			thisCase.mainBlocks = 0;
			thisCase.noSpecimens = 0;
			thisCase.procedureID = 0;
			thisCase.specialtyID = 0;
			thisCase.subspecialtyID = 0;
			thisCase.masterSpecimen = 0;
			stm = dbAP.getStatement(6);
			stm.setLong(1, thisCase.caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (masterSpecimens.matchSpecimens(rst.getInt("tmplt_profile_id"))) {
					thisCase.noSpecimens++;
					noBlocks = getNoBlocks(rst.getLong("id"));
					if (rst.getString("label_name") != null) {
						strLabel += " " + rst.getString("label_name");
					}
					if (rst.getString("description") != null) {
						strDescr += " " + rst.getString("description");
					}
					// Find best fit sub-specialty
					masterSpecimen = masterSpecimens.getItem();
					if (thisCase.procedureID < masterSpecimen.procedureID) {
						thisCase.procedureID = masterSpecimen.procedureID;
						thisCase.specialtyID = masterSpecimen.specialtyID;
						thisCase.subspecialtyID = masterSpecimen.subspecialtyID;
						thisCase.mainBlocks = noBlocks;
						thisCase.masterSpecimen = rst.getShort("tmplt_profile_id");
					} else if (thisCase.procedureID == masterSpecimen.procedureID
							&& thisCase.subspecialtyID < masterSpecimen.subspecialtyID) {
						thisCase.procedureID = masterSpecimen.procedureID;
						thisCase.specialtyID = masterSpecimen.specialtyID;
						thisCase.subspecialtyID = masterSpecimen.subspecialtyID;
						thisCase.mainBlocks = noBlocks;
						thisCase.masterSpecimen = rst.getShort("tmplt_profile_id");
					} else if (thisCase.procedureID == masterSpecimen.procedureID
							&& thisCase.subspecialtyID == masterSpecimen.subspecialtyID
							&& thisCase.mainBlocks < noBlocks) {
						thisCase.procedureID = masterSpecimen.procedureID;
						thisCase.specialtyID = masterSpecimen.specialtyID;
						thisCase.subspecialtyID = masterSpecimen.subspecialtyID;
						thisCase.mainBlocks = noBlocks;
						thisCase.masterSpecimen = rst.getShort("tmplt_profile_id");
					} else if (thisCase.masterSpecimen == 0) {
						thisCase.procedureID = masterSpecimen.procedureID;
						thisCase.specialtyID = masterSpecimen.specialtyID;
						thisCase.subspecialtyID = masterSpecimen.subspecialtyID;
						thisCase.mainBlocks = noBlocks;
						thisCase.masterSpecimen = rst.getShort("tmplt_profile_id");
					}
				}
			}
			if (thisCase.subspecialtyID <= SPECIALTY_GENERAL
					&& (strLabel.length() > 0
					|| strDescr.length() > 0)) {
				thisCase.subspecialtyID = masterSpecimens.getSubspecialtyID(
						strLabel, strDescr);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	private int insertCase() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			stm = dbPowerJ.getStatement(0);
			stm.setShort(1, thisCase.facilityID);
			stm.setLong(2, thisCase.masterSpecimen);
			stm.setShort(3, thisCase.grossID);
			stm.setShort(4, thisCase.microID);
			stm.setShort(5, thisCase.embedID);
			stm.setShort(6, thisCase.stainID);
			stm.setShort(7, thisCase.routeID);
			stm.setShort(8, thisCase.finalID);
			stm.setByte(9, thisCase.specialtyID);
			stm.setByte(10, thisCase.subspecialtyID);
			stm.setByte(11, thisCase.procedureID);
			stm.setInt(12, thisCase.statusID);
			stm.setByte(13, thisCase.noSpecimens);
			stm.setShort(14, thisCase.noBlocks);
			stm.setShort(15, thisCase.noSlides);
			stm.setTimestamp(16, new Timestamp(thisCase.accesTime));
			stm.setString(17, thisCase.caseNo);
			stm.setLong(18, thisCase.caseID);
			noUpdates = stm.executeUpdate();
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}
	
	private int updateAssigned() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			stm = dbPowerJ.getStatement(7);
			stm.setShort(1, thisCase.finalID);
			stm.setLong(2, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}

	private int updateEmbeded() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			stm = dbPowerJ.getStatement(1);
			thisCase.embedTAT = parent.dateUtils.getBusinessHours(
					thisCase.accesTime, thisCase.embedTime);
			stm.setLong(1, thisCase.masterSpecimen);
			stm.setShort(2, thisCase.embedID);
			stm.setByte(3, thisCase.specialtyID);
			stm.setByte(4, thisCase.subspecialtyID);
			stm.setByte(5, thisCase.procedureID);
			stm.setInt(6, thisCase.statusID);
			stm.setByte(7, thisCase.noSpecimens);
			stm.setShort(8, thisCase.noBlocks);
			stm.setInt(9, thisCase.embedTAT);
			stm.setTimestamp(10, new Timestamp(thisCase.embedTime));
			stm.setLong(11, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}

	private int updateFinal() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			thisCase.finalTAT = parent.dateUtils.getBusinessHours(
					thisCase.accesTime, thisCase.finalTime);
			stm = dbPowerJ.getStatement(2);
			stm.setInt(1, thisCase.statusID);
			stm.setShort(2, thisCase.finalID);
			stm.setInt(3, thisCase.finalTAT);
			stm.setTimestamp(4, new Timestamp(thisCase.finalTime));
			stm.setShort(5, thisCase.noSlides);
			stm.setLong(6, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}

	private int updateGrossed() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			thisCase.grossTAT = parent.dateUtils.getBusinessHours(
					thisCase.accesTime, thisCase.grossTime);
			stm = dbPowerJ.getStatement(3);
			stm.setLong(1, thisCase.masterSpecimen);
			stm.setShort(2, thisCase.grossID);
			stm.setByte(3, thisCase.specialtyID);
			stm.setByte(4, thisCase.subspecialtyID);
			stm.setByte(5, thisCase.procedureID);
			stm.setInt(6, thisCase.statusID);
			stm.setByte(7, thisCase.noSpecimens);
			stm.setShort(8, thisCase.noBlocks);
			stm.setInt(9, thisCase.grossTAT);
			stm.setTimestamp(10, new Timestamp(thisCase.grossTime));
			stm.setLong(11, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}

	private int updateMicrotomed() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			thisCase.microTAT = parent.dateUtils.getBusinessHours(
					thisCase.accesTime, thisCase.microTime);
			stm = dbPowerJ.getStatement(4);
			stm.setInt(1, thisCase.statusID);
			stm.setShort(2, thisCase.microID);
			stm.setInt(3, thisCase.microTAT);
			stm.setTimestamp(4, new Timestamp(thisCase.microTime));
			stm.setShort(5, thisCase.noBlocks);
			stm.setLong(6, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}

	private int updateRouted() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			thisCase.routeTAT = parent.dateUtils.getBusinessHours(
					thisCase.accesTime, thisCase.routeTime);
			stm = dbPowerJ.getStatement(5);
			stm.setInt(1, thisCase.statusID);
			stm.setShort(2, thisCase.routeID);
			stm.setInt(3, thisCase.routeTAT);
			stm.setTimestamp(4, new Timestamp(thisCase.routeTime));
			stm.setShort(5, thisCase.noSlides);
			stm.setLong(6, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}

	private int updateStained() {
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			thisCase.stainTAT = parent.dateUtils.getBusinessHours(
					thisCase.accesTime, thisCase.stainTime);
			stm = dbPowerJ.getStatement(6);
			stm.setInt(1, thisCase.statusID);
			stm.setShort(2, thisCase.stainID);
			stm.setInt(3, thisCase.stainTAT);
			stm.setTimestamp(4, new Timestamp(thisCase.stainTime));
			stm.setShort(5, thisCase.noSlides);
			stm.setLong(6, thisCase.caseID);
			noUpdates = stm.executeUpdate();
			thisCase.updated = false;
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		}
		return noUpdates;
	}
	
	class DataCase {
		boolean updated = false;
		boolean cancelled = false;
		byte statusID = 0;
		byte specialtyID = 0;
		byte subspecialtyID = 0;
		byte procedureID = 0;
		byte noSpecimens = 0;
		short facilityID = 0;
		short grossID = 0;
		short embedID = 0;
		short microID = 0;
		short stainID = 0;
		short routeID = 0;
		short finalID = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short mainBlocks = 0;
		short grossTAT = 0;
		short embedTAT = 0;
		short microTAT = 0;
		short stainTAT = 0;
		short routeTAT = 0;
		short finalTAT = 0;
		long caseID = 0;
		long masterSpecimen = 0;
		long accesTime = 0;
		long grossTime = 0;
		long embedTime = 0;
		long microTime = 0;
		long stainTime = 0;
		long routeTime = 0;
		long finalTime = 0;
		String caseNo = "";
	}
}
