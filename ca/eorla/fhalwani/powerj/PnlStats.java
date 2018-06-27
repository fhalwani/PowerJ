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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class PnlStats extends PnlMain {
	private static final long serialVersionUID = 2410335359965838242L;
	private final byte DATA_CASENO = 0;
	private final byte DATA_ACCESSION = 1;
	private final byte DATA_GROSSDATE = 2;
	private final byte DATA_ROUTEDATE = 3;
	private final byte DATA_FINALDATE = 4;
	private final byte DATA_FACILITY = 5;
	private final byte DATA_SPECIALTY = 6;
	private final byte DATA_SUBSPECIALTY = 7;
	private final byte DATA_PROCEDURE = 8;
	private final byte DATA_SPECIMEN = 9;
	private final byte DATA_STAFF = 10;
	private final byte DATA_NOSPECS = 11;
	private final byte DATA_NOBLOCKS = 12;
	private final byte DATA_NOSLIDES = 13;
	private final byte DATA_NOHE = 14;
	private final byte DATA_NOSS = 15;
	private final byte DATA_NOIHC = 16;
	private final byte DATA_NOMOL = 17;
	private final byte DATA_NOFSPECS = 18;
	private final byte DATA_NOFBLOCKS = 19;
	private final byte DATA_NOFSLIDES = 20;
	private final byte DATA_NOSYN = 21;
	private final byte DATA_TATGROSS = 22;
	private final byte DATA_TATROUTE = 23;
	private final byte DATA_TATFINAL = 24;
	private final byte DATA_TATTOTAL = 25;
	private AtomicBoolean rowsChanged = new AtomicBoolean(true);
	private AtomicBoolean colsChanged = new AtomicBoolean(true);
	private int[] filters = {0, 0, 0, 0};
	private boolean[] colsView = {true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true};
	private long timeFrom = 0;
	private long timeTo = 0;
	private String infoUpdate = "No rows ";
	private JTableEditor tblData;
	private JTableColumnModel columnModel = new JTableColumnModel();
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();
	
	PnlStats(PowerJ parent) {
		super(parent);
		setName("Stats");
		createPanel();
	}

	boolean close() {
		dataRows.clear();
		super.close();
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		add(createToolbar(), BorderLayout.NORTH);
		ModelData mdl = new ModelData();
		tblData = new JTableEditor(parent, mdl);
		tblData.setColumnModel(columnModel);
		tblData.createDefaultColumnsFromModel();
		tblData.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int viewRow = ((JTable) e.getSource()).rowAtPoint(new Point(e.getX(), e.getY()));
		        displayCase(viewRow);
			}
		});
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JScrollPane scrollPane = new JScrollPane(tblData,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(border);
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8334954865866544843L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(scrollPane, BorderLayout.CENTER);
	}

	private JPanel createToolbar() {
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		Calendar calMin = Calendar.getInstance();
		// January 1, 2011
		calMin.set(Calendar.YEAR, 2011);
		calMin.set(Calendar.DAY_OF_YEAR, 1);
		calMin.set(Calendar.HOUR_OF_DAY, 0);
		calMin.set(Calendar.MINUTE, 0);
		calMin.set(Calendar.SECOND, 0);
		calMin.set(Calendar.MILLISECOND, 0);
		timeFrom = calMin.getTimeInMillis();
		Calendar calStart = Calendar.getInstance();
		calStart.setTimeInMillis(timeFrom);
		Calendar calMax = Calendar.getInstance();
		// Must include today to include all cases till midnight yesterday
		calMax.set(Calendar.HOUR_OF_DAY, 0);
		calMax.set(Calendar.MINUTE, 0);
		calMax.set(Calendar.SECOND, 0);
		calMax.set(Calendar.MILLISECOND, 0);
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(timeFrom);
		calEnd.add(Calendar.YEAR, 1);
		timeTo = calEnd.getTimeInMillis();
		if (timeTo > calMax.getTimeInMillis()) {
			timeTo = calMax.getTimeInMillis();
			calEnd.setTimeInMillis(timeTo);
		}
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
						rowsChanged.set(true);
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
						rowsChanged.set(true);
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
		CboFacilities cboFacilities = new CboFacilities(parent);
		cboFacilities.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[0] = item.getValue();
					rowsChanged.set(true);
	    		}
	        }
	    });
		label = new JLabel("Facility:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(cboFacilities);
		panel.add(label);
		panel.add(cboFacilities);
		CboSpecialties cboSpecialties = new CboSpecialties(parent, false);
		cboSpecialties.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[1] = item.getValue();
					rowsChanged.set(true);
	    		}
	        }
	    });
		label = new JLabel("Specialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboSpecialties);
		panel.add(label);
		panel.add(cboSpecialties);
		CboSubspecial cboSubspecial = new CboSubspecial(parent, false);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[2] = item.getValue();
					rowsChanged.set(true);
	    		}
	        }
	    });
		label = new JLabel("Subspecialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_B);
		label.setLabelFor(cboSubspecial);
		panel.add(label);
		panel.add(cboSubspecial);
		CboProcedures cboProcedures = new CboProcedures(parent, false);
		cboProcedures.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[3] = item.getValue();
					rowsChanged.set(true);
	    		}
	        }
	    });
		label = new JLabel("Procedure:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setDisplayedMnemonicIndex(1);
		label.setLabelFor(cboProcedures);
		panel.add(label);
		panel.add(cboProcedures);
		CboCases cboCols = new CboCases(colsView, parent);
		cboCols.setName("cboCols");
		cboCols.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboCases cbo = (CboCases) e.getSource();
					boolean[] newValue = cbo.getValue();
					if (!colsView.equals(newValue)) {
						for (int i = 0; i < colsView.length; i++) {
							colsView[i] = newValue[i];
						}
						colsChanged.set(true);
					}
				}
			}
		});
		label = new JLabel("Columns:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_C);
		label.setLabelFor(cboCols);
		panel.add(label);
		panel.add(cboCols);
		JButton btnGo = new JButton("Go");
		btnGo.setMnemonic(KeyEvent.VK_G);
		btnGo.setIcon(Utilities.getIcon("go"));
		btnGo.setFocusable(true);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rowsChanged.get()) {
					setData();
				} else if (colsChanged.get()) {
					setColumns();
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
		String fileName = ((PJClient)parent).getFilePdf("cases.pdf").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"Case", "Accession", "Gross", "Route",
				"Final", "Facility", "Specialty", "Subspecial", "Procedure", "Specimen",
				"Staff", "Specs", "Blocks", "Slides", "H&E", "SS", "IHC", "Mol", "FSS",
				"FSB", "FSL", "Synop", "tatG", "tatH", "tatF", "tatT"};
        String str = "Cases - " + parent.dateUtils.formatter(Calendar.getInstance(),
				parent.dateUtils.FORMAT_DATETIME);
		ArrayList<XlsCol> xlsCols = new ArrayList<XlsCol>();
		XlsCol xlsCol = new XlsCol(0, 0, headers[0]);
		try {
			xlsCols.add(xlsCol);
			for (int i = 1; i < headers.length; i++) {
				if (colsView[i-1]) {
					xlsCol = new XlsCol(i, 1, headers[i]);
					xlsCols.add(xlsCol);
				}
			}
			int noCols = xlsCols.size();
			float[] widths = new float[noCols];
			for (int i = 0; i < noCols; i++) {
				widths[i] = 1f;
			}
			PdfFonts pdfLib = new PdfFonts();
			HashMap<String, Font> fonts = pdfLib.getFonts();
			Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
			if (noCols < 11) {
				// Switch from Landscape to Portrait
				document = new Document(PageSize.LETTER, 36, 36, 36, 36);
			}
	        Paragraph paragraph = new Paragraph();
			PdfPCell cell = new PdfPCell();
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
			PdfPTable table = new PdfPTable(noCols);
			table.setWidthPercentage(100);
			table.setWidths(widths);
			// header row
			for (int i = 0; i < noCols; i++) {
				xlsCol = xlsCols.get(i);
				str = xlsCol.name;
	            paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(str));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			boolean alignRight = true;
			int row = 0;
			DataRow dataRow;
			for (int j = 0; j < tblData.getRowCount(); j++) {
				row = tblData.convertRowIndexToModel(j);
				dataRow = dataRows.get(row);
				for (int i = 0; i < xlsCols.size(); i++) {
					xlsCol = xlsCols.get(i);
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					alignRight = true;
					switch (xlsCol.id) {
					case DATA_CASENO:
						str = dataRow.caseNo;
						alignRight = false;
						break;
					case DATA_ACCESSION:
						str = parent.dateUtils.formatter(dataRow.calAccession, parent.dateUtils.FORMAT_DATETIME);
						break;
					case DATA_GROSSDATE:
						str = parent.dateUtils.formatter(dataRow.calGross, parent.dateUtils.FORMAT_DATETIME);
						break;
					case DATA_ROUTEDATE:
						str = parent.dateUtils.formatter(dataRow.calRoute, parent.dateUtils.FORMAT_DATETIME);
						break;
					case DATA_FINALDATE:
						str = parent.dateUtils.formatter(dataRow.calFinal, parent.dateUtils.FORMAT_DATETIME);
						break;
					case DATA_FACILITY:
						str = dataRow.facility;
						alignRight = false;
						break;
					case DATA_SPECIALTY:
						str = dataRow.specialty;
						alignRight = false;
						break;
					case DATA_SUBSPECIALTY:
						str = dataRow.subspecialty;
						alignRight = false;
						break;
					case DATA_PROCEDURE:
						str = dataRow.procedure;
						alignRight = false;
						break;
					case DATA_STAFF:
						str = dataRow.staff;
						alignRight = false;
						break;
					case DATA_SPECIMEN:
						str = dataRow.specimen;
						alignRight = false;
						break;
					case DATA_NOSPECS:
						str = "" + dataRow.noSpecimens;
						break;
					case DATA_NOBLOCKS:
						str = "" + dataRow.noBlocks;
						break;
					case DATA_NOSLIDES:
						str = "" + dataRow.noSlides;
						break;
					case DATA_NOHE:
						str = "" + dataRow.noHE;
						break;
					case DATA_NOSS:
						str = "" + dataRow.noSS;
						break;
					case DATA_NOIHC:
						str = "" + dataRow.noIHC;
						break;
					case DATA_NOMOL:
						str = "" + dataRow.noMOL;
						break;
					case DATA_NOFSPECS:
						str = "" + dataRow.noFSpecs;
						break;
					case DATA_NOFBLOCKS:
						str = "" + dataRow.noFB;
						break;
					case DATA_NOFSLIDES:
						str = "" + dataRow.noFL;
						break;
					case DATA_NOSYN:
						str = "" + dataRow.noSynoptics;
						break;
					case DATA_TATGROSS:
						str = "" + dataRow.tatGross;
						break;
					case DATA_TATROUTE:
						str = "" + dataRow.tatRoute;
						break;
					case DATA_TATFINAL:
						str = "" + dataRow.tatFinal;
						break;
					case DATA_TATTOTAL:
						str = "" + dataRow.tatTotal;
						break;
					default:
						str = "";
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell();
					if (alignRight) {
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					} else {
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

	private void setColumns() {
		int noCols = columnModel.getColumnCount(false);
		TableColumn column = new TableColumn();
		for (int i = noCols -1; i > 0; i--) {
			column = columnModel.getColumnByModelIndex(i);
			columnModel.setColumnVisible(column, colsView[i-1]);
		}
		colsChanged.set(false);
	}

	private void setData() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("cases.xlsx").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"Case", "Accession", "Gross", "Route",
				"Final", "Facility", "Specialty", "Subspecial", "Procedure", "Specimen",
				"Staff", "Specs", "Blocks", "Slides", "H&E", "SS", "IHC", "Mol", "FSS",
				"FSB", "FSL", "Synop", "tatG", "tatH", "tatF", "tatT"};
		ArrayList<XlsCol> xlsCols = new ArrayList<XlsCol>();
		XlsCol xlsCol = new XlsCol(0, 0, headers[0]);
		try {
			xlsCols.add(xlsCol);
			for (int i = 1; i < headers.length; i++) {
				if (colsView[i-1]) {
					if (i > 1 && i < 6) {
						// Dates
						xlsCol = new XlsCol(i, 3, headers[i]);
						xlsCols.add(xlsCol);
					} else {
						// Integers
						xlsCol = new XlsCol(i, 1, headers[i]);
						xlsCols.add(xlsCol);
					}
				}
			}
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Cases");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Cases - " + parent.dateUtils.formatter(Calendar.getInstance(),
					parent.dateUtils.FORMAT_DATETIME));
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, xlsCols.size()-1));
			// header row
			row = sheet.createRow(1);
			ColumnHelper colHelper = sheet.getColumnHelper();
			for (int i = 0; i < xlsCols.size(); i++) {
				xlsCol = xlsCols.get(i);
				cell = row.createCell(i);
				switch (xlsCol.type) {
				case 1:
					colHelper.setColDefaultStyle(i, styles.get("data_int"));
					break;
				case 3:
					colHelper.setColDefaultStyle(i, styles.get("datetime"));
					break;
				default:
					colHelper.setColDefaultStyle(i, styles.get("text"));
				}
				cell.setCellValue(xlsCol.name);
				cell.setCellStyle(styles.get("header"));
			}
			// data rows
			int rownum = 2;
			int k = 0;
			CellStyle dateStyle = styles.get("datetime");
			DataRow dataRow;
			for (int j = 0; j < tblData.getRowCount(); j++) {
				k = tblData.convertRowIndexToModel(j);
				dataRow = dataRows.get(k);
				for (int i = 0; i < xlsCols.size(); i++) {
					xlsCol = xlsCols.get(i);
					row = sheet.createRow(rownum++);
					cell = row.createCell(i);
					switch (xlsCol.id) {
					case DATA_CASENO:
						cell.setCellValue(dataRow.caseNo);
						break;
					case DATA_ACCESSION:
						cell.setCellValue(dataRow.calAccession);
						cell.setCellStyle(dateStyle);
						break;
					case DATA_GROSSDATE:
						cell.setCellValue(dataRow.calGross);
						cell.setCellStyle(dateStyle);
						break;
					case DATA_ROUTEDATE:
						cell.setCellValue(dataRow.calRoute);
						cell.setCellStyle(dateStyle);
						break;
					case DATA_FINALDATE:
						cell.setCellValue(dataRow.calFinal);
						cell.setCellStyle(dateStyle);
						break;
					case DATA_FACILITY:
						cell.setCellValue(dataRow.facility);
						break;
					case DATA_SPECIALTY:
						cell.setCellValue(dataRow.specialty);
						break;
					case DATA_SUBSPECIALTY:
						cell.setCellValue(dataRow.subspecialty);
						break;
					case DATA_PROCEDURE:
						cell.setCellValue(dataRow.procedure);
						break;
					case DATA_STAFF:
						cell.setCellValue(dataRow.staff);
						break;
					case DATA_SPECIMEN:
						cell.setCellValue(dataRow.specimen);
						break;
					case DATA_NOSPECS:
						cell.setCellValue(dataRow.noSpecimens);
						break;
					case DATA_NOBLOCKS:
						cell.setCellValue(dataRow.noBlocks);
						break;
					case DATA_NOSLIDES:
						cell.setCellValue(dataRow.noSlides);
						break;
					case DATA_NOHE:
						cell.setCellValue(dataRow.noHE);
						break;
					case DATA_NOSS:
						cell.setCellValue(dataRow.noSS);
						break;
					case DATA_NOIHC:
						cell.setCellValue(dataRow.noIHC);
						break;
					case DATA_NOMOL:
						cell.setCellValue(dataRow.noMOL);
						break;
					case DATA_NOFSPECS:
						cell.setCellValue(dataRow.noFSpecs);
						break;
					case DATA_NOFBLOCKS:
						cell.setCellValue(dataRow.noFB);
						break;
					case DATA_NOFSLIDES:
						cell.setCellValue(dataRow.noFL);
						break;
					case DATA_NOSYN:
						cell.setCellValue(dataRow.noSynoptics);
						break;
					case DATA_TATGROSS:
						cell.setCellValue(dataRow.tatGross);
						break;
					case DATA_TATROUTE:
						cell.setCellValue(dataRow.tatRoute);
						break;
					case DATA_TATFINAL:
						cell.setCellValue(dataRow.tatFinal);
						break;
					case DATA_TATTOTAL:
						cell.setCellValue(dataRow.tatTotal);
						break;
					default:
						cell.setCellValue("!");
					}
				}
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

	private class DataRow {
		byte noSynoptics = 0;
		byte noSpecimens = 0;
		byte noFSpecs = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short noHE = 0;
		short noSS = 0;
		short noIHC = 0;
		short noMOL = 0;
		short noFB = 0;
		short noFL = 0;
		short tatGross = 0;
		short tatRoute = 0;
		short tatFinal = 0;
		short tatTotal = 0;
		long caseID = 0;
		Calendar calAccession = Calendar.getInstance();
		Calendar calGross = Calendar.getInstance();
		Calendar calRoute = Calendar.getInstance();
		Calendar calFinal = Calendar.getInstance();
		String caseNo = "";
		String staff = "";
		String specialty = "";
		String facility = "";
		String subspecialty = "";
		String procedure = "";
		String specimen = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = -4712162856466652236L;
		private final String[] columns = {"Case", "Accession", "Gross", "Route",
				"Final", "Facility", "Specialty", "Subspecial", "Procedure", "Specimen",
				"Staff", "Specs", "Blocks", "Slides", "H&E", "SS", "IHC", "Mol", "FSS",
				"FSB", "FSL", "Synop", "tatG", "tatH", "tatF", "tatT"};
		private final Class<?>[]  cTypes = {String.class, Calendar.class, Calendar.class,
				Calendar.class, Calendar.class, String.class, String.class, String.class,
				String.class, String.class, String.class, Integer.class, Integer.class,
				Integer.class, Integer.class, Integer.class, Integer.class, Integer.class,
				Integer.class, Integer.class, Integer.class, Integer.class, Integer.class,
				Integer.class, Integer.class, Integer.class};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return dataRows.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Class<?> getColumnClass(int col) {
			return cTypes[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (row < dataRows.size()) {
				DataRow thisRow = dataRows.get(row);
				switch (col) {
				case DATA_CASENO:
					return thisRow.caseNo;
				case DATA_ACCESSION:
					return thisRow.calAccession;
				case DATA_GROSSDATE:
					return thisRow.calGross;
				case DATA_ROUTEDATE:
					return thisRow.calRoute;
				case DATA_FINALDATE:
					return thisRow.calFinal;
				case DATA_FACILITY:
					return thisRow.facility;
				case DATA_SPECIALTY:
					return thisRow.specialty;
				case DATA_SUBSPECIALTY:
					return thisRow.subspecialty;
				case DATA_PROCEDURE:
					return thisRow.procedure;
				case DATA_STAFF:
					return thisRow.staff;
				case DATA_SPECIMEN:
					return thisRow.specimen;
				case DATA_NOSPECS:
					return thisRow.noSpecimens;
				case DATA_NOBLOCKS:
					return thisRow.noBlocks;
				case DATA_NOSLIDES:
					return thisRow.noSlides;
				case DATA_NOHE:
					return thisRow.noHE;
				case DATA_NOSS:
					return thisRow.noSS;
				case DATA_NOIHC:
					return thisRow.noIHC;
				case DATA_NOMOL:
					return thisRow.noMOL;
				case DATA_NOFSPECS:
					return thisRow.noFSpecs;
				case DATA_NOFBLOCKS:
					return thisRow.noFB;
				case DATA_NOFSLIDES:
					return thisRow.noFL;
				case DATA_NOSYN:
					return thisRow.noSynoptics;
				case DATA_TATGROSS:
					return thisRow.tatGross;
				case DATA_TATROUTE:
					return thisRow.tatRoute;
				case DATA_TATFINAL:
					return thisRow.tatFinal;
				case DATA_TATTOTAL:
					return thisRow.tatTotal;
				default:
					return value;
				}
			}
			return value;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
		}
	}

	private class DataWorker extends SwingWorker<Void, Void> {
		
		protected Void doInBackground() throws Exception {
			setName("StatsWorker");
			if (rowsChanged.get() && timeTo > timeFrom) {
				readTable();
			}
			return null;
		}
		
		private void readTable() {
			DataRow dataRow = new DataRow();
			ResultSet rst = null;
			try {
				dataRows.clear();
				rst = parent.dbPowerJ.getStats(filters, timeFrom, timeTo);
				while (rst.next()) {
					dataRow = new DataRow();
					dataRow.caseID = rst.getLong("CASEID");
					dataRow.noSynoptics = rst.getByte("NOSYN");
					dataRow.noSpecimens = rst.getByte("NOSPECS");
					dataRow.noFSpecs = rst.getByte("NOFSP");
					dataRow.noBlocks = rst.getShort("NOBLOCKS");
					dataRow.noSlides = rst.getShort("NOSLIDES");
					dataRow.noHE = rst.getShort("NOHE");
					dataRow.noSS = rst.getShort("NOSS");
					dataRow.noIHC = rst.getShort("NOIHC");
					dataRow.noMOL = rst.getShort("NOMOL");
					dataRow.noFB = rst.getShort("NOFBL");
					dataRow.noFL = rst.getShort("NOFSL");
					dataRow.tatGross = rst.getShort("GRTAT");
					dataRow.tatRoute = rst.getShort("ROTAT");
					dataRow.tatFinal = rst.getShort("FITAT");
					dataRow.tatTotal = rst.getShort("TOTAT");
					dataRow.calAccession.setTimeInMillis(rst.getTimestamp("ACCESSED").getTime());
					dataRow.calGross.setTimeInMillis(rst.getTimestamp("GROSSED").getTime());
					dataRow.calRoute.setTimeInMillis(rst.getTimestamp("ROUTED").getTime());
					dataRow.calFinal.setTimeInMillis(rst.getTimestamp("FINALED").getTime());
					dataRow.caseNo = rst.getString("CASENO").trim();
					dataRow.facility = rst.getString("FACI").trim();
					dataRow.specialty = rst.getString("SPYNAME").trim();
					dataRow.subspecialty = rst.getString("SUBINIT").trim();
					dataRow.staff = rst.getString("PLAST").trim();
					dataRow.specimen = rst.getString("SPEC").trim();
					dataRow.procedure = DataProcedure.NAMES[rst.getByte("PROID")];
					dataRows.add(dataRow);
				}
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				infoUpdate = "From " +
						parent.dateUtils.formatter(timeFrom, 
								parent.dateUtils.FORMAT_DATE) +
						" To " +
						parent.dateUtils.formatter(timeTo,
								parent.dateUtils.FORMAT_DATE);
				parent.dbPowerJ.closeRst(rst);
				parent.dbPowerJ.closeStm();
			}
		}

		public void done() {
			rowsChanged.set(false);
			parent.setBusy(false);
			((AbstractTableModel) tblData.getModel()).fireTableDataChanged();
			setColumns();
			parent.statusBar.setMessage(infoUpdate);
		}
	}
}
