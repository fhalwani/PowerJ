package ca.eorla.fhalwani.powerj;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class ExportManager {
	boolean success = false;
	private int noRows = 0;
	private final String strTab = "\t";
	private String dataDir = "";
	protected PowerJ parent;

	ExportManager(PowerJ parent) {
		this.parent = parent;
	}

	void exportDash() {
		String strFileName = "";
		String output = "";
		ResultSet rst = null;
		File file = null;
		FileOutputStream fos = null;
		try {
			parent.dbPowerJ.strSQL = "SELECT * FROM Pending ORDER BY CASEID";
			rst = parent.dbPowerJ.getResultSet();
			strFileName = dataDir + "dashboard.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "CASEID" + strTab + "FACID" + strTab + "MSID" +
						strTab + "GROSSTAT" + strTab + "EMBEDTAT" + strTab + "MICROTAT" +
						strTab + "STAINTAT" + strTab + "ROUTETAT" + strTab + "HISTOTAT" +
						strTab + "FINALTAT" + strTab + "GROSSID" + strTab + "EMBEDID" +
						strTab + "MICROID" + strTab + "STAINID" + strTab + "ROUTEID" +
						strTab + "FINALID" + strTab + "SPYID" + strTab + "SUBID" +
						strTab + "PROID" + strTab + "STATUS" + strTab + "NOSPECS" +
						strTab + "NOBLOCKS" + strTab + "NOSLIDES" + strTab + "ACCESSED" +
						strTab + "GROSSED" + strTab + "EMBEDED" + strTab + "MICROED" +
						strTab + "STAINED" + strTab + "ROUTED" + strTab + "FINALED" +
						strTab + "CASENO" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("CASEID") + strTab + rst.getInt("FACID") + strTab + rst.getInt("MSID") +
							strTab + rst.getInt("GROSSTAT") + strTab + rst.getInt("EMBEDTAT") + strTab + rst.getInt("MICROTAT") +
							strTab + rst.getInt("STAINTAT") + strTab + rst.getInt("ROUTETAT") + strTab + rst.getInt("HISTOTAT") +
							strTab + rst.getInt("FINALTAT") + strTab + rst.getInt("GROSSID") + strTab + rst.getInt("EMBEDID") +
							strTab + rst.getInt("MICROID") + strTab + rst.getInt("STAINID") + strTab + rst.getInt("ROUTEID") +
							strTab + rst.getInt("FINALID") + strTab + rst.getInt("SPYID") + strTab + rst.getInt("SUBID") +
							strTab + rst.getInt("PROID") + strTab + rst.getInt("STATUS") + strTab + rst.getInt("NOSPECS") +
							strTab + rst.getInt("NOBLOCKS") + strTab + rst.getInt("NOSLIDES") +
							strTab + rst.getTimestamp("ACCESSED").getTime() + strTab + rst.getTimestamp("GROSSED").getTime() +
							strTab + rst.getTimestamp("EMBEDED").getTime() + strTab + rst.getTimestamp("MICROED").getTime() +
							strTab + rst.getTimestamp("STAINED").getTime() + strTab + rst.getTimestamp("ROUTED").getTime() +
							strTab + rst.getTimestamp("FINALED").getTime() + strTab + rst.getString("CASENO") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 200 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
					}
				}
				fos.close();
				rst.close();
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			System.out.println("Exported " + noRows + " rows from Dashboard Table.");
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		}
	}

	void exportSetup() {
		String strFileName = "";
		String output = "";
		ResultSet rst = null;
		File file = null;
		FileOutputStream fos = null;
		try {
			System.out.println("Exporting Setup...");
			rst = parent.dbPowerJ.getSetup();
			strFileName = dataDir + "setup.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "STPID" + strTab + "STPVAL" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getByte("STPID") + strTab +
							rst.getString("STPVAL") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Setup Table.");
			}
			rst = parent.dbPowerJ.getRules();
			strFileName = dataDir + "rules.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "ID" + strTab + "NAME"  + strTab + "DESCR" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("ID") + strTab +
							rst.getString("NAME")  + strTab +
							rst.getString("DESCR") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Rules Table.");
			}
			for (byte i = 1; i < 5; i++) {
				rst = parent.dbPowerJ.getCoder(i);
				strFileName = dataDir + "coder" + i + ".txt";
				file = new File(strFileName);
				if (!file.exists())
					file.createNewFile();
				if (file.exists()) {
					noRows = 0;
					fos = new FileOutputStream(file);
					output = "ID" + strTab + "RULE" + strTab +
							"COUNT" + strTab + "VALUE1" + strTab +
							"VALUE2" + strTab + "VALUE3" + strTab +
							"NAME" + strTab + "DESCR" + Constants.NEW_LINE;
					fos.write(output.getBytes());
					while (rst.next()) {
						output = "" + rst.getShort("ID") + strTab +
								rst.getShort("RULEID") + strTab +
								rst.getShort("COUNT") + strTab +
								rst.getDouble("VALUE1") + strTab +
								rst.getDouble("VALUE2") + strTab +
								rst.getDouble("VALUE3") + strTab +
								rst.getString("NAME") + strTab +
								rst.getString("DESCR") + Constants.NEW_LINE;
						fos.write(output.getBytes());
						noRows++;
					}
					fos.close();
					rst.close();
					System.out.println("Exported " + noRows + " rows from " +
							parent.variables.codersName[i-1] + " Table.");
				}
			}
			rst = parent.dbPowerJ.getSpecialties(0);
			strFileName = dataDir + "specialties.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "SPYID" + strTab + "DASH" + strTab +
						"WLOAD" + strTab + "CODESPEC" + strTab +
						"SPYNAME" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("SPYID") + strTab +
							rst.getString("DASH") + strTab +
							rst.getString("WLOAD") + strTab +
							rst.getString("CODESPEC") + strTab +
							rst.getString("SPYNAME") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Specialties Table.");
			}
			rst = parent.dbPowerJ.getAccessions();
			strFileName = dataDir + "accessions.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "ACCID" + strTab + "SPYID" + strTab +
						"DASH" + strTab + "WLOAD" + strTab +
						"ACCNAME" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("ACCID") + strTab +
							rst.getShort("SPYID") + strTab +
							rst.getString("DASH") + strTab +
							rst.getString("WLOAD") + strTab +
							rst.getString("ACCNAME") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Accessions Table.");
			}
			rst = parent.dbPowerJ.getSubspecialties(0);
			strFileName = dataDir + "subspecialty.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "SUBID" + strTab + "SPYID" + strTab +
						"SUBINIT" + strTab + "SUBNAME" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("SUBID") + strTab +
							rst.getShort("SPYID") + strTab +
							rst.getString("SUBINIT") + strTab +
							rst.getString("SUBNAME") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Subspecialty Table.");
			}
			rst = parent.dbPowerJ.getFacilities();
			strFileName = dataDir + "facilities.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "FACID" + strTab + "DASH" + strTab +
						"WLOAD" + strTab + "CODE" + strTab +
						"NAME" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("FACID") + strTab +
							rst.getString("DASH") + strTab +
							rst.getString("WLOAD") + strTab +
							rst.getString("CODE") + strTab +
							rst.getString("NAME") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Facilities Table.");
			}
			rst = parent.dbPowerJ.getGroups(0);
			strFileName = dataDir + "groups.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "ID" + strTab + "GRP" + strTab + "CODE1" + strTab +
						"CODE2" + strTab + "CODE3" + strTab + "CODE4" +
						strTab + "NAME" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("ID") + strTab +
							rst.getShort("GRP") + strTab +
							rst.getShort("CODE1") + strTab +
							rst.getShort("CODE2") + strTab +
							rst.getShort("CODE3") + strTab +
							rst.getShort("CODE4") + strTab +
							rst.getString("NAME") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Groups Table.");
			}
			rst = parent.dbPowerJ.getPersonnel(0);
			strFileName = dataDir + "personnel.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "PERID" + strTab + "ACCESS" + strTab +
						"CODE" + strTab + "INITIALS" + strTab +
						"PLAST" + strTab + "PFIRST" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("PERID") + strTab +
							rst.getInt("ACCESS") + strTab +
							rst.getString("CODE") + strTab +
							rst.getString("INITIALS") + strTab +
							rst.getString("PLAST") + strTab +
							rst.getString("PFIRST") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Personnel Table.");
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			System.out.println("Exporting MasterSpecimens...");
			rst = parent.dbPowerJ.getMasterSpecimens(0);
			strFileName = dataDir + "masterspecimens.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "MSID" + strTab + "SPYID" + strTab +
						"SUBID" + strTab + "PROCID" + strTab +
						"ISLN" + strTab + "GROSS" + strTab +
						"EMBED" + strTab + "MICROTOMY" + strTab +
						"ROUTE" + strTab + "SIGNOUT" + strTab +
						"CODE1B" + strTab + "CODE1M" + strTab +
						"CODE1R" + strTab + "CODE2B" + strTab +
						"CODE2M" + strTab + "CODE2R" + strTab +
						"CODE3B" + strTab + "CODE3M" + strTab +
						"CODE3R" + strTab + "CODE4B" + strTab +
						"CODE4M" + strTab + "CODE4R" + strTab +
						"CODE" + strTab + "DESCR" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("MSID") + strTab +
							rst.getShort("SPYID") + strTab +
							rst.getShort("SUBID") + strTab +
							rst.getShort("PROCID") + strTab +
							rst.getShort("ISLN") + strTab +
							rst.getShort("GROSS") + strTab +
							rst.getShort("EMBED") + strTab +
							rst.getShort("MICROTOMY") + strTab +
							rst.getShort("ROUTE") + strTab +
							rst.getShort("SIGNOUT") + strTab +
							rst.getShort("CODE1B") + strTab +
							rst.getShort("CODE1M") + strTab +
							rst.getShort("CODE1R") + strTab +
							rst.getShort("CODE2B") + strTab +
							rst.getShort("CODE2M") + strTab +
							rst.getShort("CODE2R") + strTab +
							rst.getShort("CODE3B") + strTab +
							rst.getShort("CODE3M") + strTab +
							rst.getShort("CODE3R") + strTab +
							rst.getShort("CODE4B") + strTab +
							rst.getShort("CODE4M") + strTab +
							rst.getShort("CODE4R") + strTab +
							rst.getString("CODE") + strTab +
							rst.getString("DESCR") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " +
						noRows + " rows from masterspecimens Table.");
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			rst = parent.dbPowerJ.getMasterOrders();
			strFileName = dataDir + "masterorders.txt";
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "ID" + strTab + "GRPID" + strTab +
						"CODE" + strTab + "DESCR" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("ID") + strTab +
							rst.getShort("GRPID") + strTab +
							rst.getString("CODE") + strTab +
							rst.getString("DESCR") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from MasterOrders Table.");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		}
	}

	void exportStats() {
		int fileNo = 1;
		String strFileName = "";
		String output = "";
		ResultSet rst = null;
		File file = null;
		FileOutputStream fos = null;
		try {
			parent.dbPowerJ.strSQL = "SELECT * FROM Stats ORDER BY CASEID";
			rst = parent.dbPowerJ.getResultSet();
			strFileName = dataDir + "stats" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "CASEID" + strTab + "FACID" + strTab + "GROSSID" + strTab +
						"FINALID" + strTab + "SPYID" + strTab +	"SUBID" + strTab +
						"PROID" + strTab + "NOSPECS" + strTab + "NOBLOCKS" + strTab +
						"NOSLIDES" + strTab + "NOHE" + strTab + "NOSS" + strTab +
						"NOIHC" + strTab + "NOMOL" + strTab + "NOFSP" + strTab +
						"NOFBL" + strTab + "NOFSL" + strTab + "NOSYN" + strTab +
						"GRTAT" + strTab + "ROTAT" + strTab + "FITAT" + strTab +
						"TOTAT" + strTab + "MSID" + strTab + "ACCESSED" +strTab +
						"GROSSED" + strTab + "ROUTED" + strTab + "FINALED" + strTab +
						"CASENO" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("CASEID") + strTab + rst.getInt("FACID") + strTab +
							rst.getInt("GROSSID") + strTab + rst.getInt("FINALID") + strTab +
							rst.getInt("SPYID") + strTab + rst.getInt("SUBID") + strTab +
							rst.getInt("PROID") + strTab + rst.getInt("NOSPECS") + strTab +
							rst.getInt("NOBLOCKS") + strTab + rst.getInt("NOSLIDES") + strTab +
							rst.getInt("NOHE") + strTab + rst.getInt("NOSS") + strTab +
							rst.getInt("NOIHC") + strTab + rst.getInt("NOMOL") + strTab +
							rst.getInt("NOFSP") + strTab + rst.getInt("NOFBL") + strTab +
							rst.getInt("NOFSL") + strTab + rst.getInt("NOSYN") + strTab +
							rst.getInt("GRTAT") + strTab + rst.getInt("ROTAT") + strTab +
							rst.getInt("FITAT") + strTab + rst.getInt("TOTAT") + strTab +
							rst.getInt("MSID") + strTab +
							rst.getTimestamp("ACCESSED").getTime() + strTab +
							rst.getTimestamp("GROSSED").getTime() + strTab +
							rst.getTimestamp("ROUTED").getTime() + strTab +
							rst.getTimestamp("FINALED").getTime() + strTab +
							rst.getString("CASENO") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "stats" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "CASEID" + strTab + "FACID" + strTab + "GROSSID" + strTab +
										"FINALID" + strTab + "SPYID" + strTab +	"SUBID" + strTab +
										"PROID" + strTab + "NOSPECS" + strTab + "NOBLOCKS" + strTab +
										"NOSLIDES" + strTab + "NOHE" + strTab + "NOSS" + strTab +
										"NOIHC" + strTab + "NOMOL" + strTab + "NOFSP" + strTab +
										"NOFBL" + strTab + "NOFSL" + strTab + "NOSYN" + strTab +
										"GRTAT" + strTab + "ROTAT" + strTab + "FITAT" + strTab +
										"TOTAT" + strTab + "MSID" + strTab + "ACCESSED" +strTab +
										"GROSSED" + strTab + "ROUTED" + strTab + "FINALED" + strTab +
										"CASENO" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows + " rows from Stats Table.");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		}
	}

	void exportWorkload() {
		int fileNo = 1;
		String strFileName = "";
		String output = "";
		String comment = "";
		ResultSet rst = null;
		File file = null;
		FileOutputStream fos = null;
		try {
			parent.dbPowerJ.strSQL = "SELECT * FROM Cases ORDER BY CASEID";
			rst = parent.dbPowerJ.getResultSet();
			strFileName = dataDir + "cases" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "CASEID" + strTab + "FACID" + strTab + "MSID" + strTab +
						"GROSSID" + strTab + "FINALID" + strTab + "SPYID" + strTab +
						"SUBID" + strTab + "GROSSTAT" + strTab + "ROUTETAT" + strTab +
						"FINALTAT" + strTab + "TOTALTAT" + strTab + "PROID" + strTab +
						"NOSPECS" + strTab + "NOBLOCKS" + strTab + "NOSLIDES" + strTab +
						"NOSYNOPT" + strTab + "NOFS" + strTab + "ACCESSED" + strTab +
						"GROSSED" + strTab + "ROUTED" + strTab + "FINALED" + strTab +
						"VALUE1" + strTab + "VALUE2" + strTab + "VALUE3" + strTab +
						"VALUE4" + strTab + "CASENO" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("CASEID") + strTab + rst.getInt("FACID") + strTab +
							rst.getInt("MSID") + strTab + rst.getInt("GROSSID") + strTab +
							rst.getInt("FINALID") + strTab + rst.getInt("SPYID") + strTab +
							rst.getInt("SUBID") + strTab + rst.getInt("GROSSTAT") + strTab +
							rst.getInt("ROUTETAT") + strTab + rst.getInt("FINALTAT") + strTab +
							rst.getInt("TOTALTAT") + strTab + rst.getInt("PROID") + strTab +
							rst.getInt("NOSPECS") + strTab + rst.getInt("NOBLOCKS") + strTab +
							rst.getInt("NOSLIDES") + strTab + rst.getInt("NOSYNOPT") + strTab +
							rst.getInt("NOFS") + strTab +
							rst.getTimestamp("ACCESSED").getTime() + strTab +
							rst.getTimestamp("GROSSED").getTime() + strTab +
							rst.getTimestamp("ROUTED").getTime() + strTab +
							rst.getTimestamp("FINALED").getTime() + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE1")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE2")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE3")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE4")) + strTab +
							rst.getString("CASENO") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "cases" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "CASEID" + strTab + "FACID" + strTab + "MSID" + strTab +
										"GROSSID" + strTab + "FINALID" + strTab + "SPYID" + strTab +
										"SUBID" + strTab + "GROSSTAT" + strTab + "ROUTETAT" + strTab +
										"FINALTAT" + strTab + "TOTALTAT" + strTab + "PROID" + strTab +
										"NOSPECS" + strTab + "NOBLOCKS" + strTab + "NOSLIDES" + strTab +
										"NOSYNOPT" + strTab + "NOFS" + strTab + "ACCESSED" + strTab +
										"GROSSED" + strTab + "ROUTED" + strTab + "FINALED" + strTab +
										"VALUE1" + strTab + "VALUE2" + strTab + "VALUE3" + strTab +
										"VALUE4" + strTab + "CASENO" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from Cases Table.");
			}
			parent.dbPowerJ.strSQL = "SELECT * FROM Specimens ORDER BY CASEID, SPECID";
			rst = parent.dbPowerJ.getResultSet();
			fileNo = 1;
			strFileName = dataDir + "specimens" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "SPECID" + strTab + "CASEID" + strTab + "MSID" + strTab +
						"NOBLOCKS" + strTab + "NOSLIDES" + strTab + "NOFRAGS" + strTab +
						"VALUE1" + strTab + "VALUE2" + strTab + "VALUE3" + strTab +
						"VALUE4" + strTab + "DESCR" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("SPECID") + strTab + rst.getInt("CASEID") + strTab +
							rst.getInt("MSID") + strTab + rst.getInt("NOBLOCKS") + strTab +
							rst.getInt("NOSLIDES") + strTab + rst.getInt("NOFRAGS") + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE1")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE2")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE3")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE4")) + strTab +
							rst.getString("DESCR") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "specimens" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "SPECID" + strTab + "CASEID" + strTab + "MSID" + strTab +
										"NOBLOCKS" + strTab + "NOSLIDES" + strTab + "NOFRAGS" + strTab +
										"VALUE1" + strTab + "VALUE2" + strTab + "VALUE3" + strTab +
										"VALUE4" + strTab + "DESCR" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from Specimens Table.");
			}
			parent.dbPowerJ.strSQL = "SELECT * FROM Orders ORDER BY SPECID, GRPID";
			rst = parent.dbPowerJ.getResultSet();
			fileNo = 1;
			strFileName = dataDir + "orders" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "SPECID" + strTab + "GRPID" + strTab + "QTY" + strTab +
						"VALUE1" + strTab + "VALUE2" + strTab + "VALUE3" + strTab +
						"VALUE4" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("SPECID") + strTab + rst.getInt("GRPID") + strTab +
							rst.getInt("QTY") + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE1")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE2")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE3")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE4")) + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "orders" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "SPECID" + strTab + "GRPID" + strTab + "QTY" + strTab +
										"VALUE1" + strTab + "VALUE2" + strTab + "VALUE3" + strTab +
										"VALUE4" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from Orders Table.");
			}
			parent.dbPowerJ.strSQL = "SELECT * FROM Comments ORDER BY CASEID";
			rst = parent.dbPowerJ.getResultSet();
			fileNo = 1;
			strFileName = dataDir + "comments" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "CASEID" + strTab + "COMMENT" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					comment = rst.getString("COMMENT");
					output = "" + rst.getInt("CASEID") + strTab +
							comment.replaceAll(Constants.NEW_LINE, " NL ") + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "comments" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "CASEID" + strTab + "COMMENT" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from Comments Table.");
			}
			parent.dbPowerJ.strSQL = "SELECT * FROM Frozens ORDER BY CASEID";
			rst = parent.dbPowerJ.getResultSet();
			fileNo = 1;
			strFileName = dataDir + "frozens" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "CASEID" + strTab + "PERID" + strTab + "NOSPECS" + strTab +
						"NOBLOCKS" + strTab + "NOSLIDES" + strTab + "VALUE1" + strTab +
						"VALUE2" + strTab + "VALUE3" + strTab + "VALUE4" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("CASEID") + strTab + rst.getInt("PERID") + strTab +
							rst.getInt("NOSPECS") + strTab + rst.getInt("NOBLOCKS") + strTab +
							rst.getInt("NOSLIDES") + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE1")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE2")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE3")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE4")) + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "frozens" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "CASEID" + strTab + "PERID" + strTab + "NOSPECS" + strTab +
										"NOBLOCKS" + strTab + "NOSLIDES" + strTab + "VALUE1" + strTab +
										"VALUE2" + strTab + "VALUE3" + strTab + "VALUE4" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from Frozens Table.");
			}
			System.out.println("Exporting Additional...");
			parent.dbPowerJ.strSQL = "SELECT * FROM Additional ORDER BY CASEID, PERID, CODEID, FINALED";
			rst = parent.dbPowerJ.getResultSet();
			fileNo = 1;
			strFileName = dataDir + "additional" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "CASEID" + strTab + "PERID" + strTab + "CODEID" + strTab +
						"FINALED" + strTab + "VALUE1" + strTab + "VALUE2" + strTab +
						"VALUE3" + strTab + "VALUE4" + Constants.NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getInt("CASEID") + strTab + rst.getInt("PERID") + strTab +
							rst.getInt("CODEID") + strTab + rst.getDate("FINALED").getTime() + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE1")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE2")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE3")) + strTab +
							parent.numbers.formatDouble(3, rst.getDouble("VALUE4")) + Constants.NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(Constants.SLEEP_TIME);
						} catch (InterruptedException e) {
						}
						if (noRows % 5000 == 0) {
							fos.close();
							strFileName = dataDir + "additional" + fileNo + ".txt";
							fileNo++;
							file = new File(strFileName);
							if (!file.exists())
								file.createNewFile();
							if (file.exists()) {
								fos = new FileOutputStream(file);
								output = "CASEID" + strTab + "PERID" + strTab + "CODEID" + strTab +
										"FINALED" + strTab + "VALUE1" + strTab + "VALUE2" + strTab +
										"VALUE3" + strTab + "VALUE4" + Constants.NEW_LINE;
								fos.write(output.getBytes());
							}
						}
					}
				}
				fos.close();
				rst.close();
				System.out.println("Exported " + noRows +
						" rows from Additional Table.");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Export", e);
		}
	}

	boolean getDir() {
		boolean success = false;
		final JFileChooser fc = new JFileChooser();
		try {
			dataDir = parent.defaults.getString("datadir", System.getProperty("user.home"));
			File file = new File(dataDir);
			fc.setSelectedFile(file);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int val = fc.showOpenDialog(parent);
			if (val == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				if (file.isDirectory()) {
					if (!dataDir.equals(file.getAbsolutePath())) {
						dataDir = file.getAbsolutePath();
						parent.defaults.setString("datadir", dataDir);
					}
					if (!dataDir.endsWith(Constants.FILE_SEPARATOR)) {
						dataDir += Constants.FILE_SEPARATOR;
					}
					success = true;
				}
			}
		} catch (HeadlessException ignore) {}
		return success;
	}

	void importDash() {
		int noInserts = 0;
		int noLines = 0;
		int intValue = 0;
		long lngValue = 0;
		String strSQL = "";
		String strFileName = "";
		String[] columns = null;
		PreparedStatement stm = null;
		File file = null;
		Scanner scanner = null;
		try {
			strFileName = dataDir + "dashboard.txt";
			file = new File(strFileName);
			if (file.exists()) {
				noRows = 0;
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Pending (CASEID, FACID, MSID, " +
						"GROSSTAT, EMBEDTAT, MICROTAT, STAINTAT, ROUTETAT, HISTOTAT, " +
						"FINALTAT, GROSSID, EMBEDID, MICROID, STAINID, ROUTEID, " +
						"FINALID, SPYID, SUBID, PROID, STATUS, NOSPECS, " +
						"NOBLOCKS, NOSLIDES, ACCESSED, GROSSED, EMBEDED, MICROED, " +
						"STAINED, ROUTED, FINALED, CASENO) VALUES (?, ?, ?, " +
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					noLines++;
					columns = scanner.nextLine().split(strTab);
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 31) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Integer.MAX_VALUE) {
							intValue = Integer.MAX_VALUE;
						}
						stm.setInt(1, intValue);
						for (int i = 1; i < 23; i++) {
							intValue = Integer.valueOf(columns[i]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Short.MAX_VALUE) {
								intValue = Short.MAX_VALUE;
							}
							stm.setInt(i+1, intValue);
						}
						for (int i = 23; i < 30; i++) {
							lngValue = Long.valueOf(columns[i]);
							if (lngValue < 0) {
								lngValue = -lngValue;
							} else if (lngValue > Long.MAX_VALUE) {
								lngValue = Long.MAX_VALUE;
							}
							stm.setTimestamp(i+1, new Timestamp(lngValue));
						}
						columns[30] = columns[30].trim();
						if (columns[30].length() > 12) {
							columns[30] = columns[30].substring(0, 12);
						}
						stm.setString(31, columns[30]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
						if (noRows % 1000 == 0) {
							try {
								Thread.sleep(Constants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Dashboard Import Error No Columns = " +
								columns.length + " at line " + noLines);
						break;
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Dashboard Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			System.out.println("Dashboard Import Complete...");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			parent.dbPowerJ.closeStm(stm);
		}
	}

	void importSetup() {
		int noInserts = 0;
		int noLines = 0;
		int intValue = 0;
		double dblValue = 0;
		String strSQL = "";
		String strFileName = "";
		String[] columns = null;
		PreparedStatement stm = null;
		File file = null;
		Scanner scanner = null;
		try {
			noRows = 0;
			strFileName = dataDir + "setup.txt";
			file = new File(strFileName);
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Setup (STPID, STPVAL) VALUES (?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					noLines++;
					columns = scanner.nextLine().split(strTab);
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 2) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(1, intValue);
						columns[1] = columns[1].trim();
						if (columns[1].length() > 64) {
							columns[1] = columns[1].substring(0, 64);
						}
						stm.setString(2, columns[1]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Setup Import Error No Columns = " +
								columns.length + " at line " + noLines);
						break;
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Setup Table.");
			}
			strFileName = dataDir + "rules.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Rules (ID, NAME, DESCR) VALUES (?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					noLines++;
					columns = scanner.nextLine().split(strTab);
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 3) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(1, intValue);
						columns[1] = columns[1].toUpperCase().trim();
						if (columns[1].length() > 24) {
							columns[1] = columns[1].substring(0, 24);
						}
						stm.setString(2, columns[1]);
						columns[2] = columns[2].trim();
						if (columns[2].length() > 256) {
							columns[2] = columns[3].substring(0, 256);
						}
						stm.setString(3, columns[2]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Rules Import Error No Columns = " +
								columns.length + " at line " + noLines);
						break;
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Rules Table.");
			}
			for (byte i = 1; i < 5; i++) {
				strFileName = dataDir + "coder" + i + ".txt";
				file = new File(strFileName);
				if (file.exists()) {
					noRows = 0;
					noLines = 0;
					scanner = new Scanner(new FileReader(strFileName));
					strSQL = "INSERT INTO Coder" + i +" (ID, RULEID, COUNT, VALUE1, "+ 
							"VALUE2, VALUE3, NAME, DESCR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
					stm = parent.dbPowerJ.getStatement(strSQL);
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 8) {
							for (int x = 0; x < 3; x++) {
								intValue = Integer.valueOf(columns[x]);
								if (intValue < 0) {
									intValue = 0;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt((1+x), intValue);
							}
							for (int x = 3; x < 6; x++) {
								dblValue = Double.valueOf(columns[x]);
								if (dblValue < 0) {
									dblValue = 0;
								} else if (dblValue > 99d) {
									dblValue = 99d;
								}
								stm.setDouble((1+x), dblValue);
							}
							if (columns[6].length() > 16) {
								columns[6] = columns[6].substring(0, 16);
							}
							stm.setString(7, columns[6]);
							if (columns[7].length() > 128) {
								columns[7] = columns[7].substring(0, 128);
							}
							stm.setString(8, columns[7]);
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Coder" + i + " Import Error No Columns = " +
									columns.length + " at line " + noLines);
						}
					}
					scanner.close();
					stm.close();
					System.out.println("Inserted " + noRows + " rows in Coder" + i + " Table.");
				}
			}
			strFileName = dataDir + "specialties.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Specialties (SPYID, DASH, WLOAD, " +
						"CODESPEC, SPYNAME) VALUES (?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					noLines++;
					columns = scanner.nextLine().split(strTab);
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 5) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(1, intValue);
						for (int x = 1; x < 4; x++) {
							columns[x] = columns[x].toUpperCase().trim();
							if (columns[x].length() > 1) {
								columns[x] = columns[x].substring(0, 1);
							}
							if (!columns[x].equals("Y")) {
								columns[x] = "N";
							}
							stm.setString(1+x, columns[x]);
						}
						columns[4] = columns[4].trim();
						if (columns[4].length() > 16) {
							columns[4] = columns[3].substring(0, 16);
						}
						stm.setString(5, columns[4]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Specialties Import Error No Columns = " +
								columns.length + " at line " + noLines);
						break;
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Specialties Table.");
			}
			strFileName = dataDir + "accessions.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Accessions (ACCID, SPYID, DASH, " + 
						"WLOAD, ACCNAME) VALUES (?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 5) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(1, intValue);
						intValue = Integer.valueOf(columns[1]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(2, intValue);
						columns[2] = columns[2].toUpperCase().trim();
						if (columns[2].length() > 1) {
							columns[2] = columns[2].substring(0, 1);
						}
						if (!columns[2].equals("Y")) {
							columns[2] = "N";
						}
						stm.setString(3, columns[2]);
						columns[3] = columns[3].toUpperCase().trim();
						if (columns[3].length() > 1) {
							columns[3] = columns[3].substring(0, 1);
						}
						if (!columns[3].equals("Y")) {
							columns[3] = "N";
						}
						stm.setString(4, columns[3]);
						if (columns[4].length() > 30) {
							columns[4] = columns[4].substring(0, 30);
						}
						stm.setString(5, columns[4]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Accessions Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Accessions Table.");
			}
			strFileName = dataDir + "subspecialty.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Subspecial (SUBID, " + 
						"SPYID, SUBINIT, SUBNAME) VALUES (?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 4) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(1, intValue);
						intValue = Integer.valueOf(columns[1]);
						if (intValue < 0) {
							intValue = -intValue;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt(2, intValue);
						columns[2] = columns[2].toUpperCase().trim();
						if (columns[2].length() > 3) {
							columns[2] = columns[2].substring(0, 3);
						}
						stm.setString(3, columns[2]);
						columns[3] = columns[3].toUpperCase().trim();
						if (columns[3].length() > 30) {
							columns[3] = columns[3].substring(0, 30);
						}
						stm.setString(4, columns[3]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Subspecialties Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Subspecialties Table.");
			}
			strFileName = dataDir + "facilities.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Facilities (FACID, DASH, WLOAD, " +
						"CODE, NAME) VALUES (?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 5) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = 0;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt((1), intValue);
						for (int x = 1; x < 3; x++) {
							columns[x] = columns[x].toUpperCase().trim();
							if (columns[x].length() > 1) {
								columns[x] = columns[x].substring(0, 1);
							}
							if (!columns[x].equals("Y")) {
								columns[x] = "N";
							}
							stm.setString(1+x, columns[x]);
						}
						columns[3] = columns[3].trim();
						if (columns[3].length() > 4) {
							columns[3] = columns[3].substring(0, 4);
						}
						stm.setString(4, columns[3]);
						columns[4] = columns[4].trim();
						if (columns[4].length() > 80) {
							columns[4] = columns[4].substring(0, 80);
						}
						stm.setString(5, columns[4]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Facilities Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Facilities Table.");
			}
			strFileName = dataDir + "groups.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Groups (ID, GRP, CODE1, CODE2, CODE3, " +
						"CODE4, NAME) VALUES (?, ?, ?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 7) {
						for (int x = 0; x < 6; x++) {
							// ID to CODE4
							intValue = Integer.valueOf(columns[x]);
							if (intValue < 0) {
								intValue = 0;
							} else if (intValue > Short.MAX_VALUE) {
								intValue = Short.MAX_VALUE;
							}
							stm.setInt((1+x), intValue);
						}
						columns[6] = columns[6].trim();
						if (columns[6].length() > 8) {
							columns[6] = columns[6].substring(0, 8);
						}
						stm.setString(7, columns[6]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Groups Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Groups Table.");
			}
			strFileName = dataDir + "personnel.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO Personnel (PERID, ACCESS, CODE, INITIALS, " +
						"PLAST, PFIRST) VALUES (?, ?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 6) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = 0;
						}
						stm.setInt(1, intValue);
						intValue = Integer.valueOf(columns[1]);
						// Can be -1 (full access)
						stm.setInt(2, intValue);
						columns[2] = columns[2].trim();
						if (columns[2].length() > 2) {
							columns[2] = columns[2].substring(0, 2);
						}
						stm.setString(3, columns[2]);
						columns[3] = columns[3].trim();
						if (columns[3].length() > 3) {
							columns[3] = columns[3].substring(0, 3);
						}
						stm.setString(4, columns[3]);
						columns[4] = columns[4].trim();
						if (columns[4].length() > 30) {
							columns[4] = columns[4].substring(0, 30);
						}
						stm.setString(5, columns[4]);
						columns[5] = columns[5].trim();
						if (columns[5].length() > 30) {
							columns[5] = columns[5].substring(0, 30);
						}
						stm.setString(6, columns[5]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Personnel Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Personnel Table.");
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			strFileName = dataDir + "masterspecimens.txt";
			file = new File(strFileName);
			noRows = 0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO MasterSpecimens (" +
						"MSID, SPYID, SUBID, PROCID, ISLN, GROSS, EMBED, MICROTOMY, " +
						"ROUTE, SIGNOUT, CODE1B, CODE1M, CODE1R, CODE2B, CODE2M, " +
						"CODE2R, CODE3B, CODE3M, CODE3R, CODE4B, CODE4M, CODE4R, " +
						"CODE, DESCR) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 24) {
						for (int x = 0; x < 22; x++) {
							// MSID to CODE4R
							intValue = Integer.valueOf(columns[x]);
							if (intValue < 0) {
								intValue = 0;
							} else if (intValue > Short.MAX_VALUE) {
								intValue = Short.MAX_VALUE;
							}
							stm.setInt((1+x), intValue);
						}
						columns[22] = columns[22].trim();
						if (columns[22].length() > 15) {
							columns[22] = columns[22].substring(0, 15);
						}
						stm.setString(23, columns[22]);	// CODE
						columns[23] = columns[23].trim();
						if (columns[23].length() > 80) {
							columns[23] = columns[23].substring(0, 80);
						}
						stm.setString(24, columns[23]);	// DESCR
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "MasterSpecimens Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in MasterSpecimens Table.");
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			strFileName = dataDir + "masterorders.txt";
			file = new File(strFileName);
			noRows =0;
			noLines = 0;
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				strSQL = "INSERT INTO MasterOrders (ID, GRPID, " +
						"CODE, DESCR) VALUES (?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (scanner.hasNextLine()) {
					columns = scanner.nextLine().split(strTab);
					noLines++;
					if (noLines == 1) {
						continue;
					}
					if (columns.length == 4) {
						intValue = Integer.valueOf(columns[0]);
						if (intValue < 0) {
							intValue = 0;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt((1), intValue);
						intValue = Integer.valueOf(columns[1]);
						if (intValue < 0) {
							intValue = 0;
						} else if (intValue > Short.MAX_VALUE) {
							intValue = Short.MAX_VALUE;
						}
						stm.setInt((2), intValue);
						columns[2] = columns[2].trim();
						if (columns[2].length() > 15) {
							columns[2] = columns[2].substring(0, 15);
						}
						stm.setString(3, columns[2]);
						columns[3] = columns[3].trim();
						if (columns[3].length() > 80) {
							columns[3] = columns[3].substring(0, 80);
						}
						stm.setString(4, columns[3]);
						noInserts = stm.executeUpdate();
						if (noInserts > 0) {
							noRows++;
						}
					} else {
						parent.log(JOptionPane.ERROR_MESSAGE, "Import", "MasterOrders Import Error No Columns = " +
								columns.length + " at line " + noLines);
					}
				}
				scanner.close();
				stm.close();
				System.out.println("Inserted " + noRows + " rows in MasterOrders Table.");
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			System.out.println("Setup Import Complete...");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			parent.dbPowerJ.closeStm(stm);
		}
	}

	void importStats() {
		int fileNo = 1;
		int noInserts = 0;
		int noLines = 0;
		int intValue = 0;
		long lngValue = 0;
		String strSQL = "";
		String strFileName = "";
		String[] columns = null;
		PreparedStatement stm = null;
		File file = null;
		Scanner scanner = null;
		try {
			noRows = 0;
			strFileName = dataDir + "stats" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				strSQL = "INSERT INTO Stats (CASEID, FACID, GROSSID, " +
						"FINALID, SPYID, SUBID, PROID, NOSPECS, NOBLOCKS, " +
						"NOSLIDES, NOHE, NOSS, NOIHC, NOMOL, NOFSP, NOFBL, " +
						"NOFSL, NOSYN, GRTAT, ROTAT, FITAT, TOTAT, MSID, " +
						"ACCESSED, GROSSED, ROUTED, FINALED, CASENO) VALUES (" +
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 28) {
							intValue = Integer.valueOf(columns[0]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Integer.MAX_VALUE) {
								intValue = Integer.MAX_VALUE;
							}
							stm.setInt(1, intValue);
							for (int i = 1; i < 23; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							for (int i = 23; i < 27; i++) {
								lngValue = Long.valueOf(columns[i]);
								if (lngValue < 0) {
									lngValue = -lngValue;
								} else if (lngValue > Long.MAX_VALUE) {
									lngValue = Long.MAX_VALUE;
								}
								stm.setTimestamp(i+1, new Timestamp(lngValue));
							}
							columns[27] = columns[27].trim();
							if (columns[27].length() > 12) {
								columns[27] = columns[27].substring(0, 12);
							}
							stm.setString(28, columns[27]);
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Stats Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					strFileName = dataDir + "stats" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Stats Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			parent.dbPowerJ.closeStm(stm);
		}
	}

	void importWorkload() {
		int fileNo = 1;
		int noInserts = 0;
		int noLines = 0;
		int intValue = 0;
		long lngValue = 0;
		double dblValue = 0;
		String strSQL = "";
		String strFileName = "";
		String[] columns = null;
		PreparedStatement stm = null;
		File file = null;
		Scanner scanner = null;
		try {
			noRows = 0;
			strFileName = dataDir + "cases" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				strSQL = "INSERT INTO Cases (CASEID, FACID, MSID, " +
						"GROSSID, FINALID, SPYID, SUBID, GROSSTAT, ROUTETAT, " +
						"FINALTAT, TOTALTAT, PROID, NOSPECS, NOBLOCKS, NOSLIDES, " +
						"NOSYNOPT, NOFS, ACCESSED, GROSSED, ROUTED, FINALED, " +
						"VALUE1, VALUE2, VALUE3, VALUE4, CASENO) VALUES (?, ?, ?, " +
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
						"?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 26) {
							intValue = Integer.valueOf(columns[0]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Integer.MAX_VALUE) {
								intValue = Integer.MAX_VALUE;
							}
							stm.setInt(1, intValue);
							for (int i = 1; i < 17; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							for (int i = 17; i < 21; i++) {
								lngValue = Long.valueOf(columns[i]);
								if (lngValue < 0) {
									lngValue = -lngValue;
								} else if (lngValue > Long.MAX_VALUE) {
									lngValue = Long.MAX_VALUE;
								}
								stm.setTimestamp(i+1, new Timestamp(lngValue));
							}
							for (int i = 21; i < 25; i++) {
								dblValue = Double.valueOf(columns[i]);
								if (dblValue < 0) {
									dblValue = -dblValue;
								} else if (dblValue > 99.9) {
									dblValue = 99.9;
								}
								stm.setDouble(i+1, dblValue);
							}
							columns[25] = columns[25].trim();
							if (columns[25].length() > 12) {
								columns[25] = columns[25].substring(0, 12);
							}
							stm.setString(26, columns[25]);
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Workload Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					strFileName = dataDir + "cases" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Cases Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			fileNo = 1;
			strFileName = dataDir + "specimens" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				noRows = 0;
				noLines = 0;
				strSQL = "INSERT INTO Specimens (SPECID, CASEID, MSID, " +
						"NOBLOCKS, NOSLIDES, NOFRAGS, VALUE1, VALUE2, " +
						"VALUE3, VALUE4, DESCR) VALUES (?, ?, ?, " +
						"?, ?, ?, ?, ?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 11) {
							for (int i = 0; i < 2; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Integer.MAX_VALUE) {
									intValue = Integer.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							for (int i = 2; i < 6; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							for (int i = 6; i < 10; i++) {
								dblValue = Double.valueOf(columns[i]);
								if (dblValue < 0) {
									dblValue = -dblValue;
								} else if (dblValue > 99.9) {
									dblValue = 99.9;
								}
								stm.setDouble(i+1, dblValue);
							}
							columns[10] = columns[10].trim();
							if (columns[10].length() > 64) {
								columns[10] = columns[10].substring(0, 64);
							}
							stm.setString(11, columns[10]);
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Specimens Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
					strFileName = dataDir + "specimens" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Specimens Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			fileNo = 1;
			strFileName = dataDir + "orders" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				noRows = 0;
				noLines = 0;
				strSQL = "INSERT INTO Orders (SPECID, GRPID, QTY, VALUE1, " +
						"VALUE2, VALUE3, VALUE4) VALUES (?, ?, ?, " +
						"?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 7) {
							intValue = Integer.valueOf(columns[0]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Integer.MAX_VALUE) {
								intValue = Integer.MAX_VALUE;
							}
							stm.setInt(1, intValue);
							for (int i = 1; i < 3; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							for (int i = 3; i < 7; i++) {
								dblValue = Double.valueOf(columns[i]);
								if (dblValue < 0) {
									dblValue = -dblValue;
								} else if (dblValue > 99.9) {
									dblValue = 99.9;
								}
								stm.setDouble(i+1, dblValue);
							}
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Orders Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
					strFileName = dataDir + "orders" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Orders Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			fileNo = 1;
			strFileName = dataDir + "comments" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				noRows = 0;
				noLines = 0;
				strSQL = "INSERT INTO Comments (CASEID, COMMENT) VALUES (?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 2) {
							intValue = Integer.valueOf(columns[0]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Integer.MAX_VALUE) {
								intValue = Integer.MAX_VALUE;
							}
							stm.setInt(1, intValue);
							columns[1] = columns[1].trim();
							columns[1] = columns[1].replaceAll(" NL ", Constants.NEW_LINE);
							columns[1] = columns[1].replaceAll(" NL", "");
							if (columns[1].length() > 32600) {
								columns[1] = columns[1].substring(0, 32600);
							}
							stm.setString(2, columns[1]);
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Comments Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
					strFileName = dataDir + "comments" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Comments Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			fileNo = 1;
			strFileName = dataDir + "frozens" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				noRows = 0;
				noLines = 0;
				strSQL = "INSERT INTO Frozens (CASEID, PERID, NOSPECS, NOBLOCKS, NOSLIDES, " +
						"VALUE1, VALUE2, VALUE3, VALUE4) VALUES (?, ?, ?, ?, ?, " +
						"?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 9) {
							intValue = Integer.valueOf(columns[0]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Integer.MAX_VALUE) {
								intValue = Integer.MAX_VALUE;
							}
							stm.setInt(1, intValue);
							for (int i = 1; i < 5; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							for (int i = 5; i < 9; i++) {
								dblValue = Double.valueOf(columns[i]);
								if (dblValue < 0) {
									dblValue = -dblValue;
								} else if (dblValue > 99.9) {
									dblValue = 99.9;
								}
								stm.setDouble(i+1, dblValue);
							}
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Frozens Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
					strFileName = dataDir + "frozens" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Frozens Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			fileNo = 1;
			strFileName = dataDir + "additional" + fileNo + ".txt";
			fileNo++;
			file = new File(strFileName);
			if (file.exists()) {
				noRows = 0;
				noLines = 0;
				strSQL = "INSERT INTO Additional (CASEID, PERID, CODEID, FINALED, " +
						"VALUE1, VALUE2, VALUE3, VALUE4) VALUES (?, ?, ?, ?, " +
						"?, ?, ?, ?)";
				stm = parent.dbPowerJ.getStatement(strSQL);
				while (file.exists()) {
					scanner = new Scanner(new FileReader(strFileName));
					noLines = 0;
					while (scanner.hasNextLine()) {
						noLines++;
						columns = scanner.nextLine().split(strTab);
						if (noLines == 1) {
							continue;
						}
						if (columns.length == 8) {
							intValue = Integer.valueOf(columns[0]);
							if (intValue < 0) {
								intValue = -intValue;
							} else if (intValue > Integer.MAX_VALUE) {
								intValue = Integer.MAX_VALUE;
							}
							stm.setInt(1, intValue);
							for (int i = 1; i < 3; i++) {
								intValue = Integer.valueOf(columns[i]);
								if (intValue < 0) {
									intValue = -intValue;
								} else if (intValue > Short.MAX_VALUE) {
									intValue = Short.MAX_VALUE;
								}
								stm.setInt(i+1, intValue);
							}
							lngValue = Long.valueOf(columns[3]);
							if (lngValue < 0) {
								lngValue = -lngValue;
							} else if (lngValue > Long.MAX_VALUE) {
								lngValue = Long.MAX_VALUE;
							}
							stm.setTimestamp(4, new Timestamp(lngValue));
							for (int i = 4; i < 8; i++) {
								dblValue = Double.valueOf(columns[i]);
								if (dblValue < 0) {
									dblValue = -dblValue;
								} else if (dblValue > 99.9) {
									dblValue = 99.9;
								}
								stm.setDouble(i+1, dblValue);
							}
							noInserts = stm.executeUpdate();
							if (noInserts > 0) {
								noRows++;
							}
							if (noRows % 1000 == 0) {
								try {
									Thread.sleep(Constants.SLEEP_TIME);
								} catch (InterruptedException e) {
								}
							}
						} else {
							parent.log(JOptionPane.ERROR_MESSAGE, "Import", "Additional Import Error No Columns = " +
									columns.length + " at line " + noLines);
							break;
						}
					}
					scanner.close();
					try {
						Thread.sleep(Constants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
					strFileName = dataDir + "additional" + fileNo + ".txt";
					fileNo++;
					file = new File(strFileName);
				}
				stm.close();
				System.out.println("Inserted " + noRows + " rows in Additional Table.");
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
			System.out.println("Workload Import Complete...");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			parent.dbPowerJ.closeStm(stm);
		}
	}
}
