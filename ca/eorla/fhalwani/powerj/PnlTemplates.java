package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

class PnlTemplates extends PnlMain implements ItemListener {
	private static final long serialVersionUID = -920886954396626057L;
	private volatile boolean programmaticChange = true;
	private int[] filters = {0, 0, 0};
	private JStringField txtName, txtDescr;
	private JIntegerField txtGross, txtEmbed, txtMicro, txtRoute, txtSignout;
	private JCheckBox chkBoxLN;
	private CboSpecialties cboSpecialties;
	private CboSubspecial cboSubspecial;
	private CboProcedures cboProcedures;
	private ClassData thisRow = new ClassData();
	private ModelData mdlData;
	private ModelCodes mdlCodes = new ModelCodes();
	private JTableEditor tblData, tblCodes;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();

	PnlTemplates(PowerJ parent) {
		super(parent);
		setName("Templates");
		parent.dbPowerJ.prepareTemplates();
		readTable();
		createPanel();
		programmaticChange = false;
	}

	boolean close() {
		inspectRow();
		if (altered) {
			int option = parent.askSave(getName());
			switch (option) {
			case Utilities.OPTION_YES:
				save();
				break;
			case Utilities.OPTION_NO:
				altered = false;
				break;
			default:
				// Cancel close
			}
		}
		if (!altered) {
			list.clear();
			super.close();
		}
		return !altered;
	}
	
	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		// Layout 3 panels from top to bottom.
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(createPanelNames());
		boxPanel.add(createPanelDashboard());
		boxPanel.add(createPanelWorkload());
		add(createToolbar(), BorderLayout.NORTH);
		add(boxPanel, BorderLayout.CENTER);
		// Layout List panel on left side
		add(createPanelList(), BorderLayout.WEST);
		tblData.requestFocusInWindow();
		parent.statusBar.setMessage("No rows " + list.size());
	}
	
	private JPanel createPanelDashboard() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Dashboard");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Dashboard");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Gross Time: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_G);
		label.setLabelFor(txtGross);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtGross = new JIntegerField(parent, 1, Byte.MAX_VALUE);
		txtGross.setName("txtGross");
		Utilities.addComponent(txtGross, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Embed: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_E);
		label.setLabelFor(txtEmbed);
		Utilities.addComponent(label, 2, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtEmbed = new JIntegerField(parent, 1, Byte.MAX_VALUE);
		txtEmbed.setName("txtEmbed");
		Utilities.addComponent(txtEmbed, 3, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Micro Time: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_M);
		label.setLabelFor(txtMicro);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtMicro = new JIntegerField(parent, 1, Byte.MAX_VALUE);
		txtMicro.setName("txtMicro");
		Utilities.addComponent(txtMicro, 1, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Route: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_R);
		label.setLabelFor(txtRoute);
		Utilities.addComponent(label, 2, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtRoute = new JIntegerField(parent, 1, Byte.MAX_VALUE);
		txtRoute.setName("txtRoute");
		Utilities.addComponent(txtRoute, 3, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Signout: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_I);
		label.setLabelFor(txtSignout);
		Utilities.addComponent(label, 0, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtSignout = new JIntegerField(parent, 1, Byte.MAX_VALUE);
		txtSignout.setName("txtSignout");
		Utilities.addComponent(txtSignout, 1, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private JPanel createPanelList() {
		mdlData = new ModelData();
		tblData = new JTableEditor(parent, mdlData);
		// detect row selection
		tblData.setName("tblData");
        tblData.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int viewRow = lsm.getMinSelectionIndex();
		        if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblData.convertRowIndexToModel(viewRow);
					updateRow(modelRow);
		        }
			}
        });
		JScrollPane scrollPane = new JScrollPane(tblData,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		scrollPane.setBorder(borderEmpty);
		scrollPane.setPreferredSize(new Dimension(200, 500));
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(scrollPane);
		return panel;
	}
	
	private JPanel createPanelNames() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Template");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Template");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Name: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_N);
		label.setLabelFor(txtName);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtName = new JStringField(2, 15);
		txtName.setName("txtName");
		Utilities.addComponent(txtName, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		chkBoxLN = new JCheckBox();
		chkBoxLN.setName("chkBoxLN");
		chkBoxLN.setText("Has LN: ");
		chkBoxLN.setMnemonic(KeyEvent.VK_H);
		chkBoxLN.addItemListener(this);
		Utilities.addComponent(chkBoxLN, 2, 0, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Descr: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_D);
		label.setLabelFor(txtDescr);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtDescr = new JStringField(2, 80);
		txtDescr.setName("txtDescr");
		Utilities.addComponent(txtDescr, 1, 1, 5, 1, 0.5, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Procedure: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setLabelFor(cboProcedures);
		Utilities.addComponent(label, 0, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		cboProcedures = new CboProcedures(parent, true);
		cboProcedures.addItemListener(this);
		Utilities.addComponent(cboProcedures, 1, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Specialty: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboSpecialties);
		Utilities.addComponent(label, 2, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		cboSpecialties = new CboSpecialties(parent, true);
		cboSpecialties.addItemListener(this);
		Utilities.addComponent(cboSpecialties, 3, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Subspecialty: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_U);
		label.setLabelFor(cboSubspecial);
		Utilities.addComponent(label, 4, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		cboSubspecial = new CboSubspecial(parent, true);
		cboSubspecial.addItemListener(this);
		Utilities.addComponent(cboSubspecial, 5, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private JPanel createPanelWorkload() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Workload");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Workload");
		panel.setOpaque(true);
		byte coderID = 1;
		CboCoder cboCoder1 = new CboCoder(parent, true, coderID++);
		CboCoder cboCoder2 = new CboCoder(parent, true, coderID++);
		CboCoder cboCoder3 = new CboCoder(parent, true, coderID++);
		CboCoder cboCoder4 = new CboCoder(parent, true, coderID++);
		tblCodes = new JTableEditor(parent, mdlCodes);
		tblCodes.setFillsViewportHeight(true);
		TableColumn column = null;
		for (int i = 1; i < 5; i++) {
			column = tblCodes.getColumnModel().getColumn(i);
			switch (i) {
			case 1:
				column.setCellEditor(new DefaultCellEditor(cboCoder1));
				break;
			case 2:
				column.setCellEditor(new DefaultCellEditor(cboCoder2));
				break;
			case 3:
				column.setCellEditor(new DefaultCellEditor(cboCoder3));
				break;
			default:
				column.setCellEditor(new DefaultCellEditor(cboCoder4));
			}
		}
		JScrollPane scrollPane = new JScrollPane(tblCodes,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 326630825685874528L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		scrollPane.setPreferredSize(new Dimension(500, 200));
		panel.add(scrollPane);
		return panel;
	}

	private JPanel createToolbar() {
		// Setup 3 JComboBox and fill with their data
		CboSpecialties cboSpecialties = new CboSpecialties(parent, false);
		cboSpecialties.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[0] = item.getValue();
	    			setFilters();
	    		}
	        }
	    });
		CboSubspecial cboSubspecial = new CboSubspecial(parent, false);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[1] = item.getValue();
	    			setFilters();
	    		}
	        }
	    });
		CboProcedures cboProcs = new CboProcedures(parent, false);
		cboProcs.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[2] = item.getValue();
	    			setFilters();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Specialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboSpecialties);
		panel.add(label);
		panel.add(cboSpecialties);
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
		label.setLabelFor(cboProcs);
		panel.add(label);
		panel.add(cboProcs);
		return panel;
	}

	private void inspectRow() {
		if (!thisRow.altered) {
			if (txtName.altered()) {
				thisRow.altered = true;
			} else if (txtDescr.altered()) {
				thisRow.altered = true;
			} else if (txtGross.altered()) {
				thisRow.altered = true;
			} else if (txtEmbed.altered()) {
				thisRow.altered = true;
			} else if (txtMicro.altered()) {
				thisRow.altered = true;
			} else if (txtRoute.altered()) {
				thisRow.altered = true;
			} else if (txtSignout.altered()) {
				thisRow.altered = true;
			}
		}
		if (thisRow.altered) {
			// Store it
			altered = true;
			thisRow.name = txtName.getText();
			thisRow.descr = txtDescr.getText();
			thisRow.procedure = (DataItem) cboProcedures.getSelectedItem();
			thisRow.specialty = (DataItem) cboSpecialties.getSelectedItem();
			thisRow.subspecialty = (DataItem) cboSubspecial.getSelectedItem();
			thisRow.isLN = chkBoxLN.isSelected();
			thisRow.grossTime = txtGross.getShort();
			thisRow.embedTime = txtEmbed.getShort();
			thisRow.microTime = txtMicro.getShort();
			thisRow.routeTime = txtRoute.getShort();
			thisRow.signoutTime = txtSignout.getShort();
		}
	}

	private void readTable() {
		short procID = 0;
		short lastID = 0;
		ResultSet rst = parent.dbPowerJ.getMasterSpecimens(0);
		ClassData thisRow = new ClassData();
		DataItem procedure = new DataItem(0, "");
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("MSID");
				thisRow.name = rst.getString("CODE");
				thisRow.descr = rst.getString("DESCR");
				thisRow.isLN = (rst.getInt("ISLN") > 0);
				thisRow.grossTime = rst.getShort("GROSS");
				thisRow.embedTime = rst.getShort("EMBED");
				thisRow.microTime = rst.getShort("MICROTOMY");
				thisRow.routeTime = rst.getShort("ROUTE");
				thisRow.signoutTime = rst.getShort("SIGNOUT");
				thisRow.specialty = new DataItem(rst.getShort("SPYID"),
						rst.getString("SPYNAME"));
				thisRow.subspecialty = new DataItem(rst.getShort("SUBID"),
						rst.getString("SUBNAME"));
				thisRow.benign.coder1 = new DataItem(rst.getShort("CODE1B"),
						rst.getString("CODE1NB"));
				thisRow.malignant.coder1 = new DataItem(rst.getShort("CODE1M"),
						rst.getString("CODE1NM"));
				thisRow.radical.coder1 = new DataItem(rst.getShort("CODE1R"),
						rst.getString("CODE1NR"));
				thisRow.benign.coder2 = new DataItem(rst.getShort("CODE2B"),
						rst.getString("CODE2NB"));
				thisRow.malignant.coder2 = new DataItem(rst.getShort("CODE2M"),
						rst.getString("CODE2NM"));
				thisRow.radical.coder2 = new DataItem(rst.getShort("CODE2R"),
						rst.getString("CODE2NR"));
				thisRow.benign.coder3 = new DataItem(rst.getShort("CODE3B"),
						rst.getString("CODE3NB"));
				thisRow.malignant.coder3 = new DataItem(rst.getShort("CODE3M"),
						rst.getString("CODE3NM"));
				thisRow.radical.coder3 = new DataItem(rst.getShort("CODE3R"),
						rst.getString("CODE3NR"));
				thisRow.benign.coder4 = new DataItem(rst.getShort("CODE4B"),
						rst.getString("CODE4NB"));
				thisRow.malignant.coder4 = new DataItem(rst.getShort("CODE4M"),
						rst.getString("CODE4NM"));
				thisRow.radical.coder4 = new DataItem(rst.getShort("CODE4R"),
						rst.getString("CODE4NR"));
				procID = rst.getShort("PROCID");
				if (procID >= 0 && procID < DataProcedure.NAMES.length) {
					procedure = new DataItem(procID, DataProcedure.NAMES[procID]);
				} else {
					procedure = new DataItem(0, DataProcedure.NAMES[0]);
				}
				thisRow.procedure = procedure;
				list.add(thisRow);
				if (lastID < thisRow.ID) {
					lastID = thisRow.ID;
				}
			}
			if (!parent.variables.offLine) {
				readUpdates(lastID);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private void readUpdates(short lastID) {
		DbAPIS dbAP = new DbAPIS(parent);
		if (!dbAP.connected) {
			return;
		}
		int noInserts = 0;
		ClassData thisRow = new ClassData();
		ResultSet rst = dbAP.getMasterSpecimens(lastID);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.newRow = true;
				thisRow.altered = true;
				thisRow.ID = rst.getShort("id");
				thisRow.name = rst.getString("code").trim();
				thisRow.descr = rst.getString("description").trim();
				list.add(thisRow);
				noInserts++;
			}
			if (noInserts > 0) {
				parent.log(JOptionPane.INFORMATION_MESSAGE, getName(),
						"Found " + noInserts + " new specimen(s) in Powerpath.");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			dbAP.closeRst(rst);
			dbAP.closeStm();
			dbAP.close();
		}
	}
	
	void save() {
		boolean failed = false;
        int noUpdates = 0;
		PreparedStatement stm = null;
		try {
	        inspectRow();
			for (int i = 0; i < list.size(); i++) {
		        thisRow = list.get(i);
		        if (thisRow.altered) {
					thisRow.name = thisRow.name.trim();
			        if (thisRow.name.length() > 15) {
			        	thisRow.name = thisRow.name.substring(0, 15);
			        }
					thisRow.descr = thisRow.descr.trim();
			        if (thisRow.descr.length() > 80) {
			        	thisRow.descr = thisRow.descr.substring(0, 80);
			        }
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisRow.specialty.getValue());
					stm.setInt(2, thisRow.subspecialty.getValue());
					stm.setInt(3, thisRow.procedure.getValue());
					stm.setInt(4, (thisRow.isLN ? 1 : 0));
					stm.setShort(5, thisRow.grossTime);
					stm.setShort(6, thisRow.embedTime);
					stm.setShort(7, thisRow.microTime);
					stm.setShort(8, thisRow.routeTime);
					stm.setShort(9, thisRow.signoutTime);
					stm.setInt(10, thisRow.benign.coder1.getValue());
					stm.setInt(11, thisRow.malignant.coder1.getValue());
					stm.setInt(12, thisRow.radical.coder1.getValue());
					stm.setInt(13, thisRow.benign.coder2.getValue());
					stm.setInt(14, thisRow.malignant.coder2.getValue());
					stm.setInt(15, thisRow.radical.coder2.getValue());
					stm.setInt(16, thisRow.benign.coder3.getValue());
					stm.setInt(17, thisRow.malignant.coder3.getValue());
					stm.setInt(18, thisRow.radical.coder3.getValue());
					stm.setInt(19, thisRow.benign.coder4.getValue());
					stm.setInt(20, thisRow.malignant.coder4.getValue());
					stm.setInt(21, thisRow.radical.coder4.getValue());
		            stm.setString(22, thisRow.name);
		            stm.setString(23, thisRow.descr);
		            stm.setInt(24, thisRow.ID);
					noUpdates = stm.executeUpdate();
		            if (noUpdates > 0) {
			        	thisRow.altered = false;
			        	thisRow.newRow = false;
					} else {
						failed = true;
		            }
		        }
			}
			if (!failed) {
				altered = false;
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}
	
	private void setFilters() {
		int noFilters = 0;
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> lstFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[0] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).specialty.getValue() == filters[0]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[1] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).subspecialty.getValue() == filters[1]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[2] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).procedure.getValue() == filters[2]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (noFilters > 0) {
			// Add to the compound filter
			lstFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(lstFilters);
		}
		@SuppressWarnings("unchecked")
		TableRowSorter<ModelData> sorter = (TableRowSorter<ModelData>) tblData.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		// Count filtered rows
		int noCases = list.size();
		if (filters[0] > 0 || filters[1] > 0 || filters[2] > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (filters[0] > 0 && filters[0] != list.get(i).specialty.getValue()) {
					noCases--;
				} else if (filters[1] > 0 && filters[1] != list.get(i).subspecialty.getValue()) {
					noCases--;
				} else if (filters[2] > 0 && filters[2] != list.get(i).procedure.getValue()) {
					noCases--;
				}
			}
		}
		parent.statusBar.setMessage("No rows " + noCases);
	}
	
	private void updateRow(int row) {
		inspectRow();
		programmaticChange = true;
		if (row > list.size() -1) {
			// Called from an empty list; clear text fields
			thisRow = new ClassData();
		} else {
			thisRow = list.get(row);
		}
		txtName.setText(thisRow.name);
		txtDescr.setText(thisRow.descr);
		txtGross.setInt(thisRow.grossTime);
		txtEmbed.setInt(thisRow.embedTime);
		txtMicro.setInt(thisRow.microTime);
		txtRoute.setInt(thisRow.routeTime);
		txtSignout.setInt(thisRow.signoutTime);
		chkBoxLN.setSelected(thisRow.isLN);
		cboProcedures.setIndex(thisRow.procedure);
		cboSpecialties.setIndex(thisRow.specialty);
		cboSubspecial.setIndex(thisRow.subspecialty);
		mdlCodes.fireTableDataChanged();
		programmaticChange = false;
	}
	
	public void itemStateChanged(ItemEvent e) {
		thisRow.altered =  !programmaticChange;
	}

	private class ClassData {
		boolean altered = false;
		boolean newRow = false;
		boolean isLN = false;
		short grossTime = 0;
		short embedTime = 0;
		short microTime = 0;
		short routeTime = 0;
		short signoutTime = 0;
		short ID = 0;
		// specialtyID + specialtyName
		DataItem specialty = new DataItem(1, "");
		DataItem subspecialty = new DataItem(1, "");
		DataItem procedure = new DataItem(1, "");
		// coders
		ClassWorkload benign = new ClassWorkload();
		ClassWorkload malignant = new ClassWorkload();
		ClassWorkload radical = new ClassWorkload();
		String name = "";
		String descr = "";
	}
	
	private class ClassWorkload {
		// coderID + coderName
		DataItem coder1 = new DataItem(0, "");
		DataItem coder2 = new DataItem(0, "");
		DataItem coder3 = new DataItem(0, "");
		DataItem coder4 = new DataItem(0, "");
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = -5234299805312093585L;

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return "Specimen";
		}

		public Object getValueAt(int row, int col) {
			if (row < list.size()) {
				return list.get(row).name;
			}
			return "";
		}

		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			// This table is not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, table not editable
		}
	}

	private class ModelCodes extends AbstractTableModel {
		private static final long serialVersionUID = 5398752556061701062L;
		private final String[] columns = {"Catg",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};
		private final String[] rows = {"Benign", "Malignant", "Radical"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return 3;
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object objValue = Object.class;
			try {
				ClassWorkload workload;
				switch (row) {
				case 0:
					workload = thisRow.benign;
					break;
				case 1:
					workload = thisRow.malignant;
					break;
				default:
					workload = thisRow.radical;
				}
				switch (col) {
				case 1:
					objValue = workload.coder1;
					break;
				case 2:
					objValue = workload.coder2;
					break;
				case 3:
					objValue = workload.coder3;
					break;
				case 4:
					objValue = workload.coder4;
					break;
				default:
					objValue = rows[row];
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}
		
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return DataItem.class;
		}

		public boolean isCellEditable(int row, int col) {
			return (col > 0);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassWorkload workload;
				switch (row) {
				case 0:
					workload = thisRow.benign;
					break;
				case 1:
					workload = thisRow.malignant;
					break;
				default:
					workload = thisRow.radical;
				}
				switch (col) {
				case 1:
					workload.coder1 = (DataItem) value;
					break;
				case 2:
					workload.coder2 = (DataItem) value;
					break;
				case 3:
					workload.coder3 = (DataItem) value;
					break;
				case 4:
					workload.coder4 = (DataItem) value;
					break;
				default:
					// not editable
				}
				thisRow.altered = true;
				altered = true;
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
