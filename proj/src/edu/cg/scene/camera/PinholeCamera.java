package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class PinholeCamera {
	
	  //Screen resolution(Width,Height) of output img
	  int Rx;	  
	  int Ry;
	  
	  double distanceToPlain;  
	  double viewAngle;	 
	  
	  // 3 vectors im modeling cooredinates.
	  // towardsVecis negative Z.
	  Vec towardsVec;	  
	  Vec upVec;	  
	  Vec rightVec;

	  //Defines the center point of the grid.
	  //it is the vector from the camera origin, 
	  //its value is -> towards_vector.mult(distance)
	  Point centerPoint;	  
	  Point cameraPosition;	  

 
	  

	/**
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy)
	 * and View Angle 90.
	 * 
	 * @param cameraPosition  - The position of the camera.
	 * @param towardsVec      - The towards vector of the camera (not necessarily
	 *                        normalized).
	 * @param upVec           - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center
	 *                        point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		
		//Normalizing given vectors
		this.upVec = upVec.normalize();
		this.towardsVec = towardsVec.normalize();
		
		//  X = towardsVec cross upVec
	    this.rightVec = towardsVec.cross(upVec).normalize(); 

	    //Setting initial values
	    this.cameraPosition = cameraPosition;
	    this.distanceToPlain = distanceToPlain;
	    
	    //marks the center of the Grid.
	    //Useful later for transform()
	    //simply the camera position + toward_normalized*distance
	    this.centerPoint = this.cameraPosition.add(this.towardsVec.mult(this.distanceToPlain));
	    
	 }

	/**
	 * Initializes the resolution and width of the image.
	 * 
	 * @param height    - the number of pixels in the y direction.
	 * @param width     - the number of pixels in the x direction.
	 * @param viewAngle - the view Angle.
	 */
	public void initResolution(int height, int width, double viewAngle) {
	    this.Rx = width;
	    this.Ry = height;
	    this.viewAngle = viewAngle;	  
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding
	 * pixel in model coordinates.
	 * 
	 * @param x - the pixel index in the x direction.
	 * @param y - the pixel index in the y direction.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 * 
	 * Note - 
	 * 			viewWidth = 2 * tan(viewAngle/2)*distance_to_plain
	 * 			PixelWidth == PixelHeight
	 * 			Center_Pixel  = center_point + moving_up + moving_right
	 * 
	 * seeing the grid as -
	 * 
	 * (0,0)..................................(Rx,0)
	 * (1,0)..................................(Rx,1)
	 *.....Small_x....................Big_x.........  
	 *.....Small_y....................Small_y.......
	 *..............................................
	 *..............................................
	 *.....Small_x....................Big_x.........
	 *.....Big_y......................Big_y.........
	 *..............................................
	 *(1,Ry-1)...........................(Rx-1,Ry-1)
	 *
	 * We denote big as larger than halfwwidth / halfheight seeing by camera.
	 * note, right_Vector point toward High values of X
	 * 		 up_Vector the exact opposite!
	 * 
	 * therefore, wherever Y is "big", than we need to point DOWN, (using minus sign on Up_Vector)
	 * and whenver X is small, we need to assign negative value to right_Vector.
	 * 
	 * 
	 * 
	 */
	public Point transform(int x, int y) {
		
		//viewWidth = 2 * tan(viewAngle/2)*distance_to_plain
		double tan = Math.tan(Math.toRadians(this.viewAngle / 2.0));
	    double cameraViewWidth =  this.distanceToPlain * tan * 2.0;
	    
	    // now calculating pixel width, 
	    // assiginig same value to pixel height
	    double pWidth = cameraViewWidth / this.Rx;
	    double pHeight = pWidth;
	    
	    //calculating distance for up Movement
	    //doing so by calculating the screen height, 
	    //determing coefficient to up vector.
	    int HalfScreen_height = (int)(this.Ry / 2.0);
	    
	    //again, hight values means we need to point at a negative direction (with respect to up_vector)
	    // so wee can see that if y is "big", than we assign negative value.
	    //because we go down the grid.
	    double upMove = (y - HalfScreen_height) * (pHeight * -1.0);
	    Vec upForPixel = this.upVec.mult(upMove);

	    //calculating distance for right
	    //Same as for height.
	    //wheever a "big" x comes (meaning he is larger than HalfScreen_width)
	    //we need to go on the positive direction of right_vec.
	    //the opposite is true for low values of x.
	    int HalfScreen_width = (int)(this.Ry / 2.0);
	    double rightMove = (x - HalfScreen_width) * pWidth;	    
	    Vec rightForPixel = this.rightVec.mult(rightMove);
	    
	    //finally, hitting point is:
	    //center_point + moving up(/down) + moving right(/left)
	    Point pixelCenterPoint = this.centerPoint.add(upForPixel).add(rightForPixel);
	    
	    return (pixelCenterPoint);
	
	}

	/**
	 * Returns the camera position
	 * 
	 * @return a new point representing the camera position.
	 */
	public Point getCameraPosition() {
		Point ans = new Point();
		ans = this.cameraPosition;
		
		//returns a new point
		return ans;
	}
	public int getRx() {
		return this.Rx;
	}
	
	public int getRy() {
		return this.Ry;
	}
	public double getDistance() {
		return this.distanceToPlain;
	}
	public double getAngle() {
		return this.viewAngle;
	}

//Both of these methods Anti2 and Anti3 work pretty much the same.
//lets take Anti2 is an example. 
// we know we need to return 4 points to cast rays at,
//so we cedide the pixel into 4 squares (similary on Anti3, but than to 9 squares)
// than, we can use similar calculations we did in Transform the get the sizes of a pixel.
// once we have the center of the pixel (givven), its ez to calculate all the needed points
// usuing the Up and Right vectors, and center of current pixel..

// for example, to get the top left square center,\
// all we need to do is to move half pixel left, and half a pixel top (from pixels center ofcoarse)
// and so on and so on..
	
	public Point[] Anti2(Point curCenterPoint, int x, int y) {
		
		//same calculations as transform
		double tan = Math.tan(Math.toRadians(this.viewAngle / 2.0));
	    double cameraViewWidth =  this.distanceToPlain * tan * 2.0;	    
	    double pWidth = cameraViewWidth / this.Rx;
	    double pHeight = pWidth;
	    
	    //constructing helper vectors to reach our points
	    Vec moveHalfLeft = this.rightVec.mult(pWidth * -0.5);
	    Vec moveHalfRight = this.rightVec.mult(pWidth * 0.5);
	    Vec moveHalfTop = this.upVec.mult(pHeight * 0.5);
	    Vec moveHalfBot = this.upVec.mult(pHeight * -0.5);
	    
	    //building 4 points we need to cast rays at finally
	    Point TopLeftCenter = curCenterPoint.add(moveHalfTop).add(moveHalfLeft);
	    Point TopRightCenter = curCenterPoint.add(moveHalfTop).add(moveHalfRight);
	    Point BotLeftCenter = curCenterPoint.add(moveHalfBot).add(moveHalfLeft);
	    Point BotRightCenter = curCenterPoint.add(moveHalfBot).add(moveHalfRight);
	    
	    Point[] arr = {TopLeftCenter, TopRightCenter, BotLeftCenter, BotRightCenter };
	    return arr;
			
	}

	public Point[] Anti3(Point curCenterPoint, int x, int y) {
		//same calculations as transform
		double tan = Math.tan(Math.toRadians(this.viewAngle / 2.0));
	    double cameraViewWidth =  this.distanceToPlain * tan * 2.0;	    
	    double pWidth = cameraViewWidth / this.Rx;
	    double pHeight = pWidth;
	    
	    //note, need to move third of a pixel each time.
	    Vec moveThirdLeft = this.rightVec.mult(pWidth * -0.33);
	    Vec moveThirdRight = this.rightVec.mult(pWidth * 0.33);
	    Vec moveThirdTop = this.upVec.mult(pHeight * 0.33);
	    Vec moveThirdBot = this.upVec.mult(pHeight * -0.33);
	    
	    //building 8 points we need to cast rays at finally
	    //the 9th point is actually the curCenterPoint
	    Point TopLeftCenter = curCenterPoint.add(moveThirdTop).add(moveThirdLeft);
	    Point TopRightCenter = curCenterPoint.add(moveThirdTop).add(moveThirdRight);
	    Point BotLeftCenter = curCenterPoint.add(moveThirdBot).add(moveThirdLeft);
	    Point BotRightCenter = curCenterPoint.add(moveThirdBot).add(moveThirdRight);
	    Point leftMid = curCenterPoint.add(moveThirdLeft);
	    Point topMid = curCenterPoint.add(moveThirdTop);
	    Point botMid = curCenterPoint.add(moveThirdBot);
	    Point rightMid = curCenterPoint.add(moveThirdRight);

	    Point[] arr = {TopLeftCenter, TopRightCenter, BotLeftCenter, BotRightCenter,
	    		       leftMid, topMid, botMid, rightMid , curCenterPoint		};
	    return arr;
	}
}
