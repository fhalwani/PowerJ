package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JOptionPane;

class MapFacilities {
	HashMap<Short, ClassData> facilities = new HashMap<Short, ClassData>();

	MapFacilities(PowerJ parent) {
		readDB(parent);
	}

	void close() {
		facilities.clear();
	}
	
	boolean doDashboard(short id) {
		boolean included = false;
		ClassData thisRow = facilities.get(id);
		if (thisRow != null) {
			// Else, tables not sync'ed
			included = thisRow.dashboard;
		}
		return included;
	}

	boolean doWorkload(short id) {
		boolean included = false;
		ClassData thisRow = facilities.get(id);
		if (thisRow != null) {
			// Else, tables not sync'ed
			included = thisRow.workload;
		}
		return included;
	}

	private void readDB(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getFacilities();
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.dashboard = (rst.getString("DASH").equalsIgnoreCase("Y"));
				thisRow.workload = (rst.getString("WLOAD").equalsIgnoreCase("Y"));
				facilities.put(rst.getShort("FACID"), thisRow);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Facilities Map", e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private class ClassData {
		boolean dashboard = false;
		boolean workload = false;
	}
}
