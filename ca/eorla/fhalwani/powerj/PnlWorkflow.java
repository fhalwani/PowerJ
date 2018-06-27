package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class PnlWorkflow extends PnlMain {
	private static final long serialVersionUID = 5367601565668356819L;
	private final byte COL_ROW_NO = 0;
	//private final byte COL_CASENO = 1;
	private final byte COL_ACCESSION = 2;
	private final byte COL_ROUTE = 3;
	private final byte COL_PROCEDURE = 4;
	private final byte COL_SUBSPECIALTY = 5;
	private final byte COL_SPECIMEN = 6;
	private final byte COL_NOSPECS = 7;
	private final byte COL_NOBLOCKS = 8;
	private final byte COL_NOSLIDES = 9;
	private final byte COL_EXPECTED = 10;
	private final byte COL_SPENT = 11;
	private final byte COL_DELAY = 12;
	private String infoUpdate = "";
	private ChartLine chart;
	private JTableEditor tblCases;
	private ModelCases mdlCases;
	private Timer timer;
	private ArrayList<DataCase> dataCases = new ArrayList<DataCase>();
	private ArrayList<DataSlides> dataStaff = new ArrayList<DataSlides>();

	PnlWorkflow(PowerJ parent) {
		super(parent);
		setName("Workflow");
		createPanel();
		readData();
	}

	boolean close() {
		dataCases.clear();
		super.close();
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		mdlCases = new ModelCases();
		tblCases = new JTableEditor(parent, mdlCases);
		tblCases.setName("tblCases");
		// Define color column renderer
		TableColumn column = tblCases.getColumnModel().getColumn(COL_DELAY);
		column.setCellRenderer(new RendererColor(parent));
		column = tblCases.getColumnModel().getColumn(COL_ROW_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		tblCases.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int viewRow = ((JTable) e.getSource()).rowAtPoint(new Point(e.getX(), e.getY()));
		        displayCase(viewRow);
			}
		});
		JScrollPane scrollCases = new JScrollPane(tblCases,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCases.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -9024296715239285516L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Utilities.addComponent(scrollCases, 0, 0, 4, 4, 1.0, 0.5, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
		chart = new ChartLine();
		chart.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY());
			}
		});
		Utilities.addComponent(chart, 0, 4, 4, 4, 1.0, 0.5, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
		add(panel, BorderLayout.CENTER);
		// Set up timer to fire every x milliseconds
		timer = new Timer(parent.variables.timerInterval, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (parent.variables.nextUpdate - Calendar.getInstance().getTimeInMillis() < parent.variables.timerInterval) {
					readData();
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}

	private void displayCase(int row) {
		if (parent.variables.offLine) return;
		if (parent.variables.busy.get()) return;
        if (row > -1) {
			// else, Selection got filtered away.
        	// Convert to model
			row = tblCases.convertRowIndexToModel(row);
			if (row > -1 && row < dataCases.size()) {
				new DlgCase(parent, dataCases.get(row).caseID,
						dataCases.get(row).caseNo);
			}
        }
	}

	private void displayData(int x, int y) {
		if (parent.variables.busy.get()) return;
		String info = chart.getMessage(x, y);
		if (info.length() == 0) {
			info = infoUpdate;
		}
		parent.statusBar.setMessage(info);
	}

	private void readData() {
		if (parent.variables.busy.get()) return;
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}

	private void readTable() {
		parent.setBusy(true);
		DateUtils dateUtils = parent.dateUtils;
		byte statusID = 0;
		short finalID = 0;
		long cutoffFinal = 0, cutoffRoute = 0;
		Calendar calToday = Calendar.getInstance();
		Calendar calYesterday = Calendar.getInstance();
		Calendar calDayBefore = Calendar.getInstance();
		HashMap<Short, DataSlides> slides = new HashMap<Short, DataSlides>();
		DataSlides thisPerson = new DataSlides();
		DataCase thisCase = new DataCase();
		ResultSet rst = parent.dbPowerJ.getDashboard();
		try {
			parent.statusBar.setProgress(25);
			dataCases.clear();
			dataStaff.clear();
			calYesterday.add(Calendar.DAY_OF_YEAR, -1);
			while (!parent.dateUtils.isWorkDay(calYesterday)) {
				calYesterday.add(Calendar.DAY_OF_YEAR, -1);
			}
			calDayBefore.setTimeInMillis(calYesterday.getTimeInMillis());
			calDayBefore.add(Calendar.DAY_OF_YEAR, -1);
			while (!parent.dateUtils.isWorkDay(calDayBefore)) {
				calDayBefore.add(Calendar.DAY_OF_YEAR, -1);
			}
			calDayBefore.set(Calendar.HOUR_OF_DAY, 15);
			calDayBefore.set(Calendar.MINUTE, 0);
			calDayBefore.set(Calendar.SECOND, 0);
			calDayBefore.set(Calendar.MILLISECOND, 0);
			calYesterday.set(Calendar.HOUR_OF_DAY, 6);
			calYesterday.set(Calendar.MINUTE, 0);
			calYesterday.set(Calendar.SECOND, 0);
			calYesterday.set(Calendar.MILLISECOND, 0);
			cutoffFinal = calYesterday.getTimeInMillis();
			cutoffRoute = calDayBefore.getTimeInMillis();
			// Read whole table
			while (rst.next()) {
				if (rst.getShort("FINALID") <= 0) continue;
				statusID = rst.getByte("STATUS");
				finalID = rst.getShort("FINALID");
				if (statusID > Constants.STATUS_Slides && statusID < Constants.STATUS_Final) {
					// Pending case
					if (finalID == parent.variables.userID) {
						thisCase = new DataCase();
						thisCase.noSpecimens = rst.getByte("NOSPECS");
						thisCase.noBlocks = rst.getShort("NOBLOCKS");
						thisCase.noSlides = rst.getShort("NOSLIDES");
						thisCase.alloted = (short) (rst.getShort("GROSS") + rst.getShort("EMBED") +
								rst.getShort("MICROTOMY") + rst.getShort("ROUTE") +
								rst.getShort("SIGNOUT"));
						thisCase.caseID = rst.getLong("CASEID");
						thisCase.calAccession.setTimeInMillis(rst.getTimestamp("ACCESSED").getTime());
						thisCase.calRoute.setTimeInMillis(rst.getTimestamp("ROUTED").getTime());
						thisCase.caseNo = rst.getString("CASENO");
						thisCase.mainSpecimen = rst.getString("CODE");
						thisCase.subspecialty = rst.getString("SUBINIT");
						thisCase.procedure = DataProcedure.NAMES[rst.getByte("PROID")];
						thisCase.spent = dateUtils.getBusinessHours(thisCase.calAccession, calToday);
						if (thisCase.alloted > 0) {
							thisCase.delay = (short) ((100 * thisCase.spent) / thisCase.alloted);
						}
						dataCases.add(thisCase);
					}
					if (thisPerson.id != finalID) {
						thisPerson = slides.get(finalID);
						if (thisPerson == null) {
							thisPerson = new DataSlides();
							thisPerson.id = finalID;
							if (parent.variables.userAccess[Constants.ACCESS_ViewNames]
									|| finalID == parent.variables.userID) {
								// Else, Hide Names at bottom
								thisPerson.name = rst.getString("FINALINI");
							}
							slides.put(finalID, thisPerson);
						}
					}
					thisPerson.noPending += rst.getShort("NOSLIDES");
				} else if (statusID == Constants.STATUS_Final) {
					// Signed out
					if (cutoffFinal < rst.getTimestamp("FINALED").getTime()) {
						// Signed out today or yesterday
						if (thisPerson.id != finalID) {
							thisPerson = slides.get(finalID);
							if (thisPerson == null) {
								thisPerson = new DataSlides();
								thisPerson.id = finalID;
								if (parent.variables.userAccess[Constants.ACCESS_ViewNames]
										|| finalID == parent.variables.userID) {
									// Else, Hide Names at bottom
									thisPerson.name = rst.getString("FINALINI");
								}
								slides.put(finalID, thisPerson);
							}
						}
						thisPerson.noOut += rst.getShort("NOSLIDES");
					}
				}
				if (cutoffRoute < rst.getTimestamp("ROUTED").getTime()) {
					// Routed today, yesterday or night before
					if (thisPerson.id != finalID) {
						thisPerson = slides.get(finalID);
						if (thisPerson == null) {
							thisPerson = new DataSlides();
							thisPerson.id = finalID;
							if (parent.variables.userAccess[Constants.ACCESS_ViewNames]
									|| finalID == parent.variables.userID) {
								// Else, Hide Names at bottom
								thisPerson.name = rst.getString("FINALINI");
							}
							slides.put(finalID, thisPerson);
						}
					}
					thisPerson.noIn += rst.getShort("NOSLIDES");
				}
			}
			infoUpdate = "No Pending Cases: " + dataCases.size();
			parent.statusBar.setProgress(75);
			Collections.sort(dataCases, new Comparator<DataCase>() {
				public int compare(DataCase o1, DataCase o2) {
					return (o2.delay - o1.delay);
				}
			});
			for (Entry<Short, DataSlides> entry : slides.entrySet()) {
				dataStaff.add(entry.getValue());
			}
			Collections.sort(dataStaff, new Comparator<DataSlides>() {
				public int compare(DataSlides o1, DataSlides o2) {
					return (o2.noPending - o1.noPending);
				}
			});
			int size = dataStaff.size();
			for (int i = 0; i < size; i++) {
				thisPerson = dataStaff.get(i);
				if (thisPerson.name.length() == 0) {
					thisPerson.name = "P" + (i+1);
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
		}
	}

	/** Calculate and display the next schedule to scan the database **/
	void setNextUpdate() {
		parent.setNextUpdate();
		parent.statusBar.setMessage(infoUpdate);
		timer.restart();
	}

	class ModelCases extends AbstractTableModel {
		private static final long serialVersionUID = 5668202213870766281L;
		private final String[] columnNames = {"No", "Case", "Accession",
				"Route", "Proc", "Splty", "Spec", "Specs",
				"Blk", "Sld", "Cutoff", "Passed", "%" };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return dataCases.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			Object returnValue = Object.class;
			try {
				// The filters occasionally throw an Array index out of range exception
				DataCase thisRow = dataCases.get(row);
				switch (col) {
				case COL_ROW_NO:
					returnValue = tblCases.convertRowIndexToView(row) +1;
					break;
				case COL_DELAY:
					returnValue = thisRow.delay;
					break;
				case COL_SPENT:
					returnValue = thisRow.spent;
					break;
				case COL_EXPECTED:
					returnValue = thisRow.alloted;
					break;
				case COL_NOBLOCKS:
					returnValue = thisRow.noBlocks;
					break;
				case COL_NOSLIDES:
					returnValue = thisRow.noSlides;
					break;
				case COL_NOSPECS:
					returnValue = thisRow.noSpecimens;
					break;
				case COL_SPECIMEN:
					returnValue = thisRow.mainSpecimen;
					break;
				case COL_SUBSPECIALTY:
					returnValue = thisRow.subspecialty;
					break;
				case COL_ROUTE:
					returnValue = thisRow.calRoute;
					break;
				case COL_ACCESSION:
					returnValue = thisRow.calAccession;
					break;
				case COL_PROCEDURE:
					returnValue = thisRow.procedure;
					break;
				default:
					returnValue = thisRow.caseNo;
				}
			} catch (Exception ignore) {
			}
			return returnValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case COL_ROW_NO:
			case COL_DELAY:
			case COL_SPENT:
			case COL_EXPECTED:
			case COL_NOBLOCKS:
			case COL_NOSLIDES:
			case COL_NOSPECS:
				return Integer.class;
			case COL_ACCESSION:
			case COL_ROUTE:
				return Calendar.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, table not editable
		}
	}

	private class DataCase {
		byte noSpecimens = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short alloted = 0;	// time in hours alloted for entire case at its current status
		short spent = 0;
		short delay = 0;
		long caseID = 0;
		String caseNo = "";
		String mainSpecimen = "";
		String subspecialty = "";
		String procedure = "";
		Calendar calAccession = Calendar.getInstance();
		Calendar calRoute = Calendar.getInstance();
	}

	private class DataWorker extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws Exception {
			readTable();
			return null;
		}

		public void done() {
			// Display results
			mdlCases.fireTableDataChanged();
			if (dataCases.size() > 0) {
				// Else, index out of range error
				tblCases.setRowSelectionInterval(0,0);
			}
			chart.setData(dataStaff, "Slides Distribution");
			setNextUpdate();
		}
	}
}
