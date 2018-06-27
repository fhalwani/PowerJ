package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class DlgWorkload extends JDialog implements WindowListener {
	private static final long serialVersionUID = -8994239920297522080L;
	private final byte DATA_ROW_NO = 0;
	//private final byte DATA_CASE_NO = 1;
	private final byte DATA_CASE_FINAL_DATE = 2;
	private final byte DATA_CASE_SPECIALTY = 3;
	private final byte DATA_CASE_SUBSPECIALTY = 4;
	private final byte DATA_CASE_SPECIMEN = 5;
	private final byte DATA_CASE_STAFF = 6;
	private final byte DATA_CASE_CODE1 = 7;
	private final byte DATA_CASE_CODE2 = 8;
	private final byte DATA_CASE_CODE3 = 9;
	private final byte DATA_CASE_CODE4 = 10;
	private final byte DATA_CASE_NOSPECS = 11;
	private final byte DATA_CASE_NOBLOCKS = 12;
	private final byte DATA_CASE_NOSLIDES = 13;
	private final byte DATA_CASE_NOSYNOPTICS = 14;
	private final byte DATA_CASE_NOFS = 15;
	private final String strName = "Workload Details";
	private PowerJ parent;
	private JTableEditor tblCases;
	private ArrayList<ClassCase> list = new ArrayList<ClassCase>();

	DlgWorkload(PowerJ parent, int[] rows, int[] values, long timeFrom, long timeTo) {
		super();
		this.parent = parent;
		readTable(rows, values, timeFrom, timeTo);
		createDialog();
	}

	private void createDialog() {
		setName(strName);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setIconImage(Utilities.getImage(Constants.APP_NAME));
		setTitle(Constants.APP_NAME + " - " + strName);
		setLayout(new BorderLayout());
		addWindowListener(this);
		tblCases = new JTableEditor(parent, new ModelCases());
		tblCases.setName(strName);
		TableColumn column = tblCases.getColumnModel().getColumn(DATA_ROW_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		JScrollPane scroll = new JScrollPane(tblCases,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scroll.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -5301105545782919740L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(scroll, BorderLayout.CENTER);
		Rectangle r = new Rectangle(0, 0, 600, 600);
		r = parent.defaults.getRectangle("wldetailsbounds", r);
		setLocation(r.x, r.y);
		setPreferredSize(new Dimension(r.width, r.height));
		validate();
		pack();
		setVisible(true);
	}
	
	private void readTable(int[] rows, int[] values, long timeFrom, long timeTo) {
		parent.setBusy(true);
		ClassCase thisRow = new ClassCase();
		ResultSet rst = parent.dbPowerJ.getWLDetails(rows, values, timeFrom, timeTo);
		try {
			while (rst.next()) {
				thisRow = new ClassCase();
				thisRow.noSynoptics = rst.getByte("NOSYNOPT");
				thisRow.noSpecimens = rst.getShort("NOSPECS");
				thisRow.noBlocks = rst.getShort("NOBLOCKS");
				thisRow.noSlides = rst.getShort("NOSLIDES");
				thisRow.noFS = rst.getShort("NOFS");
				thisRow.value1 = rst.getDouble("VALUE1");
				thisRow.value2 = rst.getDouble("VALUE2");
				thisRow.value3 = rst.getDouble("VALUE3");
				thisRow.value4 = rst.getDouble("VALUE4");
				thisRow.calFinal.setTimeInMillis(rst.getTimestamp("FINALED").getTime());
				thisRow.caseNo = rst.getString("CASENO");
				thisRow.staff = rst.getString("INITIALS");
				thisRow.specialty = rst.getString("SPYNAME");
				thisRow.subspecialty = rst.getString("SUBINIT");
				thisRow.specimen = rst.getString("SPEC");
				list.add(thisRow);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, strName, e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
		}
	}

	private class ClassCase {
		byte noSynoptics = 0;
		short noSpecimens = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short noFS = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		Calendar calFinal = Calendar.getInstance();
		String caseNo = "";
		String staff = "";
		String specialty = "";
		String subspecialty = "";
		String specimen = "";
	}

	private class ModelCases extends AbstractTableModel {
		private static final long serialVersionUID = -5335300411076285900L;
		private final String[] columns = {"NO", "CASE", "FINAL", "SPY", "SUB",
			"SPEC", "STAFF", parent.variables.codersName[0], parent.variables.codersName[1],
			parent.variables.codersName[2], parent.variables.codersName[3],
			"SPECS", "BLK", "SLD", "SYNOP", "FSEC"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			ClassCase thisRow = new ClassCase();
			if (row < list.size()) {
				thisRow = list.get(row);
			}
			switch (col) {
			case DATA_CASE_CODE4:
				value = thisRow.value4;
				break;
			case DATA_CASE_CODE3:
				value = thisRow.value3;
				break;
			case DATA_CASE_CODE2:
				value = thisRow.value2;
				break;
			case DATA_CASE_CODE1:
				value = thisRow.value1;
				break;
			case DATA_CASE_NOBLOCKS:
				value = thisRow.noBlocks;
				break;
			case DATA_CASE_NOSLIDES:
				value = thisRow.noSlides;
				break;
			case DATA_CASE_NOSPECS:
				value = thisRow.noSpecimens;
				break;
			case DATA_CASE_NOSYNOPTICS:
				value = thisRow.noSynoptics;
				break;
			case DATA_CASE_NOFS:
				value = thisRow.noFS;
				break;
			case DATA_CASE_SPECIALTY:
				value = thisRow.specialty;
				break;
			case DATA_CASE_SUBSPECIALTY:
				value = thisRow.subspecialty;
				break;
			case DATA_CASE_SPECIMEN:
				value = thisRow.specimen;
				break;
			case DATA_CASE_STAFF:
				value = thisRow.staff;
				break;
			case DATA_CASE_FINAL_DATE:
				value = thisRow.calFinal;
				break;
			case DATA_ROW_NO:
				if (tblCases != null) {
					value = tblCases.convertRowIndexToView(row) +1;
				}
				break;
			default:
				value = thisRow.caseNo;
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_CASE_FINAL_DATE:
				return Calendar.class;
			case DATA_CASE_CODE4:
			case DATA_CASE_CODE3:
			case DATA_CASE_CODE2:
			case DATA_CASE_CODE1:
				return Double.class;
			case DATA_ROW_NO:
			case DATA_CASE_NOBLOCKS:
			case DATA_CASE_NOSLIDES:
			case DATA_CASE_NOSPECS:
			case DATA_CASE_NOSYNOPTICS:
			case DATA_CASE_NOFS:
				return Integer.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// This table is not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, table not editable
		}
	}

	public void windowClosing(WindowEvent e) {
		// Save last used bounds
		Rectangle r = getBounds();
		parent.defaults.setRectangle("wldetailsbounds", r);
		dispose();
	}

	public void windowOpened(WindowEvent ignore) {}
	public void windowClosed(WindowEvent ignore) {}
	public void windowIconified(WindowEvent ignore) {}
	public void windowDeiconified(WindowEvent ignore) {}
	public void windowActivated(WindowEvent ignore) {}
	public void windowDeactivated(WindowEvent ignore) {}
}
