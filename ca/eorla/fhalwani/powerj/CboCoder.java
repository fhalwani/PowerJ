package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboCoder extends CboMain {
	private static final long serialVersionUID = 7482882114520723818L;

	CboCoder(PowerJ parent, boolean isEditor, byte coderID) {
		super(parent);
		strName = parent.variables.codersName[coderID -1];
		setName(strName);
		getData(parent, isEditor, coderID);
		setModel();
	}

	void getData(PowerJ parent, boolean isEditor, byte coderID) {
		ResultSet rst = parent.dbPowerJ.getCoder(coderID);
		try {
			if (!isEditor) {
				list.add(new DataItem(0, "* All *"));
			}
			while (rst.next()) {
				list.add(new DataItem(rst.getShort("ID"),
						rst.getString("NAME")));
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
