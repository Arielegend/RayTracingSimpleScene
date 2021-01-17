package edu.cg.algebra;


//import ex3.UnimplementedMethodException;

public class Ops {
	public static final double epsilon = 1e-5;
	public static final double infinity = 1e8;

	public static double dot(Vec u, Vec v) {
		return u.x * v.x + u.y * v.y + u.z * v.z;
	}

	public static Vec cross(Vec u, Vec v) {
		return new Vec((u.y * v.z - u.z * v.y), (u.z * v.x - u.x * v.z), (u.x * v.y - u.y * v.x));
	}

	public static Vec mult(double a, Vec v) {
		return mult(new Vec(a), v);
	}

	public static Vec mult(Vec u, Vec v) {
		return new Vec(u.x * v.x, u.y * v.y, u.z * v.z);
	}

	public static Point mult(double a, Point p) {
		return mult(new Point(a), p);
	}

	public static Point mult(Point p1, Point p2) {
		return new Point(p1.x * p2.x, p1.y * p2.y, p1.z * p2.z);
	}

	public static double normSqr(Vec v) {
		return dot(v, v);
	}

	public static double norm(Vec v) {
		return Math.sqrt(normSqr(v));
	}

	public static double lengthSqr(Vec v) {
		return normSqr(v);
	}

	public static double length(Vec v) {
		return norm(v);
	}

	public static double dist(Point p1, Point p2) {
		return length(sub(p1, p2));
	}

	public static double distSqr(Point p1, Point p2) {
		return lengthSqr(sub(p1, p2));
	}

	public static Vec normalize(Vec v) {
		return mult(1.0 / norm(v), v);
	}

	public static Vec neg(Vec v) {
		return mult(-1, v);
	}

	public static Vec add(Vec u, Vec v) {
		return new Vec(u.x + v.x, u.y + v.y, u.z + v.z);
	}

	public static Point add(Point p, Vec v) {
		return new Point(p.x + v.x, p.y + v.y, p.z + v.z);
	}

	public static Point add(Point p1, Point p2) {
		return new Point(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
	}

	public static Point add(Point p, double t, Vec v) {
		// returns p + tv;
		return add(p, mult(t, v));
	}

	public static Vec sub(Point p1, Point p2) {
		return new Vec(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
	}

	public static boolean isFinite(Vec v) {
		return Double.isFinite(v.x) & Double.isFinite(v.y) & Double.isFinite(v.z);
	}

	public static boolean isFinite(Point p) {
		return Double.isFinite(p.x) & Double.isFinite(p.y) & Double.isFinite(p.z);
	}

	public static Vec reflect(Vec u, Vec normal) {
		return add(u, mult(-2 * dot(u, normal), normal));
	}

	/**
	 * Returns the refraction of the vector u.
	 * 
	 * @param u      the light vector direction.
	 * @param normal The normal of the surface at the intersection point
	 * @param n1     the refraction index of the first medium
	 * @param n2     the refraction index of the second medium
	 * @return
	 * 
	 * 
	 * 
	 * Snell's Law states: n1 * (Sin(alpha1)) = n2 * (Sin(alpha2))
	 * 		       
	 * 
	 * We look for Vector T = A + B 
	 * 		A = M * sin(alpha2) , B = -Normal * cos(alpha2).
	 * 			M = (Incoming+C)/sin(alpha1)
	 * 			C=cos(alpha1)*normal
	 * 
	 * 	........
	 * 
	 * Leads to
	 * 	T = X * (Incoming) + (X*C1 -C2)*Normal
	 * 			where:
	 * 				X = n1/n2
	 * 				C1 & C2 are given in website
	 * 
	 * https://www.scratchapixel.com
	 *                         /lessons/3d-basic-rendering
	 *                         /introduction-to-shading
	 *                         /reflection-refraction-fresnel
	 */
	public static Vec refract(Vec u, Vec normal, double n1, double n2) {
		//Same medium factor.
	    if (n1 == n2) return u; 
	    
	    //ray is facing to the surface,
	    //we need to neg it
	    Vec negU = u.neg().normalize();
	    
	    //lets start with getting alpha1,  sin(alpha1) and finally cos(alpha1) 
	    //alpha1 = dot product of negU and the normal    
	    double cosAlpha1 = negU.dot(normal);
	    
	    if(cosAlpha1 < 0) cosAlpha1 = -cosAlpha1;
	    
	    double Alpha1 = Math.acos(cosAlpha1);
	    
	    //ONLY if n1 is larger, need to verify for critical angle,
	    //might no pentrate at all.
	    //
	    //If alpha >  critAngel => TIR
	    //                         Total Internal Reflection
	    //
	    //whenever alpha2 > 90 degrees 
	    //(alpha2 is the direction of the vector after the hit with the surface)
	    //then there is TIR
	    //
	    //we know Sin(Crit) = n2/n1
	    if (n1 > n2) {
	        double crit = Math.asin((n2 / n1));       
	        if(Math.abs(Alpha1 - crit) < epsilon ) {
	        	//meaning alpha1, enter angle is very close to crit
	        	//therefore we need to reflect.
	        	//implemented
                return reflect(u, normal); 
	        }
	    } 
	    
	    //Constructing A and B vectors Helpers
	    double n = n1/n2;    
	    double c2Helper =  Math.pow(n, 2) * ( 1 - Math.pow(cosAlpha1, 2) );
	    double c1 = cosAlpha1;
	    double c2 = Math.sqrt( 1 - c2Helper);
	    double VecHelper = n*c1 - c2;
	    
	    //Constructing A and B vectors
	    Vec A = u.normalize().mult(n);
	    Vec B = normal.mult(VecHelper);
	    
	    return A.add(B); 
	    
	    }
	
}
