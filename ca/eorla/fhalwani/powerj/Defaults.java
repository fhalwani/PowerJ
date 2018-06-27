package ca.eorla.fhalwani.powerj;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

class Defaults {
	private boolean altered = false;
	private String strFileName = "";
	private Hashtable<String, Object> tblDefaults;
	
	Defaults(Variables variables) {
		strFileName = variables.appDir + Constants.FILE_SEPARATOR +
				"bin" + Constants.FILE_SEPARATOR + "defaults.ini";
		readFile(variables);
	}

	void close() {
		if (altered) {
			saveFile();
		}
		tblDefaults.clear();
	}

	boolean getBoolean(String key, boolean def) {
		Boolean b = (Boolean) tblDefaults.get(key);
		if (b == null) {
			tblDefaults.put(key, new Boolean(def));
			altered = true;
			return def;
		}
		return b.booleanValue();
	}

	double getDouble(String key, double def) {
		Double d = (Double) tblDefaults.get(key);
		if (d == null) {
			tblDefaults.put(key, new Double(def));
			altered = true;
			return def;
		}
		return d.doubleValue();
	}

	int getInt(String key, int def) {
		Integer i = (Integer) tblDefaults.get(key);
		if (i == null) {
			tblDefaults.put(key, new Integer(def));
			altered = true;
			return def;
		}
		return i.intValue();
	}

	long getLong(String key, long def) {
		// Key object can be integer or long; Since
		// we can't cast Integer to Long, we convert Number to long 
		Number numb =  (Number) tblDefaults.get(key);
		if (numb == null) {
			tblDefaults.put(key, new Long(def));
			altered = true;
			return def;
		}
		Long lng = numb.longValue();
		return lng.longValue();
	}

	Point getPoint(String key, Point def) {
		Point p = (Point) tblDefaults.get(key);
		if (p == null) {
			tblDefaults.put(key, def);
			altered = true;
			return def;
		}
		return p;
	}

	Rectangle getRectangle(String key, Rectangle def) {
		Rectangle r = (Rectangle) tblDefaults.get(key);
		if (r == null) {
			tblDefaults.put(key, def);
			altered = true;
			return def;
		}
		return r;
	}

	String getString(String key, String def) {
		String s = (String) tblDefaults.get(key);
		if (s == null) {
			tblDefaults.put(key, def);
			altered = true;
			return def;
		}
		return s;
	}

	private void readFile(Variables variables) {
		Object value;
		String escaped;
		try {
			tblDefaults = new Hashtable<String, Object>();
			File f = new File(strFileName);
			if (!f.exists()) {
				return;
			}
			Scanner scanner = new Scanner(new FileReader(strFileName));
			while (scanner.hasNextLine()) {
				String[] columns = scanner.nextLine().split("=");
				// Backslash in Microsoft filename needs formatting
				escaped = columns[1].trim().replace("\\\\", "\\");
				value = toObject(escaped);
				tblDefaults.put(columns[0].trim(), value);
			}
			scanner.close();
		} catch (NullPointerException ignore) {
		} catch (FileNotFoundException ignore) {
		}
	}

	/** Saves the tblDefaults to disk. */
	void saveFile() {
		String key, value, escaped;
		try {
			if (altered) {
				File f = new File(strFileName);
				if (!f.exists())
					f.createNewFile();
				if (f.exists()) {
					FileOutputStream fos = new FileOutputStream(f);
					Enumeration<String> elements = tblDefaults.keys();
					while (elements.hasMoreElements()) {
						key = (String) elements.nextElement();
						value = toString(tblDefaults.get(key));
						// Backslash in Microsoft filename needs formatting
						escaped = value.replace("\\", "\\\\");
						fos.write((key + "=" + escaped + Constants.NEW_LINE).getBytes());
					}
					fos.close();
					altered = false;
				}
			}
		} catch (IOException ignore) {
		}
	}

	/** Writes the specified key-value pair into the tblDefaults file. */
	void setBoolean(String key, boolean value) {
		if (key == null)
			return;
		if (value != getBoolean(key, value)) {
			tblDefaults.put(key, new Boolean(value));
			altered = true;
		}
	}

	void setDouble(String key, double value) {
		if (key == null)
			return;
		if (value != getDouble(key, value)) {
			tblDefaults.put(key, new Double(value));
			altered = true;
		}
	}

	void setInt(String key, int value) {
		if (key == null)
			return;
		if (value != getInt(key, value)) {
			tblDefaults.put(key, new Integer(value));
			altered = true;
		}
	}

	void setLong(String key, long value) {
		if (key == null)
			return;
		if (value != getLong(key, value)) {
			tblDefaults.put(key, new Long(value));
			altered = true;
		}
	}

	void setPoint(String key, Point value) {
		if (key == null || value == null)
			return;
		if (value != getPoint(key, value)) {
			tblDefaults.put(key, value);
			altered = true;
		}
	}

	void setRectangle(String key, Rectangle value) {
		if (key == null || value == null)
			return;
		if (value != getRectangle(key, value)) {
			tblDefaults.put(key, value);
			altered = true;
		}
	}

	void setString(String key, String value) {
		if (key == null || value == null)
			return;
		if (!value.equals(getString(key, value))) {
			tblDefaults.put(key, value);
			altered = true;
		}
	}

	private Object toObject(String s) {
		if (s.startsWith("(") && s.endsWith(")")) {
			// Point or Rectangle?
			if (s.indexOf(", ") == s.lastIndexOf(", ")) {
				return toPoint(s);
			} else {
				return toRectangle(s);
			}
		} else if (s.trim().toLowerCase().equals("true")) {
			return true;
		} else if (s.trim().toLowerCase().equals("false")) {
			return false;
		}
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {}
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException e) {}
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException e) {}
		return s;
	}

	private Point toPoint(String s) {
		try {
			int i, j, x, y;
			if (s.charAt(0) != '(')
				return null;
			i = 1;
			j = s.indexOf(", ", i);
			x = new Integer(s.substring(i, j)).intValue();
			i = j + 2;
			j = s.indexOf(")", i);
			y = new Integer(s.substring(i, j)).intValue();
			return new Point(x, y);
		} catch (NumberFormatException ignore) {
			return null;
		}
	}

	private Rectangle toRectangle(String s) {
		try {
			int i, j, x, y, w, h;
			if (s.charAt(0) != '(')
				return null;
			i = 1;
			j = s.indexOf(", ", i);
			x = new Integer(s.substring(i, j)).intValue();
			i = j + 2;
			j = s.indexOf(", ", i);
			y = new Integer(s.substring(i, j)).intValue();
			i = j + 2;
			j = s.indexOf(", ", i);
			w = new Integer(s.substring(i, j)).intValue();
			i = j + 2;
			j = s.indexOf(")", i);
			h = new Integer(s.substring(i, j)).intValue();
			return new Rectangle(x, y, w, h);
		} catch (NumberFormatException ignore) {
			return null;
		}
	}

	private String toString(Object o) {
		if (o instanceof Rectangle)
			return toString((Rectangle) o);
		else if (o instanceof Point)
			return toString((Point) o);
		else if (o instanceof Boolean)
			return String.valueOf((Boolean) o);
		return o.toString();
	}

	private String toString(Point p) {
		return "(" + p.x + ", " + p.y + ")";
	}

	private String toString(Rectangle rect) {
		return "(" + rect.x + ", " + rect.y + ", " + rect.width + ", "
				+ rect.height + ")";
	}
}
