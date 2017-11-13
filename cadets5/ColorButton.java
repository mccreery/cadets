package cadets5;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;

public class ColorButton extends JButton {
	private static final long serialVersionUID = 3845632638402810710L;
	private Color color;

	public ColorButton() {
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				color = JColorChooser.showDialog(null, "Spirograph Color", color);
				setBackground(color);
			}
		});
	}

	public Color getColor() {
		return color;
	}
}
