package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

class CboContracts extends CboMain {
	private static final long serialVersionUID = -209567259078018429L;

	CboContracts(PowerJ parent) {
		super(parent);
		setName("Contracts");
		strName = "Contracts";
		getData(parent);
		setModel();
	}

	void getData(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getContracts();
		try {
			while (rst.next()) {
				list.add(new DataItem(rst.getShort("ID"),
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
