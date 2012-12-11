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

package dmason.sim.field.grid.numeric;

import sim.field.grid.IntGrid2D;
import sim.util.Int2D;
import sim.util.IntBag;

public abstract class DIntGrid2DThin extends DIntGrid2D {
	private int width,height,localWidth,localHeight;
	public DIntGrid2DThin(int localWidth, int localHeight, int width, int height, int initialValue) {
		super(localWidth, localHeight,initialValue);
		this.width=width;
		this.height=height;
		this.localWidth=localWidth;
		this.localHeight=localHeight;
	}
	
	 /** Sets location (x,y) to val */
    public abstract void setThin(final int x, final int y, final int val);
    
    /** Returns the element at location (x,y) */
    public abstract int getThin(final int x, final int y);

    /** Sets all the locations in the grid the provided element */
    public final IntGrid2D setToThin(int thisMuch)
        {
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                fieldx[y]=thisMuch;
                }
            }
        return this;
        }

    /** Sets the grid to a copy of the provided array, which must be rectangular. */
    public IntGrid2D setToThin(int[][] field)
        {
        // check info
        
        if (field == null)
            throw new RuntimeException("IntGrid2D set to null field.");
        int w = field.length;
        int h = 0;
        if (w != 0) h = field[0].length;
        for(int i = 0; i < w; i++)
            if (field[i].length != h) // uh oh
                throw new RuntimeException("IntGrid2D initialized with a non-rectangular field.");

        // load

        this.field = new int[w][h];
        for(int i = 0; i < width; i++)
            this.field[i] = (int[]) field[i].clone();
        width = w;
        height = h;
        return this;
        }

    

    /** Flattens the grid to a one-dimensional array, storing the elements in row-major order,including duplicates and null values. 
        Returns the grid. */
    public final int[] toArrayThin()
        {
        int[][] field = this.field;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        int[] vals = new int[width * height];
        int i = 0;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y = 0; y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                vals[i++] = fieldx[y];
                }
            }
        return vals;
        }
        
    /** Returns the maximum value stored in the grid */
    public final int maxThin()
        {
        int max = Integer.MIN_VALUE;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                if (max < fieldx[y]) max = fieldx[y];
                }
            }
        return max;
        }

    /** Returns the minimum value stored in the grid */
    public final int minThin()
        {
        int min = Integer.MAX_VALUE;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                if (min > fieldx[y]) min = fieldx[y];
                }                
            }
        return min;
        }
    
    /** Returns the mean value stored in the grid */
    public final double meanThin()
        {
        long count = 0;
        double mean = 0;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                mean += fieldx[y]; 
                count++; 
                }
            }
        return (count == 0 ? 0 : mean / count);
        }
    
    /** Thresholds the grid so that values greater to <i>toNoMoreThanThisMuch</i> are changed to <i>toNoMoreThanThisMuch</i>.
        Returns the modified grid. 
    */
    public final IntGrid2D upperBoundThin(int toNoMoreThanThisMuch)
        {
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                if (fieldx[y] > toNoMoreThanThisMuch)
                    fieldx[y] = toNoMoreThanThisMuch;
                }
            }
        return this;
        }

    /** Thresholds the grid so that values smaller than <i>toNoLowerThanThisMuch</i> are changed to <i>toNoLowerThanThisMuch</i>
        Returns the modified grid. 
    */

    public final IntGrid2D lowerBoundThin(int toNoLowerThanThisMuch)
        {
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                if (fieldx[y] < toNoLowerThanThisMuch)
                    fieldx[y] = toNoLowerThanThisMuch;
                }
            }
        return this;
        }

    /** Sets each value in the grid to that value added to <i>withThisMuch</i>
        Returns the modified grid. 
    */

    public final IntGrid2D addThin(int withThisMuch)
        {
        if (withThisMuch==0.0) return this;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                fieldx[y]+=withThisMuch;
                }
            }
        return this;
        }
        
    /** Sets the value at each location in the grid to that value added to the value at the equivalent location in the provided grid.
        Returns the modified grid. 
    */

    public final IntGrid2D addThin(IntGrid2D withThis)
        {
        int[][]ofield = withThis.field;
        int[] ofieldx = null;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            ofieldx = ofield[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                fieldx[y]+=ofieldx[y];
                }
            }
        return this;
        }

    /** Sets each value in the grid to that value multiplied <i>byThisMuch</i>
        Returns the modified grid. 
    */

    public final IntGrid2D multiplyThin(int byThisMuch)
        {
        if (byThisMuch==1.0) return this;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                fieldx[y]*=byThisMuch;
                }
            }
        return this;
        }
    
    /** Sets the value at each location in the grid to that value multiplied by to the value at the equivalent location in the provided grid.
        Returns the modified grid. 
    */

    public final IntGrid2D multiplyThin(IntGrid2D withThis)
        {
        int[][]ofield = withThis.field;
        int[] ofieldx = null;
        int[] fieldx = null;
        final int width = this.localWidth;
        final int height = this.localHeight;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            ofieldx = ofield[x];
            for(int y=0;y<height;y++)
                {
                assert sim.util.LocationLog.it(this, new Int2D(x,y));
                fieldx[y]*=ofieldx[y];
                }
            }
        return this;
        }


    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist.  This region forms a
     * square 2*dist+1 cells across, centered at (X,Y).  If dist==1, this
     * is equivalent to the so-called "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result IntBag the elements at each of those <x,y> locations clearning it first.  
     * Returns the result IntBag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public final IntBag getNeighborsMaxDistanceThin( final int x, final int y, final int dist, final boolean toroidal, IntBag result, IntBag xPos, IntBag yPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsMaxDistanceThin( x, y, dist, toroidal, xPos, yPos );

        if (result != null)
            { result.clear();  result.resize(xPos.size()); }
        else
            result = new IntBag(xPos.size());

        for( int i = 0 ; i < xPos.numObjs ; i++ )
            {
            assert sim.util.LocationLog.it(this, new Int2D(xPos.objs[i],yPos.objs[i]));
            result.add( field[xPos.objs[i]][yPos.objs[i]] );
            }
        return result;
        }

    /**
     * Gets all neighbors of a location that satisfy abs(x-X) + abs(y-Y) <= dist.  This region forms a diamond
     * 2*dist+1 cells from point to opposite point inclusive, centered at (X,Y).  If dist==1 this is
     * equivalent to the so-called "Von-Neumann Neighborhood" (the four neighbors above, below, left, and right of (X,Y)),
     * plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result IntBag the elements at each of those <x,y> locations clearning it first.  
     * Returns the result IntBag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public final IntBag getNeighborsHamiltonianDistanceThin( final int x, final int y, final int dist, final boolean toroidal, IntBag result, IntBag xPos, IntBag yPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsHamiltonianDistanceThin( x, y, dist, toroidal, xPos, yPos );

        if (result != null)
            { result.clear();  result.resize(xPos.size()); }
        else
            result = new IntBag(xPos.size());

        for( int i = 0 ; i < xPos.numObjs ; i++ )
            {
            assert sim.util.LocationLog.it(this, new Int2D(xPos.objs[i],yPos.objs[i]));
            result.add( field[xPos.objs[i]][yPos.objs[i]] );
            }
        return result;
        }

    /**
     * Gets all neighbors located within the hexagon centered at (X,Y) and 2*dist+1 cells from point to opposite point 
     * inclusive.
     * If dist==1, this is equivalent to the six neighbors immediately surrounding (X,Y), 
     * plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result IntBag the elements at each of those <x,y> locations clearning it first.  
     * Returns the result IntBag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public final IntBag getNeighborsHexagonalDistanceThin( final int x, final int y, final int dist, final boolean toroidal, IntBag result, IntBag xPos, IntBag yPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsHexagonalDistanceThin( x, y, dist, toroidal, xPos, yPos );

        if (result != null)
            { result.clear();  result.resize(xPos.size()); }
        else
            result = new IntBag(xPos.size());

        for( int i = 0 ; i < xPos.numObjs ; i++ )
            {
            assert sim.util.LocationLog.it(this, new Int2D(xPos.objs[i],yPos.objs[i]));
            result.add( field[xPos.objs[i]][yPos.objs[i]] );
            }
        return result;
        }
    
    // slight revision for more efficiency
    public final int txThin(int x) 
        { 
        final int width = this.width;
        if (x >= 0 && x < width) return x;  // do clearest case first
        x = x % width;
        if (x < 0) x = x + width;
        return x;
        }
        
    /*
      public final int ty(final int y) 
      { 
      final int height = this.height; 
      if (y >= 0) return (y % height); 
      final int height2 = (y % height) + height;
      if (height2 < height) return height2;
      return 0;
      }
    */
        
    // slight revision for more efficiency
    public final int tyThin(int y) 
        { 
        final int height = this.height;
        if (y >= 0 && y < height) return y;  // do clearest case first
        y = y % height;
        if (y < 0) y = y + height;
        return y;
        }
        
    public final int stxThin(final int x) 
        { if (x >= 0) { if (x < width) return x; return x - width; } return x + width; }
        
    public final int styThin(final int y) 
        { if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; }
        
    // faster internal version
    final int stxThin(final int x, final int width) 
        { if (x >= 0) { if (x < width) return x; return x - width; } return x + width; }
        
    // faster internal version
    final int styThin(final int y, final int height) 
        { if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; }

    public final int ulxThin(final int x, final int y) { return x - 1; }

    public final int ulyThin(final int x, final int y) { if ((x & 1) == 0) return y - 1; return y; }

    public final int urxThin(final int x, final int y) { return x + 1; }

    public final int uryThin(final int x, final int y) { if ((x & 1) == 0) return y - 1; return y; }
    
    public final int dlxThin(final int x, final int y) { return x - 1; }

    public final int dlyThin(final int x, final int y) { if ((x & 1) == 0) return y ; return y + 1; }
    
    public final int drxThin(final int x, final int y) { return x + 1; }

    public final int dryThin(final int x, final int y) { if ((x & 1) == 0) return y ; return y + 1; }

    public final int upxThin(final int x, final int y) { return x; }

    public final int upyThin(final int x, final int y) { return y - 1; }

    public final int downxThin(final int x, final int y) { return x; }

    public final int downyThin(final int x, final int y) { return y + 1; }

    public boolean trbThin(final int x, final int y) { return ((x + y) & 1) == 1; }
    
    public boolean trtThin(final int x, final int y) { return ((x + y) & 1) == 0; }
        
    public void getNeighborsMaxDistanceThin( final int x, final int y, final int dist, final boolean toroidal, IntBag xPos, IntBag yPos )
        {
        // won't work for negative distances
        if( dist < 0 )
            {
            throw new RuntimeException( "Runtime exception in method getNeighborsMaxDistance: Distance must be positive" );
            }

        if( xPos == null || yPos == null )
            {
            throw new RuntimeException( "Runtime exception in method getNeighborsMaxDistance: xPos and yPos should not be null" );
            }

        xPos.clear();
        yPos.clear();

        // local variables are faster
        final int height = this.height;
        final int width = this.width;

        // for toroidal environments the code will be different because of wrapping arround
        if( toroidal )
            {
            // compute xmin and xmax for the neighborhood
            final int xmin = x - dist;
            final int xmax = x + dist;
            // compute ymin and ymax for the neighborhood
            final int ymin = y - dist;
            final int ymax = y + dist;
                
            for( int x0 = xmin ; x0 <= xmax ; x0++ )
                {
                final int x_0 = stxThin(x0, width);
                for( int y0 = ymin ; y0 <= ymax ; y0++ )
                    {
                    final int y_0 = styThin(y0, height);
                    xPos.add( x_0 );
                    yPos.add( y_0 );
                    }
                }
            }
        else // not toroidal
            {
            // compute xmin and xmax for the neighborhood such that they are within boundaries
            final int xmin = ((x-dist>=0)?x-dist:0);
            final int xmax =((x+dist<=width-1)?x+dist:width-1);
            // compute ymin and ymax for the neighborhood such that they are within boundaries
            final int ymin = ((y-dist>=0)?y-dist:0);
            final int ymax = ((y+dist<=height-1)?y+dist:height-1);
            for( int x0 = xmin; x0 <= xmax ; x0++ )
                {
                for( int y0 = ymin ; y0 <= ymax ; y0++ )
                    {
                    xPos.add( x0 );
                    yPos.add( y0 );
                    }
                }
            }
        }


    public void getNeighborsHamiltonianDistanceThin( final int x, final int y, final int dist, final boolean toroidal, IntBag xPos, IntBag yPos )
        {
        // won't work for negative distances
        if( dist < 0 )
            {
            throw new RuntimeException( "Runtime exception in method getNeighborsHamiltonianDistance: Distance must be positive" );
            }

        if( xPos == null || yPos == null )
            {
            throw new RuntimeException( "Runtime exception in method getNeighborsHamiltonianDistance: xPos and yPos should not be null" );
            }

        xPos.clear();
        yPos.clear();
                
        // local variables are faster
        final int height = this.height;
        final int width = this.width;

        // for toroidal environments the code will be different because of wrapping arround
        if( toroidal )
            {
            // compute xmin and xmax for the neighborhood
            final int xmax = x+dist;
            final int xmin = x-dist;
            for( int x0 = xmin; x0 <= xmax ; x0++ )
                {
                final int x_0 = stxThin(x0, width);
                // compute ymin and ymax for the neighborhood; they depend on the curreny x0 value
                final int ymax = y+(dist-((x0-x>=0)?x0-x:x-x0));
                final int ymin = y-(dist-((x0-x>=0)?x0-x:x-x0));
                for( int y0 =  ymin; y0 <= ymax; y0++ )
                    {
                    final int y_0 = styThin(y0, height);
                    xPos.add( x_0 );
                    yPos.add( y_0 );
                    }
                }
            }
        else // not toroidal
            {
            // compute xmin and xmax for the neighborhood such that they are within boundaries
            final int xmax = ((x+dist<=width-1)?x+dist:width-1);
            final int xmin = ((x-dist>=0)?x-dist:0);
            for( int x0 = xmin ; x0 <= xmax ; x0++ )
                {
                // compute ymin and ymax for the neighborhood such that they are within boundaries
                // they depend on the curreny x0 value
                final int ymax = ((y+(dist-((x0-x>=0)?x0-x:x-x0))<=height-1)?y+(dist-((x0-x>=0)?x0-x:x-x0)):height-1);
                final int ymin = ((y-(dist-((x0-x>=0)?x0-x:x-x0))>=0)?y-(dist-((x0-x>=0)?x0-x:x-x0)):0);
                for( int y0 =  ymin; y0 <= ymax; y0++ )
                    {
                    xPos.add( x0 );
                    yPos.add( y0 );
                    }
                }
            }
        }






    double dxForRadiusThin(double dy, double radius)  // may return NaN
        {
        return Math.sqrt(radius*radius - dy*dy);
        }

    // still in real-valued space
    double dxForAngleThin(double dy, double xa, double ya)
        {
        if (ya == 0 && dy == 0)  // horizontal line, push dx way out to the far edges of space, even if we're not in line with it
            { return xa > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY ; }
        else if ((dy >= 0 && ya >= 0) || (dy <= 0 && ya <= 0))          // same side
            {
            // dx : dy :: xa : ya
            return (xa * dy) / ya;
            }
        else return Double.NaN;
        }

    // a is next to b (<) and c and d are not between them
    boolean nextToThin(double a, double b, double c, double d)
        {
        return (a <= b &&
            !(c >= a && c <= b) &&
            !(d >= a && d <= b));
        }
        
    int pushLeftThin(double x, double y, double slope, double radiusSq)
        {
        System.err.println("<---- " + x + " " + y + " " + slope);
        double xa = x;
        double ya = y;
        int xi = (int)xa;
        int yi = (int)ya;
                
        while( 
                ((slope >= 0 && xi * slope >= yi) || 
                (slope < 0 && xi * slope < (yi + 1))) &&
            xi * xi + (xi * slope) * (xi * slope) < radiusSq)
            {
            xi--;
            }
        return xi;
        }

    int pushRightThin(double x, double y, double slope, double radiusSq)  // radius limits our distance
        {
        System.err.println("----> " + x + " " + y + " " + slope);
        double xa = x;
        double ya = y;
        int xi = (int)xa;
        int yi = (int)ya;

        while(
                ((slope <= 0 && (xi + 1) * slope >= yi) ||
                (slope > 0 && (xi + 1) * slope < (yi + 1))) &&
            (xi + 1) * (xi + 1) * ((xi + 1) * slope) * ((xi + 1) * slope) < radiusSq)
            {
            xi++;
            }
        return xi;
        }

    boolean scanThin(int x, int y, int dy, double radius, double xa, double ya, double xb, double yb, 
        boolean crossesZero, boolean crossesPi, boolean toroidal, IntBag xPos, IntBag yPos)
        {
        // too high?
        if (dy > radius || dy < -radius) return false;

        double r = dxForRadiusThin(dy, radius);
        double l = - r;
        double s = dxForAngleThin(dy, xa, ya);
        double e = dxForAngleThin(dy, xb, yb);

        int min = 0;
        int max = 0;

        System.err.println("dy=" + dy + " radius=" + radius + " zero=" + crossesZero + " Pi=" + crossesPi);
        System.err.println("xa " + xa + " ya " + ya + " xb " + xb + " yb " + yb);
        System.err.println("r " + r + " l " + l + " s " + s + " e " + e);
                
        /*if (dy == 0) // special case dy == 0
          {
          if (crossesPi) min = (int) Math.round(l);
          if (crossesZero) max = (int) Math.round(r);
          System.err.println("MIN " + min + " MAX " + max);
          if (toroidal)
          for(int i = min; i <= max; i++) { xPos.add(stx(x + i)); yPos.add(sty(y + dy));  }
          else
          for(int i = min; i <= max; i++) { xPos.add(x + i); yPos.add(y + dy); }
          return true;
          }
          else */
                
        if ((s==Double.POSITIVE_INFINITY && e == Double.POSITIVE_INFINITY) || (s == Double.NEGATIVE_INFINITY && e==Double.NEGATIVE_INFINITY))  // straight line in the same direction, handle specially
            { return false; }
        else if (dy >= 0) 
            {
            if (l!=l)  // NaN, signifies out of bounds
                return false;
            // six cases:   (L = negativeDXForRadius, R = positiveDXForRadius, S = start (Xa, Ya), E = end(Xb, Yb)
            if (s == e)  // line
                {
                System.err.println("S==E");
                if (s * s + dy * dy > radius * radius) return false;
                min = pushLeftThin(s, dy, ya / xa, radius * radius);
                max = pushRightThin(s, dy, ya / xa, radius * radius);
                }
            // L S E R
            else if (l <= s && s <= e && e <= r) 
                {
                System.err.println("LSER");
                // draw first line
                min = (int)Math.floor(l); max = (int)Math.ceil(s);
                if (toroidal)
                    for(int i = min; i <= max; i++) { xPos.add(stxThin(x + i)); yPos.add(styThin(y + dy));  }
                else
                    for(int i = min; i <= max; i++) { xPos.add(x + i); yPos.add(y + dy); }
                // set up second line
                min = (int)Math.floor(e); max = (int)Math.ceil(r);
                }
            // L R
            else if ((e <= l && l <= r && r <= s) || 
                (l <= r && r <= s && s <= e))
                        
//                      nextTo(l, r, e, s) && e <= s)  // end must be less than start for this to be valid
                {
                System.err.println("LR");
                min = (int)Math.floor(l); max = (int)Math.ceil(r);
                }
            // E S
            else if //(nextTo(e, s, l, r))
                (
                (l <= e && e <= s && s <= r) 
                //||
                //(l <= e && e <= s && r <= e) ||
                //(l >= e && e <= s && r >= e)
                )
                {
                System.err.println("ES");
                min = (int)Math.floor(e); max = (int)Math.ceil(s);
                }
            // E R
            else if (nextToThin(e, r, l, s))
                {
                System.err.println("ER");
                min = (int)Math.floor(e); max = (int)Math.ceil(r);
                }
            // L S
            else if (nextToThin(l, s, e, r))
                {
                System.err.println("LS");
                min = (int)Math.floor(l); max = (int)Math.ceil(s);
                }
            else return false;
                        
            System.err.println("MIN " + min + " MAX " + max);
            // draw line
            if (toroidal)
                for(int i = min; i <= max; i++) { xPos.add(stxThin(x + i)); yPos.add(styThin(y + dy));  }
            else
                for(int i = min; i <= max; i++) { xPos.add(x + i); yPos.add(y + dy); }
                                
            return true;
            }
        else // (dy < 0) 
            {
            if (l!=l)  // out of bounds
                return false;
            //double s = dxForAngle(dy, xa, ya);
            //double e = dxForAngle(dy, xb, yb);
                
            // five cases:  (L = negativeDXForRadius, R = positiveDXForRadius, S = start (Xa, Ya), E = end(Xb, Yb)
            // L E S R
            if (l <= e && e <= s && s <= r) 
                {
                // draw first line
                min = (int)Math.round(l); max = (int)Math.round(e);
                if (toroidal)
                    for(int i = min; i <= max; i++) { xPos.add(stxThin(x + i)); yPos.add(styThin(y + dy));  }
                else
                    for(int i = min; i <= max; i++) { xPos.add(x + i); yPos.add(y + dy); }
                // set up second line
                min = (int)Math.round(s); max = (int)Math.round(r);
                }
            // L R
            else if (nextToThin(l, r, e, s) && s <= e)  // end must be greater than start for this to be valid
                {
                min = (int)Math.round(l); max = (int)Math.round(r);
                }
            // S E
            else if (nextToThin(s, e, l, r))
                {
                min = (int)Math.round(s); max = (int)Math.round(e);
                }
            // L E
            else if (nextToThin(l, e, r, s))
                {
                min = (int)Math.round(l); max = (int)Math.round(e);
                }
            // S R
            else if (nextToThin(s, r, l, e))
                {
                min = (int)Math.round(s); max = (int)Math.round(r);
                }
            else return false;
                        
            // draw line
            if (toroidal)
                for(int i = min; i <= max; i++) { xPos.add(stxThin(x + i)); yPos.add(styThin(y));  }
            else
                for(int i = min; i <= max; i++) { xPos.add(x + i); yPos.add(y); }
                                
            return true;
            }
        }

    public void getNeighborsWithinArcThin(int x, int y, double radius, double startAngle, double endAngle, IntBag xPos, IntBag yPos)
        { getNeighborsWithinArcThin(x,y,radius,startAngle,endAngle,false,xPos,yPos); }
                
    public void getNeighborsWithinArcThin(int x, int y, double radius, double startAngle, double endAngle, boolean toroidal, IntBag xPos, IntBag yPos)
        {
        if (radius < 0)
            throw new RuntimeException("Radius must be positive");
        xPos.clear();
        yPos.clear();
                
        // move angles into [0...2 PI)
        if (startAngle < 0) startAngle += Math.PI * 2; 
        if (startAngle < 0) startAngle = ((startAngle % Math.PI * 2) + Math.PI * 2);
        if (startAngle >= Math.PI * 2) startAngle = startAngle % Math.PI * 2;
                
        if (endAngle < 0) endAngle += Math.PI * 2; 
        if (endAngle < 0) endAngle = ((endAngle % Math.PI * 2) + Math.PI * 2);
        if (endAngle >= Math.PI * 2) endAngle = endAngle % Math.PI * 2;

        // compute slopes -- avoid atan2
        double xa = Math.cos(startAngle);
        double ya = Math.sin(startAngle);
        double xb = Math.cos(endAngle);
        double yb = Math.sin(endAngle);
                
        // compute crossings
        boolean crossesZero = false;
        boolean crossesPi = false;
        if (startAngle > endAngle || startAngle == 0 || endAngle == 0)  // crosses zero for sure
            crossesZero = true;
        else if (startAngle <= Math.PI && endAngle >= Math.PI)
            crossesPi = true;
                
        // scan up
        int dy = 0;
        while(scanThin(x, y, dy, radius, xa, ya, xb, yb, crossesZero, crossesPi, toroidal, xPos, yPos))
            dy++;
        // scan down (not including zero)
        dy = -1;
        while(scanThin(x, y, dy, radius, xa, ya, xb, yb, crossesZero, crossesPi, toroidal, xPos, yPos))
            dy--;
        }









    public void getNeighborsHexagonalDistanceThin( final int x, final int y, final int dist, final boolean toroidal, IntBag xPos, IntBag yPos )
        {
        // won't work for negative distances
        if( dist < 0 )
            {
            throw new RuntimeException( "Runtime exception in method getNeighborsHexagonalDistance: Distance must be positive" );
            }

        if( xPos == null || yPos == null )
            {
            throw new RuntimeException( "Runtime exception in method getNeighborsHamiltonianDistance: xPos and yPos should not be null" );
            }

        xPos.clear();
        yPos.clear();

        // local variables are faster
        final int height = this.height;
        final int width = this.width;
                
        if( toroidal && width%2==1 )
            throw new RuntimeException( "Runtime exception in getNeighborsHexagonalDistance: toroidal hexagonal environment should have an even width" );

        if( toroidal )
            {
            // compute ymin and ymax for the neighborhood
            int ymin = y - dist;
            int ymax = y + dist;
            for( int y0 = ymin ; y0 <= ymax ; y0 = downyThin(x,y0) )
                {
                xPos.add( stxThin(x, width) );
                yPos.add( styThin(y0, height) );
                }
            int x0 = x;
            for( int i = 1 ; i <= dist ; i++ )
                {
                final int temp_ymin = ymin;
                ymin = dlyThin( x0, ymin );
                ymax = ulyThin( x0, ymax );
                x0 = dlxThin( x0, temp_ymin );
                for( int y0 = ymin ; y0 <= ymax ; y0 = downyThin(x0,y0) )
                    {
                    xPos.add( stxThin(x0, width) );
                    yPos.add( styThin(y0, height) );
                    }
                }
            x0 = x;
            ymin = y-dist;
            ymax = y+dist;
            for( int i = 1 ; i <= dist ; i++ )
                {
                final int temp_ymin = ymin;
                ymin = dryThin( x0, ymin );
                ymax = uryThin( x0, ymax );
                x0 = drxThin( x0, temp_ymin );
                for( int y0 = ymin ; y0 <= ymax ; y0 = downyThin(x0,y0) )
                    {
                    xPos.add( stxThin(x0, width) );
                    yPos.add( styThin(y0, height) );
                    }
                }
            }
        else // not toroidal
            {
            if( x < 0 || x >= width || y < 0 || y >= height )
                throw new RuntimeException( "Runtime exception in method getNeighborsHexagonalDistance: invalid initial position" );

            // compute ymin and ymax for the neighborhood
            int ylBound = (y-dist<0)?0:y-dist;
            int yuBound = ((y+dist<height)?y+dist:height-1);

            for( int y0 = ylBound ; y0 <= yuBound ; y0 = downyThin(x,y0) )
                {
                xPos.add( x );
                yPos.add( y0 );
                }

            int x0 = x;
            int ymin = y-dist;
            int ymax = y+dist;
            for( int i = 1 ; i <= dist ; i++ )
                {
                final int temp_ymin = ymin;
                ymin = dlyThin( x0, ymin );
                ymax = ulyThin( x0, ymax );
                x0 = dlxThin( x0, temp_ymin );
                ylBound = (ymin>=0)?ymin:0;
                yuBound =  (ymax<height)?ymax:(height-1);
                if( x0 >= 0 )
                    for( int y0 = ylBound ; y0 <= yuBound ; y0 = downyThin(x0,y0) )
                        {
                        if( y0 >= 0 )
                            {
                            xPos.add( x0 );
                            yPos.add( y0 );
                            }
                        }
                }

            x0 = x;
            ymin = y-dist;
            ymax = y+dist;
            for( int i = 1 ; i <= dist ; i++ )
                {
                final int temp_ymin = ymin;
                ymin = dryThin( x0, ymin );
                ymax = uryThin( x0, ymax );
                x0 = drxThin( x0, temp_ymin );
                ylBound = (ymin>=0)?ymin:0;
                yuBound =  (ymax<height)?ymax:(height-1);
                if( x0 < width )
                    for( int y0 = ylBound ; y0 <= yuBound ; y0 = downyThin(x0,y0) )
                        {
                        if( y0 >= 0 )
                            {
                            xPos.add( x0 );
                            yPos.add( y0 );
                            }
                        }
                }
            }
        }


}
