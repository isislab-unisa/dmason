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

package dmason.sim.field.grid;

import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.MutableInt2D;

public abstract class DSparseGrid2DThin extends DSparseGrid2D {

	private int width,height;
	/**
	 * 
	 * @param localWidth Effective width of the field
	 * @param localHeight Effective height of the field
	 * @param width Global width
	 * @param height Global height
	 */
	public DSparseGrid2DThin(int localWidth, int localHeight, int width, int height) {

		super(localWidth, localHeight);
		this.width=width;
		this.height=height;
	}
	
	 /** Returns the width of the grid */
    public int getWidthThin() { return width; }
    
    /** Returns the height of the grid */
    public int getHeightThin() { return height; }
    
    /*
      public final int tx(final int x) 
      { 
      final int width = this.width; 
      if (x >= 0) return (x % width); 
      final int width2 = (x % width) + height;
      if (width2 < width) return width2;
      return 0;
      }
    */

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

    public int stxThin(final int x) 
        { if (x >= 0) { if (x < width) return x; return x - width; } return x + width; }

    public int styThin(final int y) 
        { if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; }

    // faster version
    final int stxThin(final int x, final int width) 
        { if (x >= 0) { if (x < width) return x; return x - width; } return x + width; }
        
    // faster version
    final int styThin(final int y, final int height) 
        { if (y >= 0) { if (y < height) return y ; return y - height; } return y + height; }

    public int ulxThin(final int x, final int y) { return x - 1; }

    public int ulyThin(final int x, final int y) { if ((x & 1) == 0) return y - 1; return y; }

    public int urxThin(final int x, final int y) { return x + 1; }

    public int uryThin(final int x, final int y) { if ((x & 1) == 0) return y - 1; return y; }
        
    public int dlxThin(final int x, final int y) { return x - 1; }

    public int dlyThin(final int x, final int y) { if ((x & 1) == 0) return y ; return y + 1; }
    
    public int drxThin(final int x, final int y) { return x + 1; }

    public int dryThin(final int x, final int y) { if ((x & 1) == 0) return y ; return y + 1; }

    public int upxThin(final int x, final int y) { return x; }

    public int upyThin(final int x, final int y) { return y - 1; }

    public int downxThin(final int x, final int y) { return x; }

    public int downyThin(final int x, final int y) { return y + 1; }
    
    public boolean trbThin(final int x, final int y) { return ((x + y) & 1) == 1; }
    
    public boolean trtThin(final int x, final int y) { return ((x + y) & 1) == 0; }


    MutableInt2D speedyMutableInt2D = new MutableInt2D();
    /** Returns the number of objects stored in the grid at the given location. */
    public abstract int numObjectsAtLocationThin(final int x, final int y);

    /** Returns a bag containing all the objects at a given location, or null when there are no objects at the location.
        You should NOT MODIFY THIS BAG. This is the actual container bag, and modifying it will almost certainly break
        the Sparse Field object.   If you want to modify the bag, make a copy and modify the copy instead,
        using something along the lines of <b> new Bag(<i>foo</i>.getObjectsAtLocation(<i>location</i>)) </b>.
        Furthermore, changing values in the Sparse Field may result in a different bag being used -- so you should
        not rely on this bag staying valid.
    */
    public abstract Bag getObjectsAtLocationThin(final int x, final int y);

    /** Returns the object location as a Double2D, or as null if there is no such object. */
    public abstract Double2D getObjectLocationAsDouble2DThin(Object obj);
    /** Returns the object location, or null if there is no such object. */
    public abstract Int2D getObjectLocationThin(Object obj);
    /** Removes all the objects stored at the given location and returns them as a Bag (which you are free to modify). */
    public abstract Bag removeObjectsAtLocationThin(final int x, final int y);
    /** Changes the location of an object, or adds if it doesn't exist yet.  Returns false
        if the object is null (null objects cannot be put into the grid). */
    public abstract boolean setObjectLocationThin(final Object obj, final int x, final int y);
    
    /** Changes the location of an object, or adds if it doesn't exist yet.  Returns false
        if the object is null (null objects cannot be put into the grid) or if the location is null. */
    public abstract boolean setObjectLocationThin(Object obj, final Int2D location);
    
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

        if( toroidal && height%2==1 )
            throw new RuntimeException( "Runtime exception in getNeighborsHexagonalDistance: toroidal hexagonal environment should have even heights" );

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
            int ylBound = y - dist;
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
                yuBound =  ((ymax<height)?ymax:height-1);

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
                yuBound =  ((ymax<height)?ymax:height);
                if( x0 < width )
                    for( int y0 = ymin ; y0 <= yuBound; y0 = downyThin(x0,y0) )
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


    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist.  This region forms a
     * square 2*dist+1 cells across, centered at (X,Y).  If dist==1, this
     * is equivalent to the so-called "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * <b>Note that the order and size of the result Bag may not correspond to the X and Y bags.</b>  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsMaxDistance(...)
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public Bag getNeighborsMaxDistanceThin( final int x, final int y, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsMaxDistanceThin( x, y, dist, toroidal, xPos, yPos );
        return getObjectsAtLocationsThin(xPos,yPos,result);
        }

    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist.  This region forms a
     * square 2*dist+1 cells across, centered at (X,Y).  If dist==1, this
     * is equivalent to the so-called "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y) itself.
     * For each Object which falls within this distance, adds the X position, Y position, and Object into the
     * xPos, yPos, and result Bag, clearing them first.  This means that some <X,Y> positions may not appear
     * and that others may appear multiply if multiple objects share that positions.  Compare this function
     * with getNeighborsMaxDistance(...).
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public Bag getNeighborsAndCorrespondingPositionsMaxDistanceThin(final int x, final int y, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsMaxDistanceThin( x, y, dist, toroidal, xPos, yPos );
        reduceObjectsAtLocationsThin( xPos,  yPos,  result);
        return result;
        }



    /**
     * Gets all neighbors of a location that satisfy abs(x-X) + abs(y-Y) <= dist.  This region forms a diamond
     * 2*dist+1 cells from point to opposite point inclusive, centered at (X,Y).  If dist==1 this is
     * equivalent to the so-called "Von-Neumann Neighborhood" (the four neighbors above, below, left, and right of (X,Y)),
     * plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * Note that the order and size of the result Bag may not correspond to the X and Y bags.  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsHamiltonianDistance(...)
     * Returns the result Bag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public Bag getNeighborsHamiltonianDistanceThin( final int x, final int y, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsHamiltonianDistanceThin( x, y, dist, toroidal, xPos, yPos );
        return getObjectsAtLocationsThin(xPos,yPos,result);
        }



    /**
     * Gets all neighbors of a location that satisfy abs(x-X) + abs(y-Y) <= dist.  This region forms a diamond
     * 2*dist+1 cells from point to opposite point inclusive, centered at (X,Y).  If dist==1 this is
     * equivalent to the so-called "Von-Neumann Neighborhood" (the four neighbors above, below, left, and right of (X,Y)),
     * plus (X,Y) itself.
     * For each Object which falls within this distance, adds the X position, Y position, and Object into the
     * xPos, yPos, and result Bag, clearing them first.  This means that some <X,Y> positions may not appear
     * and that others may appear multiply if multiple objects share that positions.  Compare this function
     * with getNeighborsHamiltonianDistance(...).
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public Bag getNeighborsAndCorrespondingPositionsHamiltonianDistanceThin(final int x, final int y, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsHamiltonianDistanceThin( x, y, dist, toroidal, xPos, yPos );
        reduceObjectsAtLocationsThin( xPos,  yPos,  result);
        return result;
        }




    /**
     * Gets all neighbors located within the hexagon centered at (X,Y) and 2*dist+1 cells from point to opposite point 
     * inclusive.
     * If dist==1, this is equivalent to the six neighbors immediately surrounding (X,Y), 
     * plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * Note that the order and size of the result Bag may not correspond to the X and Y bags.  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsHexagonalDistance(...)
     * Returns the result Bag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public Bag getNeighborsHexagonalDistanceThin( final int x, final int y, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsHexagonalDistanceThin( x, y, dist, toroidal, xPos, yPos );
        return getObjectsAtLocationsThin(xPos,yPos,result);
        }
                
                
    /**
     * Gets all neighbors located within the hexagon centered at (X,Y) and 2*dist+1 cells from point to opposite point 
     * inclusive.
     * If dist==1, this is equivalent to the six neighbors immediately surrounding (X,Y), 
     * plus (X,Y) itself.
     * For each Object which falls within this distance, adds the X position, Y position, and Object into the
     * xPos, yPos, and result Bag, clearing them first.  This means that some <X,Y> positions may not appear
     * and that others may appear multiply if multiple objects share that positions.  Compare this function
     * with getNeighborsHexagonalDistance(...).
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     */
    public Bag getNeighborsAndCorrespondingPositionsHexagonalDistanceThin(final int x, final int y, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();

        getNeighborsHexagonalDistanceThin( x, y, dist, toroidal, xPos, yPos );
        reduceObjectsAtLocationsThin( xPos,  yPos,  result);
        return result;
        }


    // reduces xPos and yPos to just those positions used for result.  
    void reduceObjectsAtLocationsThin(final IntBag xPos, final IntBag yPos, Bag result)
        {
        if (result==null) result = new Bag();
        else result.clear();

        // build new bags with <x,y> locations one per each result
        IntBag newXPos = new IntBag();
        IntBag newYPos = new IntBag();

        final int len = xPos.numObjs;
        final int[] xs = xPos.objs;
        final int[] ys = yPos.objs;

        // for each location...
        for(int i=0; i < len; i++)
            {
            Bag temp = getObjectsAtLocationThin(xs[i],ys[i]);
            final int size = temp.numObjs;
            final Object[] os = temp.objs;
            // for each object at that location...
            for(int j = 0; j < size; j++)
                {
                // add the result, the x, and the y
                result.add(os[j]);
                newXPos.add(xs[i]);
                newYPos.add(ys[i]);
                }
            }
                
        // dump the new IntBags into the old ones
        xPos.clear();
        xPos.addAll(newXPos);
        yPos.clear();
        yPos.addAll(newYPos);
        }
                

    /** For each <xPos,yPos> location, puts all such objects into the result bag.  Returns the result bag.
        If the provided result bag is null, one will be created and returned. */
    public Bag getObjectsAtLocationsThin(final IntBag xPos, final IntBag yPos, Bag result)
        {
        if (result==null) result = new Bag();
        else result.clear();

        final int len = xPos.numObjs;
        final int[] xs = xPos.objs;
        final int[] ys = yPos.objs;
        for(int i=0; i < len; i++)
            {
            // a little efficiency: add if we're 1, addAll if we're > 1, 
            // do nothing if we're 0
            Bag temp = getObjectsAtLocationThin(xs[i],ys[i]);
            if (temp!=null)
                {
                int n = temp.numObjs;
                if (n==1) result.add(temp.objs[0]);
                else if (n > 1) result.addAll(temp);
                }
            }
        return result;
        }

    public final Double2D getDimensionsThin() { return new Double2D(width, height); }
	
}
