package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboSpecialties extends CboMain {
	private static final long serialVersionUID = 6905567884769288835L;

	CboSpecialties(PowerJ parent, boolean isEditor) {
		super(parent);
		setName("cboSpecialties");
		strName = "Specialties";
		getData(parent, isEditor);
		setModel();
	}
	
	void getData(PowerJ parent, boolean isEditor) {
		ResultSet rst = parent.dbPowerJ.getSpecialties(1);
		try {
			if (!isEditor) {
				list.add(new DataItem(0, "* All *"));
			}
			while (rst.next()) {
				if (rst.getString("WLOAD").equals("Y")
						|| isEditor) {
					list.add(new DataItem(rst.getInt("SPYID"),
							rst.getString("SPYNAME")));
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
