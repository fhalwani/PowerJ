package ca.eorla.fhalwani.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.swing.JOptionPane;

class Scheduler {
	private long lastUpdate = 0;
	private final String className = "Scheduler";
	private PowerJ parent;
	private DbPowerJ dbPowerJ;

	Scheduler(PowerJ parent) {
		parent.log(JOptionPane.INFORMATION_MESSAGE, className,
				parent.dateUtils.formatter(parent.dateUtils.FORMAT_DATETIME) +
				" - " + className + " Started...");
		this.parent = parent;
		dbPowerJ = parent.dbPowerJ;
		dbPowerJ.prepareScheduler();
		saveWorkDays(getLastUpdate());
	}

	private void saveWorkDays(long dateID) {
		boolean isWorkday = false;
		int dayOfWeek = 0;
		int noUpdates = 0;
		int year = 0;
		int easterSunday = 0;
		long maxUpdate = 0;
		Calendar calDate = Calendar.getInstance();
		Calendar calMaxDate = Calendar.getInstance();
		PreparedStatement stm = null;
		try {
			calDate.setTimeInMillis(lastUpdate);
			calDate.add(Calendar.DAY_OF_YEAR, 1);
			// Maximum range is 52 weeks in the future
			calMaxDate.add(Calendar.WEEK_OF_YEAR, 52);
			calMaxDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			System.out.println("Last Date: "
					+ parent.dateUtils.formatter(calDate,parent.dateUtils.FORMAT_DATETIME)
					+ ", Max Date: "
					+ parent.dateUtils.formatter(calMaxDate,parent.dateUtils.FORMAT_DATETIME));
			if (calMaxDate.get(Calendar.YEAR) == calDate.get(Calendar.YEAR)
					&& calMaxDate.get(Calendar.WEEK_OF_YEAR) - calDate.get(Calendar.WEEK_OF_YEAR) < 4) {
				// we are up to date
				return;
			} else if (calMaxDate.get(Calendar.YEAR) - calDate.get(Calendar.YEAR) == 1
					&& calMaxDate.get(Calendar.WEEK_OF_YEAR) + 52 - calDate.get(Calendar.WEEK_OF_YEAR) < 4) {
				// we are up to date
				return;
			} else {
				maxUpdate = calMaxDate.getTimeInMillis();
			}
			dateID++;
			stm = dbPowerJ.getStatement(0);
			while (calDate.getTimeInMillis() < maxUpdate) {
				if (year < calDate.get(Calendar.YEAR)) {
					// Calculate Easter Sunday Date
					year = calDate.get(Calendar.YEAR);
					int a = year % 19,
							b = year / 100,
							c = year % 100,
							d = b / 4,
							e = b % 4,
							f = (b + 8) / 25,
							g = (b - f + 1) / 3,
							h = ((19*a) + b - d - g + 15) % 30,
							i = c / 4,
							j = c % 4,
							k = (32 + (2*e) + (2*i) - h - j) % 7,
							l = (a + (11*h) + (22*k)) / 451,
							month = (h + k - (7*l) + 114) / 31,
							day = ((h + k - (7*l) + 114) % 31) + 1;
					Calendar easter = Calendar.getInstance();
					easter.set(Calendar.YEAR, year);
					easter.set(Calendar.MONTH, (month-1));
					easter.set(Calendar.DAY_OF_MONTH, day);
					easterSunday = easter.get(Calendar.DAY_OF_YEAR);
				}
				isWorkday = true;
				dayOfWeek = calDate.get(Calendar.DAY_OF_WEEK);
				switch (dayOfWeek) {
				case Calendar.SATURDAY:
				case Calendar.SUNDAY:
					isWorkday = false;
					break;
				default:
					// Stats days
					switch (calDate.get(Calendar.MONTH)) {
					case Calendar.JANUARY:
						if (dayOfWeek == Calendar.MONDAY
							&& calDate.get(Calendar.DAY_OF_MONTH) < 4) {
							// New Year is Sat-Mon
							isWorkday = false;
						} else if (calDate.get(Calendar.DAY_OF_MONTH) == 1) {
							// New Year is Tues-Fri
							isWorkday = false;
						}
						break;
					case Calendar.FEBRUARY:
						if (dayOfWeek == Calendar.MONDAY
							&& calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 3) {
							// Family Day is 3rd Monday of month
							isWorkday = false;
						}
						break;
					case Calendar.MARCH:
					case Calendar.APRIL:
						if (easterSunday - calDate.get(Calendar.DAY_OF_YEAR) == 2) {
							// Good Friday
							isWorkday = false;
						} else if (calDate.get(Calendar.DAY_OF_YEAR) - easterSunday == 1) {
							// Easter Monday
							isWorkday = false;
						}
						break;
					case Calendar.MAY:
						if (dayOfWeek == Calendar.MONDAY
								&& calDate.get(Calendar.DAY_OF_MONTH) > 17
								&& calDate.get(Calendar.DAY_OF_MONTH) < 25) {
							// Victoria Day is Monday before May 25th
							isWorkday = false;
						}
						break;
					case Calendar.JULY:
						if (dayOfWeek == Calendar.MONDAY && calDate.get(Calendar.DAY_OF_MONTH) < 4) {
							// Canada Day is Sat-Mon
							isWorkday = false;
						} else if (calDate.get(Calendar.DAY_OF_MONTH) == 1) {
							// Canada Day is Tues-Fri
							isWorkday = false;
						}
						break;
					case Calendar.AUGUST:
						if (dayOfWeek == Calendar.MONDAY && calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1) {
							// Civic holiday is 1st Monday of month
							isWorkday = false;
						}
						break;
					case Calendar.SEPTEMBER:
						if (dayOfWeek == Calendar.MONDAY && calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1) {
							// Labor Day is 1st Monday of month
							isWorkday = false;
						}
						break;
					case Calendar.OCTOBER:
						if (dayOfWeek == Calendar.MONDAY
							&& calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 2) {
							// Thanksgiving is 2nd Monday of month
							isWorkday = false;
						}
						break;
					case Calendar.NOVEMBER:
						if (calDate.get(Calendar.DAY_OF_MONTH) == 11) {
							// Remembrance Day is always November 11th, no Monday substitute
							isWorkday = false;
						}
						break;
					case Calendar.DECEMBER:
						if (dayOfWeek == Calendar.TUESDAY
							&& calDate.get(Calendar.DAY_OF_MONTH) > 26
							&& calDate.get(Calendar.DAY_OF_MONTH) < 29) {
							// Boxing Day is Sun-Mon
							isWorkday = false;
						} else if (dayOfWeek == Calendar.MONDAY
							&& calDate.get(Calendar.DAY_OF_MONTH) > 25
							&& calDate.get(Calendar.DAY_OF_MONTH) < 29) {
							// Xmas is Sat-Mon
							isWorkday = false;
						} else if (calDate.get(Calendar.DAY_OF_MONTH) == 25) {
							// Xmas is Tues-Fri
							isWorkday = false;
						} else if (calDate.get(Calendar.DAY_OF_MONTH) == 26) {
							// Boxing day is Tues-Fri
							isWorkday = false;
						}
						break;
					default:
						// No holidays in June
					}
				}
				if (isWorkday) {
					stm.setLong(1, dateID);
					stm.setDate(2, new java.sql.Date(calDate.getTimeInMillis()));
					noUpdates = stm.executeUpdate();
					if (noUpdates > 0) {
						dateID++;
					}
				}
				calDate.add(Calendar.DAY_OF_YEAR, 1);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbPowerJ.closeStm();
		}
	}
	
	private long getLastUpdate() {
		long dateID = 0;
		// Earliest date is May 1st, 2017
		long minimum = parent.variables.minWorkloadDate;
		Calendar calLastUpdate = Calendar.getInstance();
		ResultSet rst = null;
		try {
			calLastUpdate.setTimeInMillis(minimum);
			calLastUpdate.add(Calendar.DAY_OF_YEAR, -1);
			minimum = calLastUpdate.getTimeInMillis();
			rst = dbPowerJ.getLastScheduler();
			while (rst.next()) {
				if (rst.getTimestamp("workday") != null) {
					lastUpdate = rst.getTimestamp("workday").getTime();
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, className, e);
		} finally {
			dbPowerJ.closeRst(rst);
			dbPowerJ.closeStm();
		}
		if (lastUpdate < minimum) {
			lastUpdate = minimum;
		} else {
			calLastUpdate.setTimeInMillis(lastUpdate);
			try {
				rst = dbPowerJ.getLastSchedulerID();
				while (rst.next()) {
					dateID = rst.getLong("id");
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, className, e);
			} finally {
				dbPowerJ.closeRst(rst);
				dbPowerJ.closeStm();
			}
		}
		return dateID;
	}
}
