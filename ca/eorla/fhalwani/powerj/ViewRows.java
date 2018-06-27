package ca.eorla.fhalwani.powerj;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ViewRows extends JPanel {
	private static final long serialVersionUID = 675644246792281606L;
	private final int min = 0;
	private int max = 4;
	private volatile boolean programmaticChange;
	private int[] selected, original;
	private ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
	private final Timer selectionTimer;
	
	public ViewRows(int[] value) {
		super(new GridBagLayout());
		original = value;
		selected = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
			original[i] = value[i];
		}
		selectionTimer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!selected.equals(original)) {
					firePropertyChange("Value", original, selected);
				}
			}
		});
		selectionTimer.setRepeats(false);
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		int[] rank = new int[selected.length];
		for (byte i = 0; i < selected.length; i++) {
			switch (selected[i]) {
			case Constants.ROW_FACILITY:
				rank[0] = i+1;
				break;
			case Constants.ROW_SPECIALTY:
				rank[1] = i+1;
				break;
			case Constants.ROW_SUBSPECIALTY:
				rank[2] = i+1;
				break;
			case Constants.ROW_STAFF:
				rank[3] = i+1;
				break;
			case Constants.ROW_PROCEDURE:
				rank[4] = i+1;
				max = 5;
				break;
			default:
				// 0 = ignore
			}
		}
		SpinnerNumberModel mdlFacility = new SpinnerNumberModel(rank[0], min, max, 1);
		JSpinner spnFacility = new JSpinner(mdlFacility) {
			private static final long serialVersionUID = 2450610846855975702L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnFacility.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(0).getValue()).intValue();
					resetValues(Constants.ROW_FACILITY, newValue);
				}
			}
		});
		JFormattedTextField ftfFacility = getTextField(spnFacility);
		if (ftfFacility != null ) {
			ftfFacility.setColumns(5);
			ftfFacility.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnFacility.setEditor(new JSpinner.NumberEditor(spnFacility, "#"));
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		spnFacility.addAncestorListener(new RequestFocusListener());
		JLabel lblFacility = new JLabel("Facility");
		lblFacility.setFont(Constants.APP_FONT);
		lblFacility.setBorder(border);
		lblFacility.setDisplayedMnemonic(KeyEvent.VK_F);
		lblFacility.setLabelFor(spnFacility);
		Utilities.addComponent(lblFacility, 0, 0, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		Utilities.addComponent(spnFacility, 1, 0, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnFacility);
		SpinnerNumberModel mdlSpecialty = new SpinnerNumberModel(rank[1], min, max, 1);
		JSpinner spnSpecialty = new JSpinner(mdlSpecialty) {
			private static final long serialVersionUID = 5146612017005516647L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnSpecialty.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(1).getValue()).intValue();
					resetValues(Constants.ROW_SPECIALTY, newValue);
				}
			}
		});
		JFormattedTextField ftfSpecialty = getTextField(spnSpecialty);
		if (ftfSpecialty != null ) {
			ftfSpecialty.setColumns(5);
			ftfSpecialty.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnSpecialty.setEditor(new JSpinner.NumberEditor(spnSpecialty, "#"));
		JLabel lblSpecialty = new JLabel("Specialty");
		lblSpecialty.setFont(Constants.APP_FONT);
		lblSpecialty.setBorder(border);
		lblSpecialty.setDisplayedMnemonic(KeyEvent.VK_S);
		lblSpecialty.setLabelFor(spnSpecialty);
		Utilities.addComponent(lblSpecialty, 0, 1, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		Utilities.addComponent(spnSpecialty, 1, 1, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnSpecialty);
		SpinnerNumberModel mdlSubspecialty = new SpinnerNumberModel(rank[2], min, max, 1);
		JSpinner spnSubspecialty = new JSpinner(mdlSubspecialty) {
			private static final long serialVersionUID = -5306144978669153048L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnSubspecialty.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(2).getValue()).intValue();
					resetValues(Constants.ROW_SUBSPECIALTY, newValue);
				}
			}
		});
		JFormattedTextField ftfSubspecialty = getTextField(spnSubspecialty);
		if (ftfSubspecialty != null ) {
			ftfSubspecialty.setColumns(5);
			ftfSubspecialty.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnSubspecialty.setEditor(new JSpinner.NumberEditor(spnSubspecialty, "#"));
		JLabel lblSubspecialty = new JLabel("Subspecialty");
		lblSubspecialty.setFont(Constants.APP_FONT);
		lblSubspecialty.setBorder(border);
		lblSubspecialty.setDisplayedMnemonic(KeyEvent.VK_B);
		lblSubspecialty.setLabelFor(spnSubspecialty);
		Utilities.addComponent(lblSubspecialty, 0, 2, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		Utilities.addComponent(spnSubspecialty, 1, 2, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnSubspecialty);
		SpinnerNumberModel mdlStaff = new SpinnerNumberModel(rank[3], min, max, 1);
		JSpinner spnStaff= new JSpinner(mdlStaff) {
			private static final long serialVersionUID = 6556424348594464923L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnStaff.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(3).getValue()).intValue();
					resetValues(Constants.ROW_STAFF, newValue);
				}
			}
		});
		JFormattedTextField ftfStaff = getTextField(spnStaff);
		if (ftfStaff != null ) {
			ftfStaff.setColumns(5);
			ftfStaff.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnStaff.setEditor(new JSpinner.NumberEditor(spnStaff, "#"));
		JLabel lblStaff = new JLabel("Pathologists");
		lblStaff.setFont(Constants.APP_FONT);
		lblStaff.setBorder(border);
		lblStaff.setDisplayedMnemonic(KeyEvent.VK_P);
		lblStaff.setLabelFor(spnStaff);
		Utilities.addComponent(lblStaff, 0, 3, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		Utilities.addComponent(spnStaff, 1, 3, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnStaff);
		if (rank.length == 5) {
			SpinnerNumberModel mdlProcedures = new SpinnerNumberModel(rank[4], min, max, 1);
			JSpinner spnProcedures= new JSpinner(mdlProcedures) {
				private static final long serialVersionUID = 402555220787352560L;
				public ComponentOrientation getComponentOrientation() {
					return ComponentOrientation.RIGHT_TO_LEFT;
				}
			};
			spnProcedures.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (!programmaticChange) {
						int newValue = ((Integer) spinners.get(4).getValue()).intValue();
						resetValues(Constants.ROW_PROCEDURE, newValue);
					}
				}
			});
			JFormattedTextField ftfProcedures = getTextField(spnProcedures);
			if (ftfProcedures != null ) {
				ftfProcedures.setColumns(5);
				ftfProcedures.setHorizontalAlignment(JTextField.RIGHT);
			}
			// Make the format without a thousands separator.
			spnProcedures.setEditor(new JSpinner.NumberEditor(spnProcedures, "#"));
			JLabel lblProcedures = new JLabel("Procedures");
			lblProcedures.setFont(Constants.APP_FONT);
			lblProcedures.setBorder(border);
			lblProcedures.setDisplayedMnemonic(KeyEvent.VK_R);
			lblProcedures.setLabelFor(spnProcedures);
			Utilities.addComponent(lblProcedures, 0, 4, 1, 1, 0.5, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
			Utilities.addComponent(spnProcedures, 1, 4, 1, 1, 0.5, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
			spinners.add(spnProcedures);
		}
		JButton btnOkay = new JButton("OK");
		btnOkay.setMnemonic(KeyEvent.VK_O);
		btnOkay.setIcon(Utilities.getIcon("ok"));
		btnOkay.setActionCommand("Okay");
		btnOkay.setFocusable(true);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViewRows.this.firePropertyChange("Confirm", null, selected);
			}
		});
		if (rank.length == 5) {
			Utilities.addComponent(btnOkay, 0, 5, 2, 1, 1.0, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		} else {
			Utilities.addComponent(btnOkay, 0, 4, 2, 1, 1.0, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		}
	}

	/**
	 * Return the formatted text field used by the editor, or
	 * null if the editor doesn't descend from JSpinner.DefaultEditor.
	 */
	public JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor)editor).getTextField();
		} else {
			return null;
		}
	}

	public int[] getValue() {
		return selected;
	}
	
	private void resetValues(int element, int newPos) {
		boolean previousChange = programmaticChange;
		programmaticChange = true;
		if (newPos == 0) {
			// put it in last position so it will be ignored
			for (int i = 0; i < selected.length-1; i++) {
				selected[i] = selected[i+1];
			}
			selected[selected.length-1] = 0;
		} else {
			// Switch old element and new element positions, if not 0
			int oldElement = selected[newPos-1];
			if (oldElement != 0) {
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] == element) {
						SpinnerNumberModel model = (SpinnerNumberModel) spinners.get(0).getModel();
						switch (oldElement) {
						case Constants.ROW_FACILITY:
							model = (SpinnerNumberModel) spinners.get(0).getModel();
							break;
						case Constants.ROW_SPECIALTY:
							model = (SpinnerNumberModel) spinners.get(1).getModel();
							break;
						case Constants.ROW_SUBSPECIALTY:
							model = (SpinnerNumberModel) spinners.get(2).getModel();
							break;
						case Constants.ROW_STAFF:
							model = (SpinnerNumberModel) spinners.get(3).getModel();
							break;
						default:
							model = (SpinnerNumberModel) spinners.get(4).getModel();
						}
						model.setValue(i+1);
						selected[i] = oldElement;
						break;
					}
				}
			}
			selected[newPos-1] = element;
		}
		programmaticChange = previousChange;
		if (selectionTimer.isRunning()) {
			selectionTimer.restart();
		} else {
			selectionTimer.start();
		}
	}

	private void setValue() {
		if (!programmaticChange) {
			boolean previousChange = programmaticChange;
			programmaticChange = true;
			int[] rank = new int[selected.length];
			for (int i = 0; i < selected.length; i++) {
				original[i] = selected[i];
				switch (selected[i]) {
				case Constants.ROW_FACILITY:
					rank[0] = i+1;
					break;
				case Constants.ROW_SPECIALTY:
					rank[1] = i+1;
					break;
				case Constants.ROW_SUBSPECIALTY:
					rank[2] = i+1;
					break;
				case Constants.ROW_STAFF:
					rank[3] = i+1;
					break;
				case Constants.ROW_PROCEDURE:
					rank[4] = i+1;
					break;
				default:
					// 0 = ignore
				}
			}
			SpinnerNumberModel model = (SpinnerNumberModel) spinners.get(0).getModel();
			int oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[0]) {
				model.setValue(rank[0]);
			}
			model = (SpinnerNumberModel) spinners.get(1).getModel();
			oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[1]) {
				model.setValue(rank[1]);
			}
			model = (SpinnerNumberModel) spinners.get(2).getModel();
			oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[2]) {
				model.setValue(rank[2]);
			}
			model = (SpinnerNumberModel) spinners.get(3).getModel();
			oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[3]) {
				model.setValue(rank[3]);
			}
			if (rank.length == 5) {
				model = (SpinnerNumberModel) spinners.get(4).getModel();
				oldValue = ((Integer) model.getValue()).intValue();
				if (oldValue != rank[4]) {
					model.setValue(rank[4]);
				}
			}
			programmaticChange = previousChange;
		}
	}
	
	void setValue(int[] newValue) {
		for (int i = 0; i < newValue.length; i++) {
			selected[i] = newValue[i];
		}
		setValue();
	}
}
