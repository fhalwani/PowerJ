package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JOptionPane;

class MapSubspecialties {
	private HashMap<Byte, String> masterSpecialties = new HashMap<Byte, String>();

	MapSubspecialties(PowerJ parent) {
		readDB(parent);
	}

	void close() {
		masterSpecialties.clear();
	}
	
	String getSpecialty(byte id) {
		String specialty = masterSpecialties.get(id);
		if (specialty == null) {
			specialty = "Error";
		}
		return specialty;
	}

	private void readDB(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getSubspecialties(1);
		try {
			while (rst.next()) {
				masterSpecialties.put(rst.getByte("SUBID"),
					rst.getString("SUBINIT"));
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Subspecialties Map", e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
