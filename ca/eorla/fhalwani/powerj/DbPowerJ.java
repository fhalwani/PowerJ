package ca.eorla.fhalwani.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.swing.JOptionPane;

class DbPowerJ extends DbMain {
	
	DbPowerJ(PowerJ pj) {
		super(pj);
	}

	void close() {
		super.close();
	}
	
	int deleteCompleted(long timeInMillis) {
		strSQL = "DELETE FROM Pending " +
				"WHERE STATUS = " + Constants.STATUS_Final +
				" AND FINALED < '" + new Timestamp(timeInMillis) + "'";
		return execute();
	}
	
	int deleteDashboard(long caseID) {
		strSQL = "DELETE FROM Pending WHERE CASEID = " + caseID;
		return execute();
	}
	
	int deleteError(long caseID) {
		strSQL = "DELETE FROM Errors WHERE CASEID = " + caseID;
		return execute();
	}
	
	ResultSet getAccessions() {
		strSQL = "SELECT a.ACCID, a.SPYID, a.DASH, a.WLOAD, s.DASH AS SDASH, " +
				"s.WLOAD AS SWLOAD, a.ACCNAME, s.CODESPEC, s.SPYNAME " +
				"FROM Accessions AS a " +
				"INNER JOIN Specialties AS s ON s.SPYID = a.SPYID " +
				"ORDER BY a.ACCID";
		return getResultSet();
	}
	
	ResultSet getAccessLevel() {
		strSQL = "SELECT ACCESS FROM Personnel WHERE PERID = " + parent.variables.userID;
		return getResultSet();
	}
	
	ResultSet getAccTypes() {
		return null;
	}
	
	ResultSet getAdditionals(int[] filters) {
		return null;
	}

	ResultSet getCoder(byte coderID) {
		strSQL = "SELECT ID, RULEID, COUNT, VALUE1, VALUE2, VALUE3, NAME, DESCR " +
				"FROM ";
		switch (coderID) {
		case 1:
			strSQL += "Coder1";
			break;
		case 2:
			strSQL += "Coder2";
			break;
		case 3:
			strSQL += "Coder3";
			break;
		default:
			strSQL += "Coder4";
		}
		strSQL += " ORDER BY ID";
		return getResultSet();
	}

	ResultSet getContracts() {
		strSQL = "SELECT ID, COTYPE, " +
				"CONO1, CONO2, CONO3, CONO4, CONO5, DESCR " +
				"FROM Contracts ORDER BY COTYPE";
		return getResultSet();
	}

	ResultSet getDashboard() {
		return null;
	}
	
	ResultSet getErrors(int errID) {
		return null;
	}
	
	ResultSet getFacilities() {
		strSQL = "SELECT FACID, DASH, WLOAD, CODE, NAME FROM Facilities ORDER BY FACID";
		return getResultSet();
	}

	ResultSet getFinal(String caseNo) {
		strSQL = "SELECT CASEID FROM Cases WHERE CASENO = '" + caseNo + "'";
		return getResultSet();
	}
	
	ResultSet getFinals(int[] filters) {
		return null;
	}
	
	ResultSet getFrozens(int[] filters) {
		return null;
	}
	
	ResultSet getGroups(int order) {
		return null;
	}
	
	ResultSet getHistology() {
		strSQL = "SELECT FACID, SUBID, PROID, STATUS, NOBLOCKS, " +
				"NOSLIDES, GROSSED, EMBEDED, MICROED, STAINED, ROUTED " +
				"FROM Pending WHERE STATUS > 0";
		return getResultSet();
	}
	
	ResultSet getLastDash() {
		return null;
	}
	
	ResultSet getLastScheduler() {
		strSQL = "SELECT MAX(WODATE) AS workday FROM Workdays";
		return getResultSet();
	}
	
	ResultSet getLastSchedulerID() {
		strSQL = "SELECT MAX(ID) AS id FROM Workdays";
		return getResultSet();
	}
	
	ResultSet getLastStats() {
		return null;
	}
	
	ResultSet getLastWorkload() {
		return null;
	}
	
	ResultSet getMasterOrders() {
		return null;
	}
	
	ResultSet getMasterSpecimens(int order) {
		return null;
	}
	
	ResultSet getPathologists() {
		return null;
	}
	
	ResultSet getPending() {
		return null;
	}

	ResultSet getPersonnel(int order) {
		strSQL = "SELECT PERID, ACCESS, CODE, INITIALS, PLAST, PFIRST FROM Personnel ";
		if (order == 0) {
			strSQL += "ORDER BY PERID";
		} else {
			strSQL += "ORDER BY PLAST, PFIRST";
		}
		return getResultSet();
	}

	ResultSet getReasons() {
		strSQL = "SELECT ID, INITIALS, DESCR FROM Reasons ORDER BY DESCR";
		return getResultSet();
	}

	ResultSet getRules() {
		strSQL = "SELECT ID, NAME, DESCR FROM Rules ORDER BY ID";
		return getResultSet();
	}

	ResultSet getSetup() {
		strSQL = "SELECT STPID, STPVAL FROM Setup ORDER BY STPID";
		return getResultSet();
	}

	ResultSet getShifts() {
		strSQL = "SELECT ID, SKID, LNKID, FTE, PRIORITY, LNKTYPE, NOPOS, ACTIVE, " + 
				"ALLWEEK, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SPLIT, " + 
				"SEPARATE, INITIALS, DESCR " + 
				"FROM Shifts ORDER BY DESCR";
		return getResultSet();
	}

	ResultSet getSkills() {
		strSQL = "SELECT ID, INITIALS, DESCR " +
				"FROM Skills ORDER BY DESCR";
		return getResultSet();
	}

	ResultSet getSpecialties(int order) {
		strSQL = "SELECT SPYID, DASH, WLOAD, CODESPEC, SPYNAME FROM Specialties ";
		if (order == 0) {
			strSQL += "ORDER BY SPYID";
		} else {
			strSQL += "ORDER BY SPYNAME";
		}
		return getResultSet();
	}
	
	ResultSet getSpecimens(int[] filters) {
		return null;
	}

	ResultSet getStaff() {
		strSQL = "SELECT p.PERID, p.INITIALS, p.PLAST, p.PFIRST, " +
				"s.CONID, s.FTE, s.ACTIVE, s.STARTD, s.ENDDATE, c.DESCR " +
				"FROM Personnel AS p " +
				"LEFT OUTER JOIN Staff AS s ON s.PERID = p.PERID " +
				"LEFT OUTER JOIN Contracts AS c ON c.ID = s.CONID " +
				"WHERE p.CODE = 'PT' ORDER BY p.PLAST, p.PFIRST";
		return getResultSet();
	}

	ResultSet getSubspecialties(int order) {
		return null;
	}
	
	PreparedStatement getStatement(String sql) {
		PreparedStatement stm = null;
		try {
			stm = connection.prepareStatement(sql);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return stm;
	}

	ResultSet getStats(int[] filters, long timeFrom, long timeTo) {
		return null;
	}

	ResultSet getStatsSum(long timeFrom, long timeTo) {
		return null;
	}

	ResultSet getTATSum(long timeFrom, long timeTo) {
		return null;
	}

	ResultSet getWLDetails(int[] rows, int[] values, long timeFrom, long timeTo) {
		return null;
	}

	void prepareDaysOff() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT ID, WODATE " +
					"FROM Workdays WHERE WODATE >= ? AND WODATE <= ?");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT d.ID AS ID, f.WODATE AS FROMD, " +
					"t.WODATE AS TOD, r.DESCR AS DESCR " + 
					"FROM PowerJ.DaysOff AS d " + 
					"INNER JOIN PowerJ.Workdays AS f ON f.ID = d.FROMID " +
					"INNER JOIN PowerJ.Workdays AS t ON t.ID = d.TOID " +
					"INNER JOIN PowerJ.Reasons AS r ON r.ID = d.REID " +
					"WHERE d.PERID = ? AND d.STATUS <> ? " +
					"AND ((f.WODATE >= ? AND f.WODATE <= ?) " +
					"OR (f.WODATE < ? AND t.WODATE >= ?))");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareAccessions() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Accessions (SPYID, DASH, WLOAD, " +
					"ACCNAME, ACCID) VALUES (?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Accessions SET SPYID = ?, DASH = ?, " +
					"WLOAD = ?, ACCNAME = ? WHERE ACCID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareAdditionals() {
	}

	void prepareAuditor() {
	}

	void prepareCase() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT CASENO " +
					"FROM Cases WHERE CASEID = ?");
			stms.add(stm);
			stm = connection.prepareStatement("INSERT INTO Errors " +
					"(CASEID, ERRID, CASENO, COMMENT) VALUES (?, ?, ?, ?)");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareCoder(byte coderID) {
		String insert = "INSERT INTO ";
		String update = "UPDATE ";
		try {
			closeStms();
			switch (coderID) {
			case 1:
				insert += "Coder1";
				update += "Coder1";
				break;
			case 2:
				insert += "Coder2";
				update += "Coder2";
				break;
			case 3:
				insert += "Coder3";
				update += "Coder3";
				break;
			default:
				insert += "Coder4";
				update += "Coder4";
			}
			insert += " (RULEID, COUNT, VALUE1, VALUE2, VALUE3, NAME, DESCR, ID) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			update += " SET RULEID = ?, COUNT = ?, VALUE1 = ?, VALUE2 = ?, VALUE3 = ?, " +
						"NAME = ?, DESCR = ? WHERE ID = ?";
			PreparedStatement stm = connection.prepareStatement(insert);
			stms.add(stm);
			stm = connection.prepareStatement(update);
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareContract() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Contracts (COTYPE, " +
					"CONO1, CONO2, CONO3, CONO4, CONO5, DESCR, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Contracts SET COTYPE = ?, " +
					"CONO1 = ?, CONO2 = ?, CONO3 = ?, CONO4 = ?, CONO5 = ?, DESCR = ? WHERE ID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareDash() {
		try {
			closeStms();
			// Insert Row 0
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Pending (FACID, MSID, GROSSID, " +
				"MICROID, EMBEDID, STAINID, ROUTEID, FINALID, SPYID, " +
				"SUBID, PROID, STATUS, NOSPECS, NOBLOCKS, NOSLIDES, " +
				"ACCESSED, CASENO, CASEID) VALUES (?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			// Update Embed 1
			stm = connection.prepareStatement("UPDATE Pending SET MSID = ?, EMBEDID = ?, " +
				"SPYID = ?, SUBID = ?, PROID = ?, STATUS = ?, " +
				"NOSPECS = ?, NOBLOCKS = ?, EMBEDTAT = ?, EMBEDED = ? " +
				"WHERE CASEID = ?");
			stms.add(stm);
			// Update Final 2
			stm = connection.prepareStatement("UPDATE Pending SET STATUS = ?, FINALID = ?, FINALTAT = ?, " +
				"FINALED = ?, NOSLIDES = ? WHERE CASEID = ?");
			stms.add(stm);
			// Update Gross 3
			stm = connection.prepareStatement("UPDATE Pending SET MSID = ?, GROSSID = ?, " +
				"SPYID = ?, SUBID = ?, PROID = ?, STATUS = ?, " +
				"NOSPECS = ?, NOBLOCKS = ?, GROSSTAT = ?, GROSSED = ? " +
				"WHERE CASEID = ?");
			stms.add(stm);
			// Update Microtome 4
			stm = connection.prepareStatement("UPDATE Pending SET STATUS = ?, MICROID = ?, MICROTAT = ?, " +
				"MICROED = ?, NOBLOCKS = ? WHERE CASEID = ?");
			stms.add(stm);
			// Update Route 5
			stm = connection.prepareStatement("UPDATE Pending SET STATUS = ?, ROUTEID = ?, ROUTETAT = ?, " +
				"ROUTED = ?, NOSLIDES = ? WHERE CASEID = ?");
			stms.add(stm);
			// Update Staining 6
			stm = connection.prepareStatement("UPDATE Pending SET STATUS = ?, STAINID = ?, STAINTAT = ?, " +
				"STAINED = ?, NOSLIDES = ? WHERE CASEID = ?");
			stms.add(stm);
			// Update Assigned 7
			stm = connection.prepareStatement("UPDATE Pending SET FINALID = ? WHERE CASEID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareErrors() {
	}
	
	void prepareFacilities() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Facilities (DASH, WLOAD, " +
					"CODE, NAME, FACID) VALUES (?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Facilities SET DASH = ?, WLOAD = ?, " +
					"CODE = ?, NAME = ? WHERE FACID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareFrozens() {
	}
	
	void prepareGroups() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Groups (GRP, CODE1, " +
					"CODE2, CODE3, CODE4, NAME, ID) VALUES (?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE GROUPS SET GRP = ?, CODE1 = ?, " + 
					"CODE2 = ?, CODE3 = ?, CODE4 = ?, NAME = ? WHERE ID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareOrders() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO MasterOrders " +
					"(GRPID, CODE, DESCR, ID) VALUES (?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE MasterOrders " +
					"SET GRPID = ?, CODE = ?, DESCR = ? WHERE ID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void preparePersonDash() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("UPDATE Personnel SET INITIALS = ? " +
					"WHERE PERID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void preparePersonnel() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Personnel (ACCESS, CODE, " +
					"INITIALS, PLAST, PFIRST, PERID) VALUES (?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Personnel SET ACCESS = ?, CODE = ?, " +
					"INITIALS = ?, PLAST = ?, PFIRST = ? WHERE PERID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareRules() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement(
					"UPDATE Rules SET NAME = ?, DESCR = ? WHERE ID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareScheduler() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Workdays (ID, WODATE) VALUES (?, ?)");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareSetup() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement(
					"UPDATE Setup SET STPVAL = ? WHERE STPID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareShifts() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Shifts " +
					"(SKID, LNKID, FTE, PRIORITY, LNKTYPE, NOPOS, ACTIVE, ALLWEEK, " +
					"MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SPLIT, SEPARATE, " +
					"INITIALS, DESCR, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Shifts SET " +
					"SKID = ?, LNKID = ?, FTE = ?, PRIORITY = ?, LNKTYPE = ?, NOPOS = ?, " +
					"ACTIVE = ?, ALLWEEK = ?, MONDAY = ?, TUESDAY = ?, WEDNESDAY = ?, " +
					"THURSDAY = ?, FRIDAY = ?, SPLIT = ?, SEPARATE = ?, INITIALS = ?, " +
					"DESCR = ? WHERE ID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareSkills() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Skills (INITIALS, " +
					"DESCR, ID) VALUES (?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Skills SET INITIALS = ?, " +
					"DESCR = ? WHERE ID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareSpecialties() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Specialties (DASH, " +
					"WLOAD, CODESPEC, SPYNAME, SPYID) VALUES (?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Specialties SET DASH = ?, " +
					"WLOAD = ?, CODESPEC = ?, SPYNAME = ? WHERE SPYID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareSpecimens() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Errors " +
					"(CASEID, ERRID, CASENO, COMMENT) VALUES (?, ?, ?, ?)");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareStaff() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Staff" + 
					"(CONID, FTE, ACTIVE, STARTD, ENDDATE, PERID) " + 
					"VALUES(?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Staff SET " + 
					"CONID = ?, FTE = ?, ACTIVE = ?, STARTD = ?, ENDDATE = ? " + 
					"WHERE PERID = ?");
			stms.add(stm);
			stm = connection.prepareStatement("SELECT s.SKID, s.PRCNT, k.DESCR " +
					"FROM StaffSkills AS s " +
					"INNER JOIN Skills AS k ON k.ID = s.SKID " +
					"WHERE s.PERID = ? " +
					"ORDER BY k.DESCR");
			stms.add(stm);
			stm = connection.prepareStatement("INSERT INTO StaffSkills" + 
					"(SKID, PRCNT, PERID) VALUES(?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE StaffSkills SET " + 
					"SKID = ?, PRCNT = ? WHERE PERID = ? AND SKID = ?");
			stms.add(stm);
			stm = connection.prepareStatement("DELETE FROM StaffSkills " +
					"WHERE PERID = ? AND SKID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareStats() {
		try {
			closeStms();
			// Insert Row
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Stats " +
					"(FACID, GROSSID, FINALID, SPYID, SUBID, PROID, NOSPECS, " + 
					"NOBLOCKS, NOSLIDES, NOHE, NOSS, NOIHC, NOMOL, NOFSP, NOFBL, " + 
					"NOFSL, NOSYN, GRTAT, ROTAT, FITAT, TOTAT, MSID, ACCESSED, " + 
					"GROSSED, ROUTED, FINALED, CASENO, CASEID) VALUES (?, ?, ?, " +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?)");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareSubspecialties() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Subspecial " +
					"(SPYID, SUBINIT, SUBNAME, SUBID) VALUES (?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE Subspecial " +
					"SET SPYID = ?, SUBINIT = ?, SUBNAME = ? WHERE SUBID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareSummary() {
	}
	
	void prepareTemplateDash() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("UPDATE MasterSpecimens " +
					"SET GROSS = ?, EMBED = ?, MICROTOMY = ?, ROUTE = ?, SIGNOUT = ? " +
					"WHERE MSID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}

	void prepareTemplates() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("INSERT INTO MasterSpecimens " +
					"(SPYID, SUBID, PROCID, ISLN, GROSS, EMBED, MICROTOMY, " +
					"ROUTE, SIGNOUT, CODE1B, CODE1M, CODE1R, CODE2B, CODE2M, " +
					"CODE2R, CODE3B, CODE3M, CODE3R, CODE4B, CODE4M, CODE4R, " +
					"CODE, DESCR, MSID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "+ 
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			stm = connection.prepareStatement("UPDATE MasterSpecimens SET " +
					"SPYID = ?, SUBID = ?, PROCID = ?, ISLN = ?, GROSS = ?, " +
					"EMBED = ?, MICROTOMY = ?, ROUTE = ?, SIGNOUT = ?, CODE1B = ?, " +
					"CODE1M = ?, CODE1R = ?, CODE2B = ?, CODE2M = ?, CODE2R = ?, " +
					"CODE3B = ?, CODE3M = ?, CODE3R = ?, CODE4B = ?, CODE4M = ?, " +
					"CODE4R = ?, CODE = ?, DESCR = ? WHERE MSID = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	void prepareWorkload() {
		try {
			closeStms();
			// Insert Case 0
			PreparedStatement stm = connection.prepareStatement("INSERT INTO Cases " +
					"(FACID, MSID, GROSSID, FINALID, SPYID, SUBID, " +
					"PROID, NOSPECS, NOBLOCKS, NOSLIDES, NOSYNOPT, " +
					"NOFS, GROSSTAT, ROUTETAT, FINALTAT, TOTALTAT, " +
					"ACCESSED, GROSSED, ROUTED, FINALED, VALUE1, VALUE2, " +
					"VALUE3, VALUE4, CASENO, CASEID) VALUES (?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			// Update Case 1
			stm = connection.prepareStatement("UPDATE Cases SET " +
					"FACID = ?, MSID = ?, GROSSID = ?, FINALID = ?, " +
					"SPYID = ?, SUBID = ?, PROID = ?, NOSPECS = ?, NOBLOCKS = ?, " +
					"NOSLIDES = ?, NOSYNOPT = ?, NOFS = ?, GROSSTAT = ?, " +
					"ROUTETAT = ?, FINALTAT = ?, TOTALTAT = ?, ACCESSED = ?, " +
					"GROSSED = ?, ROUTED = ?, FINALED = ?, VALUE1 = ?, " +
					"VALUE2 = ?, VALUE3 = ?, VALUE4 = ?, CASENO = ? WHERE CASEID = ?");
			stms.add(stm);
			// Insert Comment 2
			stm = connection.prepareStatement("INSERT INTO Comments " +
					"(COMMENT, CASEID) VALUES (?, ?)");
			stms.add(stm);
			// Update comment 3
			stm = connection.prepareStatement("UPDATE Comments " +
					"SET COMMENT = ? WHERE CASEID = ?");
			stms.add(stm);
			// Insert FS 4
			stm = connection.prepareStatement("INSERT INTO Frozens " +
					"(PERID, NOSPECS, NOBLOCKS, NOSLIDES, VALUE1, VALUE2, " +
					"VALUE3, VALUE4, CASEID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			// Update FS 5
			stm = connection.prepareStatement("UPDATE Frozens " +
					"SET PERID = ?, NOSPECS = ?, NOBLOCKS = ?, " +
					"NOSLIDES = ?, VALUE1 = ?, VALUE2 = ?, VALUE3 = ?, " +
					"VALUE4 = ? WHERE CASEID = ?");
			stms.add(stm);
			// Insert Specimen 6
			stm = connection.prepareStatement("INSERT INTO Specimens " +
					"(CASEID, MSID, NOBLOCKS, NOSLIDES, NOFRAGS, VALUE1, VALUE2, " +
					"VALUE3, VALUE4, DESCR, SPECID) VALUES (?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			// Update Specimen 7
			stm = connection.prepareStatement("UPDATE Specimens " +
					"SET CASEID = ?, MSID = ?, NOBLOCKS = ?, NOSLIDES = ?, " +
					"NOFRAGS = ?, VALUE1 = ?, VALUE2 = ?, VALUE3 = ?, VALUE4 = ?, " +
					"DESCR = ? WHERE SPECID = ?");
			stms.add(stm);
			// Insert Order 8
			stm = connection.prepareStatement("INSERT INTO Orders " +
					"(QTY, VALUE1, VALUE2, VALUE3, VALUE4, GRPID, SPECID) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)");
			stms.add(stm);
			// Update Order 9
			stm = connection.prepareStatement("UPDATE Orders " +
					"SET QTY = ?, VALUE1 = ?, VALUE2 = ?, VALUE3 = ?, " +
					"VALUE4 = ? WHERE GRPID = ? AND SPECID = ?");
			stms.add(stm);
			// Insert Error 10
			stm = connection.prepareStatement("INSERT INTO Errors " +
					"(CASEID, ERRID, CASENO, COMMENT) VALUES (?, ?, ?, ?)");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
}
