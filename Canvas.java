import java.io.*;
import java.util.*;

// ===================================================
// Canvas Class - Drawing and Saving
// ===================================================
public class Canvas {
    private Pixel[][] canvas; // Drawing Canvas
    private Pixel[][] save; // Save State
    private Matrix edges; // Lines
    private Stack<Matrix> transform; // Transformation Matrix
    private int x, y; // Dimensions
    private int mode; // Edges or Polygons
    
    // Constructors
    public Canvas() {
	canvas = new Pixel[500][500];
	x = 500;
	y = 500;
	fill(255, 255, 255);
	edges = new Matrix();
	transform = new Stack<Matrix>();
	mode = 3;
    }
    public Canvas(int md) {
	this();
	mode = md;
    }
    public Canvas(int x, int y) {
	canvas = new Pixel[y][x];
	this.x = x;
	this.y = y;
	fill(255, 255, 255);
	edges = new Matrix();
	transform = new Stack<Matrix>();
	mode = 3;
    }
    public Canvas(int x, int y, int md) {
	this(x, y);
	mode = md;
    }
    public Canvas(int x, int y, Pixel p) {
	this(x, y);
	fill(p);
	mode = 3;
    }
    public Canvas(int x, int y, Pixel p, int md) {
	this(x, y, p);
	mode = md;
    }
    public Canvas(int x, int y, int R, int G, int B) {
	this(x, y);
	fill(R, G, B);
	mode = 3;
    }
    public Canvas(int x, int y, int R, int G, int B, int md) {
	this(x, y, R, G, B);
	mode = md;
    }

    // Accessors + Mutators
    public int[] getXY() {
	return new int[]{x, y};
    }
    public int getX() {
	return x;
    }
    public int getY() {
	return y;
    }
    public Matrix getEdges() {
	return edges;
    }
    public Matrix getTransform() {
	return transform.peek();
    }
    public int getMode() {
	return mode;
    }

    public int setMode(int md) {
	return mode = md;
    }

    // Transformations
    public Matrix scale(double sx, double sy, double sz) {
	if (transform.empty()) return null;
	Matrix m = Matrix.identity(4);
	m.set(0,0,sx);
	m.set(1,1,sy);
	m.set(2,2,sz);
	transform.push(transform.pop().multiply(m)); // Right
	// transform.push(m.multiply(transform.pop())); // Left 
	return transform.peek();
    }
    public Matrix scale(double s) {
	return scale(s, s, s);
    }
    public Matrix translate(double dx, double dy, double dz) {
	if (transform.empty()) return null;
	Matrix m = Matrix.identity(4);
	m.set(0,3,dx);
	m.set(1,3,dy);
	m.set(2,3,dz);
	transform.push(transform.pop().multiply(m)); // Right
	// transform.push(m.multiply(transform.pop())); // Left
	return transform.peek();
    }
    public Matrix rotate(char axis, double theta) {
	if (transform.empty()) return null;
	Matrix m = Matrix.identity(4);
	double rad = Math.toRadians(theta);
	if (axis == 'z') {
	    m.set(0,0,Math.cos(rad));
	    m.set(1,1,Math.cos(rad));
	    m.set(0,1,-1 * Math.sin(rad));
	    m.set(1,0, Math.sin(rad));
	} 
	else if (axis == 'y') {
	    m.set(0,0,Math.cos(rad));
	    m.set(2,2,Math.cos(rad));
	    m.set(0,2,Math.sin(rad));
	    m.set(2,0,-1 * Math.sin(rad));
	}
	else if (axis == 'x') {
	    m.set(1,1,Math.cos(rad));
	    m.set(2,2,Math.cos(rad));
	    m.set(1,2,-1 * Math.sin(rad));
	    m.set(2,1,Math.sin(rad));
	}
	transform.push(transform.pop().multiply(m)); // Right
	// transform.push(m.multiply(transform.pop())); // Left
	return transform.peek();
    }

    public Matrix push() {
	Matrix m = Matrix.identity(4);
	if (!transform.empty())
	    m.copy(transform.peek());
	return transform.push(m);
    }
    public Matrix pop() {
	if (!transform.empty())
	    return transform.pop();
	return null;
    }
    public Matrix peek() {
	Matrix m = Matrix.identity(4);
	if (!transform.empty()) 
	    m.copy(transform.peek());
	return m;
    }

    public Matrix apply() {
	Matrix top = Matrix.identity(4);
	if (!transform.empty()) {
	    top.copy(transform.peek());
	    edges.copy(top.multiply(edges));
	}
	return edges;
	// transform = Matrix.identity(4);
	// return edges;
    }

    // Shapes and Curves
    // Exclusively For Parsing - Not Line Algo
    public boolean line(double x1, double y1, double z1,
			double x2, double y2, double z2, Pixel p) {
	edge(x1, y1, z1, x2, y2, z2, p);
	apply();
	draw(2);
	return true;
    }

    public boolean circle(double cx, double cy, double z, double r, Pixel p) {
	double x1 = cx + r;
	double y1 = cy;
	double x2, y2;
	for (double t = 0; t < 1.001; t += 0.01) {
	    x2 = cx + r * Math.cos(t * 2 * Math.PI);
	    y2 = cy + r * Math.sin(t * 2 * Math.PI);
	    edge(x1, y1, z, x2, y2, z, p);
	    x1 = x2; y1 = y2;
	}
	apply(); // Coordinate System Shift
	draw(2); // 2D
	return true;
    }
    public boolean circle(double cx, double cy, double z, double r) {
	return circle(cx, cy, z, r, new Pixel(0,0,0));
    }

    public boolean box(double x, double y, double z, 
		       double dx, double dy, double dz, Pixel p) {
	Matrix em = box_edges(x,y,z,dx,dy,dz,p);
	edges.append(em);
	apply();
	draw(3);
	return true;
    }
    public boolean box(double x, double y, double z, 
		       double dx, double dy, double dz) {
	return box(x, y, z, dx, dy, dz, new Pixel(0,0,0));
    }
    public Matrix box_edges(double x, double y, double z, 
			    double dx, double dy, double dz, Pixel p) {
	Matrix em = new Matrix();
	
	if (mode == 2) { // Deprecated
	    // Edge Implementation ==============
	    em.add_edge(x,y,z,x+dx,y,z,p);
	    em.add_edge(x,y,z,x,y-dy,z,p);
	    em.add_edge(x,y,z,x,y,z-dz,p);
	    em.add_edge(x+dx,y,z,x+dx,y-dy,z,p);
	    em.add_edge(x+dx,y,z,x+dx,y,z-dz,p);
	    em.add_edge(x,y-dy,z,x+dx,y-dy,z,p);
	    em.add_edge(x,y-dy,z,x,y-dy,z-dz,p);
	    em.add_edge(x,y,z-dz,x+dx,y,z-dz,p);
	    em.add_edge(x,y,z-dz,x,y-dy,z-dz,p);
	    em.add_edge(x+dx,y-dy,z,x+dx,y-dy,z-dz,p);
	    em.add_edge(x+dx,y,z-dz,x+dx,y-dy,z-dz,p);
	    em.add_edge(x,y-dy,z-dz,x+dx,y-dy,z-dz,p);
	    // ==================================== */
	}

	else if (mode == 3) {
	    // Polygon Implementation ===========
	    em.add_triangle(x+dx,y-dy,z-dz,x,y-dy,z,x,y-dy,z-dz,p); // F
	    em.add_triangle(x+dx,y-dy,z-dz,x+dx,y-dy,z,x,y-dy,z,p); // F
	    em.add_triangle(x+dx,y-dy,z,x+dx,y-dy,z-dz,x+dx,y,z-dz,p); // R
	    em.add_triangle(x+dx,y-dy,z,x+dx,y,z-dz,x+dx,y,z,p); // R
	    em.add_triangle(x,y,z,x,y,z-dz,x,y-dy,z,p); // L
	    em.add_triangle(x,y-dy,z,x,y,z-dz,x,y-dy,z-dz,p); // L
	    em.add_triangle(x,y,z-dz,x+dx,y,z-dz,x+dx,y-dy,z-dz,p); // Bottom
	    em.add_triangle(x,y,z-dz,x+dx,y-dy,z-dz,x,y-dy,z-dz,p); // Bottom
	    em.add_triangle(x,y,z,x,y-dy,z,x+dx,y-dy,z,p); // T
	    em.add_triangle(x,y,z,x+dx,y-dy,z,x+dx,y,z,p); // T
	    em.add_triangle(x,y,z,x+dx,y,z,x+dx,y,z-dz,p); // Back
	    em.add_triangle(x,y,z,x+dx,y,z-dz,x,y,z-dz,p); // Back
	    // ==================================== */
	}
	return em;
    }
    public Matrix box_edges(double x, double y, double z, 
			    double dx, double dy, double dz) {
	return box_edges(x, y, z, dx, dy, dz, new Pixel(0,0,0));
    }

    public boolean sphere(double cx, double cy, double cz, double r, Pixel p) {
	Matrix em = sphere_edges(cx,cy,cz,r,p);
	edges.append(em);
	apply();
	draw(3);
	return true;
    }
    public boolean sphere(double cx, double cy, double cz, double r) {
	return sphere(cx, cy, cz, r, new Pixel(0,0,0));
    }
    public Matrix sphere_edges(double cx, double cy, double cz, double r, Pixel p) {
	Matrix em = new Matrix();
	double s; // Semicircle
	double t; // Rotation
	int n = 20; // Steps
	double ds = Math.PI / n; // Semicircle Step
	double dt = ds; // Rotation Step
	double x, y, z;
	
	if (mode == 2) { // Deprecated 
	    // Edge Implementation ==============
	    for (t = 0; t < 2 * Math.PI + dt/2; t += dt) {
		for (s = 0; s < Math.PI + ds/2; s += ds) {
		    x = r * Math.cos(s) + cx;
		    y = r * Math.sin(s) * Math.cos(t) + cy;
		    z = r * Math.sin(s) * Math.sin(t) + cz;
		    em.add_edge(x, y, z, x, y, z, p); // Change Later
		}
	    }
	    // ==================================== */
	}

	else if (mode == 3) {
	    // Polygon Implementation =============
	    double[][] sc = new double[n+1][3];
	    int c = 0; // Counter
	    for (s = 0; s < Math.PI + ds/2; s += ds) {
		sc[c][0] = r * Math.cos(s) + cx; // x
		sc[c][1] = r * Math.sin(s) + cy; // y
		sc[c][2] = cz; // z 
		c++;
	    }
	    c = 0;
	    for (t = dt; t < 2 * Math.PI + dt/2; t += dt) {
		c = 0;
		for (s = 0; s < Math.PI + ds/2; s += ds) {
		    x = r * Math.cos(s) + cx;
		    y = r * Math.sin(s) * Math.cos(t) + cy;
		    z = r * Math.sin(s) * Math.sin(t) + cz;
		    if (c > 0)
			em.add_triangle(x,y,z,
					sc[c-1][0],sc[c-1][1],sc[c-1][2],
					sc[c][0],sc[c][1],sc[c][2],p);
		    if (c < n)
			em.add_triangle(x,y,z,
					sc[c][0],sc[c][1],sc[c][2],
					sc[c+1][0],sc[c+1][1],sc[c+1][2],p);
		    sc[c][0] = x; sc[c][1] = y; sc[c][2] = z;
		    c++;
		}
	    }
	    // ==================================== */
	}
	return em;
    }
    public Matrix sphere_edges(double cx, double cy, double cz, double r) {
	return sphere_edges(cx, cy, cz, r, new Pixel(0,0,0));
    }

    public boolean torus(double cx, double cy, double cz, double r, double R, Pixel p) {
	Matrix em = torus_edges(cx,cy,cz,r,R,p);
	edges.append(em);
	apply();
	draw(3);
	return true;
    }
    public boolean torus(double cx, double cy, double cz, double r, double R) {
	return torus(cx, cy, cz, r, R, new Pixel(0,0,0));
    }
    // To Make More Efficient
    public Matrix torus_edges(double cx, double cy, double cz, double r, double R, Pixel p) {
	Matrix em = new Matrix();
	double s; // Circle
	double t; // Rotation
	int n = 10; // Steps / 2
	double ds = Math.PI / n; // Circle Step
	double dt = ds / 2; // Rotation Step
	double x, y, z;

	if (mode == 2) {
	    // Edge Implementation ==============
	    for (t = 0; t < 2 * Math.PI + dt/2; t += 100) {
		for (s = 0; s < 2 * Math.PI + ds/2; s += ds) {
		    double Rr = (r * Math.cos(s) + R);
		    x = Rr * Math.cos(t) + cx;
		    y = Rr * Math.sin(t) + cy;
		    z = r * Math.sin(s) + cz;
		    em.add_edge(x, y, z, x, y, z, p); // Change Later
		}
	    }
	    // ==================================== */
	}

	else if (mode == 3) {
	    // Polygon Implementation =============
	    double[][] sc = new double[2 * n + 1][3];
	    int c = 0; // Counter
	    for (s = 0; s < 2 * Math.PI + ds/2; s += ds) {
		double Rr = (r * Math.cos(s) + R);
		sc[c][0] = Rr + cx; // x
		sc[c][1] = cy; // y
		sc[c][2] = r * Math.sin(s) + cz; // z 
		c++;
	    }
	    c = 0;
	    for (t = dt; t < 2 * Math.PI + dt/2; t += dt) {
		c = 0;
		for (s = 0; s < 2 * Math.PI + ds/2; s += ds) {
		    double Rr = (r * Math.cos(s) + R);
		    x = Rr * Math.cos(t) + cx;
		    y = Rr * Math.sin(t) + cy;
		    z = r * Math.sin(s) + cz;
		    if (c > 0)
			em.add_triangle(x,y,z,
					sc[c][0],sc[c][1],sc[c][2],
					sc[c-1][0],sc[c-1][1],sc[c-1][2],p);
		    if (c < 2 * n)
			em.add_triangle(x,y,z,
					sc[c+1][0],sc[c+1][1],sc[c+1][2],
					sc[c][0],sc[c][1],sc[c][2],p);
		    sc[c][0] = x; sc[c][1] = y; sc[c][2] = z;
		    c++;
		}
	    }
	        
	    // ==================================== */
	}
	return em;
    }
    public Matrix torus_edges(double cx, double cy, double cz, double r, double R) {
	return torus_edges(cx, cy, cz, r, R, new Pixel(0,0,0));
    }
    
    public boolean hermite(double x0, double y0, double x1, double y1,
			   double dx0, double dy0, double dx1, double dy1, Pixel p) {
	// Why Use A Matrix When You Can Just Multiply? Efficiency is important! 
	double m0 = dx0 / dy0; double m1 = dx1 / dy1;
	double ax, ay, bx, by, cx, cy, dx, dy;
	ax = 2 * x0 - 2 * x1 + dx0 + dx1;
	bx = -3 * x0 + 3 * x1 - 2 * dx0 - dx1;
	cx = dx0;
	dx = x0;
	ay = 2 * y0 - 2 * y1 + dy0 + dy1;
	by = 3 * y1 - 3 * y0 - 2 * dy0 - dy1; 
	cy = dy0;
	dy = y0;
	double x = x0; double y = y0;
	double newx, newy;
	for (double t = 0; t < 1.001; t += 0.005) {
	        newx = ax * t * t * t +
		    bx * t * t +
		    cx * t + 
		    dx;
		    newy = ay * t * t * t +
			by * t * t +
			cy * t +
			dy;
		    edge(x, y, 0, newx, newy, 0, p);
		    x = newx; y = newy;
	}
	apply();
	draw(2);
	return true;
    }
    public boolean hermite(double x0, double y0, double x1, double y1,
			   double dx0, double dy0, double dx1, double dy1) {
	return hermite(x0, y0, x1, y1,
		       dx0, dy0, dx1, dy1, new Pixel(0,0,0));
    }

    public boolean bezier(double[] points) {
	return false; // To Implement For General Bezier Curves
    }
    public boolean bezier(double x0, double y0,
			  double x1, double y1,
			  double x2, double y2,
			  double x3, double y3, Pixel p) {
	// Matrix Implementation For 4 Points - To Add To General Bezier Later
	double[][] a = 
	    {{x0, x1, x2, x3},
	     {y0, y1, y2, y3}};
	Matrix xy = new Matrix(a); // 4 by 2
	double[][] b =
	    {{-1,3,-3,1},
	     {3,-6,3,0},
	     {-3,3,0,0},
	     {1,0,0,0}};
	Matrix bz = new Matrix(b); // 4 by 4
	bz.multiply(xy); // 4 by 2

	double ax, ay, bx, by, cx, cy, dx, dy;
	ax = bz.get(0,0); ay = bz.get(0,1);
	bx = bz.get(1,0); by = bz.get(1,1);
	cx = bz.get(2,0); cy = bz.get(2,1);
	dx = bz.get(3,0); dy = bz.get(3,1);
	
	double x = x0; double y = y0;
	double newx, newy;
	for (double t = 0; t < 1.001; t += 0.005) {
	        newx =  ax * t * t * t +
		    bx * t * t +
		    cx * t + 
		    dx;
		    newy = ay * t * t * t +
			by * t * t +
			cy * t +
			dy;
		    edge(x, y, 0, newx, newy, 0, p);
		    x = newx; y = newy;
	}
	apply();
	draw(2);
	return true;
    }
    public boolean bezier(double x0, double y0,
			  double x1, double y1,
			  double x2, double y2,
			  double x3, double y3) {
	return bezier(x0, y0, x1, y1, x2, y2, x3, y3, new Pixel(0,0,0));
    }

    // EdgeMatrix Functions
    public boolean edge(double x1, double y1, double x2, double y2) {
	return edges.add_edge(x1, y1, x2, y2, new Pixel(0,0,0));
    }
    public boolean edge(double x1, double y1, double x2, double y2, Pixel p) {
	return edges.add_edge(x1, y1, x2, y2, p);
    }
    public boolean edge(double x1, double y1, double z1,
			double x2, double y2, double z2) {
	return edges.add_edge(x1, y1, z1, x2, y2, z2, new Pixel(0,0,0));
    }
    public boolean edge(double x1, double y1, double z1,
			double x2, double y2, double z2, Pixel p) {
	return edges.add_edge(x1, y1, z1, x2, y2, z2, p);
    }

    public boolean triangle(double x1, double y1, double z1,
			    double x2, double y2, double z2,
			    double x3, double y3, double z3, Pixel p) {
	return edges.add_triangle(x1,y1,z1,x2,y2,z2,x3,y3,z3,p);
    }
    public boolean triangle(double x1, double y1, double z1,
			    double x2, double y2, double z2,
			    double x3, double y3, double z3) {
	return triangle(x1,y1,z1,x2,y2,z2,x3,y3,z3,new Pixel(0,0,0));
    }

    public boolean draw() {
	return draw(mode);
    }
    public boolean draw(int md) {
	Iterator<double[]> edgelist = edges.iterator();
	Iterator<Pixel> colors = edges.colorIterator();
	double[] p1, p2;
	double x1, x2, y1, y2;
	Pixel p;

	if (md == 2) {
	    while (edgelist.hasNext()) {
		p1 = edgelist.next();
		p2 = edgelist.next();
		x1 = p1[0];
		y1 = p1[1];
		x2 = p2[0];
		y2 = p2[1];
		p = colors.next();
		line((int)x1, (int)y1, (int)x2, (int)y2, p);
	    }
	}
	else if (md == 3) {
	    double x3, y3;
	    double[] p3;
	    while (edgelist.hasNext()) {
		p1 = edgelist.next();
		p2 = edgelist.next();
		p3 = edgelist.next();
		x1 = p1[0];
		y1 = p1[1];
		x2 = p2[0];
		y2 = p2[1];
		x3 = p3[0];
		y3 = p3[1];
		p = colors.next();
		double dx1 = x2 - x1; double dx2 = x3 - x2;
		double dy1 = y2 - y1; double dy2 = y3 - y2; 
		// double dz1 = z2 - z1; double dz2 = z3 - z2; // Not Needed
		if (dx1 * dy2 - dy1 * dx2 > 0) { 
		    // Cross Product Z is Positive (Facing Us)
		    line((int)x1, (int)y1, (int)x2, (int)y2, p);
		    line((int)x2, (int)y2, (int)x3, (int)y3, p);
		    line((int)x3, (int)y3, (int)x1, (int)y1, p);
		}
	    }
	}
	clearEdges();
	return true;
    }
    
    public boolean clearEdges() {
	edges = new Matrix();
	return true;
    }
    public boolean clearTransform() {
	transform = new Stack<Matrix>();
	return true;
    }

    // Canvas Methods
    public boolean draw_pixel(int x, int y, Pixel p) {
	if (x < 0 || x >= this.x || y < 0 || y >= this.y)
	    return false;
	canvas[y][x] = p;
	return true;
    }
    public boolean draw_pixel(int x, int y) {
	return draw_pixel(x, y, new Pixel(0, 0, 0));
    }
    public boolean draw_pixel(int x, int y, int R, int G, int B) {
	return draw_pixel(x, y, new Pixel(R, G, B));
    }

    public boolean fill(Pixel p) {
	for (int i = 0; i < y; i++) {
	    for (int j = 0; j < x; j++) {
		canvas[i][j] = p;
	    }
	}
	return true;
    }
    public boolean fill(int R, int G, int B) {
	return fill(new Pixel(R, G, B));
    }

    public boolean savestate() {
	int h = canvas.length;
	int w = canvas[0].length;
	save = new Pixel[h][w];
	for (int i = 0; i < h; i++) {
	    for (int j = 0; j < w; j++) {
		save[i][j] = canvas[i][j].copy();
	    }
	}
	return true;
    }
    public boolean loadstate() {
	if (save != null) {
	    for (int i = 0; i < y; i++) 
		for (int j = 0; j < x; j++) 
		    canvas[i][j] = save[i][j].copy();
	    return true;
	}
	return false;
    }

    // Bresenham's Line Algorithm - 8 Octants
    public boolean line(int x1, int y1, int x2, int y2) {
	return line(x1, y1, x2, y2, new Pixel(0, 0, 0));
    }
    public boolean line(int x1, int y1, int x2, int y2, Pixel p) {
	if (x2 < x1) return line(x2, y2, x1, y1, p);
	int dy = y2 > y1 ? y2 - y1 : y1 - y2; // positive difference
	int dx = x2 - x1; // always positive
	int m = y2 > y1 ? 1 : -1;
	if (dy > dx)
	    if (m > 0)
		return line2(x1, y1, x2, y2, p); // Vertical - Octant 2
	    else
		return line7(x1, y1, x2, y2, p); // Vertical - Octant 7
	else
	    if (m > 0)
		return line1(x1, y1, x2, y2, p); // Horizontal - Octant 1
	    else
		return line8(x1, y1, x2, y2, p); // Horizontal - Octant 8
    }
    public boolean line7(int x1, int y1, int x2, int y2, Pixel p) {
	int A = y2 - y1; // dy
	int B = x1 - x2; // -dx
	int d = -2 * B + A;
	A = 2 * A;
	B = -2 * B;
	while (y1 >= y2) {
	    draw_pixel(x1, y1, p);
	    if (d > 0) {
		x1++;
		d += A;
	    }
	    y1--;
	    d += B;
	}
	return true;
    }
    public boolean line2(int x1, int y1, int x2, int y2, Pixel p) {
	int A = y2 - y1; // dy
	int B = x1 - x2; // -dx
	int d = 2 * B + A;
	A = 2 * A;
	B = 2 * B;
	while (y1 <= y2) {
	    draw_pixel(x1, y1, p);
	    if (d < 0) {
		x1++;
		d += A;
	    }
	    y1++;
	    d += B;
	}
	return true;
    }
    public boolean line8(int x1, int y1, int x2, int y2, Pixel p) {
	int A = y2 - y1; // dy
	int B = x1 - x2; // -dx
	int d = 2 * A - B;
	A = 2 * A;
	B = -2 * B;
	while (x1 <= x2) {
	    draw_pixel(x1, y1, p);
	    if (d < 0) {
		y1--;
		d += B;
	    }
	    x1++;
	    d += A;
	}
	return true;
    }
    public boolean line1(int x1, int y1, int x2, int y2, Pixel p) {
	int A = y2 - y1; // dy
	int B = x1 - x2; // -dx
	int d = 2 * A + B;
	A = 2 * A;
	B = 2 * B;
	while (x1 <= x2) {
	    draw_pixel(x1, y1, p);
	    if (d > 0) {
		y1++;
		d += B;
	    }
	    x1++;
	    d += A;
	}
	return true;
    }

    // File Creation
    public boolean save(String name) throws FileNotFoundException {
	PrintWriter pw = new PrintWriter(new File(name));
	pw.print("P3 " + x + " " + y + " 255\n"); // Heading
	for (int i = y - 1; i > -1; i--) {
	    for (int j = 0; j < x; j++) {
		// System.out.printf("x: %d\ty: %d\n", j, i); // Debugging
		pw.print(canvas[i][j]);
	    }
	}
	pw.close();
	return true;
    }
}
