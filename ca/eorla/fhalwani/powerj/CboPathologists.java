package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboPathologists extends CboMain {
	private static final long serialVersionUID = -912237493459616295L;

	CboPathologists(PowerJ parent, boolean isEditor) {
		super(parent);
		setName("cboPathologists");
		strName = "Pathologists";
		getData(parent, isEditor);
		setModel();
	}

	void getData(PowerJ parent, boolean isEditor) {
		ResultSet rst = parent.dbPowerJ.getPathologists();
		try {
			if (!isEditor) {
				list.add(new DataItem(0, "* All *"));
			}
			while (rst.next()) {
				list.add(new DataItem(rst.getInt("PERID"),
						rst.getString("INITIALS")));
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
