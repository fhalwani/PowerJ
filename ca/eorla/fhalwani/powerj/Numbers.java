package ca.eorla.fhalwani.powerj;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

class Numbers {
	private int noFractions = 3;
	private NumberFormat ftNumber;
	private DecimalFormat ftDouble;

	Numbers() {
		ftDouble = (DecimalFormat) NumberFormat.getNumberInstance();
		ftDouble.setMaximumFractionDigits(noFractions);
		ftDouble.setMinimumFractionDigits(noFractions);
		// Use Banker's rounding method
		ftDouble.setRoundingMode(RoundingMode.HALF_EVEN);
		ftNumber = NumberFormat.getNumberInstance();
	}
	
	int ceiling(int x, int y) {
		// Dividing 2 integers always rounds down, ie, 4/3 = 1
		// But we need ceiling, not floor, ie, 4/3 = 2
		int counter = 0;
		if (y > 0) {
			counter = x/y;
			if (x % y > 0) {
				counter++;
			}
		}
		return counter;
	}
	
	String formatDouble(int fractions, double value) {
		if (noFractions != fractions) {
			noFractions = fractions;
			ftDouble.setMaximumFractionDigits(noFractions);
			ftDouble.setMinimumFractionDigits(noFractions);
		}
		return ftDouble.format(value);
	}
	
	String formatNumber(long value) {
		return ftNumber.format(value);
	}
	
	double parseCurrency(Object value) {
		return parseCurrency((String) value);
	}
	
	double parseDouble(Object value) {
		return parseDouble((String) value);
	}
	
	double parseDouble(String value) {
		try {
			return ftDouble.parse(value).doubleValue();
		} catch (ParseException e) {
			return 0d;
		}
	}

	byte parseByte(Object value) {
		return parseByte((String) value);
	}
	
	byte parseByte(String value) {
		try {
			return ftNumber.parse(value).byteValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	int parseInt(Object value) {
		return parseInteger((String) value);
	}
	
	int parseInteger(String value) {
		try {
			return ftNumber.parse(value).intValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	long parseLong(Object value) {
		return parseLong((String) value);
	}
	
	long parseLong(String value) {
		try {
			return ftNumber.parse(value).longValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	short parseShort(Object value) {
		return parseShort((String) value);
	}
	
	short parseShort(String value) {
		try {
			return ftNumber.parse(value).shortValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	boolean[] toBits(int value) {
		boolean[] bits = new boolean[32];
		for (int i = 0; i < 32; i++) {
			bits[i] = (value & (1 << i)) != 0;
		}
		return bits;
	}

	double toDouble(int i, int value) {
		double d = 0;
		switch (i) {
		case 1:
			d = (1.0 * value / 10);
			break;
		case 2:
			d = (1.0 * value / 100);
			break;
		default:
			d = (1.0 * value / 1000);
		}
		return d;
	}

	int toInt(boolean[] bits) {
		int value = 0;
		for (int i = 0; i < 32; i++) {
			if (bits[i]) {
				value |= (1 << i);
			}
		}
		return value;
	}

	int toInt(short decimals, double value) {
		int i = 0;
		switch (decimals) {
		case 1:
			i = (int) ((value + 0.5) * 10);
			break;
		case 2:
			i = (int) ((value + 0.05) * 100);
			break;
		default:
			i = (int) ((value + 0.005) * 1000);
		}
		return i;
	}

	byte[] toBytes(int value) {
		byte[] aBytes = new byte[] { 
	        (byte) (value >> 24),
	        (byte) (value >> 16),
	        (byte) (value >> 8),
	        (byte) value };
		return aBytes;
	}

	int toInt(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException();
		}
		return toInt(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	int toInt(byte byte1, byte byte2, byte byte3, byte byte4) {
		int value = byte1 << 24 |
			(byte2 & 0xFF) << 16 |
			(byte3 & 0xFF) << 8 |
			(byte4 & 0xFF);
		return value;
	}

	int toInt(double d) {
		if (d > 0) {
	        return (int) (d + 0.5);
	    } else {
	        return (int) (d - 0.5);
	    }
	}
}
