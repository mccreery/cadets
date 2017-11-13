package cadets5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cadets5.Spirograph.Preset;

public class SpirographViewer extends JFrame {
	private static final long serialVersionUID = -1926020157017246117L;
	private final JLayeredPane canvas;
	private final JPanel leftPanel;

	private final JList<Spirograph> list;

	private JLabel getLabel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		return label;
	}

	private JSlider getSlider(int min, int max) {
		JSlider slider = new JSlider(min, max);
		slider.setMajorTickSpacing((max-min) / 2);
		slider.setMinorTickSpacing((max-min) / 10);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(listener);
		slider.setBackground(null);
		return slider;
	}

	private boolean noUpdate = false;

	private final JSpinner x, y;
	private final JSlider inner, outer, distance, step;
	private final ColorButton color;

	private final ChangeListener listener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if(noUpdate) return;

			if(list.isSelectionEmpty()) return;
			if(outer.getValue() < inner.getValue()) outer.setValue(inner.getValue());

			Point2D.Float center = new Point2D.Float((Integer)x.getValue(), (Integer)y.getValue());
			list.getSelectedValue().update(center, inner.getValue(), outer.getValue(), distance.getValue(), (float)step.getValue() / 10f);
			list.repaint();
		}
	};

	public SpirographViewer() {
		setTitle("Spirograph");
		setSize(640, 360);

		add(canvas = new JLayeredPane());
		canvas.setLayout(null);
		//canvas.setPreferredSize(new Dimension(280, 280));

		Spirograph s = new Spirograph();
		s.loadPreset(Preset.STAR);
		s.setCenter(new Point2D.Float(100, 100));
		canvas.add(s);

		add(leftPanel = new JPanel(), BorderLayout.LINE_START);
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setPreferredSize(new Dimension(200, 0));
		leftPanel.setBackground(Color.LIGHT_GRAY);

		JPanel sliders = new JPanel();
		sliders.setBackground(null);
		//sliders.setPreferredSize(new Dimension(200, 200));
		sliders.setLayout(new GridLayout(0, 2));

		sliders.add(getLabel("X"));
		sliders.add(x = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 10)));
		x.addChangeListener(listener);
		sliders.add(getLabel("Y"));
		sliders.add(y = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 10)));
		y.addChangeListener(listener);

		sliders.add(getLabel("Inner radius"));
		sliders.add(inner = getSlider(0, 100));
		sliders.add(getLabel("Outer radius"));
		sliders.add(outer = getSlider(0, 100));
		sliders.add(getLabel("Distance"));
		sliders.add(distance = getSlider(0, 100));
		sliders.add(getLabel("Step"));
		sliders.add(step = getSlider(0, 10));

		sliders.add(getLabel("Color"));
		sliders.add(color = new ColorButton());

		leftPanel.add(sliders, BorderLayout.PAGE_START);

		list = new JList<Spirograph>(new ListModel<Spirograph>() {
			@Override
			public Spirograph getElementAt(int index) {
				return (Spirograph)canvas.getComponent(index);
			}

			@Override
			public int getSize() {
				return canvas.getComponentCount();
			}

			@Override
			public void addListDataListener(ListDataListener l) {
				canvas.addContainerListener(new ContainerListener() {
					@Override
					public void componentAdded(ContainerEvent event) {
						l.contentsChanged(new ListDataEvent(event.getSource(), ListDataEvent.INTERVAL_ADDED, canvas.getComponentCount() - 1, canvas.getComponentCount()));
					}

					@Override
					public void componentRemoved(ContainerEvent event) {
						l.contentsChanged(new ListDataEvent(event.getSource(), ListDataEvent.INTERVAL_REMOVED, canvas.getComponentCount(), canvas.getComponentCount() + 1));
					}
				});
			}

			// TODO fix so this does something
			@Override
			public void removeListDataListener(ListDataListener l) {}
		});
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Spirograph s = list.getSelectedValue();

				for(Component c : sliders.getComponents()) {
					c.setEnabled(s != null);
				}
				if(s != null) {
					noUpdate = true;
					x.setValue((int)s.getCenter().x);
					y.setValue((int)s.getCenter().y);
					inner.setValue((int)s.getInner());
					outer.setValue((int)s.getOuter());
					distance.setValue((int)s.getDistance());
					step.setValue((int)(s.getStep() * 10f));
					noUpdate = false;
				}
			}
		});
		list.setBackground(Color.GRAY);
		leftPanel.add(list, BorderLayout.CENTER);

		// To disable the controls
		list.setSelectedIndex(0);
		list.clearSelection();

		JPanel buttons = new JPanel();
		leftPanel.add(buttons, BorderLayout.PAGE_END);

		JButton add = new JButton("Add new");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Spirograph s = new Spirograph();
				canvas.add(s);
			}
		});
		buttons.add(add);
		buttons.add(new JButton("Remove"));
	}

	public static void main(String[] args) {
		new SpirographViewer().setVisible(true);
	}
}
