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

public class ViewCases extends JPanel {
	private static final long serialVersionUID = 667110764405688928L;
	private volatile boolean programmaticChange;
	private boolean[] selected, original;
	private final Timer selectionTimer;
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();

	ViewCases(boolean[] value, PowerJ parent) {
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
		original = value;
		selected = new boolean[value.length];
		final String[] labels = {"Accession", "Gross", "Route",
				"Final", "Facility", "Specialty", "Subspecial", "Procedure", "Specimen",
				"Staff", "Specs", "Blocks", "Slides", "H&E", "SS", "IHC", "Mol", "FSS",
				"FSB", "FSL", "Synop", "tatG", "tatH", "tatF", "tatT"};
		for (byte i = 0; i < value.length; i++) {
			selected[i] = value[i];
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
