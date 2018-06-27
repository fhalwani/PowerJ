package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

class PnlHistology extends PnlMain {
	private static final long serialVersionUID = -3414777868049505685L;
	private final byte FILTER_FACILITY = 0;
	private final byte FILTER_SUBSPECIAL = 1;
	private final byte FILTER_PROCEDURE = 2;
	private final byte CHART_EMBED = 0;
	private final byte CHART_MICROTOMY = 1;
	private final byte CHART_SLIDES = 2;
	private final byte CHART_ROUTE = 3;
	private int[] filters = {0, 0, 0};
	private String infoUpdate = "";
	private ChartFlow chartEmbed;
	private ChartFlow chartMicrotomy;
	private ChartFlow chartSlides;
	private ChartFlow chartRoute;
	private Timer timer;
	private ArrayList<DataCase> list = new ArrayList<DataCase>();

	PnlHistology(PowerJ parent) {
		super(parent);
		setName("Histology");
		readTable();
		createPanel();
		readFilters();
	}

	boolean close() {
		list.clear();
		super.close();
		return true;
	}
	
	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		chartEmbed = new ChartFlow();
		chartEmbed.setBorder(border);
		chartEmbed.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_EMBED);
			}
		});
		Utilities.addComponent(chartEmbed, 0, 0, 1, 1, 0.5, 0.5,
				GridBagConstraints.BOTH, GridBagConstraints.WEST, panel);
		chartMicrotomy = new ChartFlow();
		chartMicrotomy.setBorder(border);
		chartMicrotomy.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_MICROTOMY);
			}
		});
		Utilities.addComponent(chartMicrotomy, 1, 0, 1, 1, 0.5, 0.5,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartSlides = new ChartFlow();
		chartSlides.setBorder(border);
		chartSlides.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_SLIDES);
			}
		});
		Utilities.addComponent(chartSlides, 0, 1, 1, 1, 0.5, 0.5,
				GridBagConstraints.BOTH, GridBagConstraints.WEST, panel);
		chartRoute = new ChartFlow();
		chartRoute.setBorder(border);
		chartRoute.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_ROUTE);
			}
		});
		Utilities.addComponent(chartRoute, 1, 1, 1, 1, 0.5, 0.5,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		add(createToolbar(), BorderLayout.NORTH);
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

	private JPanel createToolbar() {
		// Setup 3 JComboBox and fill with their data
		CboFacilities cboFacilities = new CboFacilities(parent);
		cboFacilities.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			CboFacilities cb = (CboFacilities)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[FILTER_FACILITY] = item.getValue();
	    			readFilters();
	    		}
	        }
	    });
		CboSubspecial cboSubspecial = new CboSubspecial(parent, false);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			CboSubspecial cb = (CboSubspecial)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[FILTER_SUBSPECIAL] = item.getValue();
	    			readFilters();
	    		}
	        }
	    });
		CboProcedures cboProcedures = new CboProcedures(parent, false);
		cboProcedures.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			CboProcedures cb = (CboProcedures)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[FILTER_PROCEDURE] = item.getValue();
	    			readFilters();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Facility:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(cboFacilities);
		panel.add(label);
		panel.add(cboFacilities);
		label = new JLabel("Subspecialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_B);
		label.setLabelFor(cboSubspecial);
		panel.add(label);
		panel.add(cboSubspecial);
		label = new JLabel("Procedure:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setLabelFor(cboProcedures);
		panel.add(label);
		panel.add(cboProcedures);
		return panel;
	}
	
	private void displayData(int x, int y, byte chartID) {
		if (parent.variables.busy.get()) return;
		String info = "", title = "Embed ", units = " blocks";
		switch (chartID) {
		case CHART_EMBED:
			info = chartEmbed.getMessage(x, y);
			title = "Embed ";
			break;
		case CHART_MICROTOMY:
			info = chartMicrotomy.getMessage(x, y);
			title = "Microtomy ";
			break;
		case CHART_SLIDES:
			info = chartSlides.getMessage(x, y);
			title = "Staining ";
			units = " slides";
			break;
		default:
			info = chartRoute.getMessage(x, y);
			title = "Routing ";
			units = " slides";
		}
		if (info.length() > 0) {
			info = title + info + units;
		} else {
			info = infoUpdate;
		}
		parent.statusBar.setMessage(info);
	}
	
	private void readCharts(ArrayList<DataWorkflow> dataEmbed,
			ArrayList<DataWorkflow> dataMicrotomy,
			ArrayList<DataWorkflow> dataSlides,
			ArrayList<DataWorkflow> dataRoute) {
		final byte businessDays = 9;
		int pendingEmbed = 0, pendingMicrotomy = 0;
		int pendingSlides = 0, pendingRoute= 0;
		DataCase thisCase = new DataCase();
		DataWorkflow dayEmbed = new DataWorkflow();
		DataWorkflow dayMicrotomy = new DataWorkflow();
		DataWorkflow daySlides = new DataWorkflow();
		DataWorkflow dayRoute = new DataWorkflow();
		Calendar calToday = Calendar.getInstance();
		for (int i = 0; i < businessDays; i++) {
			dayEmbed.dayOfYear = i;
			dayMicrotomy.dayOfYear = i;
			daySlides.dayOfYear = i;
			dayRoute.dayOfYear = i;
			dayEmbed.date = parent.dateUtils.formatter(calToday,
					parent.dateUtils.FORMAT_DATESHORT);
			dayMicrotomy.date = dayEmbed.date;
			daySlides.date = dayEmbed.date;
			dayRoute.date = dayEmbed.date;
			dataEmbed.add(dayEmbed);
			dataMicrotomy.add(dayMicrotomy);
			dataSlides.add(daySlides);
			dataRoute.add(dayRoute);
			dayEmbed = new DataWorkflow();
			dayMicrotomy = new DataWorkflow();
			daySlides = new DataWorkflow();
			dayRoute = new DataWorkflow();
			parent.dateUtils.setBusinessDay(calToday, true);
		}
		for (int i = 0; i < list.size(); i++) {
			thisCase = list.get(i);
			if (filters[FILTER_FACILITY] > 0
			       && filters[FILTER_FACILITY] != thisCase.facilityID) {
				continue;
			}
			if (filters[FILTER_SUBSPECIAL] > 0
			       && filters[FILTER_SUBSPECIAL] != thisCase.subspecialtyID) {
				continue;
			}
			if (filters[FILTER_PROCEDURE] > 0
			       && filters[FILTER_PROCEDURE] != thisCase.procedureID) {
				continue;
			}
			if (thisCase.grossDays < businessDays) {
				dataEmbed.get(thisCase.grossDays).noIn += thisCase.noBlocks;
			}
			if (thisCase.statusID > Constants.STATUS_Gross) {
				if (thisCase.embedDays < businessDays) {
					dataEmbed.get(thisCase.embedDays).noOut += thisCase.noBlocks;
					dataMicrotomy.get(thisCase.embedDays).noIn += thisCase.noBlocks;
				}
				if (thisCase.statusID > Constants.STATUS_Embed) {
					if (thisCase.microDays < businessDays) {
						dataMicrotomy.get(thisCase.microDays).noOut += thisCase.noBlocks;
						dataSlides.get(thisCase.microDays).noIn += thisCase.noSlides;
					}
					if (thisCase.statusID > Constants.STATUS_Microtomy) {
						if (thisCase.stainDays < businessDays) {
							dataSlides.get(thisCase.stainDays).noOut += thisCase.noSlides;
							dataRoute.get(thisCase.stainDays).noIn += thisCase.noSlides;
						}
						if (thisCase.statusID > Constants.STATUS_Slides) {
							if (thisCase.routeDays < businessDays) {
								dataRoute.get(thisCase.routeDays).noOut += thisCase.noSlides;
							}
						} else {
							pendingRoute += thisCase.noSlides;
						}
					} else {
						pendingSlides += thisCase.noSlides;
					}
				} else {
					pendingMicrotomy += thisCase.noBlocks;
				}
			} else {
				pendingEmbed += thisCase.noBlocks;
			}
		}
		// Calculate daily pending cases
		for (int i = 0; i < businessDays; i++) {
			dayEmbed = dataEmbed.get(i);
			dayEmbed.noPending = pendingEmbed;
			pendingEmbed -= (dayEmbed.noIn - dayEmbed.noOut);
			if (pendingEmbed < 0) {
				pendingEmbed = 0;
			}
			dayMicrotomy = dataMicrotomy.get(i);
			dayMicrotomy.noPending = pendingMicrotomy;
			pendingMicrotomy -= (dayMicrotomy.noIn - dayMicrotomy.noOut);
			if (pendingMicrotomy < 0) {
				pendingMicrotomy = 0;
			}
			daySlides = dataSlides.get(i);
			daySlides.noPending = pendingSlides;
			pendingSlides -= (daySlides.noIn - daySlides.noOut);
			if (pendingSlides < 0) {
				pendingSlides = 0;
			}
			dayRoute = dataRoute.get(i);
			dayRoute.noPending = pendingRoute;
			pendingRoute -= (dayRoute.noIn - dayRoute.noOut);
			if (pendingRoute < 0) {
				pendingRoute = 0;
			}
		}
		// Sort Vectors by reverse date
		Collections.sort(dataEmbed, new Comparator<DataWorkflow>() {
			public int compare(DataWorkflow o1, DataWorkflow o2) {
				return (o2.dayOfYear - o1.dayOfYear);
			}
		});
		Collections.sort(dataMicrotomy, new Comparator<DataWorkflow>() {
			public int compare(DataWorkflow o1, DataWorkflow o2) {
				return (o2.dayOfYear - o1.dayOfYear);
			}
		});
		Collections.sort(dataSlides, new Comparator<DataWorkflow>() {
			public int compare(DataWorkflow o1, DataWorkflow o2) {
				return (o2.dayOfYear - o1.dayOfYear);
			}
		});
		Collections.sort(dataRoute, new Comparator<DataWorkflow>() {
			public int compare(DataWorkflow o1, DataWorkflow o2) {
				return (o2.dayOfYear - o1.dayOfYear);
			}
		});
	}
	
	private void readData() {
		if (parent.variables.busy.get()) return;
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}
	
	private void readFilters() {
		if (parent.variables.busy.get()) return;
		// Must initialize a new instance each time
		FilterWorker worker = new FilterWorker();
		worker.execute();
	}
	
	private void readTable() {
		parent.setBusy(true);
		int noDays = 0;
		Calendar calToday = Calendar.getInstance();
		Calendar calDate = Calendar.getInstance();
		ResultSet rst = parent.dbPowerJ.getHistology();
		DataCase thisCase = new DataCase();
		try {
			list.clear();
			// Read whole table
			parent.statusBar.setProgress(25);
			while (rst.next()) {
				thisCase = new DataCase();
				thisCase.statusID = rst.getByte("STATUS");
				thisCase.subspecialtyID = rst.getByte("SUBID");
				thisCase.procedureID = rst.getByte("PROID");
				thisCase.facilityID = rst.getShort("FACID");
				thisCase.noBlocks = rst.getShort("NOBLOCKS");
				thisCase.noSlides = rst.getShort("NOSLIDES");
				calDate.setTimeInMillis(rst.getTimestamp("GROSSED").getTime());
				noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
				thisCase.grossDays = (byte)noDays;
				if (thisCase.statusID > Constants.STATUS_Gross) {
					calDate.setTimeInMillis(rst.getTimestamp("EMBEDED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					if (noDays > Byte.MAX_VALUE) {
						noDays = Byte.MAX_VALUE;
					}
					thisCase.embedDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Embed) {
					calDate.setTimeInMillis(rst.getTimestamp("MICROED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					if (noDays > Byte.MAX_VALUE) {
						noDays = Byte.MAX_VALUE;
					}
					thisCase.microDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Microtomy) {
					calDate.setTimeInMillis(rst.getTimestamp("STAINED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					if (noDays > Byte.MAX_VALUE) {
						noDays = Byte.MAX_VALUE;
					}
					thisCase.stainDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Slides) {
					calDate.setTimeInMillis(rst.getTimestamp("ROUTED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					if (noDays > Byte.MAX_VALUE) {
						noDays = Byte.MAX_VALUE;
					}
					thisCase.routeDays = (byte)noDays;
				}
				list.add(thisCase);
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
		infoUpdate = "Next update: " +
				parent.dateUtils.formatter(parent.variables.nextUpdate,
				parent.dateUtils.FORMAT_DATETIME);
		parent.statusBar.setMessage(infoUpdate);
		timer.restart();
	}

	private class DataWorker extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws Exception {
			readTable();
			return null;
		}
		
		public void done() {
			// Display results
			readFilters();
		}
	}

	private class FilterWorker extends SwingWorker<Void, Void> {
		private ArrayList<DataWorkflow> dataEmbed = new ArrayList<DataWorkflow>();
		private ArrayList<DataWorkflow> dataMicrotomy = new ArrayList<DataWorkflow>();
		private ArrayList<DataWorkflow> dataSlides = new ArrayList<DataWorkflow>();
		private ArrayList<DataWorkflow> dataRoute = new ArrayList<DataWorkflow>();

		protected Void doInBackground() throws Exception {
			readCharts(dataEmbed, dataMicrotomy, dataSlides, dataRoute);
			return null;
		}
		
		public void done() {
			// Display results
			chartEmbed.setData(dataEmbed, Constants.STATUS_Gross);
			chartMicrotomy.setData(dataMicrotomy, Constants.STATUS_Embed);
			chartSlides.setData(dataSlides, Constants.STATUS_Microtomy);
			chartRoute.setData(dataRoute, Constants.STATUS_Slides);
			setNextUpdate();
		}
	}
	
	private class DataCase {
		byte statusID = 0;	// Case status: embedded, microtomy, slides, route
		byte subspecialtyID = 0;	// Breast, Cardiac, Derm, GI, GU, etc
		byte procedureID = 0;	// 1=Bx, 2=exc, 3=small, 4=large, 5=radical
		byte grossDays = 0;	// How many days ago the case was grossed
		byte embedDays = 0;	// How many days ago the case was embedded
		byte microDays = 0;
		byte stainDays = 0;
		byte routeDays = 0;
		short facilityID = 0;
		short noBlocks = 0;
		short noSlides = 0;
	}
}
