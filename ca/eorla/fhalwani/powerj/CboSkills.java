package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class CboSkills extends CboMain {
	private static final long serialVersionUID = -6979583277864069723L;

	public CboSkills(PowerJ parent) {
		super(parent);
		setName("cboSkills");
		strName = "Skills";
		getData(parent);
		setModel();
	}

	void getData(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getSkills();
		try {
			while (rst.next()) {
				list.add(new DataItem(rst.getInt("ID"),
						rst.getString("DESCR")));
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
