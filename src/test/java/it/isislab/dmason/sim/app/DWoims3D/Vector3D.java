package it.isislab.dmason.sim.app.DWoims3D;

import java.io.Serializable;

import sim.util.Double3D;

public class Vector3D implements Serializable{
    private static final long serialVersionUID = 1;

    public double x;
    public double y;
    public double z;

    public Vector3D( double x, double y, double z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D( final Double3D d )
    {
        this.x = d.x;
        this.y = d.y;
        this.z = d.z;
    }

    public final Vector3D add( final Vector3D b )
    {
        return new Vector3D( x + b.x, y + b.y, z + b.z );
    }

    public final Vector3D add( final Double3D b )
    {
        return new Vector3D( x + b.x, y + b.y, z + b.z );
    }

    public final Vector3D subtract( final Vector3D b )
    {
        return new Vector3D( x - b.x, y - b.y, z - b.z );
    }

    public final Vector3D subtract( final Double3D b )
    {
        return new Vector3D( x - b.x, y - b.y, z - b.z );
    }

    public final Vector3D amplify( double alpha )
    {
        return new Vector3D( x * alpha, y * alpha, z * alpha );
    }

    public final Vector3D normalize()
    {
        if( x != 0 || y != 0 || z != 0)
        {
            double temp = Math.sqrt( x*x+y*y+z*z );
            return new Vector3D( x/temp, y/temp, z/temp );
        }
        else
            return new Vector3D( 0, 0, 0 );
    }

    public final double length()
    {
        return Math.sqrt( x*x+y*y+z*z );
    }

    public final Vector3D setLength( double dist )
    {
        if( dist == 0 )
            return new Vector3D( 0, 0, 0 );
        if( x == 0 && y == 0 && z == 0 )
            return new Vector3D( 0, 0, 0 );
        double temp = Math.sqrt( x*x+y*y+z*z );
        return new Vector3D( x * dist / temp, y * dist / temp, z * dist / temp );
    }

    public String toString(){
        return "Vector3D ["+x+"-"+y+"-"+z+"]";
    }
}