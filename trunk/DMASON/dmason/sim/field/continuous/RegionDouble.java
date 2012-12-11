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

import sim.util.Double2D;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.*;
import dmason.util.Util;

/**
 * A Region specialized to be used in a field with Double coordinates
 */
public class RegionDouble extends Region<Double,Double2D>
{
	public static double width,height;
	public RegionDouble(Double upl_xx, Double upl_yy, Double down_xx,
			Double down_yy,double width,double height) 
	{		
		super(upl_xx, upl_yy, down_xx, down_yy);
		this.width=width;
		this.height=height;
		if(down_xx==0.0)
			super.down_xx=width;
		if(down_yy==0.0)
			super.down_yy=height;
	}

	public static Region<Double,Double2D> createRegion(Double upl_xx,Double upl_yy, Double down_xx,
			Double down_yy, Double MY_WIDTH, Double MY_HEIGHT, Double WIDTH, Double HEIGHT) 
			{
		int xx0=Double.compare(upl_xx, 0);
		int yy0=Double.compare(upl_yy, 0);
		int xxW=Double.compare(upl_xx, WIDTH);
		int yyH=Double.compare(upl_yy, HEIGHT);
		
		if(xx0 < 0 || yy0 < 0)
		{				
				return null;
		}
		
		if( xxW >= 0 || yyH >= 0)
		{
			return null;
		}
		
	  return new RegionDouble(upl_xx,upl_yy,down_xx,down_yy,width,height);
	}

	public Region<Double, Double2D> clone() 
	{
		RegionDouble r=new RegionDouble(upl_xx, upl_yy, down_xx, down_yy,width,height);
		for(Entry<Double2D> e: this)
		{
			r.add(new Entry(((RemoteAgent<Double2D>)(Util.clone(e.r))),e.l));
		}
		return r;
	}

	public boolean isMine(Double x, Double y) 
	{
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	public boolean addAgents(Entry<Double2D> e) 
	{
		return this.add(e);
	}	
}