/**
 * Copyright 2012 Università degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package dmason.sim.field.continuous;

import java.util.HashMap;

import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;


public abstract class DContinuous2DThin extends DContinuous2D {
	private double width,height,localWidth, localHeight;
	public DContinuous2DThin(double discretization, double localWidth, double localHeight, double width, double height) {
		super(discretization, localWidth, localHeight);
		this.width=width;
		this.height=height;
	}
	
public abstract Double2D getObjectLocationThin(Object obj);
 
 /** Synonymous with getObjectLocation, which you should generally use instead. */
 public abstract Double2D getObjectLocationAsDouble2DThin(Object obj);
     
 public abstract boolean setObjectLocationThin(Object obj, final Double2D location);
public abstract Bag getRawObjectsAtLocationThin(MutableInt2D discretizeThin);

public abstract Bag getRawObjectsAtLocationThin(Int2D discretizeThin);


public final Double2D getDimensionsThin() { return new Double2D(width, height); }
     
 /** Discretizes the location according to the internal discretization of the Continuous2D.  You can use this to determine what
     internal grid slot the continuous point would fall in.  */
 public final Int2D discretizeThin(final Double2D location)
     {
     return new Int2D((int)(location.x / discretization), (int)(location.y / discretization));
     }
 
 /** Discretizes the location according to the provided discretization, which may or may not be the discretization used internally by the Continuous2D.
     If you're trying to determine what grid slot a continuous point would fall in, you probably want discretize(location) instead. */
 public final Int2D discretizeThin(final Double2D location, int discretization)
     {
     return new Int2D((int)(location.x / discretization), (int)(location.y / discretization));
     }
 
 public final Bag clearThin()
     {
     doubleLocationHash = new HashMap();
     return super.clear();
     }
     
 public final Object removeThin(final Object obj)
     {
     Object result = super.remove(obj);
     doubleLocationHash.remove(obj);
     return result;
     }
 
 /** Get the width */
 public double getWidthThin() { return width; }
 
 /** Get the height */
 public double getHeightThin() { return height; }
 
 /** Get the width */
 public double getLocalWidthThin() { return localWidth; }
 
 /** Get the height */
 public double getLocalHeightThin() { return localHeight; }
 
 /** Toroidal x */
 // slight revision for more efficiency
 public final double txThin(double x) 
     { 
     final double width = this.width;
     if (x >= 0 && x < width) return x;  // do clearest case first
     x = x % width;
     if (x < 0) x = x + width;
     return x;
     }
     
 /** Toroidal y */
 // slight revision for more efficiency
 public final double tyThin(double y) 
     { 
     final double height = this.height;
     if (y >= 0 && y < height) return y;  // do clearest case first
     y = y % height;
     if (y < 0) y = y + height;
     return y;
     }

 /*
   public final double tx(final double x) 
   { 
   final double width = this.width; 
   if (x >= 0) return (x % width); 
   final double width2 = (x % width) + width; 
   if (width2 < width) return width2;
   return 0;
   }
 */
 
 /*
   public final double ty(final double y) 
   { 
   final double height = this.height; 
   if (y >= 0) return (y % height); 
   final double height2 = (y % height) + height;
   if (height2 < height) return height2;
   return 0;
   }
 */
     

 /** Simple [and fast] toroidal x.  Use this if the values you'd pass in never stray
     beyond (-width ... width * 2) not inclusive.  It's a bit faster than the full
     toroidal computation as it uses if statements rather than two modulos.
     The following definition:<br>
     { double width = this.width; if (x >= 0) { if (x < width) return x; return x - width; } return x + width; } <br>
     ...produces the shortest code (24 bytes) and is inlined in Hotspot for 1.4.1.   However removing
     the double width = this.width; is likely to be a little faster if most objects are within the
     toroidal region. */
 public double stxThin(final double x) 
     { if (x >= 0) { if (x < width) return x; return x - width; } return x + width; }
 
 /** Simple [and fast] toroidal y.  Use this if the values you'd pass in never stray
     beyond (-height ... height * 2) not inclusive.  It's a bit faster than the full
     toroidal computation as it uses if statements rather than two modulos.
     The following definition:<br>
     { double height = this.height; if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; } <br>
     ...produces the shortest code (24 bytes) and is inlined in Hotspot for 1.4.1.   However removing
     the double height = this.height; is likely to be a little faster if most objects are within the
     toroidal region. */
 public double styThin(final double y) 
     { if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; }
     
 
 // some efficiency to avoid width lookups
 double _stxThin(final double x, final double width) 
     { if (x >= 0) { if (x < width) return x; return x - width; } return x + width; }

 /** Minimum toroidal distance between two values in the X dimension. */
 public double tdxThin(final double x1, final double x2)
     {
     double width = this.width;
     if (Math.abs(x1-x2) <= width / 2)
         return x1 - x2;  // no wraparounds  -- quick and dirty check
     
     double dx = _stxThin(x1,width) - _stxThin(x2,width);
     if (dx * 2 > width) return dx - width;
     if (dx * 2 < -width) return dx + width;
     return dx;
     }
 
 // some efficiency to avoid height lookups
 double _styThin(final double y, final double height) 
     { if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; }

 /** Minimum toroidal distance between two values in the Y dimension. */
 public double tdyThin(final double y1, final double y2)
     {
     double height = this.height;
     if (Math.abs(y1-y2) <= height / 2)
         return y1 - y2;  // no wraparounds  -- quick and dirty check

     double dy = _styThin(y1,height) - _styThin(y2,height);
     if (dy * 2 > height) return dy - height;
     if (dy * 2 < -height) return dy + height;
     return dy;
     }
 
 /** Minimum Toroidal Distance Squared between two points. This computes the "shortest" (squared) distance between two points, considering wrap-around possibilities as well. */
 public double tdsThin(final Double2D d1, final Double2D d2)
     {
     double dx = tdxThin(d1.x,d2.x);
     double dy = tdyThin(d1.y,d2.y);
     return (dx * dx + dy * dy);
     }

 /** Minimum Toroidal difference vector between two points.  This subtracts the second point from the first and produces the minimum-length such subtractive vector, considering wrap-around possibilities as well*/
 public Double2D tvThin(final Double2D d1, final Double2D d2)
     {
     return new Double2D(tdxThin(d1.x,d2.x),tdyThin(d1.y,d2.y));
     }
 
 final static double SQRT_2_MINUS_1_DIV_2 = (Math.sqrt(2.0) - 1) * 0.5;  // about 0.20710678118654757, yeesh, I hope my math's right.
 final static int NEAREST_NEIGHBOR_GAIN = 10;  // the ratio of searches before we give up and just hand back the entire allObjects bag.
 
 /**
    Finds and returns at LEAST the 'atleastThisMany' items closest to a given 'position', plus potentially other items.
    <b>toroidal must be false -- presently it's not supported.</b>  If objects are non-point and may overlap into another discretization cell,
    set 'nonPointObjects' to true.  If you want the distance to be radial -- that is, the region searched will be a circle centered at the position,
    set 'radial' to true (almost always you want this).  If you want the region searched to be a rectangle centered at the position, set
    'radial' to be false.  Returns a bag of items.  If 'result' is provided, clears that Bag and reuses it.
 */
 public Bag getNearestNeighborsThin(Double2D position, int atLeastThisMany, final boolean toroidal, final boolean nonPointObjects, boolean radial, Bag result)
     {
     if (toroidal) throw new InternalError("Toroidal not presently supported in getNearestNeighbors");
     if (result == null) result = new Bag(atLeastThisMany);
     else result.clear();
     int maxSearches = allObjects.numObjs / NEAREST_NEIGHBOR_GAIN;

     if (atLeastThisMany >= allObjects.numObjs)  { result.clear(); result.addAll(allObjects); return result; }

     Int2D d = discretizeThin(position);
     int x1 = d.x;
     int x2 = d.x;
     int y1 = d.y;
     int y2 = d.y;
     int searches = 0;
     
     MutableInt2D speedyMutableInt2D = this.speedyMutableInt2D;  // a little faster (local)

     // grab the first box
         
     if (searches >= maxSearches) { result.clear(); result.addAll(allObjects); return result; }
     searches++;
     speedyMutableInt2D.x = x1; speedyMutableInt2D.y = y1;
     Bag temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
     if (temp!= null) result.addAll(temp);
     
     boolean nonPointOneMoreTime = false;
     // grab onion layers
     while(true)
         {
         if (result.numObjs >= atLeastThisMany)
             {
             if (nonPointObjects && !nonPointOneMoreTime)  // need to go out one more onion layer
                 nonPointOneMoreTime = true;
             else break;
             }
             
         x1--; y1--; x2++; y2++;
         // do top onion layer
         speedyMutableInt2D.y = y1;
         for(int x = x1 ; x <= x2 /* yes, <= */ ; x++)
             {
             if (searches >= maxSearches) { result.clear(); result.addAll(allObjects); return result; }
             searches++;
             speedyMutableInt2D.x = x;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);
             }

         // do bottom onion layer
         speedyMutableInt2D.y = y2;
         for(int x = x1 ; x <= x2 /* yes, <= */ ; x++)
             {
             if (searches >= maxSearches) { result.clear(); result.addAll(allObjects); return result; }
             searches++;
             speedyMutableInt2D.x = x;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);
             }
             
         // do left onion layer not including corners
         speedyMutableInt2D.x = x1;
         for(int y = y1 + 1 ; y <= y2 - 1 /* yes, <= */ ; y++)
             {
             if (searches >= maxSearches) { result.clear(); result.addAll(allObjects); return result; }
             searches++;
             speedyMutableInt2D.y = y;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);
             }

         // do right onion layer not including corners
         speedyMutableInt2D.x = x2;
         for(int y = y1 + 1 ; y <= y2 - 1 /* yes, <= */ ; y++)
             {
             if (searches >= maxSearches) { result.clear(); result.addAll(allObjects); return result; }
             searches++;
             speedyMutableInt2D.y = y;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);
             }
         }
         
     if (!radial) return result;
     
     // Now grab some more layers, in a "+" form around the box.  We need enough extension that it includes
     // the circle which encompasses the box.  To do this we need to compute 'm', the maximum extent of the
     // extension.  Let 'n' be the width of the box.
     //
     // m = sqrt(n^2 + n^2)
     //
     // Now we need to subtract m from n, divide by 2, take the floor, and add 1 for good measure.  That's the size of
     // the extension in any direction:
     //
     // e = floor(m-n) + 1
     // 
     // this comes to:
     //
     // e = floor(n * (sqrt(2) - 1)/2 ) + 1
     //
     
     int n = (x2 - x1 + 1);  // always an odd number
     int e = (int)(Math.floor(n * SQRT_2_MINUS_1_DIV_2)) + 1;
     
     // first determine: is it worth it?
     int numAdditionalSearches = (x2 - x1 + 1) * e * 4;
     if (searches + numAdditionalSearches >= maxSearches) { result.clear(); result.addAll(allObjects); return result; }
     
     // okay, let's do the additional searches
     for(int x = 0 ; x < x2 - x1 + 1 /* yes, <= */ ; x++)
         {
         for(int y = 0 ; y < e; y++)
             {
             // top
             speedyMutableInt2D.x = x1 + x ;
             speedyMutableInt2D.y = y1 - e - 1 ;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);

             // bottom
             speedyMutableInt2D.x = x1 + x ;
             speedyMutableInt2D.y = y2 + e + 1 ;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);

             // left
             speedyMutableInt2D.x = x1 - e - 1 ;
             speedyMutableInt2D.y = y1 + x;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);

             // right
             speedyMutableInt2D.x = x2 + e + 1 ;
             speedyMutableInt2D.y = y1 + x;
             temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
             if (temp!=null) result.addAll(temp);
             }
         }
         
     // it better have it now!
     return result;
     }


 /** Returns a Bag containing EXACTLY those objects within a certain distance of a given position, or equal to that distance, measuring
     using a circle of radius 'distance' around the given position.  Assumes non-toroidal point objects. 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */

 public Bag getObjectsExactlyWithinDistanceThin(final Double2D position, final double distance)
     {
     return getObjectsExactlyWithinDistanceThin(position, distance, false, true, true, null);
     }

 /** Returns a Bag containing EXACTLY those objects within a certain distance of a given position, or equal to that distance, measuring
     using a circle of radius 'distance' around the given position.  If 'toroidal' is true, then the
     distance is measured assuming the environment is toroidal.  Assumes point objects.  
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */

 public Bag getObjectsExactlyWithinDistanceThin(final Double2D position, final double distance, final boolean toroidal)
     {
     return getObjectsExactlyWithinDistanceThin(position, distance, toroidal, true, true, null);
     }

 /** Returns a Bag containing EXACTLY those objects within a certain distance of a given position.  If 'radial' is true,
     then the distance is measured using a circle around the position, else the distance is meaured using a square around
     the position (that is, it's the maximum of the x and y distances).   If 'inclusive' is true, then objects that are
     exactly the given distance away are included as well, else they are discarded.  If 'toroidal' is true, then the
     distance is measured assuming the environment is toroidal.  If the Bag 'result' is provided, it will be cleared and objects
     placed in it and it will be returned, else if it is null, then this method will create a new Bag and use that instead. 
     Assumes point objects. 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */

 public Bag getObjectsExactlyWithinDistanceThin(final Double2D position, final double distance, final boolean toroidal, 
     final boolean radial, final boolean inclusive, Bag result)
     {
     result = getObjectsWithinDistanceThin(position, distance, toroidal, false, result);
     int numObjs = result.numObjs;
     Object[] objs = result.objs;
     double distsq = distance*distance;
     if (radial) 
         for(int i=0;i<numObjs;i++)
             {
             double d = 0;
             Double2D loc = getObjectLocationThin(objs[i]);
             if (toroidal) d = tdsThin(position, loc);
             else d = position.distanceSq(loc);
             if (d > distsq || (!inclusive && d >= distsq)) 
                 { result.remove(i); i--; numObjs--; }
             }
     else 
         for(int i=0;i<numObjs;i++)
             {
             Double2D loc = getObjectLocationThin(objs[i]);
             double minx = 0;
             double miny = 0;
             if (toroidal)
                 {
                 minx = tdxThin(loc.x, position.x);
                 miny = tdyThin(loc.y, position.y);
                 }
             else
                 {
                 minx = loc.x - position.x;
                 miny = loc.y - position.y;
                 }
             if (minx < 0) minx = -minx;
             if (miny < 0) miny = -miny;
             if ((minx > distance || miny > distance) ||
                 (!inclusive && ( minx >= distance || miny >= distance)))
                 { result.remove(i); i--;  numObjs--; }
             }
     return result;
     }

 /** Returns a bag containing AT LEAST those objects within the bounding box surrounding the
     specified distance of the specified position.  The bag could include other objects than this.
     In this case we include the object if
     any part of the bounding box could overlap into the desired region.  To do this, if nonPointObjects is
     true, we extend the search space by one extra discretization in all directions.  For small distances within
     a single bucket, this returns nine bucket's worth rather than 1, so if you know you only care about the
     actual x/y points stored, rather than possible object overlap into the distance sphere you specified,
     you'd want to set nonPointObjects to FALSE. [assumes non-toroidal, point objects] 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */
 public Bag getObjectsWithinDistanceThin( final Double2D position, final double distance)
     { return getObjectsWithinDistanceThin(position,distance,false,false, null); }

 /** Returns a bag containing AT LEAST those objects within the bounding box surrounding the
     specified distance of the specified position.  The bag could include other objects than this.
     If toroidal, then wrap-around possibilities are also considered.
     In this case we include the object if
     any part of the bounding box could overlap into the desired region.  To do this, if nonPointObjects is
     true, we extend the search space by one extra discretization in all directions.  For small distances within
     a single bucket, this returns nine bucket's worth rather than 1, so if you know you only care about the
     actual x/y points stored, rather than possible object overlap into the distance sphere you specified,
     you'd want to set nonPointObjects to FALSE. [assumes point objects] 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */
 public Bag getObjectsWithinDistanceThin( final Double2D position, final double distance, final boolean toroidal)
     { return getObjectsWithinDistanceThin(position,distance,toroidal,false, null); }

 /** Returns a bag containing AT LEAST those objects within the bounding box surrounding the
     specified distance of the specified position.  The bag could include other objects than this.
     If toroidal, then wrap-around possibilities are also considered.
     If nonPointObjects, then it is presumed that
     the object isn't just a point in space, but in fact fills an area in space where the x/y point location
     could be at the extreme corner of a bounding box of the object.  In this case we include the object if
     any part of the bounding box could overlap into the desired region.  To do this, if nonPointObjects is
     true, we extend the search space by one extra discretization in all directions.  For small distances within
     a single bucket, this returns nine bucket's worth rather than 1, so if you know you only care about the
     actual x/y points stored, rather than possible object overlap into the distance sphere you specified,
     you'd want to set nonPointObjects to FALSE. 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */
     
 public Bag getObjectsWithinDistanceThin( final Double2D position, final double distance, final boolean toroidal,
     final boolean nonPointObjects)
     { return getObjectsWithinDistanceThin(position, distance, toroidal, nonPointObjects, null); }
 
 /** Puts into the result Bag (and returns it) AT LEAST those objects within the bounding box surrounding the
     specified distance of the specified position.  If the result Bag is null, then a Bag is created.
     
     <p>The bag could include other objects than this.
     If toroidal, then wrap-around possibilities are also considered.
     If nonPointObjects, then it is presumed that
     the object isn't just a point in space, but in fact fills an area in space where the x/y point location
     could be at the extreme corner of a bounding box of the object.  In this case we include the object if
     any part of the bounding box could overlap into the desired region.  To do this, if nonPointObjects is
     true, we extend the search space by one extra discretization in all directions.  For small distances within
     a single bucket, this returns nine bucket's worth rather than 1, so if you know you only care about the
     actual x/y points stored, rather than possible object overlap into the distance sphere you specified,
     you'd want to set nonPointObjects to FALSE. 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */
 
 public Bag getObjectsWithinDistanceThin( Double2D position, final double distance, final boolean toroidal,
     final boolean nonPointObjects, Bag result)
     {
     // push location to within legal boundaries
     if (toroidal && (position.x >= width || position.y >= height || position.x < 0 || position.y < 0))
         position = new Double2D(txThin(position.x), tyThin(position.y));
     
     double discDistance = distance / discretization;
     double discX = position.x / discretization;
     double discY = position.y / discretization;
     
     if (nonPointObjects)
         {
         // We assume that the discretization is larger than the bounding
         // box width or height for the object in question.  In this case, then
         // we can just increase the range by 1 in each direction and we are
         // guaranteed to have the location of the object in our collection.
         discDistance++;
         }

     final int expectedBagSize = 1;  // in the future, pick a smarter bag size?
     if (result!=null) result.clear();
     else result = new Bag(expectedBagSize);
     Bag temp;
 
     MutableInt2D speedyMutableInt2D = this.speedyMutableInt2D;  // a little faster (local)

         
     // do the loop
     if( toroidal )
         {
         final int iWidth = (int)(StrictMath.ceil(width / discretization));
         final int iHeight = (int)(StrictMath.ceil(height / discretization));

         // we're using StrictMath.floor instead of Math.floor because
         // Math.floor just calls StrictMath.floor, and so using the
         // StrictMath version may help in the inlining (one function
         // to inline, not two).  They should be identical in function anyway.
         
         int minX = (int) StrictMath.floor(discX - discDistance);
         int maxX = (int) StrictMath.floor(discX + discDistance);
         int minY = (int) StrictMath.floor(discY - discDistance);
         int maxY = (int) StrictMath.floor(discY + discDistance);

         if (position.x + distance >= width && maxX == iWidth - 1)  // oops, need to recompute wrap-around if width is not a multiple of discretization
             maxX = 0;

         if (position.y + distance >= height && maxY == iHeight - 1)  // oops, need to recompute wrap-around if height is not a multiple of discretization
             maxY = 0;



         // we promote to longs so that maxX - minX can't totally wrap around by accident
         if ((long)maxX - (long)minX >= iWidth)  // total wrap-around.
             { minX = 0; maxX = iWidth-1; }
         if ((long)maxY - (long)minY >= iHeight) // similar
             { minY = 0; maxY = iHeight-1; }

         // okay, now tx 'em.
         final int tmaxX = toroidalThin(maxX,iWidth);
         final int tmaxY = toroidalThin(maxY,iHeight);
         final int tminX = toroidalThin(minX,iWidth);
         final int tminY = toroidalThin(minY,iHeight);
                     
         int x = tminX ;
         do
             {
             int y = tminY;
             do
                 {
                 // grab location
                 speedyMutableInt2D.x=x;
                 speedyMutableInt2D.y=y;
                 temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
                 if( temp != null && !temp.isEmpty())
                     {
                     // a little efficiency: add if we're 1, addAll if we're > 1, 
                     // do nothing if we're <= 0 (we're empty)
                     final int n = temp.numObjs;
                     if (n==1) result.add(temp.objs[0]);
                     else result.addAll(temp);
                     }

                 // update y
                 if( y == tmaxY )
                     break;
                 else if( y == iHeight-1 )
                     y = 0;
                 else
                     y++;
                 }
             while(true);

             // update x
             if( x == tmaxX )
                 break;
             else if( x == iWidth-1 )
                 x = 0;
             else
                 x++;
             }
         while(true);
         }
     else
         {
         // we're using StrictMath.floor instead of Math.floor because
         // Math.floor just calls StrictMath.floor, and so using the
         // StrictMath version may help in the inlining (one function
         // to inline, not two).  They should be identical in function anyway.
         
         int minX = (int) StrictMath.floor(discX - discDistance);
         int maxX = (int) StrictMath.floor(discX + discDistance);
         int minY = (int) StrictMath.floor(discY - discDistance);
         int maxY = (int) StrictMath.floor(discY + discDistance);

         // for non-toroidal, it is easier to do the inclusive for-loops
         for(int x = minX; x<= maxX; x++)
             for(int y = minY ; y <= maxY; y++)
                 {
                 // grab location
                 speedyMutableInt2D.x=x;
                 speedyMutableInt2D.y=y;
                 temp = getRawObjectsAtLocationThin(speedyMutableInt2D);
                 if( temp != null && !temp.isEmpty())
                     {
                     // a little efficiency: add if we're 1, addAll if we're > 1, 
                     // do nothing if we're <= 0 (we're empty)
                     final int n = temp.numObjs;
                     if (n==1) result.add(temp.objs[0]);
                     else result.addAll(temp);
                     }
                 }
         }

     return result;
     }
     
 // used internally in getObjectsWithinDistance.  Note similarity to
 // AbstractGrid2D's tx method
 final int toroidalThin(final int x, final int width) 
     { 
     if (x >= 0) return (x % width); 
     final int width2 = (x % width) + width; 
     if (width2 < width) return width2;
     return 0;
     }

 MutableInt2D speedyMutableInt2D = new MutableInt2D();

 /** Returns a bag containing all the objects at a given discretized location, or null when there are no objects at the location.
     You should NOT MODIFY THIS BAG. This is the actual container bag, and modifying it will almost certainly break
     the Sparse Field object.   If you want to modify the bag, make a copy and modify the copy instead,
     using something along the lines of <b> new Bag(<i>foo</i>.getObjectsAtLocation(<i>location</i>)) </b>.
     Furthermore, changing values in the Sparse Field may result in a different bag being used -- so you should
     not rely on this bag staying valid.  The default implementation of this method simply calls getRawObjectsAtLocation(),
     but you may need to override it for more custom functionality (which is rare).
 */
 public Bag getObjectsAtDiscretizedLocationThin(final Int2D location)
     {
     return getRawObjectsAtLocation(location);
     }

 /** Returns a bag containing all the objects at a given location, 
     or null if there are no such objects or if location is null.  Unlike other SparseField versions, you may modify this bag.
 */
 public Bag getObjectsAtLocationThin(Double2D location)
     {
     if (location == null) return null;
     Bag cell = getRawObjectsAtLocationThin(discretizeThin(location));
     if (cell == null) return null;
     Bag result = new Bag();
     Object[] objs = cell.objs;
     int numObjs = cell.numObjs;
     // whittle down
     for(int i = 0; i < numObjs; i++)
         {
         Object loc = getObjectLocationThin(objs[i]);
         if (loc.equals(location))
             result.add(objs[i]);
         }
     return result;
     }
             
 /** Returns the number of the objects at a given location, 
     or 0 if there are no such objects or if location is null.
 */
 public int numObjectsAtLocationThin(Double2D location)
     {
     if (location == null) return 0;
     Bag cell = getRawObjectsAtLocationThin(discretizeThin(location));
     if (cell == null) return 0;
     int count = 0;
     Object[] objs = cell.objs;
     int numObjs = cell.numObjs;
     // whittle down
     for(int i = 0; i < numObjs; i++)
         {
         Object loc = getObjectLocationThin(objs[i]);
         if (loc.equals(location))
             count++;
         }
     return count;
     }

 /** Returns a bag containing all the objects at the exact same location as a given object, including the object itself, 
     or null if the object is not in the Field.  Unlike other SparseField versions, you may modify this bag.
 */
 public Bag getObjectsAtLocationOfObjectThin(Object obj)
     {
     Object location = getObjectLocationThin(obj);
     if (location == null) return null;
     else return getObjectsAtLocation(location);
     }
             
 /** Returns the number of objects at the exact same location as a given object, including the object itself, 
     or 0 if the object is not in the Field.
 */
 public int numObjectsAtLocationOfObjectThin(Object obj)
     {
     Object location = getObjectLocationThin(obj);
     if (location == null) return 0;
     else return numObjectsAtLocation(location);
     }

 /** Removes objects at exactly the given location, and returns a bag of them, or null of no objects are at that location.
     The Bag may be empty, or null, if there were no objects at that location.  You can freely modify this bag. */
 public Bag removeObjectsAtLocationThin(final Double2D location)
     {
     Bag bag = getObjectsAtLocationThin(location);               // this bag is a copy so it won't be reduced as I remove objects
     Object[] objs = bag.objs;
     int numObjs = bag.numObjs;
     for(int i = 0; i < bag.numObjs; i++)
         removeThin(objs[i]);
     return bag;
     }
	
}
