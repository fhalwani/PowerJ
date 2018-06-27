package ca.eorla.fhalwani.powerj;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import javax.swing.JOptionPane;

class DbDerby extends DbPowerJ {

	DbDerby(PowerJ pj) {
		super(pj);
		strName = "Derby";
		setConnection();
	}

	void close() {
		super.close();
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (!e.getSQLState().equals("XJ015")) {
				// Else, normal shut down
				parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
			}
		}
	}
	
	ResultSet getAccTypes() {
		strSQL = "SELECT a.ACCID, a.SPYID, a.DASH, a.WLOAD, " +
				"a.ACCNAME, s.SPYNAME FROM Accessions AS a " +
				"INNER JOIN Specialties AS s ON s.SPYID = a.SPYID " +
				"ORDER BY a.ACCNAME";
		return getResultSet();
	}
	
	ResultSet getAdditionals(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT a.CASEID, a.CODEID, a.VALUE1, a.VALUE2, a.VALUE3, " +
				"a.VALUE4, a.FINALED, c.CASENO, p.INITIALS " +
				"FROM Additional AS a " +
				"INNER JOIN Cases AS c ON c.CASEID = a.CASEID " +
				"INNER JOIN Facilities AS f ON f.FACID = c.FACID " +
				"INNER JOIN Personnel AS p ON p.PERID = a.PERID " +
				"INNER JOIN Specialties AS s ON s.SPYID = c.SPYID " +
				"INNER JOIN Subspecial AS b ON b.SUBID = c.SUBID ";
		if (filters[0] > 0) {
			strFilter = " AND c.FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND c.SPYID = " + filters[1];
		}
		if (filters[2] > 0) {
			strFilter += " AND c.SUBID = " + filters[2];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY a.FINALED DESC FETCH FIRST 10000 ROWS ONLY";
		return getResultSet();
	}

	ResultSet getDashboard() {
		strSQL = "SELECT p.CASEID, p.FACID, p.GROSSTAT, p.EMBEDTAT, " +
				"p.MICROTAT, p.STAINTAT, p.ROUTETAT, p.FINALTAT, p.GROSSID, " +
				"p.EMBEDID, p.MICROID, p.STAINID, p.ROUTEID, p.FINALID, p.SUBID, " +
				"p.PROID, p.STATUS, p.NOSPECS, p.NOBLOCKS, p.NOSLIDES, p.ACCESSED, " +
				"p.GROSSED, p.EMBEDED, p.MICROED, p.STAINED, p.ROUTED, p.FINALED, " +
				"p.CASENO, s.SUBINIT, m.CODE, m.GROSS, m.EMBED, m.MICROTOMY, " +
				"m.ROUTE, m.SIGNOUT, p1.INITIALS AS GROSSINI, " +
				"p2.INITIALS AS EMBEDINI, p3.INITIALS AS MICROINI, " +
				"p4.INITIALS AS STAININI, p5.INITIALS AS ROUTEINI, " +
				"p6.INITIALS AS FINALINI " +
				"FROM Pending AS p " +
				"INNER JOIN Subspecial AS s ON s.SUBID = p.SUBID " +
				"INNER JOIN MasterSpecimens AS m ON m.MSID = p.MSID " +
				"INNER JOIN Personnel AS p1 ON p1.PERID = p.GROSSID " +
				"INNER JOIN Personnel AS p2 ON p2.PERID = p.EMBEDID " +
				"INNER JOIN Personnel AS p3 ON p3.PERID = p.MICROID " +
				"INNER JOIN Personnel AS p4 ON p4.PERID = p.STAINID " +
				"INNER JOIN Personnel AS p5 ON p5.PERID = p.ROUTEID " +
				"INNER JOIN Personnel AS p6 ON p6.PERID = p.FINALID " +
				"ORDER BY p.CASEID";
		return getResultSet();
	}
	
	ResultSet getErrors(int errID) {
		switch (errID) {
		case 0:
			// To redo corrected errors
			strSQL = "SELECT CASEID " +
					"FROM Errors WHERE ERRID = 0 " +
					"ORDER BY CASEID";
			break;
		default:
			// To audit errors
			strSQL = "SELECT CASEID, ERRID, CASENO " +
					"FROM Errors WHERE ERRID > 0 " +
					"ORDER BY CASENO";
		}
		return getResultSet();
	}
	
	ResultSet getFinals(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT c.CASEID, c.FINALED, c.PROID, " +
				"c.NOSYNOPT, c.NOSPECS, c.NOBLOCKS, c.NOSLIDES, c.NOFS, " +
				"c.VALUE1, c.VALUE2, c.VALUE3, c.VALUE4, c.CASENO, " +
				"s.SUBINIT, p.INITIALS, y.SPYNAME FROM Cases AS c " +
				"INNER JOIN Specialties AS y ON y.SPYID = c.SPYID " +
				"INNER JOIN Subspecial AS s ON s.SUBID = c.SUBID " +
				"INNER JOIN Personnel AS p ON p.PERID = c.FINALID";
			if (filters[0] > 0) {
				strFilter = " AND c.FACID = " + filters[0];
			}
			if (filters[1] > 0) {
				strFilter += " AND c.SPYID = " + filters[1];
			}
			if (filters[2] > 0) {
				strFilter += " AND c.SUBID = " + filters[2];
			}
			if (filters[3] > 0) {
				strFilter += " AND c.PROID = " + filters[3];
			}
			if (strFilter.length() > 5) {
				// Substitute the 1st "AND" with a "WHERE"
				strSQL += " WHERE " + strFilter.substring(5);
			}
			strSQL += " ORDER BY c.FINALED DESC FETCH FIRST 10000 ROWS ONLY";
		return getResultSet();
	}
	
	ResultSet getFrozens(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT f.CASEID, f.NOSPECS, f.NOBLOCKS, f.NOSLIDES, " +
				"f.VALUE1, f.VALUE2, f.VALUE3, f.VALUE4, c.ACCESSED, " +
				"c.CASENO, c.NOFS, c.PROID, s.SUBINIT, p.INITIALS " +
				"FROM Frozens AS f " +
				"INNER JOIN Cases AS c ON c.CASEID = f.CASEID " +
				"INNER JOIN Subspecial AS s ON s.SUBID = c.SUBID " +
				"INNER JOIN Personnel AS p ON p.PERID = f.PERID";
		if (filters[0] > 0) {
			strFilter = " AND c.FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND c.SUBID = " + filters[1];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY c.ACCESSED DESC " +
			"FETCH FIRST 10000 ROWS ONLY";
		return getResultSet();
	}
	
	ResultSet getGroups(int order) {
		strSQL = "SELECT g.ID, g.GRP, g.CODE1, g.CODE2, g.CODE3, g.CODE4, " +
				"g.NAME, c1.NAME AS NAME1, c2.NAME AS NAME2, " +
				"c3.NAME AS NAME3, c4.NAME AS NAME4 FROM Groups AS g " +
				"INNER JOIN Coder1 AS c1 ON c1.ID = g.CODE1 " +
				"INNER JOIN Coder2 AS c2 ON c2.ID = g.CODE2 " +
				"INNER JOIN Coder3 AS c3 ON c3.ID = g.CODE3 " +
				"INNER JOIN Coder4 AS c4 ON c4.ID = g.CODE4 ";
		if (order == 0) {
			strSQL += "ORDER BY g.ID";
		} else {
			strSQL += "ORDER BY g.NAME";
		}
		return getResultSet();
	}
	
	ResultSet getLastDash() {
		strSQL = "SELECT MAX(ACCESSED) AS accession FROM Pending";
		return getResultSet();
	}

	ResultSet getLastStats() {
		strSQL = "SELECT MAX(FINALED) AS finaled FROM Stats";
		return getResultSet();
	}

	ResultSet getLastWorkload() {
		strSQL = "SELECT MAX(FINALED) AS finaled FROM Cases";
		return getResultSet();
	}
	
	ResultSet getMasterOrders() {
		strSQL = "SELECT o.ID, o.GRPID, o.CODE, o.DESCR, " +
				"g.GRP, g.NAME AS NAMEG, c1.NAME AS NAME1, " +
				"c2.NAME AS NAME2, c3.NAME AS NAME3, " + 
				"c4.NAME AS NAME4 FROM MasterOrders AS o " +
				"INNER JOIN Groups AS g ON g.ID = o.GRPID " +
				"INNER JOIN Coder1 AS c1 ON c1.ID = g.CODE1 " +
				"INNER JOIN Coder2 AS c2 ON c2.ID = g.CODE2 " +
				"INNER JOIN Coder3 AS c3 ON c3.ID = g.CODE3 " +
				"INNER JOIN Coder4 AS c4 ON c4.ID = g.CODE4 " +
				"ORDER BY o.ID";
		return getResultSet();
	}
	
	ResultSet getMasterSpecimens(int order) {
		strSQL = "SELECT m.MSID, m.SPYID, m.SUBID, m.PROCID, " +
				"m.ISLN, m.GROSS, m.EMBED, m.MICROTOMY, m.ROUTE, m.SIGNOUT, " +
				"m.CODE1B, m.CODE1M, m.CODE1R, m.CODE2B, m.CODE2M, m.CODE2R, m.CODE3B, " +
				"m.CODE3M, m.CODE3R, m.CODE4B, m.CODE4M, m.CODE4R, m.CODE, m.DESCR, " +
				"s.SPYNAME, b.SUBNAME, c1b.NAME AS CODE1NB, c1m.NAME AS CODE1NM, " +
				"c1r.NAME AS CODE1NR, c2b.NAME AS CODE2NB, c2m.NAME AS CODE2NM, " +
				"c2r.NAME AS CODE2NR, c3b.NAME AS CODE3NB, c3m.NAME AS CODE3NM, " +
				"c3r.NAME AS CODE3NR, c4b.NAME AS CODE4NB, c4m.NAME AS CODE4NM, " +
				"c4r.NAME AS CODE4NR FROM MasterSpecimens AS m " +
				"INNER JOIN Coder1 AS c1b ON c1b.ID = m.CODE1B " +
				"INNER JOIN Coder2 AS c2b ON c2b.ID = m.CODE2B " +
				"INNER JOIN Coder3 AS c3b ON c3b.ID = m.CODE3B " +
				"INNER JOIN Coder4 AS c4b ON c4b.ID = m.CODE4B " +
				"INNER JOIN Coder1 AS c1m ON c1m.ID = m.CODE1M " +
				"INNER JOIN Coder2 AS c2m ON c2m.ID = m.CODE2M " +
				"INNER JOIN Coder3 AS c3m ON c3m.ID = m.CODE3M " +
				"INNER JOIN Coder4 AS c4m ON c4m.ID = m.CODE4M " +
				"INNER JOIN Coder1 AS c1r ON c1r.ID = m.CODE1R " +
				"INNER JOIN Coder2 AS c2r ON c2r.ID = m.CODE2R " +
				"INNER JOIN Coder3 AS c3r ON c3r.ID = m.CODE3R " +
				"INNER JOIN Coder4 AS c4r ON c4r.ID = m.CODE4R " +
				"INNER JOIN Specialties AS s ON s.SPYID = m.SPYID " +
				"INNER JOIN Subspecial AS b ON b.SUBID = m.SUBID ";
		if (order == 0) {
			strSQL += "ORDER BY m.MSID";
		} else {
			strSQL += "ORDER BY m.CODE";
		}
		return getResultSet();
	}
	
	ResultSet getPathologists() {
		strSQL = "SELECT PERID, INITIALS FROM Personnel " +
				"WHERE CODE = 'PT' ORDER BY INITIALS";
		return getResultSet();
	}
	
	ResultSet getPending() {
		strSQL = "SELECT CASEID, FACID, MSID, GROSSTAT, EMBEDTAT, MICROTAT, " + 
				"STAINTAT, ROUTETAT, HISTOTAT, FINALTAT, GROSSID, EMBEDID, " + 
				"MICROID, STAINID, ROUTEID, FINALID, SPYID, SUBID, PROID, " + 
				"STATUS, NOSPECS, NOBLOCKS, NOSLIDES, ACCESSED, GROSSED, " + 
				"EMBEDED, MICROED, STAINED, ROUTED, FINALED, CASENO " +
				"FROM Pending WHERE STATUS < " + Constants.STATUS_Final +
				" ORDER BY CASEID";
		return getResultSet();
	}
	
	ResultSet getSpecimens(int[] filters) {
		String strFilter = "";
		strSQL = "SELECT s.SPECID, s.CASEID, s.MSID, s.DESCR, " +
				"m.CODE, m.DESCR AS MDESCR, c.CASENO " +
				"FROM Specimens AS s " +
				"INNER JOIN MasterSpecimens AS m ON m.MSID = s.MSID " +
				"INNER JOIN Cases AS c ON c.CASEID = s.CASEID";
		if (filters[0] > 0) {
			strFilter = " AND c.FACID = " + filters[0];
		}
		if (filters[1] > 0) {
			strFilter += " AND c.SPYID = " + filters[1];
		}
		if (filters[2] > 0) {
			strFilter += " AND c.SUBID = " + filters[2];
		}
		if (filters[3] > 0) {
			strFilter += " AND s.MSID = " + filters[3];
		}
		if (strFilter.length() > 5) {
			// Substitute the 1st "AND" with a "WHERE"
			strSQL += " WHERE " + strFilter.substring(5);
		}
		strSQL += " ORDER BY m.CODE, m.DESCR, c.CASENO " +
			"FETCH FIRST 10000 ROWS ONLY";
		return getResultSet();
	}
	
	ResultSet getSubspecialties(int order) {
		strSQL = "SELECT b.SUBID, b.SPYID, b.SUBINIT, b.SUBNAME, s.SPYNAME " +
				"FROM Subspecial AS b " +
				"INNER JOIN Specialties AS s ON s.SPYID = b.SPYID ";
		if (order == 0) {
			strSQL += "ORDER BY SUBID";
		} else {
			strSQL += "ORDER BY SUBNAME";
		}
		return getResultSet();
	}
	
	ResultSet getStats(int[] filters, long timeFrom, long timeTo) {
		strSQL = "SELECT s.CASEID, s.PROID, s.NOSPECS, " +
				"s.NOBLOCKS, s.NOSLIDES, s.NOHE, s.NOSS, s.NOIHC, s.NOMOL, " +
				"s.NOFSP, s.NOFBL, s.NOFSL, s.NOSYN, s.GRTAT, s.ROTAT, s.FITAT, " + 
				"s.TOTAT, s.ACCESSED, s.GROSSED, s.ROUTED, s.FINALED, " +
				"s.CASENO, f.CODE AS FACI, p.SPYNAME, b.SUBINIT, e.PLAST " +
				"FROM Stats AS s " +
				"INNER JOIN Facilities AS f ON f.FACID = s.FACID " +
				"INNER JOIN Specialties AS p ON p.SPYID = s.SPYID " +
				"INNER JOIN Subspecial AS b ON b.SUBID = s.SUBID " +
				"INNER JOIN Personnel AS e ON e.PERID = s.FINALID " +
				"WHERE FINALED BETWEEN '" + new Timestamp(timeFrom) +
				"' AND '" + new Timestamp(timeTo) + "'";
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
		strSQL += " ORDER BY FINALED DESC FETCH FIRST 10000 ROWS ONLY";
		return getResultSet();
	}

	ResultSet getStatsSum(long timeFrom, long timeTo) {
		try {
			pstm = connection.prepareStatement("SELECT s.FACID, s.SPYID, s.SUBID, " +
					"s.PROID, s.FINALID, f.CODE AS FACI, p.SPYNAME, b.SUBINIT, " +
					"e.PLAST, COUNT(s.CASEID) AS NOCASES, SUM(s.NOSPECS) AS NOSPECS, " +
					"SUM(s.NOBLOCKS) AS NOBLOCKS, SUM(s.NOSLIDES) AS NOSLIDES, SUM(s.NOHE) AS NOHE, " +
					"SUM(s.NOSS) AS NOSS, SUM(s.NOIHC) AS NOIHC, SUM(s.NOMOL) AS NOMOL, SUM(s.NOFSP) AS NOFSP, " +
					"SUM(s.NOFBL) AS NOFBL, SUM(s.NOFSL) AS NOFSL, SUM(s.NOSYN) AS NOSYN, " +
					"SUM(CAST(s.GRTAT as INT)) AS GRTAT, SUM(CAST(s.ROTAT as INT)) AS ROTAT, " + 
					"SUM(CAST(s.FITAT as INT)) AS FITAT, SUM(CAST(s.TOTAT as INT)) AS TOTAT " +
					"FROM Stats AS s " +
					"INNER JOIN Facilities AS f ON f.FACID = s.FACID " +
					"INNER JOIN Specialties AS p ON p.SPYID = s.SPYID " +
					"INNER JOIN Subspecial AS b ON b.SUBID = s.SUBID " +
					"INNER JOIN Personnel AS e ON e.PERID = s.FINALID " +
					"WHERE s.FINALED BETWEEN ? AND ? " +
					"GROUP BY s.FACID, s.SPYID, s.SUBID, s.PROID, s.FINALID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.PLAST " +
					"ORDER BY s.FACID, s.SPYID, s.SUBID, s.PROID, s.FINALID");
			pstm.setDate(1, new java.sql.Date(timeFrom));
			pstm.setDate(2, new java.sql.Date(timeTo));
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}

	ResultSet getTATSum(long timeFrom, long timeTo) {
		try {
			pstm = connection.prepareStatement("SELECT c.FACID, c.SPYID, c.SUBID, " +
					"c.PROID, c.FINALID, f.CODE AS FACI, p.SPYNAME, b.SUBINIT, " +
					"e.INITIALS, COUNT(c.CASEID) AS NOCASES, SUM(CAST(c.GROSSTAT as INT)) AS GROSS, " +
					"SUM(CAST(c.ROUTETAT as INT)) AS ROUTE, SUM(CAST(c.FINALTAT as INT)) AS FINAL, " + 
					"SUM(CAST(c.TOTALTAT as INT)) AS TOTAL " +
					"FROM Cases AS c " +
					"INNER JOIN Facilities AS f ON f.FACID = c.FACID " +
					"INNER JOIN Specialties AS p ON p.SPYID = c.SPYID " +
					"INNER JOIN Subspecial AS b ON b.SUBID = c.SUBID " +
					"INNER JOIN Personnel AS e ON e.PERID = c.FINALID " +
					"WHERE c.FINALED BETWEEN ? AND ? " +
					"GROUP BY c.FACID, c.SPYID, c.SUBID, c.PROID, c.FINALID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.INITIALS " +
					"ORDER BY c.FACID, c.SPYID, c.SUBID, c.PROID, c.FINALID");
			pstm.setDate(1, new java.sql.Date(timeFrom));
			pstm.setDate(2, new java.sql.Date(timeTo));
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return getResultSet(pstm);
	}

	ResultSet getWLDetails(int[] rows, int[] values, long timeFrom, long timeTo) {
		String filter = "";
		strSQL = "SELECT c.CASEID, c.FACID, c.SPYID, c.SUBID, c.FINALID, c.PROID, " +
				"c.NOSYNOPT, c.NOSPECS, c.NOBLOCKS, c.NOSLIDES, c.NOFS, " +
				"c.VALUE1, c.VALUE2, c.VALUE3, c.VALUE4, c.FINALED, c.CASENO, " +
				"f.CODE AS FACI, b.SUBINIT, p.INITIALS, s.SPYNAME, m.CODE AS SPEC " +
				"FROM Cases AS c " +
				"INNER JOIN Facilities AS f ON f.FACID = c.FACID " +
				"INNER JOIN Specialties AS s ON s.SPYID = c.SPYID " +
				"INNER JOIN Subspecial AS b ON b.SUBID = c.SUBID " +
				"INNER JOIN MasterSpecimens AS m ON m.MSID = c.MSID " +
				"INNER JOIN Personnel AS p ON p.PERID = c.FINALID " +
				"WHERE c.FINALED BETWEEN '" + new Timestamp(timeFrom) + "' AND '" +
				new Timestamp(timeTo) + "'";
		for (int i = 1; i < values.length; i++) {
			switch (rows[i -1]) {
			case Constants.ROW_FACILITY:
				filter += " AND c.FACID = " + values[i];
				break;
			case Constants.ROW_SPECIALTY:
				filter += " AND c.SPYID = " + values[i];
				break;
			case Constants.ROW_SUBSPECIALTY:
				filter += " AND c.SUBID = " + values[i];
				break;
			case Constants.ROW_STAFF:
				filter += " AND c.FINALID = " + values[i];
				break;
			default:
				// 0 (hidden rows, ignore)
			}
		}
		if (filter.length() > 5) {
			strSQL += filter;
		}
		strSQL += " ORDER BY c.FINALED DESC FETCH FIRST 10000 ROWS ONLY";
		return getResultSet();
	}
	
	void prepareAdditionals() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT c.CASEID, " +
					"s.SPECID, c.MSID, c.FINALED, c.CASENO " +
					"FROM Cases AS c INNER JOIN Specimens AS s " +
					"ON s.CASEID = c.CASEID AND s.MSID = c.MSID " +
					"WHERE c.CASEID = ?");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT a.FINALED, " +
					"a.PERID, a.CODEID, c.CASENO " +
					"FROM Additional AS a " +
					"INNER JOIN Cases AS c ON c.CASEID = a.CASEID " +
					"WHERE a.CASEID = ? " +
					"ORDER BY a.FINALED");
			stms.add(stm);
			stm = connection.prepareStatement("INSERT INTO Additional " + 
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
			PreparedStatement stm = connection.prepareStatement("SELECT COMMENT " +
					"FROM Comments WHERE CASEID = ?");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT o.QTY, " +
					"o.VALUE1, o.VALUE2, o.VALUE3, o.VALUE4, " +
					"g.NAME FROM Orders AS o " +
					"INNER JOIN Groups AS g ON g.ID = o.GRPID " +
					"WHERE o.SPECID = ? ORDER BY g.NAME");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT s.SPECID, " +
					"s.MSID, s.NOBLOCKS, s.NOSLIDES, s.NOFRAGS, s.VALUE1, " +
					"s.VALUE2, s.VALUE3, s.VALUE4, s.DESCR, m.CODE " +
					"FROM Specimens AS s " +
					"INNER JOIN MasterSpecimens AS m ON m.MSID = s.MSID " +
					"WHERE s.CASEID = ? ORDER BY s.SPECID");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT f.PERID, f.NOSPECS, " +
					"f.NOBLOCKS, f.NOSLIDES, f.VALUE1, f.VALUE2, " +
					"f.VALUE3, f.VALUE4, c.ACCESSED, p.INITIALS " +
					"FROM Frozens AS f " +
					"INNER JOIN Cases AS c ON c.CASEID = f.CASEID " +
					"INNER JOIN Personnel AS p ON p.PERID = f.PERID " +
					"WHERE f.CASEID = ?");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT a.PERID, a.CODEID, a.VALUE1, " +
					"a.VALUE2, a.VALUE3, a.VALUE4, a.FINALED, p.INITIALS " +
					"FROM Additional AS a " +
					"INNER JOIN Personnel AS p ON p.PERID = a.PERID " +
					"WHERE a.CASEID = ? " +
					"ORDER BY a.FINALED");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Frozens " +
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
			PreparedStatement stm = connection.prepareStatement("SELECT COMMENT FROM Errors WHERE CASEID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareFrozens() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT s.SPECID, " +
					"s.MSID, s.NOBLOCKS, s.NOSLIDES, s.VALUE1, " +
					"s.VALUE2, s.VALUE3, s.VALUE4, s.DESCR, m.CODE " +
					"FROM Specimens AS s " +
					"INNER JOIN MasterSpecimens AS m ON m.MSID = s.MSID " +
					"WHERE s.CASEID = ? ORDER BY s.SPECID");
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
	
	void prepareSummary() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT c.FACID, c.SPYID, c.SUBID, c.FINALID, " +
					"f.CODE AS FACI, p.SPYNAME, b.SUBINIT, e.INITIALS, " +
					"COUNT(c.CASEID) AS NOCASES, SUM(c.NOSLIDES) AS NOSLIDES, " +
					"SUM(c.VALUE1) AS VALUE1, SUM(c.VALUE2) AS VALUE2, " + 
					"SUM(c.VALUE3) AS VALUE3, SUM(c.VALUE4) AS VALUE4 " +
					"FROM Cases AS c " +
					"INNER JOIN Facilities AS f ON f.FACID = c.FACID " +
					"INNER JOIN Specialties AS p ON p.SPYID = c.SPYID " +
					"INNER JOIN Subspecial AS b ON b.SUBID = c.SUBID " +
					"INNER JOIN Personnel AS e ON e.PERID = c.FINALID " +
					"WHERE c.FINALED BETWEEN ? AND ? " +
					"GROUP BY c.FACID, c.SPYID, c.SUBID, c.FINALID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.INITIALS " +
					"ORDER BY c.FACID, c.SPYID, c.SUBID, c.FINALID");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT c.FACID, c.SPYID, c.SUBID, r.PERID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.INITIALS, " +
					"COUNT(r.CASEID) AS NOCASES, SUM(r.NOSLIDES) AS NOSLIDES, " +
					"SUM(r.VALUE1) AS VALUE1, SUM(r.VALUE2) AS VALUE2, " + 
					"SUM(r.VALUE3) AS VALUE3, SUM(r.VALUE4) AS VALUE4 " +
					"FROM Cases AS c " +
					"INNER JOIN Frozens AS r ON r.CASEID = c.CASEID " +
					"INNER JOIN Facilities AS f ON f.FACID = c.FACID " +
					"INNER JOIN Specialties AS p ON p.SPYID = c.SPYID " +
					"INNER JOIN Subspecial AS b ON b.SUBID = c.SUBID " +
					"INNER JOIN Personnel AS e ON e.PERID = r.PERID " +
					"WHERE c.ACCESSED BETWEEN ? AND ? " +
					"GROUP BY c.FACID, c.SPYID, c.SUBID, r.PERID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.INITIALS " +
					"ORDER BY c.FACID, c.SPYID, c.SUBID, r.PERID");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT c.FACID, c.SPYID, c.SUBID, a.PERID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.INITIALS, " +
					"COUNT(a.CASEID) AS NOCASES, " +
					"SUM(a.VALUE1) AS VALUE1, SUM(a.VALUE2) AS VALUE2, " + 
					"SUM(a.VALUE3) AS VALUE3, SUM(a.VALUE4) AS VALUE4 " +
					"FROM Cases AS c " +
					"INNER JOIN Additional AS a ON a.CASEID = c.CASEID " +
					"INNER JOIN Facilities AS f ON f.FACID = c.FACID " +
					"INNER JOIN Specialties AS p ON p.SPYID = c.SPYID " +
					"INNER JOIN Subspecial AS b ON b.SUBID = c.SUBID " +
					"INNER JOIN Personnel AS e ON e.PERID = a.PERID " +
					"WHERE a.FINALED BETWEEN ? AND ? " +
					"GROUP BY c.FACID, c.SPYID, c.SUBID, a.PERID, " +
					"f.CODE, p.SPYNAME, b.SUBINIT, e.INITIALS " +
					"ORDER BY c.FACID, c.SPYID, c.SUBID, a.PERID");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	/** Opens the Derby connection. */
	private void setConnection() {
		try {
			// Define physical location of Derby Database
			Properties p = System.getProperties();
			p.setProperty("derby.system.home", parent.variables.appDir + "db" +
					Constants.FILE_SEPARATOR);
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection("jdbc:derby:PJWL;create=false;");
			connected = true;
			stm = connection.createStatement();
		} catch (SQLException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} catch (ClassNotFoundException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
}
