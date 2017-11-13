package cadets6;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class CircleDetector extends JFrame implements ActionListener {
	private static final long serialVersionUID = -1479110023557857534L;
	private final JLayeredPane body;

	public CircleDetector() {
		setTitle("Circle Detector");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setUndecorated(true);
		setBackground(Color.BLACK);

		JComponent webcam;

		setBackground(Color.BLACK);
		add(body = new JLayeredPane());
		body.setBackground(Color.BLACK);
		body.add(webcam = new WebcamView());
		body.add(new CircleView((WebcamView)webcam), JLayeredPane.PALETTE_LAYER);
		setSize(webcam.getSize());

		new Timer(33, this).start();
	}

	public static void main(String[] args) {
		new CircleDetector().setVisible(true);
	}

	// Called on every timer tick
	@Override
	public void actionPerformed(ActionEvent e) {
		body.repaint();
	}
}
