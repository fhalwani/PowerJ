package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
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

class PnlStaff extends PnlMain {
	private static final long serialVersionUID = -4433696287312271368L;
	private boolean alteredSkills = false;
	private int filters = 0;
	private JStringField txtInitials, txtLastname, txtFirstname;
	private JDateField txtStartdate, txtEnddate;
	private JDoubleField txtFTE;
	private JCheckBox chkActive;
	private CboContracts cboContracts;
	private JTableEditor tblStaff, tblSkills;
	private ArrayList<ClassStaff> lstStaff = new ArrayList<ClassStaff>();
	private ArrayList<ClassSkill> lstSkills = new ArrayList<ClassSkill>();
	private ClassStaff thisStaff = new ClassStaff();

	PnlStaff(PowerJ parent) {
		super(parent);
		setName("Staff");
		parent.dbPowerJ.prepareStaff();
		readTable();
		createPanel();
		programmaticChange = false;
	}

	boolean close() {
		if (altered || alteredSkills) {
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
			lstStaff.clear();
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		// Layout List panel on left side
		add(createPanelList(), BorderLayout.WEST);
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(createPanelNames());
		boxPanel.add(createPanelSkills());
		add(createToolbar(), BorderLayout.NORTH);
		add(boxPanel, BorderLayout.EAST);
		if (lstStaff.size() > 0) {
			tblStaff.setRowSelectionInterval(0,0);
			updateRow(0);
		}
		tblStaff.requestFocusInWindow();
		parent.statusBar.setMessage("No rows " + lstStaff.size());
	}

	private JPanel createPanelList() {
		ModelStaff mdlData = new ModelStaff();
		tblStaff = new JTableEditor(parent, mdlData);
		// detect row selection
		tblStaff.setName("tblPersonnel");
        tblStaff.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int row = lsm.getMinSelectionIndex();
		        if (row > -1) {
					// else, Selection got filtered away.
					int modelRow = tblStaff.convertRowIndexToModel(row);
					updateRow(modelRow);
		        }
			}
        });
		JScrollPane scrollPane = new JScrollPane(tblStaff,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		scrollPane.setBorder(borderEmpty);
		scrollPane.setPreferredSize(new Dimension(320, 600));
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(scrollPane);
		return panel;
	}

	private JPanel createPanelNames() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Details");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Details");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("First Name: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(txtFirstname);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtFirstname = new JStringField(2, 30);
		txtFirstname.setName("Firstname");
		txtFirstname.setEnabled(false);
		Utilities.addComponent(txtFirstname, 1, 0, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Last Name: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_L);
		label.setLabelFor(txtLastname);
		Utilities.addComponent(label, 2, 0, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtLastname = new JStringField(2, 30);
		txtLastname.setName("Lastname");
		txtLastname.setEnabled(false);
		Utilities.addComponent(txtLastname, 3, 0, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Initials: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_I);
		label.setLabelFor(txtInitials);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtInitials = new JStringField(2, 3);
		txtInitials.setName("Initials");
		txtInitials.setEnabled(false);
		Utilities.addComponent(txtInitials, 1, 1, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("FTE: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(txtFTE);
		Utilities.addComponent(label, 2, 1, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtFTE = new JDoubleField(parent, 2, 0.1, 1.2);
		txtFTE.setName("FTE");
		txtFTE.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JDoubleField source = (JDoubleField) e.getSource();
					if (source.altered()) {
						thisStaff.fte = parent.numbers.toInt(100 * source.getDouble());
						thisStaff.altered = true;
						altered = true;
					}
				}
			}
		});
		Utilities.addComponent(txtFTE, 3, 1, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Contract: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_C);
		label.setLabelFor(cboContracts);
		Utilities.addComponent(label, 0, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		cboContracts = new CboContracts(parent);
		cboContracts.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			CboMain cb = (CboMain)e.getSource();
		    			thisStaff.contractID = cb.getIndex();
		    			thisStaff.altered = true;
						altered = true;
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboContracts, 1, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkActive = new JCheckBox();
		chkActive.setName("Active");
		chkActive.setText("Active");
		chkActive.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisStaff.active = source.isSelected();
					thisStaff.altered = true;
					altered = true;
					if (!thisStaff.active) {
						txtEnddate.setEnabled(true);
						txtEnddate.requestFocusInWindow();
					}
				}
			}
		});
		Utilities.addComponent(chkActive, 2, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Start Date: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(txtStartdate);
		Utilities.addComponent(label, 0, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtStartdate = new JDateField();
		txtStartdate.setName("Startdate");
		txtStartdate.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				JDateField source = (JDateField) e.getSource();
				if (source.altered()) {
					thisStaff.startDate = source.getDate();
					thisStaff.altered = true;
					altered = true;
				}
			}
		});
		Utilities.addComponent(txtStartdate, 1, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("End Date: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_E);
		label.setLabelFor(txtEnddate);
		Utilities.addComponent(label, 2, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtEnddate = new JDateField();
		txtEnddate.setName("Enddate");
		txtEnddate.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				JDateField source = (JDateField) e.getSource();
				if (source.altered()) {
					thisStaff.endDate = source.getDate();
					thisStaff.altered = true;
					altered = true;
				}
			}
		});
		Utilities.addComponent(txtEnddate, 3, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private JPanel createPanelSkills() {
		JPanel panel = new JPanel();
		panel.setName("Skills");
		panel.setOpaque(true);
		ModelSkills mdlSkills = new ModelSkills();
		tblSkills = new JTableEditor(parent, mdlSkills);
		JIntegerEditor editor = new JIntegerEditor(0, 100);
		TableColumn column = tblSkills.getColumnModel().getColumn(1);
		column.setCellEditor(editor);
		column.setMinWidth(100);
		column.setMaxWidth(100);
		CboSkills cboSkills = new CboSkills(parent);
		column = tblSkills.getColumnModel().getColumn(0);
		column.setCellEditor(new DefaultCellEditor(cboSkills));
		JScrollPane scroller = new JScrollPane(tblSkills,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -5988770619616488422L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		panel.add(scroller);
		return panel;
	}

	private JPanel createToolbar() {
		CboStaff cboStaff = new CboStaff(parent);
		cboStaff.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			CboMain cb = (CboMain)e.getSource();
	    			filters = cb.getIndex();
	    			readFilters();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Staff:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboStaff);
		panel.add(label);
		panel.add(cboStaff);
		return panel;
	}

	private void readFilters() {
		int noRows = lstStaff.size();
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (filters == 1) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (lstStaff.get(row).active);
				}
			};
			// Count filtered rows
			for (int i = 0; i < lstStaff.size(); i++) {
				if (!lstStaff.get(i).active) {
					noRows--;
				}
			}
		}
		@SuppressWarnings("unchecked")
		TableRowSorter<ModelStaff> sorter = (TableRowSorter<ModelStaff>) tblStaff.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		parent.statusBar.setMessage("No rows " + noRows);
	}

	private void readSkills() {
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			lstSkills.clear();
			stm = parent.dbPowerJ.getStatement(2);
			stm.setShort(1, thisStaff.ID);
			rst = stm.executeQuery();
			while (rst.next()) {
				ClassSkill skill = new ClassSkill();
				skill.oldID = rst.getShort("SKID");
				skill.percent = rst.getInt("PRCNT");
				skill.item = new DataItem(rst.getShort("SKID"),
						rst.getString("DESCR"));
				lstSkills.add(skill);
			}
			// Add a blank row
			ClassSkill skill = new ClassSkill();
			skill.newRow = true;
			lstSkills.add(skill);
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
		}
		alteredSkills = false;
		programmaticChange = true;
		((AbstractTableModel) tblSkills.getModel()).fireTableDataChanged();
		programmaticChange = false;
	}

	private void readTable() {
		ResultSet rst = parent.dbPowerJ.getStaff();
		try {
			while (rst.next()) {
				thisStaff = new ClassStaff();
				thisStaff.ID = rst.getShort("PERID");
				thisStaff.initials = rst.getString("INITIALS");
				thisStaff.lastname = rst.getString("PLAST");
				thisStaff.firstname = rst.getString("PFIRST");
				if (rst.getString("ACTIVE") == null) {
					thisStaff.newRow = true;
				} else {
					thisStaff.active = (rst.getString("ACTIVE").equalsIgnoreCase("Y"));
					thisStaff.fte = rst.getShort("FTE");
					thisStaff.contractID = rst.getShort("CONID");
					thisStaff.startDate.setTime(rst.getDate("STARTD").getTime());
					if (rst.getDate("ENDDATE") != null) {
						thisStaff.endDate.setTime(rst.getDate("ENDDATE").getTime());
					}
				}
				lstStaff.add(thisStaff);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	void save() {
		boolean failed = false;
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
	        if (thisStaff.altered) {
				if (thisStaff.newRow) {
					stm = parent.dbPowerJ.getStatement(0);
				} else {
					stm = parent.dbPowerJ.getStatement(1);
				}
	            stm.setInt(1, thisStaff.contractID);
	            stm.setInt(2, thisStaff.fte);
	            stm.setString(3, (thisStaff.active ? "Y": "N"));
				stm.setDate(4, new java.sql.Date(thisStaff.startDate.getTime()));
				stm.setDate(5, (thisStaff.endDate == null ? null: new java.sql.Date(thisStaff.endDate.getTime())));
	            stm.setShort(6, thisStaff.ID);
				noUpdates = stm.executeUpdate();
	            if (noUpdates > 0) {
		        	thisStaff.altered = false;
		        	thisStaff.newRow = false;
				} else {
					failed = true;
	            }
	        }
			if (!failed) {
				altered = false;
	    		if (alteredSkills) {
	    			saveSkills();
	    		}
			}
 		} catch (NullPointerException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private void saveSkills() {
		boolean failed = false;
		int total = 0;
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < lstSkills.size(); i++) {
				ClassSkill skill = lstSkills.get(i);
				if (total == 100 || skill.percent <= 0) {
					// Delete later, else resource leak warning
					skill.percent = 0;
					skill.altered = true;
				} else if (skill.percent > 100 - total) {
					skill.percent = 100 - total;
					total = 100;
					skill.altered = true;
				} else if (i+2 == lstSkills.size()
						&& skill.percent < 100 - total) {
					// last row must reach 100%
					skill.percent = 100 - total;
					skill.altered = true;
				} else {
					total += skill.percent;
				}
				if (skill.altered) {
		            if (skill.percent == 0) {
						// Delete if saved
			            if (skill.oldID > 0) {
							stm = parent.dbPowerJ.getStatement(5);
				            stm.setShort(1, thisStaff.ID);
				            stm.setShort(2, skill.oldID);
							noUpdates = stm.executeUpdate();
			            } else {
							noUpdates = 1;
			            }
		            } else if (skill.newRow) {
						// Insert
						stm = parent.dbPowerJ.getStatement(3);
			            stm.setInt(1, skill.item.getValue());
			            stm.setInt(2, skill.percent);
			            stm.setShort(3, thisStaff.ID);
						noUpdates = stm.executeUpdate();
					} else {
						// Update
						stm = parent.dbPowerJ.getStatement(4);
			            stm.setInt(1, skill.item.getValue());
			            stm.setInt(2, skill.percent);
			            stm.setShort(3, thisStaff.ID);
			            stm.setShort(4, skill.oldID);
						noUpdates = stm.executeUpdate();
					}
		            if (noUpdates > 0) {
			        	skill.altered = false;
			        	skill.newRow = false;
			        	skill.oldID = (short) skill.item.getValue();
					} else {
						failed = true;
		            }
		        }
			}
			if (!failed) {
				alteredSkills = false;
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private void updateRow(int row) {
		if (altered || alteredSkills) {
			save();
		}
		// Move to desired row
		programmaticChange = true;
		thisStaff = lstStaff.get(row);
		txtFirstname.setText(thisStaff.firstname);
		txtLastname.setText(thisStaff.lastname);
		txtInitials.setText(thisStaff.initials);
		txtFTE.setDouble(parent.numbers.toDouble(2, thisStaff.fte));
		txtStartdate.setText(thisStaff.startDate);
		txtEnddate.setText(thisStaff.endDate);
		chkActive.setSelected(thisStaff.active);
		cboContracts.setIndex(thisStaff.contractID);
		programmaticChange = false;
		readSkills();
	}
	
	private class ClassStaff {
		boolean altered = false;
		boolean newRow = false;
		boolean active = false;
		short ID = 1;
		int fte = 100;
		int contractID = 0;
		Date startDate = new Date(0);
		Date endDate = null;
		String initials = "";
		String lastname = "";
		String firstname = "";
	}

	private class ClassSkill {
		boolean altered = false;
		boolean newRow = false;
		short oldID = 0;
		int percent = 0;
		DataItem item = new DataItem(0, "");
	}

	private class ModelStaff extends AbstractTableModel {
		private static final long serialVersionUID = -5267429375007369112L;

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return lstStaff.size();
		}

		public String getColumnName(int col) {
			return "Staff";
		}

		public Object getValueAt(int row, int col) {
			if (row < lstStaff.size()) {
				ClassStaff item = lstStaff.get(row);
				return item.lastname + ", " + item.firstname;
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

	private class ModelSkills extends AbstractTableModel {
		private static final long serialVersionUID = 1944699269514273301L;
		private final String[] columns = {"SKILLS", "PRCNT"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return lstSkills.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			if (row < lstSkills.size()) {
				ClassSkill skill = lstSkills.get(row);
				if (col == 0) {
					return skill.item;
				} else {
					return skill.percent;
				}
			}
			return "";
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return DataItem.class;
			} else {
				return Integer.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassSkill skill = lstSkills.get(row);
				if (col == 0) {
					skill.item = (DataItem) value;
				} else {
					skill.percent = (Integer) value;
				}
				if (skill.item.getValue() > 0) {
					skill.altered = true;
					alteredSkills = true;
					if (row == lstSkills.size() -1) {
						// Add a blank row
						skill = new ClassSkill();
						skill.newRow = true;
						lstSkills.add(skill);
						fireTableDataChanged();
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
