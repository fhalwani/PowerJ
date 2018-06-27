package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;

class Variables {
	AtomicBoolean busy = new AtomicBoolean(false);
	boolean autoLogin = false;
	boolean hasError = false;
	boolean debugMode = false;
	boolean offLine = false;
	boolean saturdayOff = true;
	boolean sundayOff = true;
	boolean[] codersActive = new boolean[4];
	boolean[] userAccess = new boolean[32];
	byte openingHour = 6;
	byte closingHour = 22;
	byte pjSystem = 0;
	short apPort = 1433;
	// 1 FTE = 225 work days per year (260 Business Days (including statutory holidays), less 25 days vacation, less 10 days CME)
	short daysInYear = 225;
	// 1 FTE = 7560 CAP, 7500 W2Q, 8100 RCP, 6300 CPT
	short[] codersFTE = {0, 0, 0, 0};
	short pjPort = 1433;
	short userID = 0;
	int timerInterval = 60000;	// 1 minute in milliseconds
	int updateInterval = 1800000;	// 30 minutes in milliseconds
	long lastUpdate = 0;
	long minWorkloadDate = 0;	// May 1, 2017 = 1493611200000L
	long nextUpdate = 0;
	String apLogin = "";	// pathdash
	String apPassword = "";	// wTCr_8290^
	String apDatabase = "";
	String apAddress = "";
	String appDir = "";
	String[] codersName = {"CAP", "W2Q", "RCP", "CPT"};
	String pjAddress = "";
	String pjLogin = "";	// PJClient V0wTkl!P92PY$URe34vbnRL
	String pjPassword = "";	// PJServer xCyaO06ma$KO25uFiF!sZrE
	
	Variables() {
		for (int i = 0; i < userAccess.length; i++) {
			userAccess[i] = false;
		}
	}
	
	/** Retrieve Setup defaults values from Table **/
	void readDB(PowerJ parent) {
		final byte STP_SERVER = 1;
		final byte STP_PORT = 2;
		final byte STP_DATABASE = 3;
		final byte STP_LOGIN = 4;
		final byte STP_PASSWORD = 5;
		final byte STP_SAT_OFF = 6;
		final byte STP_SUN_OFF = 7;
		final byte STP_OPENING = 8;
		final byte STP_CLOSING = 9;
		final byte STP_TIMER = 10;
		final byte STP_UPDATER = 11;
		final byte STP_CODER1_NAME = 12;
		final byte STP_CODER1_ACTIVE = 13;
		final byte STP_CODER2_NAME = 14;
		final byte STP_CODER2_ACTIVE = 15;
		final byte STP_CODER3_NAME = 16;
		final byte STP_CODER3_ACTIVE = 17;
		final byte STP_CODER4_NAME = 18;
		final byte STP_CODER4_ACTIVE = 19;
		final byte STP_MIN_WL_DATE = 20;
		final byte STP_CODER1_FTE = 21;
		final byte STP_CODER2_FTE = 22;
		final byte STP_CODER3_FTE = 23;
		final byte STP_CODER4_FTE = 24;
		final byte STP_BUSINESS_DAYS = 25;
		byte key = 0;
		String value = "";
		ResultSet rst = parent.dbPowerJ.getSetup();
		try {
			while (rst.next()) {
				key = rst.getByte("STPID");
				if (rst.getString("STPVAL") == null) {
					value = "";
				} else {
					value = rst.getString("STPVAL").trim();
				}
				switch (key) {
				case STP_SERVER:
					apAddress = value;
					break;
				case STP_DATABASE:
					apDatabase = value;
					break;
				case STP_LOGIN:
					if (!offLine && autoLogin) {
						apLogin = value;
					}
					break;
				case STP_PASSWORD:
					if (!offLine && autoLogin) {
						apPassword = value;
					}
					break;
				case STP_CODER1_NAME:
					codersName[0] = value;
					break;
				case STP_CODER2_NAME:
					codersName[1] = value;
					break;
				case STP_CODER3_NAME:
					codersName[2] = value;
					break;
				case STP_CODER4_NAME:
					codersName[3] = value;
					break;
				case STP_CODER1_ACTIVE:
					codersActive[0] = (value.equalsIgnoreCase("Y"));
					break;
				case STP_CODER2_ACTIVE:
					codersActive[1] = (value.equalsIgnoreCase("Y"));
					break;
				case STP_CODER3_ACTIVE:
					codersActive[2] = (value.equalsIgnoreCase("Y"));
					break;
				case STP_CODER4_ACTIVE:
					codersActive[3] = (value.equalsIgnoreCase("Y"));
					break;
				case STP_CODER1_FTE:
					codersFTE[0] = parent.numbers.parseShort(value);
					break;
				case STP_CODER2_FTE:
					codersFTE[1] = parent.numbers.parseShort(value);
					break;
				case STP_CODER3_FTE:
					codersFTE[2] = parent.numbers.parseShort(value);
					break;
				case STP_CODER4_FTE:
					codersFTE[3] = parent.numbers.parseShort(value);
					break;
				case STP_PORT:
					apPort = parent.numbers.parseShort(value);
					break;
				case STP_OPENING:
					openingHour = parent.numbers.parseByte(value);
					// Allow time for Workload calculator
					if (openingHour < 1) {
						openingHour = 1;
					}
					break;
				case STP_CLOSING:
					closingHour = parent.numbers.parseByte(value);
					if (closingHour <= openingHour) {
						closingHour = openingHour++;
					}
					// Allow time for Workload calculator
					if (closingHour > 23) {
						closingHour = 23;
					}
					break;
				case STP_TIMER:
					timerInterval = parent.numbers.parseInt(value);
					if (timerInterval < 1) {
						timerInterval = 1;
					} else if (timerInterval > 10) {
						timerInterval = 10;
					}
					// From minutes to milliseconds
					timerInterval *= 60000;
					break;
				case STP_UPDATER:
					updateInterval = parent.numbers.parseInt(value);
					// Minimum every 10 minute to prevent denial of service attacks
					if (updateInterval < 5) {
						updateInterval = 5;
					} else if (updateInterval > 180) {
						updateInterval = 180;
					}
					// From minutes to milliseconds
					updateInterval *= 60000;
					break;
				case STP_SAT_OFF:
					saturdayOff = (value.equalsIgnoreCase("Y"));
					break;
				case STP_SUN_OFF:
					sundayOff = (value.equalsIgnoreCase("Y"));
					break;
				case STP_BUSINESS_DAYS:
					daysInYear = parent.numbers.parseShort(value);
					break;
				case STP_MIN_WL_DATE:
					minWorkloadDate = parent.numbers.parseLong(value);
					break;
				default:
					// Should not get here
				}
			}
		} catch (NumberFormatException e) {
			hasError = true;
			parent.log(JOptionPane.ERROR_MESSAGE, "Variables", e);
		} catch (SQLException e) {
			hasError = true;
			parent.log(JOptionPane.ERROR_MESSAGE, "Variables", e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
	
	void readSetup(PowerJ parent) {
		// Read PowerJ database variables that are encrypted in powerj.bin
		Crypto crypto = new Crypto(appDir);
		String[] data = crypto.getFile();
		if (data != null) {
			// Else, Derby
			if (data.length == 6) {
				if ("MSSQL".equals(data[0].toUpperCase())) {
					pjSystem = 1;
				} else if ("MYSQL".equals(data[0].toUpperCase())) {
					pjSystem = 2;
				} else {
					hasError = true;
					parent.log(JOptionPane.ERROR_MESSAGE, "Variables", "Invalid application binary file");
				}
				pjAddress = data[1];
				pjPort = parent.numbers.parseShort(data[2]);
				pjLogin = data[4];
				pjPassword = data[5];
			} else {
				hasError = true;
				parent.log(JOptionPane.ERROR_MESSAGE, "Variables", "Invalid application binary file");
			}
		}
	}
}
