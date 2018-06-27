package ca.eorla.fhalwani.powerj;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.swing.JOptionPane;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

class DbMSSQL extends DbPowerJ {
	
	DbMSSQL(PowerJ pj) {
		super(pj);
		strName = "MSSQL";
		setConnection();
	}

	void close() {
		super.close();
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException ignore) {
			// Ignore
		} finally {
			connection = null;
		}
	}

	ResultSet getAccTypes() {
		try {
			pstm = connection.prepareCall("{call udpAccessionsName}");
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getAdditionals(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT TOP (10000) * from udvAdditional";
		if (filters[0] > 0) {
			strFilter = " AND FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND SPYID = " + filters[1];
		}
		if (filters[2] > 0) {
			strFilter += " AND SUBID = " + filters[2];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY FINALED DESC";
		return getResultSet();
	}

	ResultSet getDashboard() {
		try {
			pstm = connection.prepareCall("{call udpDashboardCaseID}");
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}

	ResultSet getErrors(int order) {
		try {
			if (order == 0) {
				// To redo corrected errors
				pstm = connection.prepareCall("{call udpErrorZ}");
			} else {
				// To audit errors
				pstm = connection.prepareCall("{call udpErrorNZ}");
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getFinals(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT TOP (10000) * from udvCases";
		if (filters[0] > 0) {
			strFilter = " AND FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND SPYID = " + filters[1];
		}
		if (filters[2] > 0) {
			strFilter += " AND SUBID = " + filters[2];
		}
		if (filters[3] > 0) {
			strFilter += " AND PROID = " + filters[3];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY FINALED DESC";
		return getResultSet();
	}
	
	ResultSet getFrozens(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT TOP (10000) * from udvFrozens";
		if (filters[0] > 0) {
			strFilter = " AND FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND SUBID = " + filters[1];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY ACCESSED DESC";
		return getResultSet();
	}
	
	ResultSet getGroups(int order) {
		try {
			if (order == 0) {
				pstm = connection.prepareCall("{call udpGroupsID}");
			} else {
				pstm = connection.prepareCall("{call udpGroupsName}");
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getLastDash() {
		strSQL = "SELECT * from udvDashLastRun";
		return getResultSet();
	}

	ResultSet getLastStats() {
		strSQL = "SELECT * from udvStatsLastRun";
		return getResultSet();
	}

	ResultSet getLastWorkload() {
		strSQL = "SELECT * from udvWloadLastRun";
		return getResultSet();
	}
	
	ResultSet getMasterOrders() {
		strSQL = "SELECT * from udvMasterOrders ORDER BY ID";
		return getResultSet();
	}
	
	ResultSet getMasterSpecimens(int order) {
		try {
			if (order == 0) {
				pstm = connection.prepareCall("{call udpMasterSpecID}");
			} else {
				pstm = connection.prepareCall("{call udpMasterSpecName}");
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getPathologists() {
		try {
			pstm = connection.prepareCall("{call udpPathologists}");
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getPending() {
		try {
			pstm = connection.prepareCall("{call udpDashboardPending}");
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getSpecimens(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT TOP (10000) * from udvSpecimens";
		if (filters[0] > 0) {
			strFilter = " AND FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND SPYID = " + filters[1];
		}
		if (filters[2] > 0) {
			strFilter += " AND SUBID = " + filters[2];
		}
		if (filters[3] > 0) {
			strFilter += " AND MSID = " + filters[3];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY CODE, DESCR, CASENO";
		return getResultSet();
	}
	
	ResultSet getSubspecialties(int order) {
		try {
			if (order == 0) {
				pstm = connection.prepareCall("{call udpSubspecialID}");
			} else {
				pstm = connection.prepareCall("{call udpSubspecialName}");
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getStats(int[] filters, long timeFrom, long timeTo) {
		strSQL = "SELECT TOP (10000) * from udvStats " +
		"WHERE FINALED BETWEEN '" + new Timestamp(timeFrom) + "' AND '" +
				new Timestamp(timeTo) + "'";
		String strFilter = "";
		if (filters[0] > 0) {
			strFilter = " AND FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND SPYID = " + filters[1];
		}
		if (filters[2] > 0) {
			strFilter += " AND SUBID = " + filters[2];
		}
		if (filters[3] > 0) {
			strFilter += " AND PROID = " + filters[3];
		}
		if (strFilter.length() > 5) {
			strSQL += strFilter;
		}
		strSQL += " ORDER BY FINALED DESC";
		return getResultSet();
	}

	ResultSet getStatsSum(long timeFrom, long timeTo) {
		try {
			pstm = connection.prepareCall("{call udpStatsSum(?, ?)}");
			pstm.setDate(1, new java.sql.Date(timeFrom));
			pstm.setDate(2, new java.sql.Date(timeTo));
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}
	
	ResultSet getTATSum(long timeFrom, long timeTo) {
		try {
			pstm = connection.prepareCall("{call udpTATSum(?, ?)}");
			pstm.setDate(1, new java.sql.Date(timeFrom));
			pstm.setDate(2, new java.sql.Date(timeTo));
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}

	ResultSet getWLDetails(int[] rows, int[] values, long timeFrom, long timeTo) {
		strSQL = "SELECT TOP (10000) * from udvCases " +
		"WHERE FINALED BETWEEN '" + new Timestamp(timeFrom) + "' AND '" +
				new Timestamp(timeTo) + "'";
		String filter = "";
		for (int i = 1; i < values.length; i++) {
			switch (rows[i -1]) {
			case Constants.ROW_FACILITY:
				filter += " AND FACID = " + values[i];
				break;
			case Constants.ROW_SPECIALTY:
				filter += " AND SPYID = " + values[i];
				break;
			case Constants.ROW_SUBSPECIALTY:
				filter += " AND SUBID = " + values[i];
				break;
			case Constants.ROW_STAFF:
				filter += " AND FINALID = " + values[i];
				break;
			default:
				// 0 (hidden rows, ignore)
			}
		}
		if (filter.length() > 5) {
			strSQL += filter;
		}
		strSQL += " ORDER BY FINALED DESC";
		return getResultSet();
	}
	
	void prepareAdditionals() {
		try {
			closeStms();
			CallableStatement ctm = connection.prepareCall("{call udpSpecimensCaseID(?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpAdditionalCaseID(?)}");
			stms.add(ctm);
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Additional " + 
					"(CASEID, PERID, CODEID, FINALED, VALUE1, VALUE2, " +
					"VALUE3, VALUE4) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareAuditor() {
		try {
			closeStms();
			CallableStatement ctm = connection.prepareCall("{call udpCommentsCaseID(?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpOrdersSpecID(?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpSpecimensCaseID(?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpFrozensCaseID(?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpAdditionalsCaseID(?)}");
			stms.add(ctm);
			PreparedStatement stm = connection.prepareStatement("UPDATE Frozens " +
					"SET PERID = ? WHERE CASEID = ?");
			stms.add(stm);
			if (!parent.variables.offLine) {
				stm = connection.prepareStatement("INSERT INTO Errors " +
						"(CASEID, ERRID, CASENO, COMMENT) VALUES (?, ?, ?, ?)");
				stms.add(stm);
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareErrors() {
		try {
			closeStms();
			CallableStatement ctm = connection.prepareCall("{call udpErrorsCaseID(?)}");
			stms.add(ctm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareFrozens() {
		try {
			closeStms();
			CallableStatement ctm = connection.prepareCall("{call udpSpecimensCaseID(?)}");
			stms.add(ctm);
			if (!parent.variables.offLine) {
				PreparedStatement stm = connection.prepareStatement("INSERT INTO Errors " +
						"(CASEID, ERRID, CASENO, COMMENT) VALUES (?, ?, ?, ?)");
				stms.add(stm);
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareSummary() {
		try {
			closeStms();
			CallableStatement ctm = connection.prepareCall("{call udpCasesSum(?, ?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpFrozensSum(?, ?)}");
			stms.add(ctm);
			ctm = connection.prepareCall("{call udpAdditionalSum(?, ?)}");
			stms.add(ctm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	/**
	 * Opens the Microsoft SQL Server connection.
	 */
	private void setConnection() {
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(parent.variables.pjPort);
			ds.setServerName(parent.variables.pjAddress);
			ds.setDatabaseName(Constants.APP_NAME);
			ds.setUser(parent.variables.pjLogin);
			ds.setPassword(parent.variables.pjPassword);
			connection = ds.getConnection();
			connected = true;
			stm = connection.createStatement();
		} catch (ClassNotFoundException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} catch (SQLServerException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} catch (SQLException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
}
