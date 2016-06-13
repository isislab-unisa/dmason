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

package it.isislab.dmason.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import sim.util.Interval;

/**
 * This class extends the features of MASON's Properties class enabling the support
 * for reduce.able parameters.
 * @author Luca Vicidomini
 *
 */

public class DistributedProperties extends sim.util.Properties implements java.io.Serializable
    {
    private static final long serialVersionUID = 1;

    ArrayList<Method> reduceMethods = new ArrayList<Method>();
    ArrayList<Method> globalMethods = new ArrayList<Method>();
    ArrayList<Method> mapMethods = new ArrayList<Method>();
    
    ArrayList<Method> getMethods = new ArrayList<Method>();
    ArrayList<Method> setMethods = new ArrayList<Method>(); // if no setters, that corresponding spot will be null
    ArrayList<Method> domMethods = new ArrayList<Method>(); // if no domain, that corresponding spot will be null
    ArrayList<Method> hideMethods = new ArrayList<Method>(); // if not hidden (or explicitly shown), that corresponding spot will be null
    sim.util.Properties auxillary = null;  // if non-null, we use this properties instead
    
    /** Gathers all properties for the object, including ones defined in superclasses. 
        SimpleProperties will search the object for methods of the form <tt>public Object dom<i>Property</i>()</tt>
        which define the domain of the property.  The domFoo() and hideFoo() property extension methods are respected.
    */
    public DistributedProperties(Object o) { this(o,true,false,true); }
       
    /** Gathers all properties for the object, possibly including ones defined in superclasses. 
        If includeGetClass is true, then the Class property will be included. If includeDomains is true, then
        SimpleProperties will search the object for methods of the form <tt>public Object dom<i>Property</i>()</tt>
        which define the domain of the property.  The domFoo() and hideFoo() property extension methods are respected
        if <tt>includeExtensions</tt> is true.
    */
    public DistributedProperties(Object o, boolean includeSuperclasses, boolean includeGetClass, boolean includeExtensions)
        {
        object = o;
        if (o!=null && o instanceof sim.util.Proxiable)
            object = ((sim.util.Proxiable)(o)).propertiesProxy();
        else if (o!=null && o instanceof sim.util.Propertied)
            auxillary = ((sim.util.Propertied)(o)).properties();
        generateProperties(includeSuperclasses,includeGetClass,includeExtensions);
        }
    
    void generateProperties(boolean includeSuperclasses, boolean includeGetClass, boolean includeExtensions)
        {
        if (object != null && auxillary == null) 
            try
                {
                // generate the properties
                Class<?> c = object.getClass();

                // handle reduce/map/globals properties
                Method[] m = (includeSuperclasses ? c.getMethods() : c.getDeclaredMethods());
                for(int x = 0 ; x < m.length; x++)
                    {
                    if (!("get".equals(m[x].getName())) && !("is".equals(m[x].getName())) &&  // "get()" and "is()" aren't properties
                        (m[x].getName().startsWith("get") || m[x].getName().startsWith("is"))) // corrrect syntax?
                        {
                    	Method reducer = getReduceProperty(m[x], c);
                    	if (reducer == null)
                    		continue;
                    	
                    	reduceMethods.add(reducer);
                    	
                        int modifier = m[x].getModifiers();
                        if ((includeGetClass || !m[x].getName().equals("getClass")) &&
                            m[x].getParameterTypes().length == 0 &&
                            Modifier.isPublic(modifier)) // no arguments, and public, non-abstract?
                            {
                            //// Add all properties...
                            Class<?> returnType = m[x].getReturnType();
                            if (returnType!= Void.TYPE)
                                {
                                getMethods.add(m[x]);
                                setMethods.add(getWriteProperty(m[x],c));
                                domMethods.add(getDomain(m[x],c,includeExtensions));
                                hideMethods.add(getHidden(m[x], c, includeExtensions));
                                globalMethods.add(getGlobal(m[x], c, includeExtensions));
                                                                                                                                         
                                // simple check for invalid Interval domains
                                int lastIndex = domMethods.size() - 1;
                                Object domain = getDomain(lastIndex);
                                if (returnType == Float.TYPE || returnType == Double.TYPE)
                                    {
                                    if (domain != null && domain instanceof Interval)
                                        {
                                        Interval interval = (Interval) domain;
                                        if (!interval.isDouble())
                                            {
                                            System.err.println("WARNING: Property is double or float valued, but the Interval provided for the property's domain is byte/short/integer/long valued: " + 
                                                getName(lastIndex) + " on Object " + object);
                                            // get rid of the domain
                                            domMethods.set(lastIndex, null);
                                            }
                                        }
                                    }
                                else if (returnType == Byte.TYPE || returnType == Short.TYPE || returnType == Integer.TYPE || returnType == Long.TYPE)
                                    {
                                    if (domain != null && domain instanceof Interval)
                                        {
                                        Interval interval = (Interval) domain;
                                        if (interval.isDouble())
                                            {
                                            System.err.println("WARNING: Property is byte/short/integer/long valued, but the Interval provided for the property's domain is double or float valued: " + 
                                                getName(lastIndex) + " on Object " + object);
                                            // get rid of the domain
                                            domMethods.set(lastIndex, null);
                                            }
                                        }
                                    }
                                else if (domain != null && domain instanceof Interval)
                                    {
                                    System.err.println("WARNING: Property is not a basic number type, but an Interval was provided for the property's domain: " + 
                                        getName(lastIndex) + " on Object " + object);
                                    // get rid of the domain
                                    domMethods.set(lastIndex, null);
                                    }
                                }
                            }
                        }
                    }
                }
            catch (Exception e)
                {
                e.printStackTrace();
                }
        }
    
    /* If it exists, returns a method of the form 'public boolean hideFoo() { ...}'.  In this method the developer can declare
       whether or not he wants to hide this property.  If there is no such method, we must assume that the property is to be
       shown. */
    Method getHidden(Method m, Class<?> c, boolean includeExtensions)
        {
        if (!includeExtensions) return null;
        try
            {
            if (m.getName().startsWith("get"))
                {
                Method m2 = c.getMethod("hide" + (m.getName().substring(3)), new Class[] { });
                if (m2.getReturnType() == Boolean.TYPE) return m2;
                }
            else if (m.getName().startsWith("is"))
                {
                Method m2 = c.getMethod("hide" + (m.getName().substring(2)), new Class[] { });
                if (m2.getReturnType() == Boolean.TYPE) return m2;
                }
            }
        catch (Exception e)
            {
            // couldn't find a domain
            }
        return null;
        }
    
    Method getWriteProperty(Method m, Class<?> c)
        {
        try
            {
            if (m.getName().startsWith("get"))
                {
                return c.getMethod("map" + (m.getName().substring(3)), new Class[] { m.getReturnType() });
                }
            else if (m.getName().startsWith("is"))
                {
                return c.getMethod("map" + (m.getName().substring(2)), new Class[] { m.getReturnType() });
                }
            else return null;
            }
        catch (Exception e)
            {
            // couldn't find a setter
            return null;
            }
        }
    
    Method getReduceProperty(Method m, Class<?> c)
    {
    try
        {
        if (m.getName().startsWith("get"))
            {
            return c.getMethod("reduce" + (m.getName().substring(3)), new Class[] { Object[].class });
            }
        else if (m.getName().startsWith("is"))
            {
            return c.getMethod("reduce" + (m.getName().substring(2)), new Class[] { Object[].class });
            }
        else return null;
        }
    catch (Exception e)
        {
        // couldn't find a reducer
        return null;
        }
    }
    
     /* If it exists, returns a method of the form 'public boolean globalFoo() { ...}'.  In this method the developer can declare
     whether or not this property is global.  If there is no such method, we must assume that the property is not global. */
     Method getGlobal(Method m, Class<?> c, boolean includeExtensions)
     {
     if (!includeExtensions) return null;
     try
         {
         if (m.getName().startsWith("get"))
             {
             Method m2 = c.getMethod("global" + (m.getName().substring(3)), new Class[] { });
             if (m2.getReturnType() == Boolean.TYPE) return m2;
             }
         else if (m.getName().startsWith("is"))
             {
             Method m2 = c.getMethod("global" + (m.getName().substring(2)), new Class[] { });
             if (m2.getReturnType() == Boolean.TYPE) return m2;
             }
         }
     catch (Exception e)
         {
         // couldn't find a domain
         }
     return null;
     }
    
    Method getDomain(Method m, Class<?> c, boolean includeExtensions)
        {
        if (!includeExtensions) return null;
        try
            {
            if (m.getName().startsWith("get"))
                {
                return c.getMethod("dom" + (m.getName().substring(3)), new Class[] {});
                }
            else if (m.getName().startsWith("is"))
                {
                return c.getMethod("dom" + (m.getName().substring(2)), new Class[] { });
                }
            else return null;
            }
        catch (Exception e)
            {
            // couldn't find a domain
            return null;
            }
        }
    
    @Override
	public boolean isVolatile() { if (auxillary!=null) return auxillary.isVolatile(); return false; }

    /** Returns the number of properties discovered */
    @Override
	public int numProperties()
        {
        if (auxillary!=null) return auxillary.numProperties();
        return getMethods.size();
        }

    /** Returns the name of the given property.
        Returns null if the index is out of the range [0 ... numProperties() - 1 ]*/
    @Override
	public String getName(int index)
        {
        if (auxillary!=null) return auxillary.getName(index);
        if (index < 0 || index > numProperties()) return null;
        String name = (getMethods.get(index)).getName();
        if (name.startsWith("is"))
            return name.substring(2);
        else if (name.equals("longValue"))   // Integers of various kinds
            return "Value";
        else if (name.equals("doubleValue"))   // Other Numbers
            return "Value";
        else if (name.equals("booleanValue"))   // Booleans
            return "Value";
        else if (name.equals("toString"))   // Strings, StringBuffers
            return "Value";
        else return name.substring(3);  // "get", "set"
        }
        
    /** Returns whether or not the property can be written as well as read
        Returns false if the index is out of the range [0 ... numProperties() - 1 ]*/
    @Override
	public boolean isReadWrite(int index)
        {
        if (auxillary!=null) return auxillary.isReadWrite(index);
        if (index < 0 || index > numProperties()) return false;
        if (isComposite(index)) return false;
        return (setMethods.get(index)!=null);
        }

    /** Returns the return type of the property (see the TYPE_... values)
        Returns -1 if the index is out of the range [0 ... numProperties() - 1 ]*/
    @Override
	public Class<?> getType(int index)
        {
        if (auxillary!=null) return auxillary.getType(index);
        if (index < 0 || index > numProperties()) return null;
        Class<?> returnType = (getMethods.get(index)).getReturnType();

        return getTypeConversion(returnType);
        }

    /** Returns the current value of the property.  Simple values (byte, int, etc.)
        are boxed (into Byte, Integer, etc.).
        Returns null if an error occurs or if the index is out of the range [0 ... numProperties() - 1 ]*/
    @Override
	public Object getValue(int index)
        {
        if (auxillary!=null) return auxillary.getValue(index);
        if (index < 0 || index > numProperties()) return null;
        try
            {
            return (getMethods.get(index)).invoke(object, new Object[0]);
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return null;
            }
        }
    
    @Override
	protected Object _setValue(int index, Object value)
        {
        if (auxillary!=null) return auxillary.setValue(index,value);  // I think this is right
        try
            {
            if (setMethods.get(index) == null) return null;
            (setMethods.get(index)).invoke(object, new Object[] { value });
            return getValue(index);
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return null;
            }
        }

    @Override
	public Object getDomain(int index)
        {
        if (auxillary!=null) return auxillary.getDomain(index);
        if (index < 0 || index > numProperties()) return null;
        try
            {
            if (domMethods.get(index) == null) return null;
            return (domMethods.get(index)).invoke(object, new Object[0]);
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return null;
            }
        }

    @Override
	public boolean isHidden(int index)
        {
        if (auxillary!=null) return auxillary.isHidden(index);
        if (index < 0 || index > numProperties()) return false;
        try
            {
            if (hideMethods.get(index) == null) return false;
            return ((Boolean)(hideMethods.get(index)).invoke(object, new Object[0])).booleanValue();
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return false;
            }
        }
    
    public boolean isGlobal(int index)
    	{
    //if (auxillary!=null) return auxillary.isHidden(index);
    if (index < 0 || index > numProperties()) return false;
    try
        {
        if (globalMethods.get(index) == null) return false;
        return ((Boolean)(globalMethods.get(index)).invoke(object, new Object[0])).booleanValue();
        }
    catch (Exception e)
        {
        e.printStackTrace();
        return false;
        }
    }
    }
