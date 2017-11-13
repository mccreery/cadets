package cadets6;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Iterator;

/** Representation of a circle on a 2D plane */
public class Circle implements Iterable<Point> {

	/** The X coordinate, Y coordinate and radius of the circle */
	public int x, y, radius;

	/** @param x The X coordinate of the circle
	 * @param y The Y coordinate of the circle
	 * @param radius The radius of the circle */
	public Circle(int x, int y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	/** Draw this circle with reference to the given origin point
	 * @param g The graphics context
	 * @param xOrigin the X coordinate of the origin
	 * @param xOrigin the Y coordinate of the origin */
	public void draw(Graphics g, int xOrigin, int yOrigin) {
		int diameter = radius * 2;
		g.drawArc(xOrigin + x - radius, yOrigin + y - radius, diameter, diameter, 0, 360);
	}

	@Override
	public Iterator<Point> iterator() {
		return new Iterator<Point>() {
			/** The current angle round the circle */
			private float theta = 0;

			@Override
			public boolean hasNext() {
				return theta < Math.PI*2; // Circle is incomplete
			}

			@Override
			public Point next() {
				// Find the point at the current angle
				Point p = new Point(
					x + (int)(Math.cos(theta) * radius),
					y + (int)(Math.sin(theta) * radius)
				);
				theta += Math.PI / 180f; // Rotate around a little
				return p;
			}
		};
	}
}
