package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class PnlDashboard extends PnlMain {
	private static final long serialVersionUID = -1070986422117626518L;
	private final byte COL_ROW_NO = 0;
	private final byte COL_CASENO = 1;
	private final byte COL_ACCESSION = 2;
	private final byte COL_PROCEDURE = 3;
	private final byte COL_SUBSPECIALTY = 4;
	private final byte COL_STATUS = 5;
	private final byte COL_SPECIMEN = 6;
	private final byte COL_NOSPECS = 7;
	private final byte COL_NOBLOCKS = 8;
	private final byte COL_NOSLIDES = 9;
	private final byte COL_EXPECTED = 10;
	private final byte COL_SPENT = 11;
	private final byte COL_DELAY = 12;
	private final byte FILTER_FACILITY = 0;
	private final byte FILTER_STATUS = 1;
	private final byte FILTER_SUBSPECIAL = 2;
	private final byte FILTER_PROCEDURE = 3;
	private final byte FILTER_COLOR = 4;
	private final byte BIN_COLOR = 0;
	private final byte BIN_STATUS = 1;
	private final byte BIN_SPECIALTY = 2;
	private final byte CHART_PIE = 0;
	private final byte CHART_FLOW = 1;
	private final byte CHART_LOAD = 2;
	private int[] filters = {0, 0, 0, 0, 0};
	private String infoUpdate = "";
	private ChartPie chartPie;
	private ChartFlow chartFlow;
	private ChartLoad chartLoad;
	private ChartGauge chartGauge;
	private JTableEditor tblCases;
	private ModelCases mdlCases;
	private Timer timer;
	private ArrayList<DataCase> list = new ArrayList<DataCase>();

	PnlDashboard(PowerJ parent) {
		super(parent);
		setName("Dashboard");
		filters[FILTER_STATUS] = Constants.STATUS_All;
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
		Utilities.addComponent(scrollCases, 0, 0, 4, 4, 0.6, 0.5, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
		chartPie = new ChartPie();
		chartPie.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_PIE);
			}
		});
		Utilities.addComponent(chartPie, 4, 0, 2, 2, 0.4, 0.25, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
		chartFlow = new ChartFlow();
		chartFlow.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_FLOW);
			}
		});
		Utilities.addComponent(chartFlow, 4, 2, 2, 2, 0.4, 0.25, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
		chartLoad = new ChartLoad();
		chartLoad.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_LOAD);
			}
		});
		chartLoad.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				displayPersonnel(e.getX(), e.getY());
			}
		});
		Utilities.addComponent(chartLoad, 0, 4, 4, 4, 0.6, 0.5, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
		chartGauge = new ChartGauge();
		Utilities.addComponent(chartGauge, 4, 4, 2, 2, 0.4, 0.5, GridBagConstraints.BOTH,
				GridBagConstraints.EAST, panel);
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
		// Setup 4 JComboBox and fill with their data
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
		int flags = 0;
		if (parent.variables.userAccess[Constants.ACCESS_Gross]) {
			flags = 1;
		}
		if (parent.variables.userAccess[Constants.ACCESS_Histology]) {
			flags += 2;
		}
		if (parent.variables.userAccess[Constants.ACCESS_Diagnosis]) {
			flags += 4;
		}
		CboStatus cboStatus = new CboStatus(parent, flags);
		cboStatus.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			CboStatus cb = (CboStatus)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[FILTER_STATUS] = item.getValue();
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
		CboColor cboColor = new CboColor();
		cboColor.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			CboColor cb = (CboColor)e.getSource();
	    			filters[FILTER_COLOR] = cb.getSelectedIndex();
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
		if (flags == 1) {
			filters[FILTER_STATUS] = Constants.STATUS_Accession;
		} else if (flags == 4) {
			filters[FILTER_STATUS] = Constants.STATUS_Routed;
		} else {
			label = new JLabel("Status:");
			label.setFont(Constants.APP_FONT);
			label.setBorder(border);
			label.setDisplayedMnemonic(KeyEvent.VK_S);
			label.setLabelFor(cboStatus);
			panel.add(label);
			panel.add(cboStatus);
			if (flags == 2) {
				filters[FILTER_STATUS] = Constants.STATUS_Histology;
			}
		}
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
		label = new JLabel("Code:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_C);
		label.setLabelFor(cboColor);
		panel.add(label);
		panel.add(cboColor);
		return panel;
	}
	
	private void displayCase(int row) {
		if (parent.variables.offLine) return;
		if (parent.variables.busy.get()) return;
        if (row > -1) {
			// else, Selection got filtered away.
        	// Convert to model
			row = tblCases.convertRowIndexToModel(row);
			if (row > -1 && row < list.size()) {
				new DlgCase(parent,
					list.get(row).caseID,
					list.get(row).caseNo);
			}
        }
	}
	
	private void displayData(int x, int y, byte chartID) {
		if (parent.variables.busy.get()) return;
		String info = "", units = " Cases";
		switch (chartID) {
		case CHART_PIE:
			info = chartPie.getMessage(parent, x, y);
			break;
		case CHART_FLOW:
			info = chartFlow.getMessage(x, y);
			break;
		default:
			info = chartLoad.getMessage(x, y);
			units = " Units";
		}
		if (info.length() > 0) {
			info += units;
		} else {
			info = infoUpdate;
		}
		parent.statusBar.setMessage(info);
	}
	
	private void displayPersonnel(int x, int y) {
		if (!parent.variables.userAccess[Constants.ACCESS_ViewNames]) return;
		if (parent.variables.offLine) return;
		if (parent.variables.busy.get()) return;
		int prsID = chartLoad.getPersonID(x, y);
		if (prsID > 0) {
			new DlgPersonnel(parent, prsID);
		}
	}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("dashboard.pdf").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"No", "Case", "Accession",
				"Proc", "Splty", "Status", "Spec", "Specs",
				"Blk", "Sld", "Cutoff", "Passed", "%" };
		final float[] widths = {3, 3, 2, 2, 3, 3, 2, 1, 1, 2, 2, 2};
        String str = "Dashboard - " + parent.dateUtils.formatter(Calendar.getInstance(),
				parent.dateUtils.FORMAT_DATETIME);
		PdfFonts pdfLib = new PdfFonts();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
        Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		try {
			// Write the output to a file
			FileOutputStream fos = new FileOutputStream(fileName);
            PdfWriter.getInstance(document, fos);
            document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(Constants.LAB_NAME));
            document.add(paragraph);
            paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(headers.length -1);
			table.setWidthPercentage(100);
			table.setWidths(widths);
			// header row
			for (int i = 1; i < headers.length; i++) {
	            paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(headers[i]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			DataCase dataRow;
			int row = 0;
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				row = tblCases.convertRowIndexToModel(j);
				dataRow = list.get(row);
				for (int i = 1; i < headers.length; i++) {
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					switch (i) {
					case COL_CASENO:
						str = dataRow.caseNo;
						break;
					case COL_ACCESSION:
						str = parent.dateUtils.formatter(dataRow.calAccession, parent.dateUtils.FORMAT_DATETIME);
						break;
					case COL_PROCEDURE:
						str = dataRow.procedure;
						break;
					case COL_SUBSPECIALTY:
						str = dataRow.subspecialty;
						break;
					case COL_STATUS:
						str = dataRow.status;
						break;
					case COL_SPECIMEN:
						str = dataRow.mainSpecimen;
						if (str.length() > 8) {
							str = str.substring(0, 8);
						}
						break;
					case COL_NOSPECS:
						str = "" + dataRow.noSpecimens;
						break;
					case COL_NOBLOCKS:
						str = "" + dataRow.noBlocks;
						break;
					case COL_NOSLIDES:
						str = "" + dataRow.noSlides;
						break;
					case COL_EXPECTED:
						str = "" + dataRow.alloted;
						break;
					case COL_SPENT:
						str = parent.numbers.formatNumber(dataRow.spent);
						break;
					default:
						str = parent.numbers.formatNumber(dataRow.delay);
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell();
					switch (i) {
					case COL_ACCESSION:
					case COL_NOSPECS:
					case COL_NOBLOCKS:
					case COL_NOSLIDES:
					case COL_EXPECTED:
					case COL_SPENT:
					case COL_DELAY:
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					}
					cell.addElement(paragraph);
					table.addCell(cell);
				}
			}
			document.add(table);
            document.close();
        } catch (DocumentException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        } catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        }
	}

	private int readCharts(byte binID, ArrayList<DataPie> dataPie,
			ArrayList<DataWorkflow> dataWorkflow, ArrayList<DataWorkload> dataWorkload) {
		boolean includePie = true;
		final byte businessDays = 7;
		int tatHours = 0;
		int tatCases = 0, pendingCases = 0;
		DataCase thisCase = new DataCase();
		DataWorkflow thisDay = new DataWorkflow();
		Calendar calToday = Calendar.getInstance();
		for (int i = 0; i < businessDays; i++) {
			thisDay.dayOfYear = i;
			thisDay.date = parent.dateUtils.formatter(calToday,
					parent.dateUtils.FORMAT_DATESHORT);
			dataWorkflow.add(thisDay);
			thisDay = new DataWorkflow();
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
			includePie = false;
			switch (filters[FILTER_STATUS]) {
			case Constants.STATUS_Accession:
				if (thisCase.accesDays < businessDays) {
					dataWorkflow.get(thisCase.accesDays).noIn++;
				}
				if (thisCase.statusID > Constants.STATUS_Accession) {
					includePie = false;
					// Set turnaround time
					tatHours += thisCase.grossTAT;
					tatCases++;
					// Set workflow
					if (thisCase.grossDays < businessDays) {
						dataWorkflow.get(thisCase.grossDays).noOut++;
					}
					// Set workload
					if (thisCase.grossDays < 3) {
						setWorkload(thisCase.grossDays, thisCase.grossID,
								(1 + thisCase.noSpecimens + thisCase.noBlocks),
								thisCase.grossName, dataWorkload);
					}
				} else {
					includePie = true;
					pendingCases++;
				}
				break;
			case Constants.STATUS_Gross:
				if (thisCase.statusID > Constants.STATUS_Accession) {
					if (thisCase.grossDays < businessDays) {
						dataWorkflow.get(thisCase.grossDays).noIn++;
					}
					if (thisCase.statusID > Constants.STATUS_Gross) {
						includePie = false;
						tatHours += thisCase.embedTAT;
						tatCases++;
						if (thisCase.embedDays < businessDays) {
							dataWorkflow.get(thisCase.embedDays).noOut++;
						}
						if (thisCase.embedDays < 3) {
							setWorkload(thisCase.embedDays, thisCase.embedID,
									thisCase.noBlocks, thisCase.embedName, dataWorkload);
						}
					} else {
						includePie = true;
						pendingCases++;
					}
				}
				break;
			case Constants.STATUS_Embed:
				if (thisCase.statusID > Constants.STATUS_Gross) {
					if (thisCase.embedDays < businessDays) {
						dataWorkflow.get(thisCase.embedDays).noIn++;
					}
					if (thisCase.statusID > Constants.STATUS_Embed) {
						includePie = false;
						tatHours += thisCase.microTAT;
						tatCases++;
						if (thisCase.microDays < businessDays) {
							dataWorkflow.get(thisCase.microDays).noOut++;
						}
						if (thisCase.microDays < 3) {
							setWorkload(thisCase.microDays, thisCase.microID,
									thisCase.noSlides, thisCase.microName, dataWorkload);
						}
					} else {
						includePie = true;
						pendingCases++;
					}
				}
				break;
			case Constants.STATUS_Microtomy:
				if (thisCase.statusID > Constants.STATUS_Embed) {
					if (thisCase.microDays < businessDays) {
						dataWorkflow.get(thisCase.microDays).noIn++;
					}
					if (thisCase.statusID > Constants.STATUS_Microtomy) {
						includePie = false;
						tatHours += thisCase.stainTAT;
						tatCases++;
						if (thisCase.stainDays < businessDays) {
							dataWorkflow.get(thisCase.stainDays).noOut++;
						}
						if (thisCase.stainDays < 3) {
							setWorkload(thisCase.stainDays, thisCase.stainID,
									1, thisCase.stainName, dataWorkload);
						}
					} else {
						includePie = true;
						pendingCases++;
					}
				}
				break;
			case Constants.STATUS_Slides:
				if (thisCase.statusID > Constants.STATUS_Microtomy) {
					if (thisCase.stainDays < businessDays) {
						dataWorkflow.get(thisCase.stainDays).noIn++;
					}
					if (thisCase.statusID > Constants.STATUS_Slides) {
						includePie = false;
						tatHours += thisCase.routeTAT;
						tatCases++;
						if (thisCase.routeDays < businessDays) {
							dataWorkflow.get(thisCase.routeDays).noOut++;
						}
						if (thisCase.routeDays < 3) {
							setWorkload(thisCase.routeDays, thisCase.routeID,
									1, thisCase.routeName, dataWorkload);
						}
					} else {
						includePie = true;
						pendingCases++;
					}
				}
				break;
			case Constants.STATUS_Routed:
				if (thisCase.statusID > Constants.STATUS_Slides) {
					if (thisCase.routeDays < businessDays) {
						dataWorkflow.get(thisCase.routeDays).noIn++;
					}
					if (thisCase.statusID == Constants.STATUS_Final) {
						includePie = false;
						tatHours += thisCase.finalTAT;
						tatCases++;
						if (thisCase.finalDays < businessDays) {
							dataWorkflow.get(thisCase.finalDays).noOut++;
						}
						if (thisCase.finalDays < 3) {
							setWorkload(thisCase.finalDays, thisCase.finalID,
									(thisCase.noSpecimens + thisCase.noSlides),
									thisCase.finalName, dataWorkload);
						}
					} else {
						includePie = true;
						pendingCases++;
					}
				}
				break;
			case Constants.STATUS_Histology:
				if (thisCase.statusID > Constants.STATUS_Accession) {
					if (thisCase.grossDays < businessDays) {
						dataWorkflow.get(thisCase.grossDays).noIn++;
					}
					if (thisCase.statusID > Constants.STATUS_Slides) {
						includePie = false;
						tatHours += thisCase.routeTAT;
						tatCases++;
						if (thisCase.routeDays < businessDays) {
							dataWorkflow.get(thisCase.routeDays).noOut++;
						}
						if (thisCase.routeDays < 3) {
							setWorkload(thisCase.routeDays, thisCase.routeID,
									1, thisCase.routeName, dataWorkload);
						}
					} else {
						includePie = true;
						pendingCases++;
					}
					if (thisCase.statusID > Constants.STATUS_Gross) {
						if (thisCase.embedDays < 3) {
							setWorkload(thisCase.embedDays, thisCase.embedID,
									thisCase.noBlocks, thisCase.embedName, dataWorkload);
						}
					}
					if (thisCase.statusID > Constants.STATUS_Embed) {
						if (thisCase.microDays < 3) {
							setWorkload(thisCase.microDays, thisCase.microID,
									thisCase.noSlides, thisCase.microName, dataWorkload);
						}
					}
					if (thisCase.statusID > Constants.STATUS_Microtomy) {
						if (thisCase.stainDays < 3) {
							setWorkload(thisCase.stainDays, thisCase.stainID,
									1, thisCase.stainName, dataWorkload);
						}
					}
				}
				break;
			default:
				if (thisCase.accesDays < businessDays) {
					dataWorkflow.get(thisCase.accesDays).noIn++;
				}
				if (thisCase.statusID == Constants.STATUS_Final) {
					includePie = false;
					tatHours += thisCase.finalTAT;
					tatCases++;
					if (thisCase.finalDays < businessDays) {
						dataWorkflow.get(thisCase.finalDays).noOut++;
					}
					if (thisCase.finalDays < 3) {
						setWorkload(thisCase.finalDays, thisCase.finalID,
								(thisCase.noSpecimens + thisCase.noSlides),
								thisCase.finalName, dataWorkload);
					}
				} else {
					includePie = true;
					switch (filters[FILTER_COLOR]) {
					case 1:
						if (thisCase.delay < 101) {
							includePie = false;
						}
						break;
					case 2:
						if (thisCase.delay < 71
							|| thisCase.delay > 100) {
							includePie = false;
						}
						break;
					case 3:
						if (thisCase.delay > 70) {
							includePie = false;
						}
						break;
					default:
						includePie = true;
					}
					pendingCases++;
				}
			}
			if (includePie) {
				switch (binID) {
				case BIN_SPECIALTY:
					dataPie.get(thisCase.subspecialtyID).value++;
					break;
				case BIN_STATUS:
					dataPie.get(thisCase.statusID).value++;
					break;
				default:
					if (thisCase.delay > 100) {
						dataPie.get(0).value++;
					} else if (thisCase.delay > 70) {
						dataPie.get(1).value++;
					} else {
						dataPie.get(2).value++;
					}
				}
			}
		}
		// Calculate Turnaround time in business hours
		if (tatCases > 0) {
			tatHours = tatHours / tatCases;
		}
		// Calculate daily pending cases
		for (int i = 0; i < businessDays; i++) {
			thisDay = dataWorkflow.get(i);
			thisDay.noPending = pendingCases;
			pendingCases -= (thisDay.noIn - thisDay.noOut);
			if (pendingCases < 0) {
				pendingCases = 0;
			}
		}
		// Sort Workflow by reverse date
		Collections.sort(dataWorkflow, new Comparator<DataWorkflow>() {
			public int compare(DataWorkflow o1, DataWorkflow o2) {
				return (o2.dayOfYear - o1.dayOfYear);
			}
		});
		// Sort Workload by total cases descending
		Collections.sort(dataWorkload, new Comparator<DataWorkload>() {
			public int compare(DataWorkload o1, DataWorkload o2) {
				return ((o2.casesToday + o2.casesYesterday + o2.casesOld)
						- (o1.casesToday + o1.casesYesterday + o1.casesOld));
			}
		});
		if (!parent.variables.userAccess[Constants.ACCESS_ViewNames]) {
			// Hide Names at bottom
			String name = "P";
			if (filters[FILTER_STATUS] == Constants.STATUS_Accession) {
				// Hide assistants Name at bottom
				name = "A";
    		} else if (filters[FILTER_STATUS] < Constants.STATUS_Routed
    				|| filters[FILTER_STATUS] == Constants.STATUS_Histology) {
				// Hide histo tech Name at bottom
				name = "H";
    		}
			for (int i = 0; i < dataWorkload.size(); i++) {
				dataWorkload.get(i).name = name + (i + 1);
			}
		}
		return tatHours;
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
		ResultSet rst = parent.dbPowerJ.getDashboard();
		DataCase thisCase = new DataCase();
		try {
			parent.statusBar.setProgress(25);
			list.clear();
			// Read whole table
			while (rst.next()) {
				thisCase = new DataCase();
				thisCase.statusID = rst.getByte("STATUS");
				thisCase.subspecialtyID = rst.getByte("SUBID");
				thisCase.procedureID = rst.getByte("PROID");
				thisCase.noSpecimens = rst.getByte("NOSPECS");
				thisCase.facilityID = rst.getShort("FACID");
				thisCase.noBlocks = rst.getShort("NOBLOCKS");
				thisCase.noSlides = rst.getShort("NOSLIDES");
				thisCase.caseID = rst.getLong("CASEID");
				thisCase.caseNo = rst.getString("CASENO");
				thisCase.mainSpecimen = rst.getString("CODE");
				thisCase.subspecialty = rst.getString("SUBINIT");
				thisCase.calAccession.setTimeInMillis(rst.getTimestamp("ACCESSED").getTime());
				thisCase.procedure = DataProcedure.NAMES[thisCase.procedureID];
				thisCase.status = Constants.STATUS_NAMES[thisCase.statusID];
				thisCase.spent = parent.dateUtils.getBusinessHours(thisCase.calAccession, calToday);
				thisCase.alloted = rst.getShort("GROSS");
				noDays = parent.dateUtils.getBusinessDays(thisCase.calAccession, calToday);
				thisCase.accesDays = (byte)noDays;
				if (thisCase.statusID > Constants.STATUS_Accession) {
					thisCase.alloted += rst.getShort("EMBED");
					thisCase.grossID = rst.getShort("GROSSID");
					thisCase.grossTAT = rst.getShort("GROSSTAT");
					thisCase.grossName = rst.getString("GROSSINI").trim();
					calDate.setTimeInMillis(rst.getTimestamp("GROSSED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					thisCase.grossDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Gross) {
					thisCase.alloted += rst.getShort("MICROTOMY");
					thisCase.embedID = rst.getShort("EMBEDID");
					thisCase.embedTAT = rst.getShort("EMBEDTAT");
					thisCase.embedName = rst.getString("EMBEDINI").trim();
					calDate.setTimeInMillis(rst.getTimestamp("EMBEDED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					thisCase.embedDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Embed) {
					thisCase.alloted += rst.getShort("ROUTE");
					thisCase.microID = rst.getShort("MICROID");
					thisCase.microTAT = rst.getShort("MICROTAT");
					thisCase.microName = rst.getString("MICROINI").trim();
					calDate.setTimeInMillis(rst.getTimestamp("MICROED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					thisCase.microDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Microtomy) {
					thisCase.stainTAT = rst.getShort("STAINTAT");
					thisCase.stainID = rst.getShort("STAINID");
					thisCase.stainName = rst.getString("STAININI").trim();
					calDate.setTimeInMillis(rst.getTimestamp("STAINED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					thisCase.stainDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Slides) {
					thisCase.alloted += rst.getShort("SIGNOUT");
					thisCase.routeID = rst.getShort("ROUTEID");
					thisCase.routeTAT = rst.getShort("ROUTETAT");
					thisCase.routeName = rst.getString("ROUTEINI").trim();
					calDate.setTimeInMillis(rst.getTimestamp("ROUTED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					thisCase.routeDays = (byte)noDays;
				}
				if (thisCase.statusID > Constants.STATUS_Routed) {
					thisCase.finalID = rst.getShort("FINALID");
					thisCase.finalTAT = rst.getShort("FINALTAT");
					thisCase.finalName = rst.getString("FINALINI").trim();
					calDate.setTimeInMillis(rst.getTimestamp("FINALED").getTime());
					noDays = parent.dateUtils.getBusinessDays(calDate, calToday);
					thisCase.finalDays = (byte)noDays;
				}
				if (thisCase.alloted > 0) {
					thisCase.delay = (short) ((100 * thisCase.spent) / thisCase.alloted);
				}
				list.add(thisCase);
			}
			parent.statusBar.setProgress(75);
			Collections.sort(list, new Comparator<DataCase>() {
				public int compare(DataCase o1, DataCase o2) {
					return (o2.delay - o1.delay);
				}
			});
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
		}
	}

	private void setFilters() {
		int noFilters = 0;
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> lstFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_FACILITY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).facilityID == filters[FILTER_FACILITY]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[FILTER_SUBSPECIAL] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).subspecialtyID == filters[FILTER_SUBSPECIAL]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[FILTER_PROCEDURE] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).procedureID == filters[FILTER_PROCEDURE]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[FILTER_COLOR] > 0) {
			switch (filters[FILTER_COLOR]) {
			case 1:
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						int row = entry.getIdentifier();
						return (list.get(row).delay > 100);
					}
				};
				break;
			case 2:
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					public boolean include(
							Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						int row = entry.getIdentifier();
						return (list.get(row).delay > 70
								&& list.get(row).delay < 101);
					}
				};
				break;
			default:
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					public boolean include(
							Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						int row = entry.getIdentifier();
						return (list.get(row).delay < 71);
					}
				};
			}
			lstFilters.add(rowFilter);
			noFilters++;
		}
		// Filter by Status (always on)
		switch (filters[FILTER_STATUS]) {
		case Constants.STATUS_All:
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).statusID < Constants.STATUS_Final);
				}
			};
			break;
		case Constants.STATUS_Histology:
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).statusID > Constants.STATUS_Accession
							&& list.get(row).statusID < Constants.STATUS_Routed);
				}
			};
			break;
		case Constants.STATUS_Routed:
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).statusID > Constants.STATUS_Slides
							&& list.get(row).statusID < Constants.STATUS_Final);
				}
			};
			break;
		default:
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).statusID == filters[FILTER_STATUS]);
				}
			};
		}
		if (noFilters > 0) {
			// Add Status Filter to the compound filter
			lstFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(lstFilters);
		}
		@SuppressWarnings("unchecked")
		TableRowSorter<ModelCases> sorter = (TableRowSorter<ModelCases>) tblCases.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
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

	private void setWorkload(byte noDays, short personID, int noUnits, String personName,
			ArrayList<DataWorkload> dataWorkload) {
		boolean matched = false;
		DataWorkload thisPerson = new DataWorkload();
		for (int j = 0; j < dataWorkload.size(); j++) {
			thisPerson = dataWorkload.get(j);
			if (thisPerson.personID == personID) {
				matched = true;
				break;
			}
		}
		if (!matched) {
			thisPerson = new DataWorkload();
			thisPerson.personID = personID;
			thisPerson.name = personName;
			dataWorkload.add(thisPerson);
		}
		if (noDays == 0) {
			thisPerson.casesToday += noUnits;
		} else if (noDays == 1) {
			thisPerson.casesYesterday += noUnits;
		} else {
			thisPerson.casesOld += noUnits;
		}
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("dashboard.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		final String[] headers = {"No", "Case", "Accession",
				"Proc", "Splty", "Status", "Spec", "Specs",
				"Blk", "Sld", "Cutoff", "Passed", "%" };
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Dashboard");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Dashboard - " + parent.dateUtils.formatter(Calendar.getInstance(),
					parent.dateUtils.FORMAT_DATETIME));
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$L$1"));
			// header row
			row = sheet.createRow(1);
			row.setHeightInPoints(30);
			for (int i = 0; i < headers.length-1; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i+1]);
				cell.setCellStyle(styles.get("header"));
				if (i == 1 || i == 4) {
					sheet.setColumnWidth(i, 20 * 256); // 20 characters
				} else {
					sheet.setColumnWidth(i, 15 * 256); // 15 characters
				}
			}
			ColumnHelper colHelper = sheet.getColumnHelper();
			colHelper.setColDefaultStyle(0, styles.get("text"));
			colHelper.setColDefaultStyle(1, styles.get("datetime"));
			colHelper.setColDefaultStyle(2, styles.get("text"));
			colHelper.setColDefaultStyle(3, styles.get("text"));
			colHelper.setColDefaultStyle(4, styles.get("text"));
			colHelper.setColDefaultStyle(5, styles.get("text"));
			colHelper.setColDefaultStyle(6, styles.get("data_int"));
			colHelper.setColDefaultStyle(7, styles.get("data_int"));
			colHelper.setColDefaultStyle(8, styles.get("data_int"));
			colHelper.setColDefaultStyle(9, styles.get("data_int"));
			colHelper.setColDefaultStyle(10, styles.get("data_int"));
			colHelper.setColDefaultStyle(11, styles.get("data_int"));
			// data rows
			int rownum = 2;
			int i = 0;
			CellStyle dateStyle = styles.get("datetime");
			DataCase dataRow;
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				i = tblCases.convertRowIndexToModel(j);
				dataRow = list.get(i);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(dataRow.caseNo);
				cell = row.createCell(1);
				cell.setCellValue(dataRow.calAccession);
				cell.setCellStyle(dateStyle);
				cell = row.createCell(2);
				cell.setCellValue(dataRow.procedure);
				cell = row.createCell(3);
				cell.setCellValue(dataRow.subspecialty);
				cell = row.createCell(4);
				cell.setCellValue(dataRow.status);
				cell = row.createCell(5);
				cell.setCellValue(dataRow.mainSpecimen);
				cell = row.createCell(6);
				cell.setCellValue(dataRow.noSpecimens);
				cell = row.createCell(7);
				cell.setCellValue(dataRow.noBlocks);
				cell = row.createCell(8);
				cell.setCellValue(dataRow.noSlides);
				cell = row.createCell(9);
				cell.setCellValue(dataRow.alloted);
				cell = row.createCell(10);
				cell.setCellValue(dataRow.spent);
				cell = row.createCell(11);
				cell.setCellValue(dataRow.delay);
			}
			sheet.createFreezePane(1, 2);
			// Write the output to a file
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (Exception e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	class ModelCases extends AbstractTableModel {
		private static final long serialVersionUID = 8571434976929982796L;
		private final String[] columnNames = {"No", "Case", "Accession",
			"Proc", "Splty", "Status", "Spec", "Specs",
			"Blk", "Sld", "Cutoff", "Passed", "%" };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			Object returnValue = Object.class;
			try {
				// The filters occasionally throw an Array index out of range exception
				DataCase thisRow = list.get(row);
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
				case COL_STATUS:
					returnValue = thisRow.status;
					break;
				case COL_SUBSPECIALTY:
					returnValue = thisRow.subspecialty;
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

	private class DataWorker extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws Exception {
			readTable();
			return null;
		}
		
		public void done() {
			// Display results
			mdlCases.fireTableDataChanged();
			readFilters();
			setNextUpdate();
		}
	}

	private class FilterWorker extends SwingWorker<Void, Void> {
		private int tatHours = 0;
		private ArrayList<DataPie> dataPie = new ArrayList<DataPie>();
		private ArrayList<DataWorkflow> dataWorkflow = new ArrayList<DataWorkflow>();
		private ArrayList<DataWorkload> dataWorkload = new ArrayList<DataWorkload>();

		protected Void doInBackground() throws Exception {
			byte binID = BIN_COLOR;
			DataPie slice = new DataPie();
			if (filters[FILTER_COLOR] > 0) {
				if (filters[FILTER_SUBSPECIAL] == 0) {
					binID = BIN_SPECIALTY;
				} else if (filters[FILTER_STATUS] == 0) {
					binID = BIN_STATUS;
				}
			}
			switch (binID) {
			case BIN_SPECIALTY:
				for (int i = 0; i < Constants.SPECIALTY_COLORS.length; i++) {
					slice = new DataPie();
					slice.color = Constants.SPECIALTY_COLORS[i];
					slice.label = Constants.SPECIALTIES[i];
					dataPie.add(slice);
				}
				break;
			case BIN_STATUS:
				for (int i = 0; i < Constants.STATUS_COLORS.length; i++) {
					slice = new DataPie();
					slice.color = Constants.STATUS_COLORS[i];
					slice.label = Constants.STATUS_NAMES[i];
					dataPie.add(slice);
				}
				break;
			default:
				for (int i = 0; i < 3; i++) {
					slice = new DataPie();
					slice.color = Constants.CODE_COLORS[i];
					slice.label = Constants.CODE_NAMES[i];
					dataPie.add(slice);
				}
			}
			tatHours = readCharts(binID, dataPie, dataWorkflow, dataWorkload);
			return null;
		}
		
		public void done() {
			// Display results
			setFilters();
			chartPie.setData(dataPie, filters[FILTER_STATUS]);
			chartFlow.setData(dataWorkflow, filters[FILTER_STATUS]);
			chartLoad.setData(dataWorkload, filters[FILTER_STATUS]);
			chartGauge.setData(tatHours, filters[FILTER_STATUS]);
		}
	}
	
	private class DataCase {
		byte statusID = 0;	// Case status: accession, grossed, embedded, etc
		byte subspecialtyID = 0;	// Breast, Cardiac, Derm, GI, GU, etc
		byte procedureID = 0;	// 1=Bx, 2=exc, 3=small, 4=large, 5=radical
		byte noSpecimens = 0;
		byte accesDays = 0;	// How many days ago the case was accessioned
		byte grossDays = 0;	// How many days ago the case was grossed
		byte embedDays = 0;
		byte microDays = 0;
		byte stainDays = 0;
		byte routeDays = 0;
		byte finalDays = 0;
		short facilityID = 0;
		short finalID = 0;	// ID of pathologist who has the case
		short grossID = 0;	// ID of path assistant who grossed the case
		short embedID = 0;	// ID of personnel who embedded the blocks
		short microID = 0;	// ID of personnel who cut the blocks & created unstained slides
		short stainID = 0;	// ID of personnel who stained the slides & did the QC
		short routeID = 0;	// ID of personnel who routed the case
		short noBlocks = 0;
		short noSlides = 0;
		short alloted = 0;	// time in hours alloted for entire case at its current status
		short spent = 0;
		short delay = 0;
		short grossTAT = 0;
		short embedTAT = 0;
		short microTAT = 0;
		short stainTAT = 0;
		short routeTAT = 0;
		short finalTAT = 0;
		long caseID = 0;
		String caseNo = "";
		String mainSpecimen = "";
		String subspecialty = "";
		String procedure = "";
		String status = "";
		String grossName = "";
		String embedName = "";
		String microName = "";
		String stainName = "";
		String routeName = "";
		String finalName = "";
		Calendar calAccession = Calendar.getInstance();
	}
}
