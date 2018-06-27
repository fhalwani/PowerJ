package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JOptionPane;

class MapAccessions {
	private boolean codeSpecimen = false;
	private byte specialtyID = 0;
	private HashMap<Short, ClassData> accessions = new HashMap<Short, ClassData>();

	MapAccessions(PowerJ parent) {
		readDB(parent);
	}
	
	void close() {
		accessions.clear();
	}
	
	boolean codeSpecimen() {
		return codeSpecimen;
	}
	
	boolean doDashboard(short id) {
		boolean included = false;
		ClassData thisRow = accessions.get(id);
		if (thisRow != null) {
			// Else, tables not sync'ed
			included = thisRow.dashboard;
			specialtyID = thisRow.specialtyID;
			codeSpecimen = thisRow.codeSpec;
		}
		return included;
	}
	
	boolean doWorkload(short id) {
		boolean included = false;
		ClassData thisRow = accessions.get(id);
		if (thisRow != null) {
			// Else, tables not sync'ed
			included = thisRow.workload;
			specialtyID = thisRow.specialtyID;
			codeSpecimen = thisRow.codeSpec;
		}
		return included;
	}
	
	byte getSpecialty() {
		return specialtyID;
	}
	
	private void readDB(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getAccessions();
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.specialtyID = rst.getByte("SPYID");
				// Both Accessions and main switch (specialty) must be active
				thisRow.dashboard = (rst.getString("DASH").equalsIgnoreCase("Y")
						&& rst.getString("SDASH").equalsIgnoreCase("Y"));
				// Both Accessions and main switch (specialty) must be active
				thisRow.workload = (rst.getString("WLOAD").equalsIgnoreCase("Y")
						&& rst.getString("SWLOAD").equalsIgnoreCase("Y"));
				thisRow.codeSpec = (rst.getString("CODESPEC").equalsIgnoreCase("Y"));
				accessions.put(rst.getShort("ACCID"), thisRow);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Accessions Map", e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private class ClassData {
		boolean dashboard = false;
		boolean workload = false;
		boolean codeSpec = true;
		byte specialtyID = 0;
	}
}
