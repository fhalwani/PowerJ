package ca.eorla.fhalwani.powerj;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

class Constants {
	// Error Codes
	static final byte ERROR_NONE = 0;
	static final byte ERROR_CODING_RULE_UNKNOWN = 1;
	static final byte ERROR_ORDER_UNKNOWN = 2;
	static final byte ERROR_SPECIMEN_UNKNOWN = 3;
	static final byte ERROR_SPECIMENS_COUNT_ZERO = 4;
	// Cases status ordered by hierarchy
	static final byte STATUS_Accession = 0;
	static final byte STATUS_Gross = 1;
	static final byte STATUS_Embed = 2;
	static final byte STATUS_Microtomy = 3;
	static final byte STATUS_Slides = 4;
	static final byte STATUS_Routed = 5;
	static final byte STATUS_Diagnosis = 6;
	static final byte STATUS_Final = 7;
	static final byte STATUS_Addendum = 8;
	static final byte STATUS_Amended = 9;
	static final byte STATUS_Histology = 10;
	static final byte STATUS_All = 11;
	// User access codes
	static final byte ACCESS_Gross = 0;
	static final byte ACCESS_Histology = 1;
	static final byte ACCESS_Diagnosis = 2;
	static final byte ACCESS_ViewNames = 3;
	static final byte ACCESS_Workload = 4;
	static final byte ACCESS_Specimens = 5;
	static final byte ACCESS_Frozen = 6;
	static final byte ACCESS_Errors = 7;
	static final byte ACCESS_Additionals = 8;
	static final byte ACCESS_Cases = 9;
	static final byte ACCESS_STATS = 10;
	static final byte ACCESS_NOT_11 = 11;
	static final byte ACCESS_REP_SCAN = 12;
	static final byte ACCESS_REP_TAT = 13;
	static final byte ACCESS_REP_TRACK = 14;
	static final byte ACCESS_REP_WL = 15;
	static final byte ACCESS_REP_STATS = 16;
	static final byte ACCESS_NOT_17 = 17;
	static final byte ACCESS_NOT_28 = 28;
	static final byte ACCESS_SetupSchedule = 29;
	static final byte ACCESS_SetupDash = 30;
	static final byte ACCESS_SetupWorkload = 31;
	static final byte ROW_FACILITY = 1;
	static final byte ROW_SPECIALTY = 2;
	static final byte ROW_SUBSPECIALTY = 3;
	static final byte ROW_STAFF = 4;
	static final byte ROW_PROCEDURE = 5;
	static final byte PANEL_ACCESSION = 1;
	static final byte PANEL_ADDITIONAL = 2;
	static final byte PANEL_CASES = 3;
	static final byte PANEL_CODER1 = 4;
	static final byte PANEL_CODER2 = 5;
	static final byte PANEL_CODER3 = 6;
	static final byte PANEL_CODER4 = 7;
	static final byte PANEL_DASH = 8;
	static final byte PANEL_ERRORS = 9;
	static final byte PANEL_FACILITY = 10;
	static final byte PANEL_FROZENS = 11;
	static final byte PANEL_GROUPS = 12;
	static final byte PANEL_HISTO = 13;
	static final byte PANEL_ORDERS = 14;
	static final byte PANEL_PATHOLOGISTS = 15;
	static final byte PANEL_PERSONNEL = 16;
	static final byte PANEL_RULES = 17;
	static final byte PANEL_SCANNING = 18;
	static final byte PANEL_SPECIALTY = 19;
	static final byte PANEL_SPECIMEN = 20;
	static final byte PANEL_SUBSPECIAL = 21;
	static final byte PANEL_STATS = 22;
	static final byte PANEL_STATSUM = 23;
	static final byte PANEL_TEMPLATES = 24;
	static final byte PANEL_TRACKER = 25;
	static final byte PANEL_TURNAROUND = 26;
	static final byte PANEL_VARIABLES = 27;
	static final byte PANEL_WORKFLOW = 28;
	static final byte PANEL_WORKLOAD = 29;
	static final byte PANEL_WLSUMMARY = 30;
	static final byte PANEL_PERSONDASH = 31;
	static final byte PANEL_TEMPLATEDASH = 32;
	static final byte PANEL_CONTRACTS = 33;
	static final byte PANEL_SKILLS = 34;
	static final byte PANEL_STAFF = 35;
	static final byte PANEL_SHIFTS = 36;
	static final short SLEEP_TIME = 100;
	static final String NEW_LINE = System.getProperty("line.separator");
	static final String FILE_SEPARATOR = System.getProperty("file.separator");
	static final String LAB_NAME = "EORLA";
	static final String APP_NAME = "PowerJ";
	static final String APP_VERSION = "Version 3.0";
	static final String[] ERROR_STRINGS = {"none", "Rule Unknown", "Order Unknown",
			"Spec Unknown", "No Specimens"};
	static final String[] CODE_NAMES = {"Red", "Amber", "Green"};
	static final String[] STATUS_NAMES = {"Needs Gross", "Needs Embedding",
		"Needs Microtomy", "Needs Staining", "Needs Routing", "Needs Diagnosis",
		"Needs Final", "Needs Addendum", "Needs Amendment", "Needs Histology", "All Cases"};
	static final String[] STATUS_NAME2 = {"Accession", "Gross", "Embed", "Microtomy", 
		"Staining", "Routing", "Diagnosis", "Final", "Addendum", "Amendment", "Other"};
	static final String[] SPECIALTIES = {"ALL", "GNR", "MSK", "DERM", "LYMPH",
		"CARD", "ENT", "BRST", "THOR", "GU", "GYN", "GI", "OPH", "RENAL", "NEU", "BM"};
	static final String[] SPECIALTY_SORTED = {"ALL", "BRST", "CRD", "DRM", "ENT",
		"GI", "GU", "GYN", "LYM", "MSK", "NRO", "OPH", "RNL", "THO", "GNR"};
	static final Color COLOR_LIGHT_BLUE = new Color(0, 190, 255);
	static final Color COLOR_AZURE_BLUE = new Color(0, 127, 255);
	static final Color COLOR_DARK_BLUE = new Color(0, 0, 176);
    static final Color AMBER = new Color(255, 191, 0);
	static final Color[] CODE_COLORS = {Color.RED, AMBER, Color.GREEN};
	static final Color[] COLOR_EVEN_ODD = {new Color(190, 190, 190), Color.WHITE};
	static final Color[] SPECIALTY_COLORS = {Color.BLACK,	// Only used by Workload
		new Color(210, 180, 140),	// Tan - General
		Color.GRAY,	// MSK
		new Color(192, 192, 192),	// Derm (whiteish)
		new Color(182, 102, 210),	// Lilac - Lymphoma
		Color.BLUE,	// Cardiac
		new Color(162, 0, 37),	// Crimson - ENT
		Color.ORANGE,	// Breast 
		Color.GREEN,	// Thoracic
		new Color(255, 229, 180),	// Peach - GU
		Color.PINK,	// Gyne
		Color.YELLOW,	// GI
		Color.CYAN,	// Ophthalmic
		new Color(164, 196, 0),	// Lime - Renal
		new Color(54, 117, 136),	// Teal - Neuro
		Color.RED,	// BM
		new Color(240, 230, 140),	// khaki (workload)
		new Color(128, 128, 0),	// olive (workload)
		new Color(216, 191, 216),	// thistle (workload)
		new Color(210, 105, 30),	// chocolate (workload)
		new Color(128, 0, 0),	// maroon (workload)
		new Color(102, 102, 0)};	// dark yellow (workload)
	static final Color[] STATUS_COLORS = {Color.BLACK,	// Needs Gross
		Color.YELLOW,	// Needs Embedding
		Color.BLUE,	// Needs Microtomy
		Color.ORANGE,	// Needs Slides
		Color.GRAY,	// Needs Routing
		Color.CYAN,	// Needs Routing
		Color.PINK,	// Needs Diagnosis
		Color.GREEN};	// Needs Final
	static final Font APP_FONT = new Font("Serif", Font.BOLD, 14);
	static final Font FONT_SMALL = new Font("Serif", Font.BOLD, 12);
	static final Font FONT_LARGE = new Font("Serif", Font.BOLD, 16);
	static final Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);
}
