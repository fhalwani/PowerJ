package ca.eorla.fhalwani.powerj;
import java.awt.Dimension;
import javax.swing.JComboBox;

class CboPersonnel extends JComboBox {
	private static final long serialVersionUID = -3920799869685622446L;

	CboPersonnel(boolean isEditor) {
		super();
		setFont(Constants.APP_FONT);
		setEditable(false);
		setData(isEditor);
		Dimension dim = new Dimension(150, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
	}
	
	private void setData(boolean isEditor) {
		final String[] filter = { "* All *", "C ", "CG", "H ", "IS", "LT", "PA", "PT", "R ", "T " };
		final String[] editor = { "C ", "CG", "H ", "IS", "LT", "PA", "PT", "R ", "T " };
		if (isEditor) {
			for (int i = 0; i < editor.length; i++) {
				addItem(editor[i]);
			}
		} else {
			for (int i = 0; i < filter.length; i++) {
				addItem(filter[i]);
			}
		}
	}
}
