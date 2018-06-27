package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboSubspecial extends CboMain {
	private static final long serialVersionUID = -8554297647654415259L;

	CboSubspecial(PowerJ parent, boolean isEditor) {
		super(parent);
		setName("cboSubspecialties");
		strName = "Subspecialties";
		getData(parent, isEditor);
		setModel();
	}
	
	void getData(PowerJ parent, boolean isEditor) {
		ResultSet rst = parent.dbPowerJ.getSubspecialties(1);
		try {
			if (!isEditor) {
				list.add(new DataItem(0, "* All *"));
			}
			while (rst.next()) {
				if (isEditor) {
					list.add(new DataItem(rst.getInt("SUBID"),
							rst.getString("SUBINIT")));
				} else {
					list.add(new DataItem(rst.getInt("SUBID"),
							rst.getString("SUBNAME")));
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
