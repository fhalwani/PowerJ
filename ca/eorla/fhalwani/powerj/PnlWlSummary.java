package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
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

public class PnlWlSummary extends PnlMain implements KeyListener {
	private static final long serialVersionUID = -1913410438118574885L;
	private final byte DATA_NAME = 0;
	private final byte DATA_CASES = 1;
	private final byte DATA_RCASES = 2;
	private final byte DATA_SLIDES = 3;
	private final byte DATA_RSLIDES = 4;
	private final byte DATA_VALUE1 = 5;
	private final byte DATA_RVALUE1 = 6;
	private final byte DATA_FVALUE1 = 7;
	private final byte DATA_VALUE2 = 8;
	private final byte DATA_RVALUE2 = 9;
	private final byte DATA_FVALUE2 = 10;
	private final byte DATA_VALUE3 = 11;
	private final byte DATA_RVALUE3 = 12;
	private final byte DATA_FVALUE3 = 13;
	private final byte DATA_VALUE4 = 14;
	private final byte DATA_RVALUE4 = 15;
	private final byte DATA_FVALUE4 = 16;
	private final byte CHART_CASES = 0;
	private final byte CHART_SLIDES = 1;
	private final byte CHART_CODER1 = 2;
	private final byte CHART_CODER2 = 3;
	private final byte CHART_CODER3 = 4;
	private final byte CHART_CODER4 = 5;
	private final byte RULE_CASES = 0;
	private final byte RULE_SLIDES = 1;
	private final byte RULE_CODER1 = 2;
	private final byte RULE_CODER2 = 3;
	private final byte RULE_CODER3 = 4;
	private final byte RULE_CODER4 = 5;
	private final byte RULE_COUNT = 6;
	private final byte RULE_RELATIVE = 7;
	private final byte RULE_FTE = 8;
	private AtomicBoolean datesChanged = new AtomicBoolean(true);
	private AtomicBoolean rowsChanged = new AtomicBoolean(false);
	private AtomicBoolean colsChanged = new AtomicBoolean(true);
	private boolean[] colsView = {true, true, true, true, true, true, true, true, true};
	private int[] rowsView = {Constants.ROW_FACILITY, Constants.ROW_SPECIALTY, Constants.ROW_SUBSPECIALTY, Constants.ROW_STAFF};
	private long timeFrom = 0;
	private long timeTo = 0;
	private String infoUpdate = "Dates";
	private ChartPie chartCases;
	private ChartPie chartSlides;
	private ChartPie chartCoder1;
	private ChartPie chartCoder2;
	private ChartPie chartCoder3;
	private ChartPie chartCoder4;
	private JTreeTable treeTable;
	private WLModel treeTableModel;
	private TreePath treePath;
	private JTableColumnModel columnModel = new JTableColumnModel();
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();

	PnlWlSummary(PowerJ parent) {
		super(parent);
		setName("WLSummary");
		parent.dbPowerJ.prepareSummary();
		createPanel();
	}

	boolean close() {
		super.close();
		dataRows.clear();
		parent.dbPowerJ.closeStms();
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		TreeTableNode nodeRoot = new TreeTableNode("Total");
		treeTableModel = new WLModel(nodeRoot);
		treeTable = new JTreeTable(parent, treeTableModel);
		treeTable.setColumnModel(columnModel);
		treeTable.createDefaultColumnsFromModel();
		treeTable.setFocusable(true);
		treeTable.addKeyListener(this);
		TableColumn column = treeTable.getColumnModel().getColumn(DATA_NAME);
		column.setMinWidth(250);
		column.setPreferredWidth(250);
		treeTable.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath newPath = e.getNewLeadSelectionPath();
				if (newPath != null) {
					if (treePath == null || !treePath.equals(newPath)) {
						treePath = newPath;
						setCharts();
					}
				}
			}
		});
		treeTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() > 1) {
					showDetails();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(treeTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		scrollPane.setBorder(border);
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 771027087739168338L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		Utilities.addComponent(scrollPane, 0, 0, 6, 3, 1.0, 0.75,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartCases = new ChartPie();
		chartCases.setBorder(border);
		chartCases.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_CASES);
			}
		});
		Utilities.addComponent(chartCases, 0, 3, 1, 1, 0.167, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartSlides = new ChartPie();
		chartSlides.setBorder(border);
		chartSlides.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_SLIDES);
			}
		});
		Utilities.addComponent(chartSlides, 1, 3, 1, 1, 0.167, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartCoder1 = new ChartPie();
		chartCoder1.setBorder(border);
		chartCoder1.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_CODER1);
			}
		});
		Utilities.addComponent(chartCoder1, 2, 3, 1, 1, 0.167, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartCoder2 = new ChartPie();
		chartCoder2.setBorder(border);
		chartCoder2.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_CODER2);
			}
		});
		Utilities.addComponent(chartCoder2, 3, 3, 1, 1, 0.167, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartCoder3 = new ChartPie();
		chartCoder3.setBorder(border);
		chartCoder3.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_CODER3);
			}
		});
		Utilities.addComponent(chartCoder3, 4, 3, 1, 1, 0.167, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartCoder4 = new ChartPie();
		chartCoder4.setBorder(border);
		chartCoder4.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				displayData(e.getX(), e.getY(), CHART_CODER4);
			}
		});
		Utilities.addComponent(chartCoder4, 5, 3, 1, 1, 0.167, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		add(createToolbar(), BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);
	}

	private JPanel createToolbar() {
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		timeFrom = parent.variables.minWorkloadDate;
		Calendar calMin = Calendar.getInstance();
		calMin.setTimeInMillis(timeFrom);
		Calendar calMax = Calendar.getInstance();
		// Must include today to include all cases till midnight yesterday
		calMax.set(Calendar.HOUR_OF_DAY, 0);
		calMax.set(Calendar.MINUTE, 0);
		calMax.set(Calendar.SECOND, 0);
		calMax.set(Calendar.MILLISECOND, 0);
		Calendar calStart = Calendar.getInstance();
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		timeTo = calMax.getTimeInMillis();
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
						datesChanged.set(true);
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
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		CboDate cboEnd = new CboDate(calEnd, calMin, calMax);
		cboEnd.setName("cboEnd");
		cboEnd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					if (timeTo != cal.getTimeInMillis()) {
						timeTo = cal.getTimeInMillis();
						datesChanged.set(true);
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
		CboRows cboRows = new CboRows(rowsView);
		cboRows.setName("cboRows");
		cboRows.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboRows cbo = (CboRows)e.getSource();
					int[] newValue = cbo.getValue();
					if (!rowsView.equals(newValue)) {
						for (int i = 0; i < rowsView.length; i++) {
							rowsView[i] = newValue[i];
						}
						rowsChanged.set(true);
					}
				}
			}
		});
		label = new JLabel("Rows:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_R);
		label.setLabelFor(cboRows);
		panel.add(label);
		panel.add(cboRows);
		CboColumns cboCols = new CboColumns(colsView, parent);
		cboCols.setName("cboCols");
		cboCols.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboColumns cbo = (CboColumns)e.getSource();
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
				if (datesChanged.get() || rowsChanged.get()) {
					setData();
				} else if (colsChanged.get()) {
					setColumns();
				}
			}
		});
		panel.add(btnGo);
		return panel;
	}

	private void displayData(int x, int y, byte chartID) {
		if (parent.variables.busy.get()) return;
		String info = "", units = " Units ";
		switch (chartID) {
		case CHART_CASES:
			info = chartCases.getMessage(parent, x, y);
			units = " Cases";
			break;
		case CHART_SLIDES:
			info = chartSlides.getMessage(parent, x, y);
			units = " Slides";
			break;
		case CHART_CODER1:
			info = chartCoder1.getMessage(parent, x, y);
			units += parent.variables.codersName[0];
			break;
		case CHART_CODER2:
			info = chartCoder2.getMessage(parent, x, y);
			units += parent.variables.codersName[1];
			break;
		case CHART_CODER3:
			info = chartCoder3.getMessage(parent, x, y);
			units += parent.variables.codersName[2];
			break;
		default:
			info = chartCoder4.getMessage(parent, x, y);
			units += parent.variables.codersName[3];
		}
		if (info.length() > 0) {
			info += units;
		} else {
			info = infoUpdate;
		}
		parent.statusBar.setMessage(info);
	}

	public void keyPressed(KeyEvent e) {
		if (treePath == null) return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ADD: // 107
		case KeyEvent.VK_PLUS: // 521
			if (e.isControlDown()) {
				treeTable.expandAll();
			} else if (e.isAltDown()) {
				treeTable.expandAllUnder(treePath);
			} else {
				treeTable.expandPath(treePath);
			}
			break;
		case KeyEvent.VK_SUBTRACT: // 109
		case KeyEvent.VK_MINUS: // 45
			if (e.isControlDown()) {
				treeTable.collapseAll();
			} else if (e.isAltDown()) {
				treeTable.collapseAllUnder(treePath);
			} else {
				treeTable.collapsePath(treePath);
			}
			break;
		default:
			// Ignore rest
		}
	}

	public void keyReleased(KeyEvent ignore) {}
	public void keyTyped(KeyEvent ignore) {}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("summary.pdf").trim();
		if (fileName.trim().length() == 0) return;
		final String[] header = {"CCases", "RCases", "CSlides", "RSlides",
				parent.variables.codersName[0], "R" +parent.variables.codersName[0],
				"F" +parent.variables.codersName[0], parent.variables.codersName[1],
				"R" +parent.variables.codersName[1], "F" +parent.variables.codersName[1],
				parent.variables.codersName[2], "R" +parent.variables.codersName[2],
				"F" +parent.variables.codersName[2], parent.variables.codersName[3],
				"R" +parent.variables.codersName[3], "F" +parent.variables.codersName[3]};
        String str = "Workload Summary";
		ArrayList<XlsCol> xlsCols = new ArrayList<XlsCol>();
		XlsCol xlsCol;
		try {
			for (int i = 0; i < rowsView.length; i++) {
				if (rowsView[i] > 0) {
					switch (rowsView[i]) {
					case Constants.ROW_FACILITY:
						str = "Fac";
						break;
					case Constants.ROW_SPECIALTY:
						str = "Spy";
						break;
					case Constants.ROW_SUBSPECIALTY:
						str = "Sub";
						break;
					default:
						str = "Staff";
					}
					xlsCol = new XlsCol(i, 0, str);
					xlsCols.add(xlsCol);
				}
			}
			int noColNames = xlsCols.size();
			for (int i = 0; i < header.length; i++) {
				switch (i+1) {
				case DATA_CASES:
					if (colsView[RULE_CASES] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RCASES:
					if (colsView[RULE_CASES] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_SLIDES:
					if (colsView[RULE_SLIDES] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RSLIDES:
					if (colsView[RULE_SLIDES] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE1:
					if (colsView[RULE_CODER1] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE1:
					if (colsView[RULE_CODER1] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_FVALUE1:
					if (colsView[RULE_CODER1] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE2:
					if (colsView[RULE_CODER2] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE2:
					if (colsView[RULE_CODER2] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_FVALUE2:
					if (colsView[RULE_CODER2] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE3:
					if (colsView[RULE_CODER3] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE3:
					if (colsView[RULE_CODER3] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_FVALUE3:
					if (colsView[RULE_CODER3] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE4:
					if (colsView[RULE_CODER4] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE4:
					if (colsView[RULE_CODER4] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				default:
					if (colsView[RULE_CODER4] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
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
	        str = "Workload Summary";
			paragraph.add(new Chunk(str));
            document.add(paragraph);
			str = parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE);
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
			TreeTableNode root = (TreeTableNode) treeTableModel.getRoot();
			pdfAddRow(-1, noColNames, root, table, fonts, xlsCols);
			document.add(table);
            document.close();
        } catch (DocumentException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        } catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        }
	}

	private void pdfAddRow(int iter, int noColNames, TreeTableNode node,
			PdfPTable table, HashMap<String, Font> fonts, ArrayList<XlsCol> xlsCols) {
        String str = "";
		XlsCol xlsCol;
        Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		for (int i = 0; i < xlsCols.size(); i++) {
			str = "";
			xlsCol = xlsCols.get(i);
			if (i == 0 && iter < 0) {
				str = node.title;
			} else if (i == iter) {
				str = node.title;
			} else if (i > noColNames -1) {
				switch (xlsCol.id - noColNames +1) {
				case DATA_CASES:
					str = parent.numbers.formatNumber(node.noCases);
					break;
				case DATA_RCASES:
					str = parent.numbers.formatDouble(2, node.prcntCases);
					break;
				case DATA_SLIDES:
					str = parent.numbers.formatNumber(node.noSlides);
					break;
				case DATA_RSLIDES:
					str = parent.numbers.formatDouble(2, node.prcntSlides);
					break;
				case DATA_VALUE1:
					str = parent.numbers.formatNumber(node.value1);
					break;
				case DATA_RVALUE1:
					str = parent.numbers.formatDouble(2, node.prcnt1);
					break;
				case DATA_FVALUE1:
					str = parent.numbers.formatDouble(2, node.fte1);
					break;
				case DATA_VALUE2:
					str = parent.numbers.formatNumber(node.value2);
					break;
				case DATA_RVALUE2:
					str = parent.numbers.formatDouble(2, node.prcnt2);
					break;
				case DATA_FVALUE2:
					str = parent.numbers.formatDouble(2, node.fte2);
					break;
				case DATA_VALUE3:
					str = parent.numbers.formatNumber(node.value3);
					break;
				case DATA_RVALUE3:
					str = parent.numbers.formatDouble(2, node.prcnt3);
					break;
				case DATA_FVALUE3:
					str = parent.numbers.formatDouble(2, node.fte3);
					break;
				case DATA_VALUE4:
					str = parent.numbers.formatNumber(node.value4);
					break;
				case DATA_RVALUE4:
					str = parent.numbers.formatDouble(2, node.prcnt4);
					break;
				default:
					str = parent.numbers.formatDouble(2, node.fte4);
				}
			}
            paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font10n"));
			paragraph.add(new Chunk(str));
			cell = new PdfPCell();
			if (i < noColNames) {
				paragraph.setAlignment(Element.ALIGN_LEFT);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			} else {
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
			cell.addElement(paragraph);
			table.addCell(cell);
		}
		if (node.children != null) {
			iter++;
			TreeTableNode child;
			for (int i = 0; i < node.children.length; i++) {
				child = (TreeTableNode) node.children[i];
				pdfAddRow(iter, noColNames, child, table, fonts, xlsCols);
			}
		}
	}

	private void setCharts() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		// Must initialize a new instance each time
		ChartsWorker worker = new ChartsWorker();
		worker.execute();
	}

	private void setColumns() {
		byte rule1 = 0, rule2 = 0;
		boolean isActive = true;
		int noCols = columnModel.getColumnCount(false);
		TableColumn column = new TableColumn();
		for (int i = noCols -1; i > 0; i--) {
			column = columnModel.getColumnByModelIndex(i);
			switch (i) {
			case DATA_CASES:
				rule1 = RULE_CASES;
				rule2 = RULE_COUNT;
				isActive = true;
				chartCases.setVisible(colsView[RULE_CASES]);
				break;
			case DATA_RCASES:
				rule1 = RULE_CASES;
				rule2 = RULE_RELATIVE;
				isActive = true;
				break;
			case DATA_SLIDES:
				rule1 = RULE_SLIDES;
				rule2 = RULE_COUNT;
				isActive = true;
				chartSlides.setVisible(colsView[rule1]);
				break;
			case DATA_RSLIDES:
				rule1 = RULE_SLIDES;
				rule2 = RULE_RELATIVE;
				isActive = true;
				break;
			case DATA_VALUE1:
				rule1 = RULE_CODER1;
				rule2 = RULE_COUNT;
				isActive = parent.variables.codersActive[0];
				chartCoder1.setVisible(isActive && colsView[rule1]);
				break;
			case DATA_RVALUE1:
				rule1 = RULE_CODER1;
				rule2 = RULE_RELATIVE;
				isActive = parent.variables.codersActive[0];
				break;
			case DATA_FVALUE1:
				rule1 = RULE_CODER1;
				rule2 = RULE_FTE;
				isActive = parent.variables.codersActive[0];
				break;
			case DATA_VALUE2:
				rule1 = RULE_CODER2;
				rule2 = RULE_COUNT;
				isActive = parent.variables.codersActive[1];
				chartCoder2.setVisible(isActive && colsView[rule1]);
				break;
			case DATA_RVALUE2:
				rule1 = RULE_CODER2;
				rule2 = RULE_RELATIVE;
				isActive = parent.variables.codersActive[1];
				break;
			case DATA_FVALUE2:
				rule1 = RULE_CODER2;
				rule2 = RULE_FTE;
				isActive = parent.variables.codersActive[1];
				break;
			case DATA_VALUE3:
				rule1 = RULE_CODER3;
				rule2 = RULE_COUNT;
				isActive = parent.variables.codersActive[2];
				chartCoder3.setVisible(isActive && colsView[rule1]);
				break;
			case DATA_RVALUE3:
				rule1 = RULE_CODER3;
				rule2 = RULE_RELATIVE;
				isActive = parent.variables.codersActive[2];
				break;
			case DATA_FVALUE3:
				rule1 = RULE_CODER3;
				rule2 = RULE_FTE;
				isActive = parent.variables.codersActive[2];
				break;
			case DATA_VALUE4:
				rule1 = RULE_CODER4;
				rule2 = RULE_COUNT;
				isActive = parent.variables.codersActive[3];
				chartCoder4.setVisible(isActive && colsView[rule1]);
				break;
			case DATA_RVALUE4:
				rule1 = RULE_CODER4;
				rule2 = RULE_RELATIVE;
				isActive = parent.variables.codersActive[3];
				break;
			case DATA_FVALUE4:
				rule1 = RULE_CODER4;
				rule2 = RULE_FTE;
				isActive = parent.variables.codersActive[3];
				break;
			default:
				// Names
				continue;
			}
			if (isActive && colsView[rule1] && colsView[rule2]) {
				if (!columnModel.isColumnVisible(column)) {
					columnModel.setColumnVisible(column, true);
				}
			} else if (columnModel.isColumnVisible(column)) {
				columnModel.setColumnVisible(column, false);
			}
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
	
	private void showDetails() {
		if (treePath == null) return;
		int count = treePath.getPathCount();
		// Ignore root click (too many cases)
		if (count == 0) return;
		int[] values = new int[count];
		for (int i = 0; i < count; i++) {
			TreeTableNode node = (TreeTableNode) treePath.getPathComponent(i);
			values[i] = node.id;
		}
		new DlgWorkload(parent, rowsView, values, timeFrom, timeTo);
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("summary.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		final String[] header = {"CCases", "RCases", "CSlides", "RSlides",
				parent.variables.codersName[0], "R" +parent.variables.codersName[0],
				"F" +parent.variables.codersName[0], parent.variables.codersName[1],
				"R" +parent.variables.codersName[1], "F" +parent.variables.codersName[1],
				parent.variables.codersName[2], "R" +parent.variables.codersName[2],
				"F" +parent.variables.codersName[2], parent.variables.codersName[3],
				"R" +parent.variables.codersName[3], "F" +parent.variables.codersName[3]};
		String str = "";
		ArrayList<XlsCol> xlsCols = new ArrayList<XlsCol>();
		XlsCol xlsCol;
		try {
			for (int i = 0; i < rowsView.length; i++) {
				if (rowsView[i] > 0) {
					switch (rowsView[i]) {
					case Constants.ROW_FACILITY:
						str = "Fac";
						break;
					case Constants.ROW_SPECIALTY:
						str = "Spy";
						break;
					case Constants.ROW_SUBSPECIALTY:
						str = "Sub";
						break;
					case Constants.ROW_PROCEDURE:
						str = "Proc";
						break;
					default:
						str = "Staff";
					}
					xlsCol = new XlsCol(i, 0, str);
					xlsCols.add(xlsCol);
				}
			}
			int noColNames = xlsCols.size();
			for (int i = 0; i < header.length; i++) {
				switch (i+1) {
				case DATA_CASES:
					if (colsView[RULE_CASES] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RCASES:
					if (colsView[RULE_CASES] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_SLIDES:
					if (colsView[RULE_SLIDES] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RSLIDES:
					if (colsView[RULE_SLIDES] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE1:
					if (colsView[RULE_CODER1] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE1:
					if (colsView[RULE_CODER1] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_FVALUE1:
					if (colsView[RULE_CODER1] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE2:
					if (colsView[RULE_CODER2] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE2:
					if (colsView[RULE_CODER2] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_FVALUE2:
					if (colsView[RULE_CODER2] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE3:
					if (colsView[RULE_CODER3] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE3:
					if (colsView[RULE_CODER3] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_FVALUE3:
					if (colsView[RULE_CODER3] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_VALUE4:
					if (colsView[RULE_CODER4] && colsView[RULE_COUNT]) {
						xlsCol = new XlsCol(noColNames+i, 1, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				case DATA_RVALUE4:
					if (colsView[RULE_CODER4] && colsView[RULE_RELATIVE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
					break;
				default:
					if (colsView[RULE_CODER4] && colsView[RULE_FTE]) {
						xlsCol = new XlsCol(noColNames+i, 2, header[i]);
						xlsCols.add(xlsCol);
					}
				}
			}
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Summary");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Workload Summary");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, xlsCols.size()-1));
			row = sheet.createRow(1);
			row.setHeightInPoints(45);
			cell = row.createCell(0);
			cell.setCellValue(parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE));
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, xlsCols.size()-1));
			// header row
			row = sheet.createRow(2);
			ColumnHelper colHelper = sheet.getColumnHelper();
			for (int i = 0; i < xlsCols.size(); i++) {
				xlsCol = xlsCols.get(i);
				cell = row.createCell(i);
				cell.setCellValue(xlsCol.name);
				cell.setCellStyle(styles.get("header"));
				switch (xlsCol.type) {
				case 1:
					colHelper.setColDefaultStyle(i, styles.get("data_int"));
					break;
				case 2:
					colHelper.setColDefaultStyle(i, styles.get("data_double"));
					break;
				default:
					colHelper.setColDefaultStyle(i, styles.get("text"));
				}
			}
			int rownum = 3;
			TreeTableNode root = (TreeTableNode) treeTableModel.getRoot();
			xlsAddRow(-1, rownum, noColNames, root, xlsCols, sheet);
			sheet.createFreezePane(noColNames, 3);
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

	private int xlsAddRow(int iter, int rownum, int noColNames, TreeTableNode node,
			ArrayList<XlsCol> xlsCols, XSSFSheet sheet) {
		Row row = sheet.createRow(rownum++);
		Cell cell;
		XlsCol xlsCol;
		for (int i = 0; i < xlsCols.size(); i++) {
			cell = row.createCell(i);
			xlsCol = xlsCols.get(i);
			if (i < noColNames) {
				if (i == 0 && iter < 0) {
					xlsCol.name = node.title;
				} else if (i == iter) {
					xlsCol.name = node.title;
					for (int j = i+1; j < noColNames; j++) {
						xlsCol = xlsCols.get(j);
						xlsCol.name = "";
					}
					xlsCol = xlsCols.get(i);
				}
				cell.setCellValue(xlsCol.name);
				continue;
			}
			switch (xlsCol.id - noColNames +1) {
			case DATA_CASES:
				cell.setCellValue(node.noCases);
				break;
			case DATA_RCASES:
				cell.setCellValue(node.prcntCases);
				break;
			case DATA_SLIDES:
				cell.setCellValue(node.noSlides);
				break;
			case DATA_RSLIDES:
				cell.setCellValue(node.prcntSlides);
				break;
			case DATA_VALUE1:
				cell.setCellValue(node.value1);
				break;
			case DATA_RVALUE1:
				cell.setCellValue(node.prcnt1);
				break;
			case DATA_FVALUE1:
				cell.setCellValue(node.fte1);
				break;
			case DATA_VALUE2:
				cell.setCellValue(node.value2);
				break;
			case DATA_RVALUE2:
				cell.setCellValue(node.prcnt2);
				break;
			case DATA_FVALUE2:
				cell.setCellValue(node.fte2);
				break;
			case DATA_VALUE3:
				cell.setCellValue(node.value3);
				break;
			case DATA_RVALUE3:
				cell.setCellValue(node.prcnt3);
				break;
			case DATA_FVALUE3:
				cell.setCellValue(node.fte3);
				break;
			case DATA_VALUE4:
				cell.setCellValue(node.value4);
				break;
			case DATA_RVALUE4:
				cell.setCellValue(node.prcnt4);
				break;
			default:
				cell.setCellValue(node.fte4);
			}
		}
		if (node.children != null) {
			iter++;
			TreeTableNode child;
			for (int i = 0; i < node.children.length; i++) {
				child = (TreeTableNode) node.children[i];
				rownum = xlsAddRow(iter, rownum, noColNames, child, xlsCols, sheet);
			}
		}
		return rownum;
	}
	
	class DataArray {
		short id = 0;
		int noCases = 0;
		int noSlides = 0;
		int value1 = 0;
		int value2 = 0;
		int value3 = 0;
		int value4 = 0;
		double prcntCases = 0;
		double prcntSlides = 0;
		double prcnt1 = 0;
		double prcnt2 = 0;
		double prcnt3 = 0;
		double prcnt4 = 0;
		double fte1 = 0;
		double fte2 = 0;
		double fte3 = 0;
		double fte4 = 0;
		String name = "";
		ArrayList<DataArray> children = new ArrayList<DataArray>();
	}

	class DataRow {
		short facID = 0;
		short spyID = 0;
		short subID = 0;
		short perID = 0;
		int noCases = 0;
		int noSlides = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		String facName = "";
		String spyName = "";
		String subName = "";
		String perName = "";
	}

	class TreeTableNode {
		short id = 0;
		int noCases = 0;
		int noSlides = 0;
		int value1 = 0;
		int value2 = 0;
		int value3 = 0;
		int value4 = 0;
		double prcntCases = 0;
		double prcntSlides = 0;
		double prcnt1 = 0;
		double prcnt2 = 0;
		double prcnt3 = 0;
		double prcnt4 = 0;
		double fte1 = 0;
		double fte2 = 0;
		double fte3 = 0;
		double fte4 = 0;
		String title = "";
		Object[] children; 

		TreeTableNode(String name) {
			this.title = name;
		}

		public String toString() {
			return this.title;
		}
	}

	class WLModel extends AbstractTreeTableModel implements TreeTableModel {
		private final String[] cNames = {"Name",
				"CCases", "RCases", "CSlides", "RSlides",
				parent.variables.codersName[0], "R" + parent.variables.codersName[0],
				"F" + parent.variables.codersName[0], parent.variables.codersName[1],
				"R" + parent.variables.codersName[1], "F" + parent.variables.codersName[1], 
				parent.variables.codersName[2], "R" + parent.variables.codersName[2],
				"F" + parent.variables.codersName[2], parent.variables.codersName[3],
				"R" + parent.variables.codersName[3], "F" + parent.variables.codersName[3]};
		private final Class<?>[]  cTypes = {TreeTableModel.class, Integer.class, Double.class,
				Integer.class, Double.class, Integer.class, Double.class, Double.class,
				Integer.class, Double.class, Double.class, Integer.class, Double.class,
				Double.class, Integer.class, Double.class, Double.class};

		public WLModel(Object nodeRoot) {
			super(nodeRoot);
		}

		public Object getChild(Object node, int element) {
			return ((TreeTableNode)node).children[element];
		}

		public int getChildCount(Object node) {
			Object[] children = getChildren(node); 
			return (children == null) ? 0 : children.length;
		}

		protected Object[] getChildren(Object node) {
			return ((TreeTableNode)node).children;
		}

		public int getColumnCount() {
			return cNames.length;
		}

		public String getColumnName(int column) {
			return cNames[column];
		}

		public Class<?> getColumnClass(int column) {
			return cTypes[column];
		}

		public Object getValueAt(Object node, int column) {
			TreeTableNode data = (TreeTableNode) node;
			switch(column) {
			case DATA_NAME:
				return data.title;
			case DATA_CASES:
				return data.noCases;
			case DATA_RCASES:
				return data.prcntCases;
			case DATA_SLIDES:
				return data.noSlides;
			case DATA_RSLIDES:
				return data.prcntSlides;
			case DATA_VALUE1:
				return data.value1;
			case DATA_RVALUE1:
				return data.prcnt1;
			case DATA_FVALUE1:
				return data.fte1;
			case DATA_VALUE2:
				return data.value2;
			case DATA_RVALUE2:
				return data.prcnt2;
			case DATA_FVALUE2:
				return data.fte2;
			case DATA_VALUE3:
				return data.value3;
			case DATA_RVALUE3:
				return data.prcnt3;
			case DATA_FVALUE3:
				return data.fte3;
			case DATA_VALUE4:
				return data.value4;
			case DATA_RVALUE4:
				return data.prcnt4;
			case DATA_FVALUE4:
				return data.fte4;
			}
			return null;
		}
	}

	private class ChartsWorker extends SwingWorker<Void, Void> {
		private ArrayList<DataPie> dataCases = new ArrayList<DataPie>();
		private ArrayList<DataPie> dataSlides = new ArrayList<DataPie>();
		private ArrayList<DataPie> dataCoder1 = new ArrayList<DataPie>();
		private ArrayList<DataPie> dataCoder2 = new ArrayList<DataPie>();
		private ArrayList<DataPie> dataCoder3 = new ArrayList<DataPie>();
		private ArrayList<DataPie> dataCoder4 = new ArrayList<DataPie>();

		protected Void doInBackground() throws Exception {
			setName("ChartsWorker");
			if (treePath != null) {
				TreeTableNode node = (TreeTableNode) treePath.getPathComponent(treePath.getPathCount() -1);
				if (node != null && node.children.length > 0) {
					int count = node.children.length;
					int colorID = 0;
					DataPie element = new DataPie();
					TreeTableNode leaf = new TreeTableNode("");
					for (int i = 0; i < count; i++) {
						leaf = (TreeTableNode) node.children[i];
						for (int k = 0; k < 6; k++) {
							element = new DataPie();
							element.key = leaf.id;
							element.label = leaf.title;
							element.color = Constants.SPECIALTY_COLORS[colorID];
							switch (k) {
							case 0:
								element.value = leaf.noCases;
								dataCases.add(element);
								break;
							case 1:
								element.value = leaf.noSlides;
								dataSlides.add(element);
								break;
							case 2:
								element.value = leaf.value1;
								dataCoder1.add(element);
								break;
							case 3:
								element.value = leaf.value2;
								dataCoder2.add(element);
								break;
							case 4:
								element.value = leaf.value3;
								dataCoder3.add(element);
								break;
							default:
								element.value = leaf.value4;
								dataCoder4.add(element);
								break;
							}
						}
						colorID++;
						if (colorID == Constants.SPECIALTY_COLORS.length) {
							colorID = 0;
						}
					}
				}
			}
			return null;
		}

		public void done() {
			// Display results
			chartCases.setData(dataCases, "Cases");
			chartSlides.setData(dataSlides, "Slides");
			chartCoder1.setData(dataCoder1, parent.variables.codersName[0]);
			chartCoder2.setData(dataCoder2, parent.variables.codersName[1]);
			chartCoder3.setData(dataCoder3, parent.variables.codersName[2]);
			chartCoder4.setData(dataCoder4, parent.variables.codersName[3]);
			parent.setBusy(false);
		}
	}
	
	private class DataWorker extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws Exception {
			setName("WLWorker");
			if (datesChanged.get() && timeTo > timeFrom) {
				readTable();
			} else if (rowsChanged.get()) {
				structureData();
			}
			return null;
		}
		
		private void readTable() {
			boolean exists = false;
			java.sql.Date start = new java.sql.Date(timeFrom);
			java.sql.Date end = new java.sql.Date(timeTo);
			PreparedStatement stm = null;
			ResultSet rst = null;
			DataRow dataRow = new DataRow();
			try {
				dataRows.clear();
				stm = parent.dbPowerJ.getStatement(0);
				stm.setDate(1, start);
				stm.setDate(2, end);
				rst = stm.executeQuery();
				while (rst.next()) {
					dataRow = new DataRow();
					dataRow.facID = rst.getShort("FACID");
					dataRow.spyID = rst.getShort("SPYID");
					dataRow.subID = rst.getShort("SUBID");
					dataRow.perID = rst.getShort("FINALID");
					dataRow.noCases = rst.getInt("NOCASES");
					dataRow.noSlides = rst.getInt("NOSLIDES");
					dataRow.value1 = rst.getDouble("VALUE1");
					dataRow.value2 = rst.getDouble("VALUE2");
					dataRow.value3 = rst.getDouble("VALUE3");
					dataRow.value4 = rst.getDouble("VALUE4");
					dataRow.facName = rst.getString("FACI").trim();
					dataRow.spyName = rst.getString("SPYNAME").trim();
					dataRow.subName = rst.getString("SUBINIT").trim();
					dataRow.perName = rst.getString("INITIALS").trim();
					dataRows.add(dataRow);
				}
				rst.close();
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				// Frozen Sections
				stm = parent.dbPowerJ.getStatement(1);
				stm.setDate(1, start);
				stm.setDate(2, end);
				rst = stm.executeQuery();
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < dataRows.size(); i++) {
						dataRow = dataRows.get(i);
						if (dataRow.facID == rst.getShort("FACID")
								&& dataRow.subID == rst.getShort("SUBID")
								&& dataRow.perID == rst.getShort("PERID")
								&& dataRow.spyName.equals("FSEC")) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						dataRow = new DataRow();
						dataRow.facID = rst.getShort("FACID");
						dataRow.spyID = 0;
						dataRow.subID = rst.getShort("SUBID");
						dataRow.perID = rst.getShort("PERID");
						dataRow.facName = rst.getString("CODE").trim();
						dataRow.spyName = "FSEC";
						dataRow.subName = rst.getString("SUBINIT").trim();
						dataRow.perName = rst.getString("INITIALS").trim();
						dataRows.add(dataRow);
					}
					dataRow.noCases += rst.getInt("NOCASES");
					dataRow.noSlides += rst.getInt("NOSLIDES");
					dataRow.value1 += rst.getDouble("VALUE1");
					dataRow.value2 += rst.getDouble("VALUE2");
					dataRow.value3 += rst.getDouble("VALUE3");
					dataRow.value4 += rst.getDouble("VALUE4");
				}
				rst.close();
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				stm = parent.dbPowerJ.getStatement(2);
				stm.setDate(1, start);
				stm.setDate(2, end);
				rst = stm.executeQuery();
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < dataRows.size(); i++) {
						dataRow = dataRows.get(i);
						if (dataRow.facID == rst.getShort("FACID")
								&& dataRow.spyID == rst.getShort("SPYID")
								&& dataRow.subID == rst.getShort("SUBID")
								&& dataRow.perID == rst.getShort("PERID")) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						dataRow = new DataRow();
						dataRow.facID = rst.getShort("FACID");
						dataRow.spyID = rst.getShort("SPYID");
						dataRow.subID = rst.getShort("SUBID");
						dataRow.perID = rst.getShort("PERID");
						dataRow.facName = rst.getString("CODE").trim();
						dataRow.spyName = rst.getString("SPYNAME").trim();
						dataRow.subName = rst.getString("SUBINIT").trim();
						dataRow.perName = rst.getString("INITIALS").trim();
						dataRows.add(dataRow);
					}
					dataRow.value1 += rst.getDouble("VALUE1");
					dataRow.value2 += rst.getDouble("VALUE2");
					dataRow.value3 += rst.getDouble("VALUE3");
					dataRow.value4 += rst.getDouble("VALUE4");
				}
				structureData();
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

		private void structureData() {
			short id = 0;
			short ids[] = new short[rowsView.length];
			int rowNos[] = new int[rowsView.length];
			int noRows = dataRows.size();
			int value = 0;
			String name = "";
			DataArray dataRoot = new DataArray();
			DataArray data1 = new DataArray();
			DataArray data2 = new DataArray();
			DataArray data3 = new DataArray();
			DataArray data4 = new DataArray();
			DataRow dataRow = new DataRow();
			for (int i = 0; i < rowsView.length; i++) {
				// 0 = FSE
				ids[i] = -1;
			}
			for (int x = 0; x < noRows; x++) {
				dataRow = dataRows.get(x);
				// Match 1st node
				switch (rowsView[0]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				default:
					id = -2;
				}
				if (ids[0] != id) {
					ids[0] = id;
					ids[1] = -1;
					ids[2] = -1;
					ids[3] = -1;
					rowNos[0] = -1;
					rowNos[1] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					for (int i = 0; i < dataRoot.children.size(); i++) {
						data1 = dataRoot.children.get(i);
						if (data1.id == ids[0]) {
							rowNos[0] = i;
							break;
						}
					}
					if (rowNos[0] < 0) {
						rowNos[0] = dataRoot.children.size();
						data1 = new DataArray();
						data1.id = ids[0];
						data1.name = name;
						dataRoot.children.add(data1);
					}
				}
				// Match 2nd node
				switch (rowsView[1]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				default:
					id = -2;
				}
				if (ids[1] != id) {
					ids[1] = id;
					ids[2] = -1;
					ids[3] = -1;
					rowNos[1] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					for (int i = 0; i < data1.children.size(); i++) {
						data2 = data1.children.get(i);
						if (data2.id == ids[1]) {
							rowNos[1] = i;
							break;
						}
					}
					if (rowNos[1] < 0) {
						rowNos[1] = data1.children.size();
						data2 = new DataArray();
						data2.id = ids[1];
						data2.name = name;
						data1.children.add(data2);
					}
				}
				// Match 3rd node
				switch (rowsView[2]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				default:
					id = -2;
				}
				if (ids[2] != id) {
					ids[2] = id;
					ids[3] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					for (int i = 0; i < data2.children.size(); i++) {
						data3 = data2.children.get(i);
						if (data3.id == ids[2]) {
							rowNos[2] = i;
							break;
						}
					}
					if (rowNos[2] < 0) {
						rowNos[2] = data2.children.size();
						data3 = new DataArray();
						data3.id = ids[2];
						data3.name = name;
						data2.children.add(data3);
					}
				}
				// Match 4th node
				switch (rowsView[3]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				default:
					id = -2;
				}
				if (ids[3] != id) {
					ids[3] = id;
					rowNos[3] = -1;
					for (int i = 0; i < data3.children.size(); i++) {
						data4 = data3.children.get(i);
						if (data4.id == ids[3]) {
							rowNos[3] = i;
							break;
						}
					}
					if (rowNos[3] < 0) {
						rowNos[3] = data3.children.size();
						data4 = new DataArray();
						data4.id = ids[3];
						data4.name = name;
						data3.children.add(data4);
					}
				}
				data4.noCases += dataRow.noCases;
				data3.noCases += dataRow.noCases;
				data2.noCases += dataRow.noCases;
				data1.noCases += dataRow.noCases;
				dataRoot.noCases += dataRow.noCases;
				data4.noSlides += dataRow.noSlides;
				data3.noSlides += dataRow.noSlides;
				data2.noSlides += dataRow.noSlides;
				data1.noSlides += dataRow.noSlides;
				dataRoot.noSlides += dataRow.noSlides;
				value = numbers.toInt(dataRow.value1);
				data4.value1 += value;
				data3.value1 += value;
				data2.value1 += value;
				data1.value1 += value;
				dataRoot.value1 += value;
				value = numbers.toInt(dataRow.value2);
				data4.value2 += value;
				data3.value2 += value;
				data2.value2 += value;
				data1.value2 += value;
				dataRoot.value2 += value;
				value = numbers.toInt(dataRow.value3);
				data4.value3 += value;
				data3.value3 += value;
				data2.value3 += value;
				data1.value3 += value;
				dataRoot.value3 += value;
				value = numbers.toInt(dataRow.value4);
				data4.value4 += value;
				data3.value4 += value;
				data2.value4 += value;
				data1.value4 += value;
				dataRoot.value4 += value;
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			dataRoot.prcntCases = 100;
			dataRoot.prcntSlides = 100;
			dataRoot.prcnt1 = 100;
			dataRoot.prcnt2 = 100;
			dataRoot.prcnt3 = 100;
			dataRoot.prcnt4 = 100;
			// Calculate FTE's equivalent for # of worked days
			int noDays = parent.dateUtils.getNoDays(timeFrom, timeTo);
			double fte1 = 0, fte2 = 0, fte3 = 0, fte4 = 0;
			fte1 = 1.0 * noDays * parent.variables.codersFTE[0] / 365;
			fte2 = 1.0 * noDays * parent.variables.codersFTE[1] / 365;
			fte3 = 1.0 * noDays * parent.variables.codersFTE[2] / 365;
			fte4 = 1.0 * noDays * parent.variables.codersFTE[3] / 365;
			if (fte1 == 0) {
				fte1 = 1.0;
			}
			if (fte2 == 0) {
				fte2 = 1.0;
			}
			if (fte3 == 0) {
				fte3 = 1.0;
			}
			if (fte4 == 0) {
				fte4 = 1.0;
			}
			dataRoot.fte1 = dataRoot.value1 / fte1;
			dataRoot.fte2 = dataRoot.value2 / fte2;
			dataRoot.fte3 = dataRoot.value3 / fte3;
			dataRoot.fte4 = dataRoot.value4 / fte4;
			setTotals(dataRoot, fte1, fte2, fte3, fte4);
			sortChildren(dataRoot);
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			setModel(dataRoot);
		}

		private void setModel(DataArray dataRoot) {
			TreeTableNode nodeRoot = (TreeTableNode) treeTableModel.getRoot();
			TreeTableNode node1 = new TreeTableNode("node");
			TreeTableNode node2 = new TreeTableNode("node");
			TreeTableNode node3 = new TreeTableNode("node");
			TreeTableNode node4 = new TreeTableNode("node");
			DataArray data1 = new DataArray();
			DataArray data2 = new DataArray();
			DataArray data3 = new DataArray();
			DataArray data4 = new DataArray();
			nodeRoot.id = dataRoot.id;
			nodeRoot.noCases = dataRoot.noCases;
			nodeRoot.noSlides = dataRoot.noSlides;
			nodeRoot.value1 = dataRoot.value1;
			nodeRoot.value2 = dataRoot.value2;
			nodeRoot.value3 = dataRoot.value3;
			nodeRoot.value4 = dataRoot.value4;
			nodeRoot.prcntCases = dataRoot.prcntCases;
			nodeRoot.prcntSlides = dataRoot.prcntSlides;
			nodeRoot.prcnt1 = dataRoot.prcnt1;
			nodeRoot.prcnt2 = dataRoot.prcnt2;
			nodeRoot.prcnt3 = dataRoot.prcnt3;
			nodeRoot.prcnt4 = dataRoot.prcnt4;
			nodeRoot.fte1 = dataRoot.fte1;
			nodeRoot.fte2 = dataRoot.fte2;
			nodeRoot.fte3 = dataRoot.fte3;
			nodeRoot.fte4 = dataRoot.fte4;
			nodeRoot.children = new TreeTableNode[dataRoot.children.size()];
			for (int i = 0; i < dataRoot.children.size(); i++) {
				data1 = dataRoot.children.get(i);
				node1 = new TreeTableNode(data1.name);
				node1.id = data1.id;
				node1.noCases = data1.noCases;
				node1.noSlides = data1.noSlides;
				node1.value1 = data1.value1;
				node1.value2 = data1.value2;
				node1.value3 = data1.value3;
				node1.value4 = data1.value4;
				node1.prcntCases = data1.prcntCases;
				node1.prcntSlides = data1.prcntSlides;
				node1.prcnt1 = data1.prcnt1;
				node1.prcnt2 = data1.prcnt2;
				node1.prcnt3 = data1.prcnt3;
				node1.prcnt4 = data1.prcnt4;
				node1.fte1 = data1.fte1;
				node1.fte2 = data1.fte2;
				node1.fte3 = data1.fte3;
				node1.fte4 = data1.fte4;
				node1.children = new TreeTableNode[data1.children.size()];
				for (int j = 0; j < data1.children.size(); j++) {
					data2 = data1.children.get(j);
					node2 = new TreeTableNode(data2.name);
					node2.id = data2.id;
					node2.noCases = data2.noCases;
					node2.noSlides = data2.noSlides;
					node2.value1 = data2.value1;
					node2.value2 = data2.value2;
					node2.value3 = data2.value3;
					node2.value4 = data2.value4;
					node2.prcntCases = data2.prcntCases;
					node2.prcntSlides = data2.prcntSlides;
					node2.prcnt1 = data2.prcnt1;
					node2.prcnt2 = data2.prcnt2;
					node2.prcnt3 = data2.prcnt3;
					node2.prcnt4 = data2.prcnt4;
					node2.fte1 = data2.fte1;
					node2.fte2 = data2.fte2;
					node2.fte3 = data2.fte3;
					node2.fte4 = data2.fte4;
					node2.children = new TreeTableNode[data2.children.size()];
					for (int k = 0; k < data2.children.size(); k++) {
						data3 = data2.children.get(k);
						node3 = new TreeTableNode(data3.name);
						node3.id = data3.id;
						node3.noCases = data3.noCases;
						node3.noSlides = data3.noSlides;
						node3.value1 = data3.value1;
						node3.value2 = data3.value2;
						node3.value3 = data3.value3;
						node3.value4 = data3.value4;
						node3.prcntCases = data3.prcntCases;
						node3.prcntSlides = data3.prcntSlides;
						node3.prcnt1 = data3.prcnt1;
						node3.prcnt2 = data3.prcnt2;
						node3.prcnt3 = data3.prcnt3;
						node3.prcnt4 = data3.prcnt4;
						node3.fte1 = data3.fte1;
						node3.fte2 = data3.fte2;
						node3.fte3 = data3.fte3;
						node3.fte4 = data3.fte4;
						node3.children = new TreeTableNode[data3.children.size()];
						for (int l = 0; l < data3.children.size(); l++) {
							data4 = data3.children.get(l);
							node4 = new TreeTableNode(data4.name);
							node4.id = data4.id;
							node4.noCases = data4.noCases;
							node4.noSlides = data4.noSlides;
							node4.value1 = data4.value1;
							node4.value2 = data4.value2;
							node4.value3 = data4.value3;
							node4.value4 = data4.value4;
							node4.prcntCases = data4.prcntCases;
							node4.prcntSlides = data4.prcntSlides;
							node4.prcnt1 = data4.prcnt1;
							node4.prcnt2 = data4.prcnt2;
							node4.prcnt3 = data4.prcnt3;
							node4.prcnt4 = data4.prcnt4;
							node4.fte1 = data4.fte1;
							node4.fte2 = data4.fte2;
							node4.fte3 = data4.fte3;
							node4.fte4 = data4.fte4;
							node3.children[l] = node4;
						}
						data3.children.clear();
						node2.children[k] = node3;
					}
					data2.children.clear();
					node1.children[j] = node2;
				}
				data1.children.clear();
				nodeRoot.children[i] = node1;
			}
			dataRoot.children.clear();
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
		}

		private void setTotals(DataArray master, double fte1, double fte2,
				double fte3, double fte4) {
			DataArray child = new DataArray();
			for (int i = master.children.size() -1; i >= 0; i--) {
				child = master.children.get(i);
				if (child.id < 0) {
					// filtered out
					master.children.remove(i);
					continue;
				}
				if (master.noCases > 0) {
					child.prcntCases = (100.00 * child.noCases / master.noCases);
				}
				if (master.noSlides > 0) {
					child.prcntSlides = (100.00 * child.noSlides / master.noSlides);
				}
				if (master.value1 > 0) {
					child.prcnt1 = (100.00 * child.value1 / master.value1);
				}
				if (master.value2 > 0) {
					child.prcnt2 = (100.00 * child.value2 / master.value2);
				}
				if (master.value3 > 0) {
					child.prcnt3 = (100.00 * child.value3 / master.value3);
				}
				if (master.value4 > 0) {
					child.prcnt4 = (100.00 * child.value4 / master.value4);
				}
				child.fte1 = child.value1 / fte1;
				child.fte2 = child.value2 / fte2;
				child.fte3 = child.value3 / fte3;
				child.fte4 = child.value4 / fte4;
				if (child.children.size() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4);
				}
			}
		}

		private void sortChildren(DataArray master) {
			Collections.sort(master.children, new Comparator<DataArray>() {
				public int compare(DataArray o1, DataArray o2) {
					return o1.noSlides - o2.noSlides;
				}
			});
			DataArray child = new DataArray();
			for (int i = 0; i < master.children.size(); i++) {
				child = master.children.get(i);
				if (child.children.size() > 0) {
					sortChildren(child);
				}
			}
		}

		public void done() {
			datesChanged.set(false);
			rowsChanged.set(false);
			parent.setBusy(false);
			// Display results
			TreeTableNode nodeRoot = (TreeTableNode) treeTableModel.getRoot();
			treeTableModel = new WLModel(nodeRoot);
			treeTable.setTreeTableModel(treeTableModel);
			setColumns();
			parent.statusBar.setMessage(infoUpdate);
		}
	}
}
