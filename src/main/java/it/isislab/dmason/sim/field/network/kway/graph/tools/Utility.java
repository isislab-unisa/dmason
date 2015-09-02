package it.isislab.dmason.sim.field.network.kway.graph.tools;

import it.isislab.dmason.annotation.AuthorAnnotation;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)
public class Utility {

	public static String findSeparator(String info){
		String separator = "";
		
		for(int i=0; i<info.length(); i++){
			char c = info.charAt(i);
			
			if(!Character.isDigit(c)){
				separator = c + "";
				break;
			}	
		}
		
		return separator;
	}
	
}
