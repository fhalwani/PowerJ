package ca.eorla.fhalwani.powerj;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

class ViewColumns extends JPanel {
	private static final long serialVersionUID = -3686429187725624271L;
	private volatile boolean programmaticChange;
	private boolean[] selected, original;
	private final Timer selectionTimer;
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();

	public ViewColumns(boolean[] value, PowerJ parent) {
		super();
		selectionTimer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!selected.equals(original)) {
					firePropertyChange("Value", original, selected);
				}
			}
			
		});
		selectionTimer.setRepeats(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		String[] labels = {"Cases", "Slides", "Coder1",
				"Coder2", "Coder3", "Coder4", "Count", "Relative", "FTE"};
		boolean isActive = true;
		original = value;
		selected = new boolean[value.length];
		for (byte i = 0; i < value.length; i++) {
			selected[i] = value[i];
			if (i > 1 && i < 6) {
				isActive = parent.variables.codersActive[i -2];
				labels[i] = parent.variables.codersName[i -2];
			} else {
				isActive = true;
			}
			if (isActive) {
				JCheckBox checkBox = new JCheckBox();
				checkBox.setBorder(border);
				checkBox.setText(labels[i]);
				checkBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				final byte x = i;
				checkBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (!programmaticChange) {
							selected[x] = ((JCheckBox)e.getSource()).isSelected();
							if (selectionTimer.isRunning()) {
								selectionTimer.restart();
							} else {
								selectionTimer.start();
							}
						}
					}
				});
				checkboxes.add(checkBox);
				add(checkBox);
			}
		}
		JButton btnOkay = new JButton("OK");
		btnOkay.setMnemonic(KeyEvent.VK_O);
		btnOkay.setIcon(Utilities.getIcon("ok"));
		btnOkay.setActionCommand("Okay");
		btnOkay.setFocusable(true);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firePropertyChange("Confirm", original, selected);
			}
		});
		add(btnOkay);
	}

	public boolean[] getValue() {
		return selected;
	}
	
	private void setValue() {
		if (!programmaticChange) {
			boolean previousChange = programmaticChange;
			programmaticChange = true;
			for (int i = 0; i < selected.length; i++) {
				checkboxes.get(i).setSelected(selected[i]);
			}
			programmaticChange = previousChange;
		}
	}
	
	void setValue(boolean[] newValue) {
		for (int i = 0; i < newValue.length; i++) {
			selected[i] = newValue[i];
		}
		original = newValue;
		setValue();
	}
}
