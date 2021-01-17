package edu.cg.scene.lightSources;

import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class CutoffSpotlight extends PointLight {
	private Vec direction;
	private double cutoffAngle;

	public CutoffSpotlight(Vec dirVec, double cutoffAngle) {
		this.direction = dirVec;
		this.cutoffAngle = cutoffAngle;
	}

	public CutoffSpotlight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}

	public CutoffSpotlight initCutoffAngle(double cutoffAngle) {
		this.cutoffAngle = cutoffAngle;
		return this;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Spotlight: " + endl + description() + "Direction: " + direction + endl;
	}

	@Override
	public CutoffSpotlight initPosition(Point position) {
		return (CutoffSpotlight) super.initPosition(position);
	}

	@Override
	public CutoffSpotlight initIntensity(Vec intensity) {
		return (CutoffSpotlight) super.initIntensity(intensity);
	}

	@Override
	public CutoffSpotlight initDecayFactors(double q, double l, double c) {
		return (CutoffSpotlight) super.initDecayFactors(q, l, c);
	}

	@Override
	public boolean isOccludedBy(Surface surface, Ray rayToLight) {
		
		//If the dot product is 0, angle between rayToLight to Main_direction around 90 degrees.
		//therefore, we count it as a hit - meaning is black.
	    if (rayToLight.direction().neg().dot(this.direction.normalize()) < Ops.epsilon) return true; 
	    
	    //if this is not the case, we use super implemented Method.
	    boolean isOccludded = super.isOccludedBy(surface, rayToLight);
	    return isOccludded;
	}

	//we note that the differences between spotlight intensity and Point light intensity
	//is the COS( V.dot(V_D) )
	//we calculate gamma = V.dot(V_D)
	//and multiply it by its super
	@Override
	public Vec intensity(Point hittingPoint, Ray rayToLight) {
		Vec I_l;
		Vec V = rayToLight.direction().neg().normalize();
		Vec V_d = this.direction.normalize();
				
	    double coss = V.dot(V_d);
	    double actualAngle = Math.toDegrees(Math.acos(coss));
	    
	    //Checking maybe Actualy angle is larger than the opening angel of the ight
	    //Also, checking if light and rayTolight are around 90 degrees.
	    //in both cases - returning black
	    if( actualAngle > this.cutoffAngle ) return new Vec();
	    if ( coss < Ops.epsilon )            return new Vec(); 
	    
	    //Again, differences between spotlight_intensity to Point_light_intensity -> 
	    //        is ONLY the coss Veraible.
	    //therefore, getting intestiny from implememnted above super method,
	    //adn then multiplying it by coss
	    I_l = super.intensity(hittingPoint, rayToLight).mult(coss);
	    return I_l;
	}
}
