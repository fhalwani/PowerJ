package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboFacilities extends CboMain {
	private static final long serialVersionUID = -4476817004256059536L;

	CboFacilities(PowerJ parent) {
		super(parent);
		setName("cboFacility");
		strName = "Facilities";
		getData(parent);
		setModel();
	}
	
	void getData(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getFacilities();
		try {
			list.add(new DataItem(0, "* All *"));
			while (rst.next()) {
				if (rst.getString("WLOAD").equals("Y")) {
					list.add(new DataItem(rst.getInt("FACID"),
							rst.getString("CODE")));
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
