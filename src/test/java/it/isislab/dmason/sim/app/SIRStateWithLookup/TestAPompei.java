package it.isislab.dmason.sim.app.SIRStateWithLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TestAPompei {
	public boolean isFucked=false;
	public boolean getisFucked(){return isFucked;}
	public void setisFucked(boolean fuck){}
	public static void main(String[] args) throws Throwable {
		
		
		// TODO Auto-generated method stub
		TestAPompei maron=new TestAPompei();
		MethodHandle m=MethodHandles.lookup().findSetter(TestAPompei.class, "isFucked", 
				boolean.class);
				//lookupGetter(maron.getClass(),"isFucked", Void.TYPE);
		m.invokeExact(maron,true);
		System.out.println("sa piajt");

	}
	
	
}
