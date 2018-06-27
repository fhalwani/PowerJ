package ca.eorla.fhalwani.powerj;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;

class DbMain {
	boolean connected = false;
	String strName = "Database";
	String strSQL = "";
	Connection connection = null;
	Statement stm = null;
	PreparedStatement pstm = null;
	ArrayList<PreparedStatement> stms = new ArrayList<PreparedStatement>();
	PowerJ parent;

	DbMain(PowerJ pj) {
		parent = pj;
	}

	void close() {
		try {
			closeStm();
			closeStms();
			if (!connection.isClosed()) {
				connection.close();
			}
		} catch (Exception ignore) {
		} finally {
			connection = null;
			connected = false;
		}
	}
	
	/** Closes the active recordset. */
	void closeRst(ResultSet rst) {
		try {
			if (!rst.isClosed())
				rst.close();
		} catch (Exception ignore) {}
	}
	
	/** Closes the default statement. */
	void closeStm() {
		if (stm != null) {
			closeStm(stm);
		}
		if (pstm != null) {
			closeStm(pstm);
		}
	}
	
	void closeStm(PreparedStatement stm) {
		try {
			if (!stm.isClosed())
				stm.close();
		} catch (Exception ignore) {}
	}
	
	void closeStm(Statement stm) {
		try {
			if (!stm.isClosed())
				stm.close();
		} catch (Exception ignore) {}
	}

	/** Closes the statements array. */
	void closeStms() {
		for (int i = stms.size() -1; i >= 0; i--) {
			closeStm(stms.get(i));
			stms.remove(i);
		}
	}
	
	int execute() {
		int n = 0;
		try {
			if (connected) {
				stm = connection.createStatement();
				n = stm.executeUpdate(strSQL);
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return n;
	}
	
	ResultSet getResultSet() {
		Statement stm = null;
		ResultSet rst = null;
		try {
			if (connected) {
				stm = connection.createStatement();
				rst = stm.executeQuery(strSQL);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return rst;
	}
	
	ResultSet getResultSet(PreparedStatement pstm) {
		ResultSet rst = null;
		try {
			if (connected) {
				rst = pstm.executeQuery();
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return rst;
	}
	
	PreparedStatement getStatement(int i) {
		return stms.get(i);
	}
}
