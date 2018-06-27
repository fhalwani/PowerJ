package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class DlgPersonnel extends JDialog implements WindowListener {
	private static final long serialVersionUID = 6497749686406590763L;
	private final byte COL_ROW_NO = 0;
	private final byte COL_EVNT_TIME = 1;
	private final byte COL_EVNT_GAP = 2;
	private final byte COL_EVNT_CASE = 3;
	private final byte COL_EVNT_MATERIAL = 4;
	private final byte COL_EVNT_LOCATION = 5;
	// private final byte COL_EVNT_DESCRIPTION = 6;
	private int prsID = 0;
	private String prsName = "";
	private PowerJ parent;
	private DbAPIS dbAP;
	private JTableEditor tblData;
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();
	
	DlgPersonnel(PowerJ parent, int prsID) {
		super();
		this.parent = parent;
		this.prsID = prsID;
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareTracker();
				readEvents();
				readPerson();
				dbAP.close();
			}
		}
		createDialog();
	}

	private void createDialog() {
		setName("PersonEvents");
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setIconImage(Utilities.getImage(Constants.APP_NAME));
		setTitle(Constants.APP_NAME + " - " + prsName);
		setLayout(new BorderLayout());
		addWindowListener(this);
		tblData = new JTableEditor(parent, new EventsModel());
		tblData.setName("tblData");
		TableColumn column = tblData.getColumnModel().getColumn(COL_ROW_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		column = tblData.getColumnModel().getColumn(COL_EVNT_GAP);
		column.setCellRenderer(new RendererInterval());
		JScrollPane scrollEvents = new JScrollPane(tblData,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollEvents.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scrollEvents.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 2512157753156516151L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(scrollEvents, BorderLayout.CENTER);
		Rectangle r = new Rectangle(0, 0, 600, 600);
		r = parent.defaults.getRectangle("personbounds", r);
		Dimension dim = new Dimension(r.width, r.height);
		setLocation(r.x, r.y);
		setPreferredSize(dim);
		validate();
		pack();
		setVisible(true);
	}
	
	private void readEvents() {
		Calendar calEnd = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();
		calStart.add(Calendar.WEEK_OF_YEAR, -1);
		calStart.set(Calendar.HOUR, 0);
		calStart.set(Calendar.MINUTE, 0);
		calStart.set(Calendar.SECOND, 0);
		calStart.set(Calendar.MILLISECOND, 1);
		String strMaterial = "";
		DataRow dataRow = new DataRow();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = dbAP.getStatement(1);
			stm.setTimestamp(1, new Timestamp(calStart.getTimeInMillis()));
			stm.setTimestamp(2, new Timestamp(calEnd.getTimeInMillis()));
			stm.setInt(3, prsID);
			rst = stm.executeQuery();
			while (rst.next()) {
				strMaterial = rst.getString("source_rec_type").trim();
				if (strMaterial.equals("S")) {
					strMaterial = "Specimen ";
				} else if (strMaterial.equals("B")) {
					strMaterial = "Block ";
				} else if (strMaterial.equals("L")) {
					if (rst.getString("event_type").trim().
							toLowerCase().equals("folder_scanned")) {
						// Skip these, meaningless
						continue;
					}
					strMaterial = "Slide ";
				} else {
					strMaterial += " ";
				}
				strMaterial += rst.getString("material_label").trim();
				dataRow = new DataRow();
				dataRow.material = strMaterial;
				dataRow.time.setTimeInMillis(rst.getTimestamp("event_date").getTime());
				dataRow.location = rst.getString("event_location");
				dataRow.description = rst.getString("event_description");
				dataRow.caseNo = rst.getString("accession_no");
				dataRows.add(dataRow);
			}
			rst.close();
			stm = dbAP.getStatement(2);
			stm.setTimestamp(1, new Timestamp(calStart.getTimeInMillis()));
			stm.setTimestamp(2, new Timestamp(calEnd.getTimeInMillis()));
			stm.setInt(3, prsID);
			rst = stm.executeQuery();
			while (rst.next()) {
				dataRow = new DataRow();
				dataRow.time.setTimeInMillis(rst.getTimestamp("created_date").getTime());
				dataRow.material = "Orders";
				dataRow.description = "Ordered " + rst.getString("code");
				dataRow.caseNo = rst.getString("accession_no");
				dataRows.add(dataRow);
			}
			rst.close();
			stm = dbAP.getStatement(3);
			stm.setTimestamp(1, new Timestamp(calStart.getTimeInMillis()));
			stm.setTimestamp(2, new Timestamp(calEnd.getTimeInMillis()));
			stm.setInt(3, prsID);
			rst = stm.executeQuery();
			while (rst.next()) {
				dataRow = new DataRow();
				dataRow.material = "Case";
				dataRow.time.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
				dataRow.description = "Completed " + rst.getString("description");
				dataRow.caseNo = rst.getString("accession_no");
				dataRows.add(dataRow);
			}
			rst.close();
			// Sort Vector by time descending
			Collections.sort(dataRows, new Comparator<DataRow>() {
				public int compare(DataRow o1, DataRow o2) {
					if (o1.time.getTimeInMillis() > o2.time.getTimeInMillis())
						return 1;
					else if (o2.time.getTimeInMillis() > o1.time.getTimeInMillis())
						return -1;
					else
						return 0;
				}
			});
			for (int i = 0; i < dataRows.size()-1; i++) {
				dataRow = dataRows.get(i);
				dataRow.gap = dataRow.time.getTimeInMillis()
					- dataRows.get(i+1).time.getTimeInMillis();
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Staff Dialog", e);
		} finally {
			dbAP.closeRst(rst);
		}
	}

	private void readPerson() {
		ResultSet rst = null;
		PreparedStatement stm = null;
		try {
			stm = dbAP.getStatement(0);
			stm.setInt(1, prsID);
			rst = stm.executeQuery();
			while (rst.next()) {
				prsName = rst.getString("first_name").trim() +
					" " + rst.getString("last_name").trim();
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Staff Dialog", e);
		} finally {
			dbAP.closeRst(rst);
		}
	}
	
	class EventsModel extends AbstractTableModel {
		private static final long serialVersionUID = 8607320495699389072L;
		private final String[] columHeaders = {"Row", "Time", "Interval", "Case No", "Material", "Location", "Description"};
		
		public int getColumnCount() {
			return columHeaders.length;
		}

		public int getRowCount() {
			return dataRows.size();
		}

		public String getColumnName(int col) {
			return columHeaders[col];
		}

		public Object getValueAt(int row, int col) {
			DataRow dataRow = dataRows.get(row);
			Object returnValue = Object.class;
			switch (col) {
			case COL_ROW_NO:
				if (tblData != null) {
					returnValue = tblData.convertRowIndexToView(row) +1;
				}
				break;
			case COL_EVNT_TIME:
				returnValue = dataRow.time;
				break;
			case COL_EVNT_GAP:
				returnValue = dataRow.gap;
				break;
			case COL_EVNT_CASE:
				returnValue = dataRow.caseNo;
				break;
			case COL_EVNT_MATERIAL:
				returnValue = dataRow.material;
				break;
			case COL_EVNT_LOCATION:
				returnValue = dataRow.location;
				break;
			default:
				returnValue = dataRow.description;
			}
			return returnValue;
		}

		public Class<?> getColumnClass(int column) {
			if (column == COL_EVNT_GAP) {
				return Long.class;
			} else if (column == COL_EVNT_TIME) {
				return Calendar.class;
			} else {
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// Not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, not editable
		}
	}
	
	private class DataRow {
		long gap = 0;
		Calendar time = Calendar.getInstance();
		String material = "";
		String location = "";
		String description = "";
		String caseNo = "";
	}
	
	public void windowClosing(WindowEvent e) {
		// Save last used bounds
		Rectangle r = getBounds();
		parent.defaults.setRectangle("personbounds", r);
		dispose();
	}

	public void windowOpened(WindowEvent ignore) {}
	public void windowClosed(WindowEvent ignore) {}
	public void windowIconified(WindowEvent ignore) {}
	public void windowDeiconified(WindowEvent ignore) {}
	public void windowActivated(WindowEvent ignore) {}
	public void windowDeactivated(WindowEvent ignore) {}
}
