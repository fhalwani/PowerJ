package ca.eorla.fhalwani.powerj;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class DateUtils {
	final byte FORMAT_DATE = 1;
	final byte FORMAT_DATETIME = 2;
	final byte FORMAT_DATESHORT = 3;
	final byte FORMAT_TIME = 4;
	private int calculationsDate = 0;
	private final SimpleDateFormat tinyFormat = new SimpleDateFormat("d/M", Locale.getDefault());
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yy", Locale.getDefault());
	private final SimpleDateFormat dateTime = new SimpleDateFormat("d/M/yy H:mm", Locale.getDefault());
	private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	private Calendar calDate = Calendar.getInstance();
	private int[][] intStatDays = new int[12][2];
	private byte closingHour = 0;

	DateUtils(byte closingHour) {
		// Calculate Statutory days
		this.closingHour = closingHour;
		setStatDays();
	}

	String formatter(byte format) {
		Date date = new Date();
		return formatter(date, format);
	}
	
	String formatter(Calendar date, byte format) {
		Calendar cal = Calendar.getInstance();
		try {
			if (date != null) {
				cal.setTimeInMillis(date.getTimeInMillis());
			}
		} catch (Exception e) {
			cal = Calendar.getInstance();
		}
		return formatter(cal.getTime(), format);
	}

	String formatter(long millis, byte format) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return formatter(cal.getTime(), format);
	}

	public String formatter(Date value, byte format) {
		String strDate = "";
		Date date = new Date();
		try {
			if (value != null) {
				date.setTime(value.getTime());
			}
		} catch (Exception e) {
			date = new Date();
		}
		switch (format) {
		case FORMAT_DATE:
			strDate = dateFormat.format(value);
			break;
		case FORMAT_TIME:
			strDate = timeFormat.format(value);
			break;
		case FORMAT_DATETIME:
			strDate = dateTime.format(value);
			break;
		default:
			strDate = tinyFormat.format(value);
		}
		return strDate;
	}

	void setBusinessDay(Calendar cal, boolean backwards) {
		int days = 1;
		if (backwards) {
			days = -1;
		}
		cal.add(Calendar.DAY_OF_YEAR, days);
		while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| isStatDay(cal)) {
			cal.add(Calendar.DAY_OF_YEAR, days);
		}
	}
	
	int getBusinessDays(Calendar calStart, Calendar calEnd) {
		int days = 0;
		if (calculationsDate != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
			setStatDays();
			calculationsDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		}
		calDate.setTimeInMillis(calStart.getTimeInMillis());
		while (calDate.get(Calendar.YEAR) < calEnd.get(Calendar.YEAR)
			|| calDate.get(Calendar.DAY_OF_YEAR) < calEnd.get(Calendar.DAY_OF_YEAR)) {
			calDate.add(Calendar.DAY_OF_YEAR, 1);
			if (calDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& calDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
					&& !isStatDay(calDate)) {
				days++;
			}
		}
		if (days < 0) {
			days = 0;
		}
		return days;
	}
	
	int getBusinessDays(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getBusinessDays(calStart, calEnd);
	}
	
	short getBusinessHours(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getBusinessHours(calStart, calEnd);
	}

	short getBusinessHours(Calendar calStart, Calendar calEnd) {
		// Calculate Difference in working hours between 2 dates
		// Saturday, Sunday and statutory holidays are skipped
		int hours = 24 * getBusinessDays(calStart, calEnd);
		if (calEnd.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calEnd.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| isStatDay(calEnd)) {
			// On weekends, clock stops at closing hour
			hours += (closingHour - calStart.get(Calendar.HOUR_OF_DAY));
		} else if (calStart.get(Calendar.HOUR_OF_DAY) > calEnd.get(Calendar.HOUR_OF_DAY)) {
			// Substract difference from 24 hours
			hours -= (calStart.get(Calendar.HOUR_OF_DAY)
					- calEnd.get(Calendar.HOUR_OF_DAY));
		} else {
			hours += (calEnd.get(Calendar.HOUR_OF_DAY)
					- calStart.get(Calendar.HOUR_OF_DAY));
		}
		if (hours < 0) {
			hours = 0;
		}
		if (hours > Short.MAX_VALUE) {
			hours = Short.MAX_VALUE;
		}
		return (short)hours;
	}

	int getNoDays(Calendar calStart, Calendar calEnd) {
		int days = 0;
		calDate.setTimeInMillis(calStart.getTimeInMillis());
		while (calDate.get(Calendar.YEAR) < calEnd.get(Calendar.YEAR)
			|| calDate.get(Calendar.DAY_OF_YEAR) < calEnd.get(Calendar.DAY_OF_YEAR)) {
			days++;
			calDate.add(Calendar.DAY_OF_YEAR, 1);
		}
		return days;
	}

	int getNoDays(long start, long end) {
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(start);
		calEnd.setTimeInMillis(end);
		return getNoDays(calStart, calEnd);
	}

	boolean isStatDay(Calendar thisDate) {
		boolean isStatDay = false;
		int thisYear = thisDate.get(Calendar.YEAR);
		int thisDay = thisDate.get(Calendar.DAY_OF_YEAR);
		for (int i = 0; i < 12; i++) {
			if (thisYear == intStatDays[i][0]
					&& thisDay == intStatDays[i][1]) {
				isStatDay = true;
				break;
			}
		}
		return isStatDay;
	}

	boolean isWorkDay(Calendar thisDate) {
		boolean isWorkDay = true;
		if (thisDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| thisDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| isStatDay(thisDate)) {
			isWorkDay = false;
		}
		return isWorkDay;
	}

	/** Calculate and display the next schedule to scan the database **/
	void setNextUpdate(Variables variables) {
		boolean resetTime = false;
		int dayNext = 0, hourNext = 0;
		long now = Calendar.getInstance().getTimeInMillis();
		// First, set last update
		if (now - variables.nextUpdate <= 0) {
			// Avoid updates time shifting if timer fires early
			variables.lastUpdate = variables.nextUpdate;
		} else {
			// Next update was in the past (overdue) because software was powered off
			variables.lastUpdate = now;
		}
		Calendar calNext = Calendar.getInstance();
		calNext.setTimeInMillis(variables.lastUpdate + variables.updateInterval);
		hourNext = calNext.get(Calendar.HOUR_OF_DAY);
		if (hourNext > variables.closingHour) {
			// Sleep from 11 pm till 6 am next day
			calNext.add(Calendar.DAY_OF_YEAR, 1);
			resetTime = true;
		}
		dayNext = calNext.get(Calendar.DAY_OF_WEEK);
		if (dayNext == Calendar.SATURDAY && variables.saturdayOff) {
			// Sleep all day
			calNext.add(Calendar.DAY_OF_YEAR, 2);
			resetTime = true;
		} else if (dayNext == Calendar.SUNDAY && variables.sundayOff) {
			// Sleep all day
			calNext.add(Calendar.DAY_OF_YEAR, 1);
			resetTime = true;
		} else if (isStatDay(calNext)) {
			// Sleep all day on statutory holidays
			calNext.add(Calendar.DAY_OF_YEAR, 1);
			resetTime = true;
		}
		if (resetTime) {
			calNext.set(Calendar.HOUR_OF_DAY, variables.openingHour);
			calNext.set(Calendar.MINUTE, 0);
			// Wake up at 6:30 am next day
			calNext.add(Calendar.MILLISECOND, variables.updateInterval);
		}
		calNext.set(Calendar.SECOND, 0);
		calNext.set(Calendar.MILLISECOND, 0);
		variables.nextUpdate = calNext.getTimeInMillis();
	}

	private void setStatDays() {
		final byte DATE_NEWYEAR = 0;
		final byte DATE_FAMILY = 1;
		final byte DATE_GOOD = 2;
		final byte DATE_EASTER = 3;
		final byte DATE_VICTORIA = 4;
		final byte DATE_CANADA = 5;
		final byte DATE_CIVIC = 6;
		final byte DATE_LABOUR = 7;
		final byte DATE_THANKS = 8;
		final byte DATE_REMEMBER = 9;
		final byte DATE_XMAS = 10;
		final byte DATE_BOXING = 11;
		int intNoDays = 0;
		boolean[] blnMatched = {false, false, false, false,
				false, false, false, false,
				false, false, false, false};
		calDate = Calendar.getInstance();
		calDate.set(Calendar.MILLISECOND, 1);
		calDate.set(Calendar.SECOND, 0);
		calDate.set(Calendar.MINUTE, 0);
		calDate.set(Calendar.HOUR_OF_DAY, 0);
		calDate.add(Calendar.YEAR, -1);
		while (intNoDays < 366) {
			intNoDays++;
			calDate.add(Calendar.DAY_OF_YEAR, 1);
			if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				continue;
			} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				continue;
			} else if (calDate.get(Calendar.MONTH) == Calendar.JANUARY) {
				if (!blnMatched[DATE_NEWYEAR]) {
					if (calDate.get(Calendar.DAY_OF_MONTH) == 1) {
						// New Year any weekday
						intStatDays[DATE_NEWYEAR][0] = calDate.get(Calendar.YEAR);
						intStatDays[DATE_NEWYEAR][1] = calDate.get(Calendar.DAY_OF_YEAR);
						blnMatched[DATE_NEWYEAR] = true;
					} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_MONTH) < 4) {
							// New Year was on Saturday or Sunday
							intStatDays[DATE_NEWYEAR][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_NEWYEAR][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_NEWYEAR] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.FEBRUARY) {
				if (!blnMatched[DATE_FAMILY]) {
					if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 3) {
							// Family Day is 3rd Monday of week
							intStatDays[DATE_FAMILY][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_FAMILY][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_FAMILY] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.MARCH || calDate.get(Calendar.MONTH) == Calendar.APRIL) {
				if (!blnMatched[DATE_EASTER]) {
					int y = calDate.get(Calendar.YEAR);
					int a = y % 19;
					int b = y / 100;
					int c = y % 100;
					int d = b / 4;
					int e = b % 4;
					int f = (b + 8) / 25;
					int g = (b - f + 1) / 3;
					int h = (19 * a + b - d - g + 15) % 30;
					int i = c / 4;
					int k = c % 4;
					int l = (32 + 2 * e + 2 * i - h - k) % 7;
					int m = (a + 11 * h + 22 * l) / 451;
					int month = ((h + l - 7 * m + 114) / 31) -1;
					int day = ((h + l - 7 * m + 114) % 31) + 1;
					Calendar easter = Calendar.getInstance();
					easter.set(Calendar.MILLISECOND, 1);
					easter.set(Calendar.SECOND, 0);
					easter.set(Calendar.MINUTE, 0);
					easter.set(Calendar.HOUR_OF_DAY, 0);
					easter.set(Calendar.YEAR, y);
					easter.set(Calendar.MONTH, month);
					easter.set(Calendar.DAY_OF_MONTH, day);
					easter.add(Calendar.DAY_OF_YEAR, -2);
					intStatDays[DATE_EASTER][0] = easter.get(Calendar.YEAR);
					intStatDays[DATE_EASTER][1] = easter.get(Calendar.DAY_OF_YEAR);
					blnMatched[DATE_EASTER] = true;
					easter.add(Calendar.DAY_OF_YEAR, -3);
					intStatDays[DATE_GOOD][0] = easter.get(Calendar.YEAR);
					intStatDays[DATE_GOOD][1] = easter.get(Calendar.DAY_OF_YEAR);
					blnMatched[DATE_GOOD] = true;
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.MAY) {
				if (!blnMatched[DATE_VICTORIA]) {
					if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_MONTH) > 17 && calDate.get(Calendar.DAY_OF_MONTH) < 25) {
							// Victoria Day is Monday before May 25th
							intStatDays[DATE_VICTORIA][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_VICTORIA][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_VICTORIA] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.JULY) {
				if (!blnMatched[DATE_CANADA]) {
					if (calDate.get(Calendar.DAY_OF_MONTH) == 1) {
						// Canada Day on any weekday
						intStatDays[DATE_CANADA][0] = calDate.get(Calendar.YEAR);
						intStatDays[DATE_CANADA][1] = calDate.get(Calendar.DAY_OF_YEAR);
						blnMatched[DATE_CANADA] = true;
					} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_MONTH) < 4) {
							// Canada Day was on Saturday or Sunday
							intStatDays[DATE_CANADA][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_CANADA][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_CANADA] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.AUGUST) {
				if (!blnMatched[DATE_CIVIC]) {
					if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1) {
							// Civic holiday is 1st Monday of week
							intStatDays[DATE_CIVIC][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_CIVIC][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_CIVIC] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.SEPTEMBER) {
				if (!blnMatched[DATE_LABOUR]) {
					if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1) {
							// Labour Day is 1st Monday of September
							intStatDays[DATE_LABOUR][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_LABOUR][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_LABOUR] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.OCTOBER) {
				if (!blnMatched[DATE_THANKS]) {
					if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 2) {
							// Thanksgiving is 2nd Monday of October
							intStatDays[DATE_THANKS][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_THANKS][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_THANKS] = true;
						}
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.NOVEMBER) {
				if (!blnMatched[DATE_REMEMBER]) {
					if (calDate.get(Calendar.DAY_OF_MONTH) == 11) {
						// Remembrance Day is always November 11th, no Monday substitute
						intStatDays[DATE_REMEMBER][0] = calDate.get(Calendar.YEAR);
						intStatDays[DATE_REMEMBER][1] = calDate.get(Calendar.DAY_OF_YEAR);
						blnMatched[DATE_REMEMBER] = true;
					}
				}
			} else if (calDate.get(Calendar.MONTH) == Calendar.DECEMBER) {
				if (!blnMatched[DATE_XMAS]) {
					if (calDate.get(Calendar.DAY_OF_MONTH) == 25) {
						// Christmas on any weekday
						intStatDays[DATE_XMAS][0] = calDate.get(Calendar.YEAR);
						intStatDays[DATE_XMAS][1] = calDate.get(Calendar.DAY_OF_YEAR);
						blnMatched[DATE_XMAS] = true;
					} else if (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						if (calDate.get(Calendar.DAY_OF_MONTH) > 25 && calDate.get(Calendar.DAY_OF_MONTH) < 28) {
							// Christmas was Saturday or Sunday 
							intStatDays[DATE_XMAS][0] = calDate.get(Calendar.YEAR);
							intStatDays[DATE_XMAS][1] = calDate.get(Calendar.DAY_OF_YEAR);
							blnMatched[DATE_XMAS] = true;
						}
					}
					if (blnMatched[DATE_XMAS]) {
						// Boxing Day is next day, unless weekend
						calDate.add(Calendar.DAY_OF_YEAR, 1);
						while (calDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
								|| calDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
							calDate.add(Calendar.DAY_OF_YEAR, 1);
						}
						intStatDays[DATE_BOXING][0] = calDate.get(Calendar.YEAR);
						intStatDays[DATE_BOXING][1] = calDate.get(Calendar.DAY_OF_YEAR);
						blnMatched[DATE_BOXING] = true;
					}
				}
			}
		}
	}
}
