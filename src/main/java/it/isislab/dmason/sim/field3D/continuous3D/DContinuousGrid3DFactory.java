package it.isislab.dmason.sim.field3D.continuous3D;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field3D.DistributedField3D;
import sim.engine.SimState;


public class DContinuousGrid3DFactory {

    public static final DContinuousGrid3D createDContinuous3D(double discretization,double width, double height, double length,SimState sm,int max_distance,int i,int j,int z,int rows,int columns,int depths, int MODE, String name, String topicPrefix, boolean isToroidal)
            throws DMasonException
    {

        //general parameters check for value
        if(discretization<=Double.MIN_VALUE) {throw new DMasonException("Illegal value : discretization exceeds Double MIN value");}
        if(discretization>=Double.MAX_VALUE) {throw new DMasonException("Illegal value : discretization value exceeds Double MAX value");}
        if(width>=Double.MAX_VALUE) {throw new DMasonException("Illegal value : width value exceeds Double MAX value");}
        if(width<=Double.MIN_VALUE) {throw new DMasonException("Illegal value: Field exceeds Double MIN value");}
        if(height<=Double.MIN_VALUE) {throw new DMasonException("Illegal value: Field exceeds Double MIN value");}
        if(height>=Double.MAX_VALUE) {throw new DMasonException("Illegal value : height value exceeds Double MAX value");}
        if(length<=Double.MIN_VALUE) {throw new DMasonException("Illegal value: Field exceeds Double MIN value");}
        if(length>=Double.MAX_VALUE) {throw new DMasonException("Illegal value : length value exceeds Double MAX value");}
        if(sm==null){throw new DMasonException("Illegal value : SimState is null");}
        if(max_distance<=0){throw new DMasonException("Illegal value, max_distance value must be greater than 0");}
        if(max_distance>=Integer.MAX_VALUE ){throw new DMasonException("Illegal value : max_distance value exceded Integer max value");}
        if(max_distance>=width ){throw new DMasonException(String.format("Illegal value : max_distance (%d) value exceded width(%f) value",max_distance,width));}
        if(i<0){throw new DMasonException("Illegal value : celltype_i value should not be negative");}
        if(i>=Integer.MAX_VALUE){throw new DMasonException("Illegal value : celltype_i exceeds Integer MAX value");}
        if(j<0){throw new DMasonException("Illegal value : celltype_j value should not be negative");}
        if(j>=Integer.MAX_VALUE){throw new DMasonException("Illegal value : celltype_j exceeds Integer MAX value");}
        if(z<0){throw new DMasonException("Illegal value : celltype_z value should not be negative");}
        if(z>=Integer.MAX_VALUE){throw new DMasonException("Illegal value : celltype_z exceeds Integer MAX value");}
        if(rows <=0){throw new DMasonException("Illegal value : rows value must be greater than 0");}
        if(rows >= Integer.MAX_VALUE){throw new DMasonException("Illegal value : rows exceeds Integer MAX value");}
        if(columns<=0){throw new DMasonException("Illegal value : columns value must be greater than 0");}
        if(columns >= Integer.MAX_VALUE){throw new DMasonException("Illegal value : columns exceeds Integer MAX value");}
        if(depths<=0){throw new DMasonException("Illegal value : depths value must be greater than 0");}
        if(depths >= Integer.MAX_VALUE){throw new DMasonException("Illegal value : depths exceeds Integer MAX value");}
        if(rows==1 && columns==1 &&depths==1){throw new DMasonException("Illegal value : field partitioning with one row and one column is not defined");}
        if(name == null){throw new DMasonException("Illegal value : name should not be null");}
        if(topicPrefix == null){throw new DMasonException("Illegal value : topicPrefix should not be null");}
        if(MODE==DistributedField3D.UNIFORM_PARTITIONING_MODE)
        {
            DistributedField3D field = new DContinuousGrid3DXYZ(discretization,width, height,length,sm, max_distance, i, j,z, rows,columns,depths,name,topicPrefix,isToroidal);
            ((DistributedMultiSchedule)((DistributedState)sm).schedule).add3DField(field);
            return (DContinuousGrid3D)field;
        } else throw new DMasonException("Illegal Distribution Mode");

    }
}
