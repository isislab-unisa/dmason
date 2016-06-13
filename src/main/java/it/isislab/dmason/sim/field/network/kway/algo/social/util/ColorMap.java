/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.field.network.kway.algo.social.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

public class ColorMap
{
	private static HashMap<Integer,Color> colors=new HashMap<Integer,Color>();
	private static HashMap<Double,Color> colors_dispersion=new HashMap<Double,Color>();
	private static HashMap<Integer,Color> colors_dispersion_community=new HashMap<Integer,Color>();
	private static Random r=new Random();
	public static Color pickColor(int i)
	{
		if(colors.get(i)==null)
		{
			colors.put(i,new Color(r.nextInt()));
		}
		return colors.get(i);
	}
	
	public static Color pickColor(double i,double max)
	{
		int val=(int) ((255*i)/max);
		if(colors_dispersion.get(i)==null)
		{
			colors_dispersion.put(i,new Color(val,Color.red.getGreen(),Color.red.getBlue()));
		}
		return colors_dispersion.get(i);
	}
	public static Color pickColor(int community,double dispersion,double max)
	{
		int val=(int) ((255*dispersion)/max);
		
		if(colors.get(community)==null)
		{
			colors.put(community,new Color(r.nextInt()));
			
		}
		
		return new Color(val,colors.get(community).getGreen(),colors.get(community).getBlue());
	}
}