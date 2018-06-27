package ca.eorla.fhalwani.powerj;

class DataItem {
	private Integer value;
	private String description;
	
	DataItem(int i, String detail) {
		value = i;
		description = detail;
	}
	
	int getValue() {
		return value;
	}
	
	String getDescription() {
		return description;
	}

	public String toString() {
		return description;
	}

	public int compareTo(DataItem o) {
		if (o == null) {
			return 1;
		}
		return getDescription().compareTo(o.getDescription());
	}

	public boolean equals(Object o) {
		DataItem item = (DataItem)o;
		if (item == null) {
			return false;
		}
		return value.equals(item.getValue());
	}
}
