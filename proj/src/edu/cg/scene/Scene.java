package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; // gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(1, 1, 1); // white
	private Vec backgroundColor = new Vec(0, 0.5, 1); // blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();

	/////////////////////////////////////////////////////
	// initializers and Getters  Start///////////////////
	/////////////////////////////////////////////////////
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec, distanceToPlain);
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}

	
	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	//////////////////////////////////////////////////////
	
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl + "Ambient: " + ambient + endl + "Background Color: " + backgroundColor + endl
				+ "Max recursion level: " + maxRecursionLevel + endl + "Anti aliasing factor: " + antiAliasingFactor
				+ endl + "Light sources:" + endl + lightSources + endl + "Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
	}
	
	/////////////////////////////////////////////////////
	// initializers and Getters  End/////////////////////
	
	
	/////////////////////////////////////////////////////
	public BufferedImage render(int imgWidth, int imgHeight, double viewAngle, Logger logger)
			throws InterruptedException, ExecutionException, IllegalArgumentException {

		initSomeFields(imgWidth, imgHeight, logger);
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, viewAngle);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][]) (new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " + (imgHeight * imgWidth * antiAliasingFactor * antiAliasingFactor)
				+ " rays over " + name);

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");
		
		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			
			Point curCenterPoint = camera.transform(x, y);		
			
			//getting Color c by AntiAlias Fcrtor
			Color c = contineFromHereAntiAlias(curCenterPoint,x, y);
			
			return c;
		});
	}
	
	

	private Color contineFromHereAntiAlias(Point curCenterPoint, int x, int y) {
		switch(this.antiAliasingFactor) {
	
		// antiAliasingFactor is 1
		default:
			//Normal presidure
			Ray ray = new Ray(camera.getCameraPosition(), curCenterPoint);
			Vec color = calcColor(ray, 0);	
			return color.toColor();
		
		case 2:
			Vec finalColor2 = goOnAnti2(curCenterPoint, x, y);
			return finalColor2.toColor();
			
		case 3:
			Vec finalColor3 = goOnAnti3(curCenterPoint, x, y);
			return finalColor3.toColor();
		}
	}
	
	// on Methods goOnAnti3 and goOnAnti2
	//we simply get the relevant points we need to shoot extra rays to
	//the get-these-points methods are implemented in camera class.
	//than, we shoot threse rays normaly, and calculate the average of them.

	private Vec goOnAnti2(Point curCenterPoint, int x, int y) {
		
		//getting 4 points to shoot rays to
		Point[] arr = this.camera.Anti2(curCenterPoint, x , y);
		
		Ray ray1 = new Ray(camera.getCameraPosition(), arr[0]);
		Vec c1 = calcColor(ray1, 0);	

		Ray ray2 = new Ray(camera.getCameraPosition(), arr[1]);
		Vec c2 = calcColor(ray2, 0);	

		Ray ray3 = new Ray(camera.getCameraPosition(), arr[2]);
		Vec c3 = calcColor(ray3, 0);	

		Ray ray4 = new Ray(camera.getCameraPosition(), arr[3]);
		Vec c4 = calcColor(ray4, 0);	
		
		//summing up all colors
		Vec finalColor = c1.add(c2).add(c3).add(c4);
		
		//returnning the avaerage color
		finalColor = finalColor.mult(0.25);
		
		return finalColor;
}

	private Vec goOnAnti3(Point curCenterPoint, int x, int y) {
		
		//getting 9 points to shoot rays to
		Point[] arr = this.camera.Anti3(curCenterPoint, x , y);
		
		Ray ray1 = new Ray(camera.getCameraPosition(), arr[0]);
		Vec c1 = calcColor(ray1, 0);	

		Ray ray2 = new Ray(camera.getCameraPosition(), arr[1]);
		Vec c2 = calcColor(ray2, 0);	

		Ray ray3 = new Ray(camera.getCameraPosition(), arr[2]);
		Vec c3 = calcColor(ray3, 0);	

		Ray ray4 = new Ray(camera.getCameraPosition(), arr[3]);
		Vec c4 = calcColor(ray4, 0);	
		
		Ray ray5 = new Ray(camera.getCameraPosition(), arr[4]);
		Vec c5 = calcColor(ray5, 0);	

		Ray ray6 = new Ray(camera.getCameraPosition(), arr[5]);
		Vec c6 = calcColor(ray6, 0);	

		Ray ray7 = new Ray(camera.getCameraPosition(), arr[6]);
		Vec c7 = calcColor(ray7, 0);	

		Ray ray8 = new Ray(camera.getCameraPosition(), arr[7]);
		Vec c8 = calcColor(ray8, 0);
		
		Ray ray9 = new Ray(camera.getCameraPosition(), arr[8]);
		Vec c9 = calcColor(ray9, 0);
		
		//averaging them.
		//0.11 is actually 1/9
		Vec finalColor = c1.add(c2).add(c3).add(c4).add(c5).add(c6).add(c7).add(c8).add(c9);
		finalColor = finalColor.mult(0.11);
		
		return finalColor;
}
	

	//ray is the ray we shoot from camera to the center of each pixel.
	private Vec calcColor(Ray ray, int recusionLevel) {
		
		//this is a recursive function.
		//starting at stop value, which is maxRecursionLevel
	    if (this.maxRecursionLevel <= recusionLevel )  return new Vec();

	    //first we need to verify this ray is hitting something
	    //if no hit, then we return the background color
	    Hit hit = getIntersectionOfRayWithScene(ray);
	    if (hit == null) return (this.backgroundColor);
	    	   
	    //each hit represented by the surface it hits
	    Surface curSurface = hit.getSurface();
	    
	    //initializing finalColor vector.
	    //Ambient part is a shared property
	    Vec FinalColor = curSurface.Ka().mult(this.ambient);
	    
	    //getting to actual point of where this ray hits the surface
	    //getting it by adding source of ray, hit.t times the ray direction
	    //already implemented
	    Point scene_ray_hit_Point = ray.getHittingPoint(hit);
	    
	    //At this point, we have a Hit
	    //Need to Loop by each light to get Total intensity
	    //
	    //Refraction and Reflactions will be added after if needed
	    for(int i = 0; i < this.lightSources.size(); i++) {
	    	
	    	//Loop by each light
	    	Light curLight = this.lightSources.get(i);
	    	
	    	//Constructs a ray originated from the given point to the light.
	        Ray comingRayToLight = curLight.rayToLight(scene_ray_hit_Point);
	        
	        //First need to verify this light is essantial
	        //for it might be blocked by a surface
	        boolean isRayBlocked = isRayBlockedBySomething(comingRayToLight, curLight);
	        
	        //whenever ray IS NOT blocked to this specific light,
	        //(whene there is no intersection with any surface
	        //on the line connecting ray_source and light position.)
	        //
	        //we need to calculate this light diffuse + specular part
	        if (isRayBlocked == false) {
	        	
	          //calculating Diffusal part
	          Vec diffPart = getDiffusePart(hit, comingRayToLight);	   
	          
	          //calculating Specular part
	          Vec specPart = getSpecPart(hit, comingRayToLight, ray);
	          
	          //before adding Diffusal & Specurlar - we multiply by Intensity of light  at this point
	          Vec Intensity = curLight.intensity(scene_ray_hit_Point, comingRayToLight);
	          Vec withIntensity = diffPart.add(specPart).mult(Intensity);
	          
	          FinalColor = FinalColor.add(withIntensity);
	          
	        } 
	        
	      }
	    
	    //At this point we Are off to go with no refraction nor reflection.
	    //We check if requiered each seperately.    
	    
	    //checks for Refarctions
	    if (this.renderRefarctions) {	    	
	    	Vec refractionPart = getRefractionPart(curSurface, ray, hit, recusionLevel, scene_ray_hit_Point);
	    	FinalColor.add(refractionPart);
	      }
	    
	    //checks for Reflections
	    //Using Implemented Method for Reflections in Ops class.
	    if (this.renderReflections) {
	    	Vec relectionPart = getReflectionPart(curSurface, ray, hit, recusionLevel, scene_ray_hit_Point);
	        FinalColor = FinalColor.add(relectionPart);
	      } 
	    
	    //We added the 3 commponents by Phong - Ambient + diffusal + Spec
	    //we added (is neccessary) reflection & refraction 
		return FinalColor;
	}


	private Vec getReflectionPart(Surface surface, Ray ray, Hit hit, int recusionLevel, Point hit_Point) {
		
		//Getting how much this surface reflects, 
		//and the reflection Vector (direction)
        Vec Rate_of_reflection = new Vec(surface.reflectionIntensity());
        Vec reflection = Ops.reflect(ray.direction(), hit.getNormalToSurface());
        
        //continue shoot the ray -> by calculate color for its reflection
        //we construct a new ray,
        //which starts at hitting point, with the new reflection vector 
        //rasing recursion level
        //we stop at recursive factor
        Vec ref_color = calcColor(new Ray(hit_Point, reflection), recusionLevel + 1).mult(Rate_of_reflection);
        
        //Reflection after all continues going for ever in  an Utopic World, (as long as there are new hits) 
        //gaining more and more of the colors it hits
        //Same is refraction, need to multiply by Rate_of_reflection of each surface.
        return (ref_color);
	}

	//Verifying if this surface is transparent
	//if it is, continue calculating,
	//rase recursion level by 1
	//finish with multiply with Rate_of_refraction of each surface.
	private Vec getRefractionPart(Surface surface, Ray ray, Hit hit, int recusionLevel, Point hit_Point) {
		
		Vec refractionPart = new Vec();
		
		boolean seeThrough = surface.isTransparent();
        if (seeThrough == true) {
        	
          double medium1 = surface.n1(hit);
          double medium2 = surface.n2(hit);
          
          //Sending parameters to initiate the Refrect Method On Ops class.
          //reciving the direction of the vector in medium 2!
          Vec refraction_ray = calcRefractionRay(medium1, medium2, ray, hit); 
        
          //getting refract factor of this surface.
          Vec Rate_of_refraction = new Vec(surface.refractionIntensity());
          
          Ray rayHelper = new Ray(hit_Point, refraction_ray);
          refractionPart = calcColor(rayHelper, recusionLevel + 1).mult(Rate_of_refraction);
        } 
        
        // object is not seeing through, we count as 0 the refract part.
        return refractionPart;
	}
	
	
	//using given unImplemented Method in Ops class.
	private Vec calcRefractionRay(double medium1, double medium2, Ray ray, Hit hit) {
        Vec refraction_ray = Ops.refract(ray.direction(), hit.getNormalToSurface(), medium1, medium2);
        return refraction_ray;
	}

	//I_s = K_s * I_L* [ cos(alpha) to the power of n]
	//alpha is angle between L_hat and Viewr.
	private Vec getSpecPart(Hit hit, Ray comingRayToLight, Ray Viewr) {
		
	    Vec K_s = hit.getSurface().Ks();
	    int shine = hit.getSurface().shininess();
	    
	    Vec rayToLight = comingRayToLight.direction();
	    Vec normal = hit.getNormalToSurface();
	    Vec L_hat = Ops.reflect(rayToLight.neg(), normal);
	    Vec v = Viewr.direction();
	    	    
	    Vec V_helper = v.neg();
	    double angel_Viewer_L_hat = L_hat.dot(V_helper);
	    
	    //if angle is around 90 than sperc part is black (0)
	    if(angel_Viewer_L_hat <= Ops.epsilon) {
	    	return new Vec();
	    }
	    else {
		    return K_s.mult(Math.pow(angel_Viewer_L_hat, shine));
	    }
	}

	//I_D = I_L*cos(theta)*K_D
	//I_l is intensity of light - will be computed fater returning from here.
	//theta is angle between L which is vecotor to light, and Normal to surface.
	private Vec getDiffusePart(Hit hit, Ray comingRayToLight) {
		
		Vec I_d;
	    Vec normal = hit.getNormalToSurface();
	    Vec toLight = comingRayToLight.direction();   
	    
	    if(normal.dot(toLight) <= Ops.epsilon) {
	    	I_d = new Vec();
	    	return I_d;
	    }
	    
	    Vec K_d = hit.getSurface().Kd();
    	I_d = K_d.mult(normal.dot(toLight));
    	
    	//all we need to do now is to calculate 
    	//current light Intensity at hit point
    	return I_d;
	}

	//At this function, we are givven a ray toward a given light.
	//Need to loop through each surface to understand if its blocked or not.
	//Once there is a surface that is hiding this light, we return true
	private boolean isRayBlockedBySomething(Ray comingRayToLight, Light curLight) {
		
		for(int i=0; i < this.surfaces.size(); i++) {
			Surface curSurface = this.surfaces.get(i);
			
			//Once there is a surface hiding this light. returning true
	        if (curLight.isOccludedBy(curSurface, comingRayToLight)) return true; 
	      } 
			
		  //none of surfaces is blocking this ray to this light.
	      return false;
	 }


	//Return Hit of the ray with the scene.
	//Return null if none.
	private Hit getIntersectionOfRayWithScene(Ray rayToScene) {
		
	    Hit hitToReturn = null;
	    for(int i=0; i < this.surfaces.size(); i++) {
	    	
	    	//loop through each surface at the scene
	    	Surface curSurface = this.surfaces.get(i);
	    	
	    	//the heart of this method.
	    	//according to the surface shape searches for intersection.
	    	Hit curHit = curSurface.intersect(rayToScene);
	      
	      if(hitToReturn == null) hitToReturn = curHit;
	      
	      if(hitToReturn != null) {
	    	  // compare function return -1 if this.t is LOWER than other
	    	  //therfore need to switch
	    	  if(curHit != null && curHit.compareTo(hitToReturn) < 0) hitToReturn = curHit;
	      }
	      
	    }
	    
	    //Returning minimal Hit (comparable by t)
	    return hitToReturn;
	}

}
