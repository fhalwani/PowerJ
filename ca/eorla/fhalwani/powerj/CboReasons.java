package ca.eorla.fhalwani.powerj;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

class CboReasons extends CboMain {

	public CboReasons(PowerJ parent) {
		super(parent);
		setName("cboReasons");
		strName ="cboReasons";
		getData(parent);
		setModel();
	}

	void getData(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getReasons();
		try {
			while (rst.next()) {
				list.add(new DataItem(rst.getByte("ID"),
						rst.getString("Descr")));
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
