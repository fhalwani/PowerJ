package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
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
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class PnlTracker extends PnlMain {
	private static final long serialVersionUID = -4834899271013616196L;
	private final byte COL_ROW_NO = 0;
	private final byte COL_EVNT_TIME = 1;
	private final byte COL_EVNT_GAP = 2;
	private final byte COL_EVNT_CASE = 3;
	private final byte COL_EVNT_MATERIAL = 4;
	private final byte COL_EVNT_LOCATION = 5;
	private final byte COL_EVNT_DESCRIPTION = 6;
	private int prsID = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	private String infoUpdate = "No rows ";
	private String prsName = "";
	private DbAPIS dbAP;
	private JTableEditor tblData;
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();

	PnlTracker(PowerJ parent) {
		super(parent);
		setName("Tracker");
		createPanel();
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareTracker();
			}
		}
		altered = true;
	}

	boolean close() {
		super.close();
		dataRows.clear();
		if (!parent.variables.offLine) {
			if (dbAP.connected) {
				dbAP.close();
			}
		}
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		tblData = new JTableEditor(parent, new DataModel());
		tblData.setName("tblData");
		TableColumn column = tblData.getColumnModel().getColumn(COL_ROW_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		column = tblData.getColumnModel().getColumn(COL_EVNT_GAP);
		column.setCellRenderer(new RendererInterval());
		tblData.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = ((JTable) e.getSource()).rowAtPoint(new Point(e.getX(), e.getY()));
		        displayCase(row);
			}
		});
		JScrollPane scrollCases = new JScrollPane(tblData,
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
		add(createToolbar(), BorderLayout.NORTH);
		add(scrollCases, BorderLayout.CENTER);
	}
	
	private JPanel createToolbar() {
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		Calendar calMin = Calendar.getInstance();
		calMin.set(Calendar.YEAR, 2011);
		calMin.set(Calendar.DAY_OF_YEAR, 1);
		calMin.set(Calendar.HOUR_OF_DAY, 0);
		calMin.set(Calendar.MINUTE, 0);
		calMin.set(Calendar.SECOND, 0);
		calMin.set(Calendar.MILLISECOND, 1);
		Calendar calMax = Calendar.getInstance();
		// Must include today to include all cases till midnight yesterday
		calMax.set(Calendar.HOUR_OF_DAY, 0);
		calMax.set(Calendar.MINUTE, 0);
		calMax.set(Calendar.SECOND, 0);
		calMax.set(Calendar.MILLISECOND, 0);
		Calendar calStart = Calendar.getInstance();
		calStart.add(Calendar.MONTH, -1);
		calStart.set(Calendar.DAY_OF_MONTH, 1);
		calStart.set(Calendar.HOUR_OF_DAY, 0);
		calStart.set(Calendar.MINUTE, 0);
		calStart.set(Calendar.SECOND, 0);
		calStart.set(Calendar.MILLISECOND, 1);
		Calendar calEnd = Calendar.getInstance();
		calEnd.set(Calendar.DAY_OF_MONTH, 1);
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		CboDate cboStart = new CboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		cboStart.addAncestorListener(new RequestFocusListener());
		cboStart.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					if (timeFrom != cal.getTimeInMillis()) {
						timeFrom = cal.getTimeInMillis();
						altered = true;
					}
				}
			}

		});
		JLabel label = new JLabel("From:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(cboStart);
		panel.add(label);
		panel.add(cboStart);
		CboDate cboEnd = new CboDate(calEnd, calMin, calMax);
		cboEnd.setName("cboEnd");
		cboEnd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					if (timeTo != cal.getTimeInMillis()) {
						timeTo = cal.getTimeInMillis();
						altered = true;
					}
				}
			}

		});
		label = new JLabel("To:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_T);
		label.setLabelFor(cboEnd);
		panel.add(label);
		panel.add(cboEnd);
		CboPathologists cboStaff = new CboPathologists(parent, true);
		cboStaff.setName("cboStaff");
		cboStaff.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			if (prsID != item.getValue()) {
	    				prsID = item.getValue();
						altered = true;
	    			}
				}
			}

		});
		DataItem item = (DataItem) cboStaff.getItemAt(0);
		prsID = item.getValue();
		label = new JLabel("Staff:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboStaff);
		panel.add(label);
		panel.add(cboStaff);
		JButton btnGo = new JButton("Go");
		btnGo.setMnemonic(KeyEvent.VK_G);
		btnGo.setIcon(Utilities.getIcon("go"));
		btnGo.setFocusable(true);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (altered) {
					setData();
				}
			}
		});
		panel.add(btnGo);
		return panel;
	}
	
	private void displayCase(int row) {
		if (parent.variables.offLine) return;
		if (parent.variables.busy.get()) return;
        if (row > -1) {
			// else, Selection got filtered away.
        	// Convert to model
			row = tblData.convertRowIndexToModel(row);
			if (row > -1 && row < dataRows.size()) {
				new DlgCase(parent,
						dataRows.get(row).caseID,
						dataRows.get(row).caseNo);
			}
        }
	}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("tracker.pdf").trim();
		if (fileName.trim().length() == 0) return;
		boolean highlight = false;
		final String[] columHeaders = {"Time", "Interval", "Case No", "Material", "Location", "Description"};
		final float[] columnWidths = {1, 1, 1.2f, 1, 1, 3};
		long seconds = 0, minutes = 0, hours = 0;
		DataRow dataRow;
		PdfFonts pdfLib = new PdfFonts();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document();
        Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		try {
			// Write the output to a file
			FileOutputStream out = new FileOutputStream(fileName);
            PdfWriter.getInstance(document, out);
            document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(Constants.LAB_NAME));
            document.add(paragraph);
            String str = prsName + " Events: "
					+ parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE);
            paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(columHeaders.length);
			table.setWidthPercentage(100);
			table.setWidths(columnWidths);
			for (int i = 0; i < columHeaders.length; i++) {
	            paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(columHeaders[i]));
				cell = new PdfPCell(paragraph);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			for (int j = 0; j < dataRows.size()-1; j++) {
				dataRow = dataRows.get(j);
				for (int i = 0; i < columHeaders.length; i++) {
					highlight = false;
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					paragraph.setAlignment(Element.ALIGN_CENTER);
					switch (i) {
					case 0:
						str = parent.dateUtils.formatter(dataRow.calDate, parent.dateUtils.FORMAT_DATETIME);
						break;
					case 1:
						str = "";
						if (i == 0) {
							str = "";
						} if (dataRow.gap < 28800000) {
							seconds = (dataRow.gap / 1000) % 60;
							minutes = (dataRow.gap / 60000) % 60;
							hours = dataRow.gap / 3600000;
							if (hours > 0) {
								str = hours + ":";
								if (minutes > 9) {
									str += minutes + ":";
								} else {
									str += "0" + minutes + ":";
								}
							} else if (minutes > 0) {
								str = minutes + ":";
							}
							if (seconds > 9) {
								str += seconds;
							} else if (hours > 0 || minutes > 0) {
								str += "0" + seconds;
							} else {
								str += seconds;
							}
						} else {
							// Next work shift (>8hours) marker
							str = "--";
							highlight = true;
						}
						break;
					case 2:
						str = dataRow.caseNo;
						break;
					case 3:
						str = dataRow.material;
						break;
					case 4:
						str = dataRow.location;
						break;
					default:
						str = dataRow.description;
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell();
					switch (i) {
					case COL_EVNT_CASE:
					case COL_EVNT_MATERIAL:
					case COL_EVNT_LOCATION:
					case COL_EVNT_DESCRIPTION:
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						if (highlight) {
							// Next work shift (>8hours) marker
							cell.setBackgroundColor(BaseColor.RED);
						}
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
	
	private void setData() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}
	
	
	void xls() {
		String fileName = ((PJClient)parent).getFileXls("tracker.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		final String[] headers = {"Time", "Interval", "Case", "Material", "Location", "Description"};
		long seconds = 0, minutes = 0, hours = 0;
		String interval = "";
		DataRow dataRow;
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Events");
			sheet.setFitToPage(true);
			sheet.setHorizontallyCenter(true);
			sheet.createFreezePane(0, 2);
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue(prsName + " Events: "
					+ parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE));
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$D$1"));
			// header row
			row = sheet.createRow(1);
			row.setHeightInPoints(40);
			for (int i = 0; i < headers.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(styles.get("header"));
				// Set column widths
				if (i == headers.length -1) {
					sheet.setColumnWidth(i, 55 * 256); // 55 characters
				} else
					sheet.setColumnWidth(i, 15 * 256); // 15 characters
			}
			ColumnHelper colHelper = sheet.getColumnHelper();
			colHelper.setColDefaultStyle(0, styles.get("datetime"));
			colHelper.setColDefaultStyle(1, styles.get("text"));
			colHelper.setColDefaultStyle(2, styles.get("text"));
			colHelper.setColDefaultStyle(3, styles.get("left"));
			colHelper.setColDefaultStyle(4, styles.get("left"));
			colHelper.setColDefaultStyle(5, styles.get("left"));
			int rownum = 2;
			CellStyle dateStyle = styles.get("datetime");
			for (int i = 0; i < dataRows.size()-1; i++) {
				dataRow = dataRows.get(i);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(dataRow.calDate.getTime());
				cell.setCellStyle(dateStyle);
				cell = row.createCell(1);
				interval = "";
				if (i > 0) {
					if (dataRow.gap < 28800000) {
						seconds = (dataRow.gap / 1000) % 60;
						minutes = (dataRow.gap / 60000) % 60;
						hours = dataRow.gap / 3600000;
						if (hours > 0) {
							if (hours > 99) {
								hours = 99;
							}
							interval += hours + ":";
							if (minutes > 9) {
								interval += minutes + ":";
							} else {
								interval += "0" + minutes + ":";
							}
						} else if (minutes > 0) {
							interval += minutes + ":";
						}
						if (seconds > 9) {
							interval += seconds;
						} else if (hours > 0 || minutes > 0) {
							interval += "0" + seconds;
						} else {
							interval += seconds;
						}
					} else {
						// Next work shift (>8hours) marker
						interval = "--";
						cell.setCellStyle(styles.get("textHighlight"));
					}
					cell.setCellValue(interval);
				}
				cell = row.createCell(2);
				cell.setCellValue(dataRow.caseNo);
				cell = row.createCell(3);
				cell.setCellValue(dataRow.material);
				cell = row.createCell(4);
				cell.setCellValue(dataRow.location);
				cell = row.createCell(5);
				cell.setCellValue(dataRow.description);
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
	
	class DataModel extends AbstractTableModel {
		private static final long serialVersionUID = 1983579004968510141L;
		private final String[] columHeaders = {"Row", "Time", "Interval",
				"Case No", "Material", "Location", "Description"};
		
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
				returnValue = dataRow.calDate;
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
		int caseID = 0;
		long gap = 0;
		Calendar calDate = Calendar.getInstance();
		String material = "";
		String location = "";
		String description = "";
		String caseNo = "";
	}
	
	private class DataWorker extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
			setName("TrackerWorker");
			if (!parent.variables.offLine) {
				if (dbAP.connected) {
					if (altered && timeTo > timeFrom && prsID > 0) {
						readTable();
						readPerson();
					}
				}
			}
			return null;
		}
		
		private void readTable() {
			String strMaterial = "";
			DataRow dataRow = new DataRow();
			PreparedStatement stm = null;
			ResultSet rst = null;
			try {
				dataRows.clear();
				stm = dbAP.getStatement(1);
				stm.setTimestamp(1, new Timestamp(timeFrom));
				stm.setTimestamp(2, new Timestamp(timeTo));
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
					dataRow.calDate.setTimeInMillis(rst.getTimestamp("event_date").getTime());
					dataRow.location = rst.getString("event_location");
					dataRow.description = rst.getString("event_description");
					dataRow.caseNo = rst.getString("accession_no");
					dataRows.add(dataRow);
				}
				rst.close();
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				stm = dbAP.getStatement(2);
				stm.setTimestamp(1, new Timestamp(timeFrom));
				stm.setTimestamp(2, new Timestamp(timeTo));
				stm.setInt(3, prsID);
				rst = stm.executeQuery();
				while (rst.next()) {
					dataRow = new DataRow();
					dataRow.calDate.setTimeInMillis(rst.getTimestamp("created_date").getTime());
					dataRow.material = "Orders";
					dataRow.description = "Ordered " + rst.getString("code");
					dataRow.caseNo = rst.getString("accession_no");
					dataRows.add(dataRow);
				}
				rst.close();
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				stm = dbAP.getStatement(3);
				stm.setTimestamp(1, new Timestamp(timeFrom));
				stm.setTimestamp(2, new Timestamp(timeTo));
				stm.setInt(3, prsID);
				rst = stm.executeQuery();
				while (rst.next()) {
					dataRow = new DataRow();
					dataRow.material = "Case";
					dataRow.calDate.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
					dataRow.description = "Completed " + rst.getString("description");
					dataRow.caseNo = rst.getString("accession_no");
					dataRows.add(dataRow);
				}
				rst.close();
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				// Sort by time descending
				Collections.sort(dataRows, new Comparator<DataRow>() {
					public int compare(DataRow o1, DataRow o2) {
						if (o1.calDate.getTimeInMillis() > o2.calDate.getTimeInMillis())
							return 1;
						else if (o2.calDate.getTimeInMillis() > o1.calDate.getTimeInMillis())
							return -1;
						else
							return 0;
					}
				});
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				for (int i = 0; i < dataRows.size()-1; i++) {
					dataRow = dataRows.get(i);
					dataRow.gap = dataRow.calDate.getTimeInMillis()
						- dataRows.get(i+1).calDate.getTimeInMillis();
				}
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
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
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				dbAP.closeRst(rst);
			}
		}
		
		public void done() {
			altered = false;
			parent.setBusy(false);
			// Display results
			((AbstractTableModel) tblData.getModel()).fireTableDataChanged();
			infoUpdate = prsName + ": " + dataRows.size() + " events";
			parent.statusBar.setMessage(infoUpdate);
		}
	}
}
