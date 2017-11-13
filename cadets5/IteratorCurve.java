package cadets5;

import java.awt.geom.Point2D;
import java.util.Iterator;

public class IteratorCurve implements Iterator<Point2D> {
	private final Spirograph parent;
	private       float      theta;

	public IteratorCurve(Spirograph parent) {
		this.parent = parent;
		theta       = 0;
	}

	public void reset() {theta = 0;}
	@Override
	public boolean hasNext() {return true;}

	@Override
	public Point2D next() {
		float dr = parent.getOuter() - parent.getInner();
		float rTheta = (dr / parent.getInner()) * theta;
		float d = parent.getDistance();

		float x = (float)(dr*Math.cos(theta) + d*Math.cos(rTheta));
		float y = (float)(dr*Math.sin(theta) - d*Math.sin(rTheta));

		Point2D.Float p = new Point2D.Float(dr + d + x, dr + d + y);

		/*Point p = new Point(
			(int)((LARGE+SMALL) * Math.cos(theta) - (SMALL+OFFSET) * Math.cos(theta * (LARGE+SMALL) / SMALL)),
			(int)((LARGE+SMALL) * Math.sin(theta) - (SMALL+OFFSET) * Math.sin(theta * (LARGE+SMALL) / SMALL))
		);*/

		theta += parent.getStep();
		return p;
	}
}
