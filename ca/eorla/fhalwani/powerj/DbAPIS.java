package ca.eorla.fhalwani.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.swing.JOptionPane;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

class DbAPIS extends DbMain {
	
	DbAPIS(PowerJ parent) {
		super(parent);
		strName = "PowerPath";
		if (!parent.variables.offLine) {
			setConnection();
		}
	}
	
	ResultSet getAccessions() {
		ResultSet rst = null;
		try {
			pstm = connection.prepareStatement("SELECT id, " +
					"acc_type_id, accession_no, created_date, facility_id " +
					"FROM accession_2 WITH (NOLOCK) " +
					"WHERE created_date > ? " +
					"AND imported_case = 'N' " +
					"ORDER BY created_date");
			pstm.setTimestamp(1, new Timestamp(parent.variables.lastUpdate));
			rst = pstm.executeQuery();
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return rst;
	}
	
	ResultSet getAccTypes(short lastID) {
		strSQL = "SELECT id, name FROM acc_type WHERE id > " +
				lastID + " ORDER BY id";
		return getResultSet();
	}
	
	ResultSet getCorrelations(long lastUpdate, long lastDebug) {
		ResultSet rst = null;
		try {
			pstm = connection.prepareStatement("SELECT c.correlation_date, " +
					"c.correlated_by_id, s.acc_id  " + 
					"FROM acc_correlation AS c WITH (NOLOCK) " +
					"INNER JOIN acc_specimen AS s WITH (NOLOCK) " +
					"ON s.id = c.correlated_specimen_id " +
					"WHERE c.correlation_date BETWEEN ? AND ? " +
					"AND c.correlation_status = 'C' " +
					"ORDER BY s.acc_id");
			pstm.setTimestamp(1, new Timestamp(lastUpdate));
			pstm.setTimestamp(2, new Timestamp(lastDebug));
			rst = pstm.executeQuery();
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return rst;
	}
	
	ResultSet getFacilities(short lastID) {
		strSQL = "SELECT id, code, name FROM facility WHERE id > " +
				lastID + " ORDER BY id";
		return getResultSet();
	}
	
	ResultSet getLogin(String loginName) {
		strSQL = "SELECT id FROM personnel_2 WHERE login_name = '" +
				loginName + "'";
		return getResultSet();
	}
	
	ResultSet getMasterOrders(short lastID) {
		strSQL = "SELECT id, code, description FROM lab_procedure WHERE id > " +
				lastID + " ORDER BY id";
		return getResultSet();
	}
	
	ResultSet getMasterSpecimens(short lastID) {
		strSQL = "SELECT id, code, description FROM tmplt_profile " +
				"WHERE id > " + lastID + " AND type = 'S' ORDER BY id";
		return getResultSet();
	}
	
	ResultSet getOrders(long lastUpdate, long lastDebug) {
		ResultSet rst = null;
		try {
			pstm = connection.prepareStatement("SELECT o.created_date, o.procedure_id, " + 
					"o.quantity, o.acc_id, o.ordered_by_id, p.code " +
					"FROM acc_order AS o WITH (NOLOCK) " +
					"INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id " +
					"WHERE o.created_date BETWEEN ? AND ? " +
					"ORDER BY o.acc_id, o.created_date");
			pstm.setTimestamp(1, new Timestamp(lastUpdate));
			pstm.setTimestamp(2, new Timestamp(lastDebug));
			rst = pstm.executeQuery();
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
		return rst;
	}
	
	ResultSet getPersons(short lastID) {
		strSQL = "SELECT id, persnl_class_id, last_name, first_name " +
				"FROM personnel_2 WHERE id > " + lastID + " ORDER BY id";
		return getResultSet();
	}
	
	void prepareAmendments() {
		try {
			closeStms();
			// Get Addenda/Amendment steps 0
			PreparedStatement stm = connection.prepareStatement("SELECT a.acc_id, " +
					"a.assigned_to_id, a.completed_date, s.description " +
					"FROM acc_process_step AS a WITH (NOLOCK) " +
					"INNER JOIN process_step AS s WITH (NOLOCK) " +
					"ON s.id = a.step_id " +
					"WHERE a.completed_date BETWEEN ? AND ? AND s.type = 'F' " +
					"ORDER BY a.acc_id, a.completed_date");
			stms.add(stm);
			// Get Orders 1
			stm = connection.prepareStatement("SELECT o.procedure_id, " +
					"o.quantity, o.created_date, p.code " +
					"FROM acc_order AS o WITH (NOLOCK) " +
					"INNER JOIN lab_procedure AS p WITH (NOLOCK) " +
					"ON p.id = o.procedure_id " +
					"WHERE o.acc_id = ? " +
					"AND o.created_date BETWEEN ? AND ? " +
					"ORDER BY o.procedure_id");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareCase() {
		try {
			closeStms();
			// Get a case by it's No
			PreparedStatement stm = connection.prepareStatement("SELECT id " +
					"FROM accession_2 WITH (NOLOCK) WHERE accession_no = ?");
			stms.add(stm);
			// Get specimens of a case
			stm = connection.prepareStatement("SELECT s.id, s.specimen_label, " +
					"s.tmplt_profile_id, s.description, s.collection_date, " +
					"s.recv_date, t.code, c.label_name " +
					"FROM acc_specimen AS s WITH (NOLOCK) " +
					"LEFT OUTER JOIN tmplt_profile AS t WITH (NOLOCK) ON t.id = s.tmplt_profile_id " +
					"LEFT OUTER JOIN specimen_category AS c WITH (NOLOCK) ON c.id = s.specimen_category_id " +
					"WHERE s.acc_id = ? " +
					"ORDER BY s.specimen_label");
			stms.add(stm);
			// Get events of a case
			stm = connection.prepareStatement("SELECT event_date, " +
					"source_rec_type, material_label, event_type, " +
					"event_location, event_description " + 
					"FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? " +
					"ORDER BY event_date");
			stms.add(stm);
			// Save altered template of a specimen
			stm = connection.prepareStatement("UPDATE acc_specimen SET tmplt_profile_id = ? WHERE id = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
		
	void prepareDash() {
			try {
				closeStms();
			// Get Blocks 0
			PreparedStatement stm = connection.prepareStatement("SELECT count(*) AS Blocks " +
					"FROM acc_block WITH (NOLOCK) WHERE acc_specimen_id = ?");
			stms.add(stm);
			// Get Embeded 1
			stm = connection.prepareStatement("SELECT event_date, personnel_id " +
					"FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? " +
					"AND source_rec_type = 'B' " +
					"AND amp_mode = 'embedding' " +
					"AND event_type = 'material_scan' " +
					"ORDER BY event_date");
			stms.add(stm);
			// Get Microtomed 2
			stm = connection.prepareStatement("SELECT event_date, personnel_id " +
					"FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? " +
					"AND source_rec_type = 'B' " +
					"AND amp_mode = 'histology' " +
					"AND event_type = 'material_scan' " +
					"ORDER BY event_date");
			stms.add(stm);
			// Get Orders 3
			stm = connection.prepareStatement("SELECT o.procedure_id, o.quantity " +
					"FROM acc_order AS o WITH (NOLOCK) " +
					"INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id " +
					"WHERE o.acc_id = ? AND o.status = 'C' AND p.preparation_catg = 'L'");
			stms.add(stm);
			// Get Processes 4
			stm = connection.prepareStatement("SELECT a.assigned_to_id, a.completed_date, s.description " +
					"FROM acc_process_step AS a WITH (NOLOCK) " +
					"INNER JOIN process_step AS s WITH (NOLOCK) ON s.id = a.step_id " +
					"WHERE a.acc_id = ? " +
					"ORDER BY s.sort_ord");
			stms.add(stm);
			// Get Routed 5
			stm = connection.prepareStatement("SELECT event_date, personnel_id " +
					"FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? " +
					"AND source_rec_type = 'L' " +
					"AND amp_mode = 'slide distribution' " +
					"AND event_type = 'material_routed' " +
					"ORDER BY event_date");
			stms.add(stm);
			// Get Specimens 6
			stm = connection.prepareStatement("SELECT s.id, s.tmplt_profile_id, s.description, c.label_name " +
					"FROM acc_specimen AS s WITH (NOLOCK) " +
					"INNER JOIN specimen_category AS c WITH (NOLOCK) " +
					"ON c.id = s.specimen_category_id " +
					"WHERE s.acc_id = ? " +
					"ORDER BY s.id");
			stms.add(stm);
			// Get Stained 7
			stm = connection.prepareStatement("SELECT event_date, personnel_id " +
					"FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? " +
					"AND source_rec_type = 'L' " +
					"AND amp_mode = 'slide distribution' " +
					"AND event_type = 'slide_completed' " +
					"ORDER BY event_date");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareErrors() {
		try {
			closeStms();
			// Update Row 0
			PreparedStatement stm = connection.prepareStatement("UPDATE acc_specimen " +
					"SET tmplt_profile_id = ? WHERE id = ?");
			stms.add(stm);
			// Select Specimens from PowerPath
			stm = connection.prepareStatement("SELECT s.id, " +
					"s.tmplt_profile_id, s.description, t.code " +
					"FROM acc_specimen AS s WITH (NOLOCK) " +
					"INNER JOIN tmplt_profile AS t WITH (NOLOCK) " +
					"ON t.id = s.tmplt_profile_id " +
					"WHERE s.acc_id = ? " +
					"ORDER BY s.specimen_label");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareSpecimens() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("UPDATE acc_specimen " +
					"SET tmplt_profile_id = ? WHERE id = ?");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareUnscanned() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT DISTINCT a.id, " +
					"a.accession_no, s.completed_date, pr.last_name, pr.first_name " +
					"FROM accession_2 AS a WITH (NOLOCK) " +
					"INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id " +
					"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id " +
					"INNER JOIN personnel_2 AS pr WITH (NOLOCK) ON pr.id = s.assigned_to_id " +
					"WHERE s.completed_date BETWEEN ? AND ? " +
					"AND p.description = 'Final' " +
					"AND a.accession_no NOT LIKE 'CN%' " +
					"AND a.accession_no NOT LIKE 'NSW%' " +
					"AND a.accession_no NOT LIKE 'OIM%' " +
					"AND a.id NOT IN (SELECT DISTINCT a.id FROM accession_2 AS a WITH (NOLOCK) " +
					"INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id " +
					"INNER JOIN acc_amp_event AS e WITH (NOLOCK) ON e.acc_id = a.id " +
					"WHERE e.event_date BETWEEN ? AND ? " +
					"AND e.personnel_id = s.assigned_to_id " +
					"AND e.event_type = 'material_scan') " +
					"ORDER BY pr.last_name, s.completed_date;");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareTracker() {
		try {
			closeStms();
			PreparedStatement stm = connection.prepareStatement("SELECT last_name, " +
					"first_name FROM personnel_2 WITH (NOLOCK) WHERE id = ?");
			stms.add(stm);
			// Events
			stm = connection.prepareStatement("SELECT e.event_date, e.source_rec_type, " +
					"e.material_label, e.event_location, " +
					"e.event_description, e.amp_mode, " +
					"e.event_type, a.accession_no " +
					"FROM acc_amp_event AS e WITH (NOLOCK) " +
					"INNER JOIN accession_2 AS a WITH (NOLOCK) " +
					"ON a.id = e.acc_id " +
					"WHERE (e.event_date BETWEEN ? AND ?)" +
					"AND e.personnel_id = ? "+
					"ORDER BY e.event_date");
			stms.add(stm);
			// Orders
			stm = connection.prepareStatement("SELECT o.created_date, p.code, a.accession_no " +
					"FROM acc_order AS o WITH (NOLOCK) " +
					"INNER JOIN lab_procedure AS p WITH (NOLOCK) ON p.id = o.procedure_id " +
					"INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = o.acc_id " +
					"WHERE (o.created_date BETWEEN ? AND ?)" +
					"AND o.ordered_by_id = ? "+
					"ORDER BY o.created_date");
			stms.add(stm);
			// Processes
			stm = connection.prepareStatement("SELECT aps.completed_date, ps.description, a.accession_no " +
					"FROM acc_process_step AS aps WITH (NOLOCK) " +
					"INNER JOIN process_step AS ps WITH (NOLOCK) ON ps.id = aps.step_id " +
					"INNER JOIN accession_2 AS a WITH (NOLOCK) ON a.id = aps.acc_id " +
					"WHERE (aps.completed_date BETWEEN ? AND ?)" +
					"AND aps.assigned_to_id = ? "+
					"ORDER BY aps.completed_date");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	void prepareWorkload() {
		try {
			closeStms();
			// Get Final Cases 0
			PreparedStatement  stm = connection.prepareStatement("SELECT a.id AS CaseID, " +
					"a.acc_type_id, a.facility_id FROM accession_2 AS a WITH (NOLOCK) " +
					"INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id " +
					"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id " +
					"WHERE s.completed_date BETWEEN ? AND ? AND p.description = 'Final' " +
					"ORDER BY s.completed_date");
			stms.add(stm);
			// Get a case by No 1
			stm = connection.prepareStatement("SELECT id " +
					"FROM accession_2 WITH (NOLOCK) WHERE accession_no = ?");
			stms.add(stm);
			// Get a case by ID 2
			stm = connection.prepareStatement("SELECT a.accession_no, " +
					"a.created_date, a.facility_id, a.acc_type_id, s.completed_date, " +
					"s.assigned_to_id FROM accession_2 AS a WITH (NOLOCK) " +
					"INNER JOIN acc_process_step AS s WITH (NOLOCK) ON s.acc_id = a.id " +
					"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id " +
					"WHERE a.id = ? AND p.description = 'Final'");
			stms.add(stm);
			// Get diagnosis text 3
			stm = connection.prepareStatement("SELECT r.finding, " +
					"r.finding_text FROM acc_results AS r WITH (NOLOCK) " +
					"INNER JOIN path_rpt_heading AS h WITH (NOLOCK) ON h.id = r.heading_id " +
					"WHERE r.acc_id = ?");
			stms.add(stm);
			// Get Gross Description 4
			stm = connection.prepareStatement("SELECT r.finding, " +
					"r.finding_text FROM acc_results AS r WITH (NOLOCK) " +
					"INNER JOIN path_rpt_heading AS h WITH (NOLOCK) ON h.id = r.heading_id " +
					"WHERE r.acc_id = ? AND h.name LIKE 'gross%'");
			stms.add(stm);
			// Get Gross Time 5
			stm = connection.prepareStatement("SELECT s.completed_date, s.assigned_to_id " +
					"FROM acc_process_step AS s WITH (NOLOCK) " +
					"INNER JOIN process_step AS p WITH (NOLOCK) ON p.id = s.step_id " +
					"WHERE s.acc_id = ? AND " +
					"(p.description = 'Gross Dictation' " +
					"OR p.description = 'Cytotechnologist Gyne Screening' " +
					"OR p.description = 'Cytotechnologist Non-Gyne Screening' " +
					"OR p.description = 'Provisional Diagnosis Final')");
			stms.add(stm);
			// Get Orders 6
			stm = connection.prepareStatement("SELECT o.procedure_id, " +
					"o.quantity, o.created_date, p.code " +
					"FROM acc_order AS o WITH (NOLOCK) " +
					"INNER JOIN lab_procedure AS p WITH (NOLOCK) " +
					"ON p.id = o.procedure_id " +
					"WHERE o.acc_specimen_id = ? " +
					"AND o.created_date < ? " +
					"ORDER BY o.created_date");
			stms.add(stm);
			// Get Route Time 7
			stm = connection.prepareStatement("SELECT event_date, personnel_id " +
					"FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? AND source_rec_type = 'L' " +
					"AND amp_mode = 'slide distribution' " +
					"AND event_type = 'material_routed' " +
					"ORDER BY event_date");
			stms.add(stm);
			// Get specimens 8
			// left outer join, or no specimens in autopsies
			stm = connection.prepareStatement("SELECT s.id AS SpecID, " +
					"s.tmplt_profile_id, s.description, t.code, c.label_name " +
					"FROM acc_specimen AS s WITH (NOLOCK) " +
					"INNER JOIN tmplt_profile AS t WITH (NOLOCK) ON t.id = s.tmplt_profile_id " +
					"LEFT OUTER JOIN specimen_category AS c WITH (NOLOCK) ON c.id = s.specimen_category_id " +
					"WHERE s.acc_id = ? ORDER BY s.id");
			stms.add(stm);
			// Get Synoptics 9
			stm = connection.prepareStatement("SELECT count(*) AS NoSynoptics " +
					"FROM acc_worksheet WITH (NOLOCK) WHERE acc_id = ?");
			stms.add(stm);
			// Get Scan Time 10
			stm = connection.prepareStatement("SELECT event_date FROM acc_amp_event WITH (NOLOCK) " +
					"WHERE acc_id = ? AND event_type = 'material_scan' " +
					"AND source_rec_type = 'L' AND amp_mode = 'Diagnostician' " +
					"ORDER BY event_date");
			stms.add(stm);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
	
	private void setConnection() {
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(parent.variables.apPort);
			ds.setServerName(parent.variables.apAddress);
			ds.setDatabaseName(parent.variables.apDatabase);
			ds.setUser(parent.variables.apLogin);
			ds.setPassword(parent.variables.apPassword);
			connection = ds.getConnection();
			connected = true;
		} catch (ClassNotFoundException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} catch (SQLServerException e) {
			connected = false;
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		}
	}
}
