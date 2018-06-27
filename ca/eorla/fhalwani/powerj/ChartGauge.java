package ca.eorla.fhalwani.powerj;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

class ChartGauge extends JComponent {
	private static final long serialVersionUID = 428713205099941486L;
	private boolean hasImage = false;
	private int value = 0, max = 0;
	private String title ="";
	private BufferedImage image = null;

	ChartGauge() {
		super();
		image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				paintImage();
			}
		});
	}
	
	/** Display BufferedImage */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
	}
	
	/** Draws BufferedImage */
	void paintImage() {
		if (!hasImage) return;
		int startAngle = 0, arcAngle = 0;
		double zeroAngle = 225.0;
		double range = 270.0;
		double needleAngle = 0;
		Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
		Rectangle square = new Rectangle(0, 0, getWidth(), getHeight());
		// For best results, image must be square and not rectangular
		// Then use the extra space to draw the labels
		if (square.width > square.height) {
			square.width = square.height;
		} else {
			square.height = square.width;
		}
		// if the area is wider than the square, the center the gauge horizontally
		Point center = new Point(area.width /2, square.height /2);
		square.x = center.x - (square.width /2);
		image = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, area.width, area.height);
		// Draw outer black circle
		g2d.setColor(Color.BLACK);
		g2d.fillOval(square.x, square.y, square.width, square.height);
		// Draw inner white circle
		square.x += 5;
		square.y += 5;
		square.width -= 10;
		square.height -= 10;
		g2d.setColor(Color.WHITE);
		g2d.fillOval(square.x, square.y, square.width, square.height);
		// Draw red arc
		square.x += 5;
		square.y += 5;
		square.width -= 10;
		square.height -= 10;
		startAngle = -45;
		arcAngle = 90;
		g2d.setColor(Color.RED);
		g2d.fillArc(square.x, square.y, square.width, square.height, startAngle, arcAngle);
		// Draw amber arc
		startAngle += arcAngle;
		arcAngle = 45;
		g2d.setColor(Constants.AMBER);
		g2d.fillArc(square.x, square.y, square.width, square.height, startAngle, arcAngle);
		// Draw green arc
		startAngle += arcAngle;
		arcAngle = 135;
		g2d.setColor(Color.GREEN);
		g2d.fillArc(square.x, square.y, square.width, square.height, startAngle, arcAngle);
		// Draw a smaller inner white circle
		square.x += 10;
		square.y += 10;
		square.width -= 20;
		square.height -= 20;
		g2d.setColor(Color.WHITE);
		g2d.fillOval(square.x, square.y, square.width, square.height);
		// Draw scale
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_SMALL);
		Point point = new Point(center.x +20 -(square.width/2), center.y -20 +(square.height/2));
		g2d.drawString("0", point.x, point.y);
		point.x = center.x -40 +(square.width/2);
		g2d.drawString("" + max, point.x, point.y);
		point.x = center.x - 7;
		point.y = 35;
		g2d.drawString("" +(max /2), point.x, point.y);
		point.x = center.x +10 -(square.width/2);
		point.y = center.y *4/5;
		g2d.drawString("" +(max /4), point.x, point.y);
		point.x = center.x -25 +(square.width/2);
		g2d.drawString("" +(max *3/4), point.x, point.y);
		// Draw needle
		point.x = center.x;
		point.y = center.y;
		if (value < 0) {
			value = 0;
		}
		if (value > max) {
			needleAngle = zeroAngle - 1.0 * range;
		} else {
			needleAngle = zeroAngle - 1.0 * range * (value * 1.0 / max * 1.0);
		}
		float radius = square.height /2;
		// Convert from degrees to radians via multiplication by PI/180        
        float x = (float)(radius * Math.cos(needleAngle * Math.PI / 180F));
        float y = (float)(radius * Math.sin(needleAngle * Math.PI / 180F));
		point.x = center.x + (int)x;
		point.y = center.y - (int)y;
		g2d.setColor(Color.RED);
		g2d.setStroke(new BasicStroke(4));
		g2d.drawLine(center.x, center.y, point.x, point.y);
		// Write value
		g2d.setColor(Color.BLACK);
		g2d.drawString(""+ value + " Hr", center.x - 20, area.height - 25);
		g2d.setFont(Constants.FONT_LARGE);
		FontMetrics fm = g2d.getFontMetrics();
		g2d.drawString(title, (area.width - fm.stringWidth(title)) /2, area.height -6);
		g2d.dispose();
		repaint();
	}
	
	void setData(int value, int status) {
		this.value = value;
		switch (status) {
		case Constants.STATUS_Accession:
			max = 24;
			title = "Gross TAT";
			break;
		case Constants.STATUS_Gross:
			max = 48;
			title = "Embed TAT";
			break;
		case Constants.STATUS_Embed:
			max = 72;
			title = "Microtomy";
			break;
		case Constants.STATUS_Microtomy:
			max = 96;
			title = "Staining";
			break;
		case Constants.STATUS_Slides:
			max = 96;
			title = "Routing";
			break;
		case Constants.STATUS_Histology:
			max = 96;
			title = "Histo TAT";
			break;
		case Constants.STATUS_Routed:
			max = 120;
			title = "Diagnosis TAT";
			break;
		default:
			max = 120;
			title = "Total TAT";
		}
		hasImage = true;
		paintImage();
	}
}
