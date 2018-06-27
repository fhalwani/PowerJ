package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboGroups extends CboMain {
	private static final long serialVersionUID = 5058852697662583602L;

	CboGroups(PowerJ parent, boolean isEditor) {
		super(parent);
		setName("cboGroups");
		strName = "Groups";
		getData(parent, isEditor);
		setModel();
	}

	void getData(PowerJ parent, boolean isEditor) {
		ResultSet rst = parent.dbPowerJ.getGroups(1);
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
