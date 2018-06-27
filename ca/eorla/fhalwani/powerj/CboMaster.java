package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboMaster extends CboMain {
	private static final long serialVersionUID = 941499526083642507L;

	CboMaster(PowerJ parent, boolean isEditor) {
		super(parent);
		setName("cboMaster");
		strName = "cboMaster";
		getData(parent, isEditor);
		setModel();
	}
	
	void getData(PowerJ parent, boolean isEditor) {
		ResultSet rst = parent.dbPowerJ.getMasterSpecimens(1);
		try {
			if (!isEditor) {
				list.add(new DataItem(0, "* All *"));
			}
			while (rst.next()) {
				list.add(new DataItem(rst.getInt("MSID"),
					rst.getString("CODE")));
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
