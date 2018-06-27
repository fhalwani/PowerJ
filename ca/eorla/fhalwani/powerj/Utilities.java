package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

class Utilities {
	final static byte OPTION_YES = 0;
	final static byte OPTION_NO = 1;
	final static byte OPTION_CANCEL = 2;

	/** Add a component to the container or the toolbar **/
	static void addComponent(Component component,
			int gridx, int gridy, int gridwidth, int gridheight,
			double weightx, double weighty, int fill, int anchor,
			JPanel pnl) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.gridwidth = gridwidth;
		constraints.gridheight = gridheight;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = fill;
		constraints.anchor = anchor;
		pnl.add(component, constraints);
	}

	/** Returns an ImageIcon, or null if the path is invalid. */
	static ImageIcon getIcon(String name) {
		ImageIcon icon = new ImageIcon();
		String path = "icons/" + name.toLowerCase() + ".png";
		URL url = ClassLoader.getSystemClassLoader().getResource(path);
		if (url != null)
			icon = new ImageIcon(url);
		return icon;
	}

	/** Returns an ImageIcon, or null if the path is invalid. */
	static Image getImage(String name) {
		ImageIcon icon = getIcon(name);
		return icon.getImage();
	}
	
	static int askSave(Frame owner, String name) {
		String message = String.format("Save changes to %s before closing?", name);
		String[] choices = {"Save", "Ignore", "Cancel"};
		int[] mnemonics = {KeyEvent.VK_S, KeyEvent.VK_I, KeyEvent.VK_C};
		int answer = getChoice(JOptionPane.QUESTION_MESSAGE, name, message,
				owner, choices, mnemonics, 0);
		return answer;
	}

	static void showMessage(int type, Frame owner, String name, String message) {
		String[] choices = {"Ok"};
		int[] mnemonics = {KeyEvent.VK_O};
		getChoice(type, name, message, owner, choices, mnemonics, 0);
	}

	static int getChoice(int type, String name, String message, Frame owner,
			String[] choices, int[] mnemonics, int option) {
		Icon icon;
		switch (type) {
		case JOptionPane.QUESTION_MESSAGE:
			icon = Utilities.getIcon("m_question");
			if (icon == null) {
				icon = UIManager.getIcon("OptionPane.questionIcon");
			}
			break;
		case JOptionPane.INFORMATION_MESSAGE:
			icon = Utilities.getIcon("m_info");
			if (icon == null) {
				icon = UIManager.getIcon("OptionPane.informationIcon");
			}
			break;
		case JOptionPane.WARNING_MESSAGE:
			icon = Utilities.getIcon("m_warning");
			if (icon == null) {
				icon = UIManager.getIcon("OptionPane.warningIcon");
			}
			break;
		default:
			icon = Utilities.getIcon("m_error");
			if (icon == null) {
				icon = UIManager.getIcon("OptionPane.errorIcon");
			}
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(300, 100));
		scrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JTextArea textArea = new JTextArea(message);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setMargin(new Insets(5,5,5,5));
		textArea.setEditable(false);
		textArea.setFont(new Font("Serif", Font.PLAIN, 14));
		scrollPane.getViewport().setView(textArea);
		Object obj = scrollPane;
		final JOptionPane pane = new JOptionPane(obj);
		pane.setIcon(icon);
		JButton buttons[] = new JButton[choices.length];
		for (int i = 0; i < choices.length; i++) {
			buttons[i] = new JButton(choices[i]);
			buttons[i].setMnemonic(mnemonics[i]);
			buttons[i].setIcon(Utilities.getIcon(choices[i]));
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton button = (JButton) e.getSource();
					pane.setValue(button);
				}
			});
		}
		pane.setOptions(buttons);
		pane.setInitialValue(buttons[option]);
		buttons[option].requestFocusInWindow();
		buttons[option].setSelected(true);
		JDialog dialog = pane.createDialog(owner, name);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// set the default button for <Enter> key
		dialog.getRootPane().setDefaultButton(buttons[option]);
		dialog.setVisible(true);
		Object selectedValue = pane.getValue();
		dialog.dispose();
		int choice = -1;
		if (selectedValue != null) {
			// If there is an array of option buttons:
			for (int counter = 0; counter < buttons.length; counter++) {
				if (buttons[counter].equals(selectedValue)) {
					choice = counter;
				}
			}
		}
		return choice;
	}
	
}
