package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;
	
	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}
	
	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}
	
	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}
	
	public double substitute(Point p) {
		    return p.distSqr(this.center) - this.radius * this.radius;
	}
	
	//used help with -
	//				https://www.scratchapixel.com/lessons/
	//										3d-basic-rendering/
	//										minimal-ray-tracer-rendering-simple-shapes/
	//										ray-sphere-intersection
	@Override
	public Hit intersect(Ray ray) {
		Vec L = new Vec( ray.source(), this.center);
		
		double tca = L.dot(ray.direction());
		if(tca < 0) {
			return null;
		}
		
		//we know d^2 + tca^2 = L^2
		//d = sqrt(L^2 - tca^2)
		double d = Math.pow(L.length(), 2) - Math.pow(tca, 2);
		if(d < Ops.epsilon) {
			return null;
		}
		
		d = Math.sqrt(d);
		
		//we know d^2 + thc^2 = Radius^2
		//thc = sqrt(Radius^2 - d^2)	
		//whenever there is 1 hit - its when Radius == d 
		if( Math.abs(d - this.radius) < Ops.epsilon ) {
			
			double t_Singlehit = Math.pow(L.length(), 2) - Math.pow(this.radius, 2);
			t_Singlehit = Math.sqrt(t_Singlehit);
			
			Point P_hit = ray.source().add(ray.direction().mult(t_Singlehit));
			
			Vec normal = getNormalAtPoint(P_hit);
			Hit hit = new Hit(t_Singlehit, normal);
			return hit;	
		}
		
		double thc = Math.pow(this.radius, 2) - Math.pow(d , 2);
		if(thc < 0) return null;
		thc = Math.sqrt(thc);
		
		//we have 2 hits at this point.
		//need to get closer one
		double t0 = tca - thc;
		double t1 = tca + thc;
		
		//getting minimal value for t.
		double t = Math.min(t0,  t1);
		
		//Hitting Point is the direction of ray(normalized) times distance.
		Point P_hit = ray.source().add(ray.direction().mult(t));
		
		Vec normal = getNormalAtPoint(P_hit);
		Hit hit = new Hit(t, normal);
		return hit;
		
	}
	
	//return normal at given point
	//by sub the point and center of sphere.
	private Vec getNormalAtPoint(Point pointOnSphere) {
	  	Vec normal = pointOnSphere.sub(this.center);
	    return normal.normalize();
	 }
}
