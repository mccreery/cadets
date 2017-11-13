package cadets5;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.Timer;

public class Spirograph extends JComponent implements ActionListener {
	private static final long serialVersionUID = 862154624893928882L;
	private Point2D.Float center;
	private float inner, outer, distance, step;
	private Color color;

	public static final float STEP = 0.1f;

	private final Path2D path;
	private final IteratorCurve curve;
	private final Timer timer;

	public Spirograph() {
		this(new Point2D.Float(), 0, 0, 0, STEP);
	}
	public Spirograph(Point2D.Float center, float inner, float outer, float distance, float step) {
		path  = new Path2D.Float(0, 0);
		curve = new IteratorCurve(this);
		color = Color.BLACK;

		update(center, inner, outer, distance, step);
		(timer = new Timer(10, this)).start();
	}

	public Point2D.Float getCenter() {return center;}
	public float          getInner() {return inner;}
	public float          getOuter() {return outer;}
	public float       getDistance() {return distance;}
	public float           getStep() {return step;}

	public void setCenter(Point2D.Float center) {update(center, inner, outer, distance, step);}
	public void  setInner(float         inner)  {update(center, inner, outer, distance, step);}
	public void  setOuter(float         outer)  {update(center, inner, outer, distance, step);}
	public void setOffset(float         offset) {update(center, inner, outer, offset, step);}
	public void   setStep(float         step)   {update(center, inner, outer, distance, step);}

	public void loadPreset(Preset preset) {
		update(center, preset.inner, preset.outer, preset.distance, step);
	}

	public void update(Point2D.Float center, float inner, float outer, float distance, float step) {
		this.center   = center;
		this.inner    = inner;
		this.outer    = outer;
		this.distance = distance;
		this.step     = step;

		float r = outer - inner + distance;
		setLocation(new Point((int)(center.x - r), (int)(center.y - r)));
		setSize((int)(2*r), (int)(2*r));

		path.reset();
		curve.reset();
		Point2D first = curve.next();
		path.moveTo(first.getX(), first.getY());
	}

	@Override
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
		g.setColor(color);
		((Graphics2D)g).draw(path);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == timer) {
			Point2D p = curve.next();
			path.lineTo(p.getX(), p.getY());
			this.repaint();
		}
	}

	@Override
	public String toString() {
		return inner + ":" + outer + ":" + distance;
	}

	public enum Preset {
		STAR(63, 100, 100);

		public final float inner, outer, distance;

		Preset(float inner, float outer, float distance) {
			this.inner = inner;
			this.outer = outer;
			this.distance = distance;
		}
	}
}
