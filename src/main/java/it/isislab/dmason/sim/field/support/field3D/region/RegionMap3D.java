package it.isislab.dmason.sim.field.support.field3D.region;

import java.io.Serializable;


public class RegionMap3D<E,F> implements Serializable {

    /**
     *
     */
    //	private static final long serialVersionUID = 1L;

    //region mine
    public Region3D<E,F>  WEST_MINE, NORTH_WEST_MINE, SOUTH_WEST_MINE, WEST_FRONT_MINE, WEST_REAR_MINE,
            NORTH_WEST_FRONT_MINE,NORTH_WEST_REAR_MINE, SOUTH_WEST_FRONT_MINE, SOUTH_WEST_REAR_MINE = null;

    public Region3D<E,F> EAST_MINE, NORTH_EAST_MINE, SOUTH_EAST_MINE, EAST_FRONT_MINE, EAST_REAR_MINE,
            NORTH_EAST_FRONT_MINE, NORTH_EAST_REAR_MINE, SOUTH_EAST_FRONT_MINE,SOUTH_EAST_REAR_MINE = null;

    public Region3D<E,F> NORTH_MINE, NORTH_FRONT_MINE,NORTH_REAR_MINE = null;

    public Region3D<E,F> SOUTH_MINE, SOUTH_FRONT_MINE, SOUTH_REAR_MINE = null;

    public Region3D<E,F> FRONT_MINE,REAR_MINE = null;


    //region out
    public Region3D<E,F>  WEST_OUT, NORTH_WEST_OUT, SOUTH_WEST_OUT, WEST_FRONT_OUT, WEST_REAR_OUT,
            NORTH_WEST_FRONT_OUT,NORTH_WEST_REAR_OUT, SOUTH_WEST_FRONT_OUT, SOUTH_WEST_REAR_OUT = null;

    public Region3D<E,F> 	EAST_OUT, NORTH_EAST_OUT, SOUTH_EAST_OUT, EAST_FRONT_OUT, EAST_REAR_OUT,
            NORTH_EAST_FRONT_OUT, NORTH_EAST_REAR_OUT, SOUTH_EAST_FRONT_OUT,SOUTH_EAST_REAR_OUT = null;

    public Region3D<E,F> NORTH_OUT, NORTH_FRONT_OUT,NORTH_REAR_OUT = null;

    public Region3D<E,F> SOUTH_OUT, SOUTH_FRONT_OUT, SOUTH_REAR_OUT = null;

    public Region3D<E,F> FRONT_OUT,REAR_OUT = null;



    public RegionMap3D() { super(); }





    public RegionMap3D(Region3D<E, F> wEST_MINE, Region3D<E, F> nORTH_WEST_MINE, Region3D<E, F> sOUTH_WEST_MINE,
                       Region3D<E, F> wEST_FRONT_MINE, Region3D<E, F> wEST_REAR_MINE, Region3D<E, F> nORTH_WEST_FRONT_MINE,
                       Region3D<E, F> nORTH_WEST_REAR_MINE, Region3D<E, F> sOUTH_WEST_FRONT_MINE,
                       Region3D<E, F> sOUTH_WEST_REAR_MINE, Region3D<E, F> eAST_MINE, Region3D<E, F> nORTH_EAST_MINE,
                       Region3D<E, F> sOUTH_EAST_MINE, Region3D<E, F> eAST_FRONT_MINE, Region3D<E, F> eAST_REAR_MINE,
                       Region3D<E, F> nORTH_EAST_FRONT_MINE, Region3D<E, F> nORTH_EAST_REAR_MINE,
                       Region3D<E, F> sOUTH_EAST_FRONT_MINE, Region3D<E, F> sOUTH_EAST_REAR_MINE, Region3D<E, F> nORTH_MINE,
                       Region3D<E, F> nORTH_FRONT_MINE, Region3D<E, F> nORTH_REAR_MINE, Region3D<E, F> sOUTH_MINE,
                       Region3D<E, F> sOUTH_FRONT_MINE, Region3D<E, F> sOUTH_REAR_MINE, Region3D<E, F> fRONT_MINE,
                       Region3D<E, F> rEAR_MINE, Region3D<E, F> wEST_OUT, Region3D<E, F> nORTH_WEST_OUT,
                       Region3D<E, F> sOUTH_WEST_OUT, Region3D<E, F> wEST_FRONT_OUT, Region3D<E, F> wEST_REAR_OUT,
                       Region3D<E, F> nORTH_WEST_FRONT_OUT, Region3D<E, F> nORTH_WEST_REAR_OUT,
                       Region3D<E, F> sOUTH_WEST_FRONT_OUT, Region3D<E, F> sOUTH_WEST_REAR_OUT, Region3D<E, F> eAST_OUT,
                       Region3D<E, F> nORTH_EAST_OUT, Region3D<E, F> sOUTH_EAST_OUT, Region3D<E, F> eAST_FRONT_OUT,
                       Region3D<E, F> eAST_REAR_OUT, Region3D<E, F> nORTH_EAST_FRONT_OUT, Region3D<E, F> nORTH_EAST_REAR_OUT,
                       Region3D<E, F> sOUTH_EAST_FRONT_OUT, Region3D<E, F> sOUTH_EAST_REAR_OUT, Region3D<E, F> nORTH_OUT,
                       Region3D<E, F> nORTH_FRONT_OUT, Region3D<E, F> nORTH_REAR_OUT, Region3D<E, F> sOUTH_OUT,
                       Region3D<E, F> sOUTH_FRONT_OUT, Region3D<E, F> sOUTH_REAR_OUT, Region3D<E, F> fRONT_OUT,
                       Region3D<E, F> rEAR_OUT) {
        super();
        WEST_MINE = wEST_MINE;
        NORTH_WEST_MINE = nORTH_WEST_MINE;
        SOUTH_WEST_MINE = sOUTH_WEST_MINE;
        WEST_FRONT_MINE = wEST_FRONT_MINE;
        WEST_REAR_MINE = wEST_REAR_MINE;
        NORTH_WEST_FRONT_MINE = nORTH_WEST_FRONT_MINE;
        NORTH_WEST_REAR_MINE = nORTH_WEST_REAR_MINE;
        SOUTH_WEST_FRONT_MINE = sOUTH_WEST_FRONT_MINE;
        SOUTH_WEST_REAR_MINE = sOUTH_WEST_REAR_MINE;
        EAST_MINE = eAST_MINE;
        NORTH_EAST_MINE = nORTH_EAST_MINE;
        SOUTH_EAST_MINE = sOUTH_EAST_MINE;
        EAST_FRONT_MINE = eAST_FRONT_MINE;
        EAST_REAR_MINE = eAST_REAR_MINE;
        NORTH_EAST_FRONT_MINE = nORTH_EAST_FRONT_MINE;
        NORTH_EAST_REAR_MINE = nORTH_EAST_REAR_MINE;
        SOUTH_EAST_FRONT_MINE = sOUTH_EAST_FRONT_MINE;
        SOUTH_EAST_REAR_MINE = sOUTH_EAST_REAR_MINE;
        NORTH_MINE = nORTH_MINE;
        NORTH_FRONT_MINE = nORTH_FRONT_MINE;
        NORTH_REAR_MINE = nORTH_REAR_MINE;
        SOUTH_MINE = sOUTH_MINE;
        SOUTH_FRONT_MINE = sOUTH_FRONT_MINE;
        SOUTH_REAR_MINE = sOUTH_REAR_MINE;
        FRONT_MINE = fRONT_MINE;
        REAR_MINE = rEAR_MINE;
        WEST_OUT = wEST_OUT;
        NORTH_WEST_OUT = nORTH_WEST_OUT;
        SOUTH_WEST_OUT = sOUTH_WEST_OUT;
        WEST_FRONT_OUT = wEST_FRONT_OUT;
        WEST_REAR_OUT = wEST_REAR_OUT;
        NORTH_WEST_FRONT_OUT = nORTH_WEST_FRONT_OUT;
        NORTH_WEST_REAR_OUT = nORTH_WEST_REAR_OUT;
        SOUTH_WEST_FRONT_OUT = sOUTH_WEST_FRONT_OUT;
        SOUTH_WEST_REAR_OUT = sOUTH_WEST_REAR_OUT;
        EAST_OUT = eAST_OUT;
        NORTH_EAST_OUT = nORTH_EAST_OUT;
        SOUTH_EAST_OUT = sOUTH_EAST_OUT;
        EAST_FRONT_OUT = eAST_FRONT_OUT;
        EAST_REAR_OUT = eAST_REAR_OUT;
        NORTH_EAST_FRONT_OUT = nORTH_EAST_FRONT_OUT;
        NORTH_EAST_REAR_OUT = nORTH_EAST_REAR_OUT;
        SOUTH_EAST_FRONT_OUT = sOUTH_EAST_FRONT_OUT;
        SOUTH_EAST_REAR_OUT = sOUTH_EAST_REAR_OUT;
        NORTH_OUT = nORTH_OUT;
        NORTH_FRONT_OUT = nORTH_FRONT_OUT;
        NORTH_REAR_OUT = nORTH_REAR_OUT;
        SOUTH_OUT = sOUTH_OUT;
        SOUTH_FRONT_OUT = sOUTH_FRONT_OUT;
        SOUTH_REAR_OUT = sOUTH_REAR_OUT;
        FRONT_OUT = fRONT_OUT;
        REAR_OUT = rEAR_OUT;
    }





    public Region3D<E, F> getWEST_MINE() {
        return WEST_MINE;
    }



    public void setWEST_MINE(Region3D<E, F> wEST_MINE) {
        WEST_MINE = wEST_MINE;
    }



    public Region3D<E, F> getNORTH_WEST_MINE() {
        return NORTH_WEST_MINE;
    }



    public void setNORTH_WEST_MINE(Region3D<E, F> nORTH_WEST_MINE) {
        NORTH_WEST_MINE = nORTH_WEST_MINE;
    }



    public Region3D<E, F> getSOUTH_WEST_MINE() {
        return SOUTH_WEST_MINE;
    }



    public void setSOUTH_WEST_MINE(Region3D<E, F> sOUTH_WEST_MINE) {
        SOUTH_WEST_MINE = sOUTH_WEST_MINE;
    }



    public Region3D<E, F> getWEST_FRONT_MINE() {
        return WEST_FRONT_MINE;
    }



    public void setWEST_FRONT_MINE(Region3D<E, F> wEST_FRONT_MINE) {
        WEST_FRONT_MINE = wEST_FRONT_MINE;
    }



    public Region3D<E, F> getWEST_REAR_MINE() {
        return WEST_REAR_MINE;
    }



    public void setWEST_REAR_MINE(Region3D<E, F> wEST_REAR_MINE) {
        WEST_REAR_MINE = wEST_REAR_MINE;
    }



    public Region3D<E, F> getNORTH_WEST_FRONT_MINE() {
        return NORTH_WEST_FRONT_MINE;
    }



    public void setNORTH_WEST_FRONT_MINE(Region3D<E, F> nORTH_WEST_FRONT_MINE) {
        NORTH_WEST_FRONT_MINE = nORTH_WEST_FRONT_MINE;
    }



    public Region3D<E, F> getNORTH_WEST_REAR_MINE() {
        return NORTH_WEST_REAR_MINE;
    }



    public void setNORTH_WEST_REAR_MINE(Region3D<E, F> nORTH_WEST_REAR_MINE) {
        NORTH_WEST_REAR_MINE = nORTH_WEST_REAR_MINE;
    }



    public Region3D<E, F> getSOUTH_WEST_FRONT_MINE() {
        return SOUTH_WEST_FRONT_MINE;
    }



    public void setSOUTH_WEST_FRONT_MINE(Region3D<E, F> sOUTH_WEST_FRONT_MINE) {
        SOUTH_WEST_FRONT_MINE = sOUTH_WEST_FRONT_MINE;
    }



    public Region3D<E, F> getSOUTH_WEST_REAR_MINE() {
        return SOUTH_WEST_REAR_MINE;
    }



    public void setSOUTH_WEST_REAR_MINE(Region3D<E, F> sOUTH_WEST_REAR_MINE) {
        SOUTH_WEST_REAR_MINE = sOUTH_WEST_REAR_MINE;
    }



    public Region3D<E, F> getEAST_MINE() {
        return EAST_MINE;
    }



    public void setEAST_MINE(Region3D<E, F> eAST_MINE) {
        EAST_MINE = eAST_MINE;
    }



    public Region3D<E, F> getNORTH_EAST_MINE() {
        return NORTH_EAST_MINE;
    }



    public void setNORTH_EAST_MINE(Region3D<E, F> nORTH_EAST_MINE) {
        NORTH_EAST_MINE = nORTH_EAST_MINE;
    }



    public Region3D<E, F> getSOUTH_EAST_MINE() {
        return SOUTH_EAST_MINE;
    }



    public void setSOUTH_EAST_MINE(Region3D<E, F> sOUTH_EAST_MINE) {
        SOUTH_EAST_MINE = sOUTH_EAST_MINE;
    }



    public Region3D<E, F> getEAST_FRONT_MINE() {
        return EAST_FRONT_MINE;
    }



    public void setEAST_FRONT_MINE(Region3D<E, F> eAST_FRONT_MINE) {
        EAST_FRONT_MINE = eAST_FRONT_MINE;
    }



    public Region3D<E, F> getEAST_REAR_MINE() {
        return EAST_REAR_MINE;
    }



    public void setEAST_REAR_MINE(Region3D<E, F> eAST_REAR_MINE) {
        EAST_REAR_MINE = eAST_REAR_MINE;
    }



    public Region3D<E, F> getNORTH_EAST_FRONT_MINE() {
        return NORTH_EAST_FRONT_MINE;
    }



    public void setNORTH_EAST_FRONT_MINE(Region3D<E, F> nORTH_EAST_FRONT_MINE) {
        NORTH_EAST_FRONT_MINE = nORTH_EAST_FRONT_MINE;
    }



    public Region3D<E, F> getNORTH_EAST_REAR_MINE() {
        return NORTH_EAST_REAR_MINE;
    }



    public void setNORTH_EAST_REAR_MINE(Region3D<E, F> nORTH_EAST_REAR_MINE) {
        NORTH_EAST_REAR_MINE = nORTH_EAST_REAR_MINE;
    }



    public Region3D<E, F> getSOUTH_EAST_FRONT_MINE() {
        return SOUTH_EAST_FRONT_MINE;
    }



    public void setSOUTH_EAST_FRONT_MINE(Region3D<E, F> sOUTH_EAST_FRONT_MINE) {
        SOUTH_EAST_FRONT_MINE = sOUTH_EAST_FRONT_MINE;
    }



    public Region3D<E, F> getSOUTH_EAST_REAR_MINE() {
        return SOUTH_EAST_REAR_MINE;
    }



    public void setSOUTH_EAST_REAR_MINE(Region3D<E, F> sOUTH_EAST_REAR_MINE) {
        SOUTH_EAST_REAR_MINE = sOUTH_EAST_REAR_MINE;
    }



    public Region3D<E, F> getNORTH_MINE() {
        return NORTH_MINE;
    }



    public void setNORTH_MINE(Region3D<E, F> nORTH_MINE) {
        NORTH_MINE = nORTH_MINE;
    }



    public Region3D<E, F> getNORTH_FRONT_MINE() {
        return NORTH_FRONT_MINE;
    }



    public void setNORTH_FRONT_MINE(Region3D<E, F> nORTH_FRONT_MINE) {
        NORTH_FRONT_MINE = nORTH_FRONT_MINE;
    }



    public Region3D<E, F> getNORTH_REAR_MINE() {
        return NORTH_REAR_MINE;
    }



    public void setNORTH_REAR_MINE(Region3D<E, F> nORTH_REAR_MINE) {
        NORTH_REAR_MINE = nORTH_REAR_MINE;
    }



    public Region3D<E, F> getSOUTH_MINE() {
        return SOUTH_MINE;
    }



    public void setSOUTH_MINE(Region3D<E, F> sOUTH_MINE) {
        SOUTH_MINE = sOUTH_MINE;
    }



    public Region3D<E, F> getSOUTH_FRONT_MINE() {
        return SOUTH_FRONT_MINE;
    }



    public void setSOUTH_FRONT_MINE(Region3D<E, F> sOUTH_FRONT_MINE) {
        SOUTH_FRONT_MINE = sOUTH_FRONT_MINE;
    }



    public Region3D<E, F> getSOUTH_REAR_MINE() {
        return SOUTH_REAR_MINE;
    }



    public void setSOUTH_REAR_MINE(Region3D<E, F> sOUTH_REAR_MINE) {
        SOUTH_REAR_MINE = sOUTH_REAR_MINE;
    }



    public Region3D<E, F> getFRONT_MINE() {
        return FRONT_MINE;
    }



    public void setFRONT_MINE(Region3D<E, F> fRONT_MINE) {
        FRONT_MINE = fRONT_MINE;
    }



    public Region3D<E, F> getREAR_MINE() {
        return REAR_MINE;
    }



    public void setREAR_MINE(Region3D<E, F> rEAR_MINE) {
        REAR_MINE = rEAR_MINE;
    }



    public Region3D<E, F> getWEST_OUT() {
        return WEST_OUT;
    }



    public void setWEST_OUT(Region3D<E, F> wEST_OUT) {
        WEST_OUT = wEST_OUT;
    }



    public Region3D<E, F> getNORTH_WEST_OUT() {
        return NORTH_WEST_OUT;
    }



    public void setNORTH_WEST_OUT(Region3D<E, F> nORTH_WEST_OUT) {
        NORTH_WEST_OUT = nORTH_WEST_OUT;
    }



    public Region3D<E, F> getSOUTH_WEST_OUT() {
        return SOUTH_WEST_OUT;
    }



    public void setSOUTH_WEST_OUT(Region3D<E, F> sOUTH_WEST_OUT) {
        SOUTH_WEST_OUT = sOUTH_WEST_OUT;
    }



    public Region3D<E, F> getWEST_FRONT_OUT() {
        return WEST_FRONT_OUT;
    }



    public void setWEST_FRONT_OUT(Region3D<E, F> wEST_FRONT_OUT) {
        WEST_FRONT_OUT = wEST_FRONT_OUT;
    }



    public Region3D<E, F> getWEST_REAR_OUT() {
        return WEST_REAR_OUT;
    }



    public void setWEST_REAR_OUT(Region3D<E, F> wEST_REAR_OUT) {
        WEST_REAR_OUT = wEST_REAR_OUT;
    }



    public Region3D<E, F> getNORTH_WEST_FRONT_OUT() {
        return NORTH_WEST_FRONT_OUT;
    }



    public void setNORTH_WEST_FRONT_OUT(Region3D<E, F> nORTH_WEST_FRONT_OUT) {
        NORTH_WEST_FRONT_OUT = nORTH_WEST_FRONT_OUT;
    }



    public Region3D<E, F> getNORTH_WEST_REAR_OUT() {
        return NORTH_WEST_REAR_OUT;
    }



    public void setNORTH_WEST_REAR_OUT(Region3D<E, F> nORTH_WEST_REAR_OUT) {
        NORTH_WEST_REAR_OUT = nORTH_WEST_REAR_OUT;
    }



    public Region3D<E, F> getSOUTH_WEST_FRONT_OUT() {
        return SOUTH_WEST_FRONT_OUT;
    }



    public void setSOUTH_WEST_FRONT_OUT(Region3D<E, F> sOUTH_WEST_FRONT_OUT) {
        SOUTH_WEST_FRONT_OUT = sOUTH_WEST_FRONT_OUT;
    }



    public Region3D<E, F> getSOUTH_WEST_REAR_OUT() {
        return SOUTH_WEST_REAR_OUT;
    }



    public void setSOUTH_WEST_REAR_OUT(Region3D<E, F> sOUTH_WEST_REAR_OUT) {
        SOUTH_WEST_REAR_OUT = sOUTH_WEST_REAR_OUT;
    }



    public Region3D<E, F> getEAST_OUT() {
        return EAST_OUT;
    }



    public void setEAST_OUT(Region3D<E, F> eAST_OUT) {
        EAST_OUT = eAST_OUT;
    }



    public Region3D<E, F> getNORTH_EAST_OUT() {
        return NORTH_EAST_OUT;
    }



    public void setNORTH_EAST_OUT(Region3D<E, F> nORTH_EAST_OUT) {
        NORTH_EAST_OUT = nORTH_EAST_OUT;
    }



    public Region3D<E, F> getSOUTH_EAST_OUT() {
        return SOUTH_EAST_OUT;
    }



    public void setSOUTH_EAST_OUT(Region3D<E, F> sOUTH_EAST_OUT) {
        SOUTH_EAST_OUT = sOUTH_EAST_OUT;
    }



    public Region3D<E, F> getEAST_FRONT_OUT() {
        return EAST_FRONT_OUT;
    }



    public void setEAST_FRONT_OUT(Region3D<E, F> eAST_FRONT_OUT) {
        EAST_FRONT_OUT = eAST_FRONT_OUT;
    }



    public Region3D<E, F> getEAST_REAR_OUT() {
        return EAST_REAR_OUT;
    }



    public void setEAST_REAR_OUT(Region3D<E, F> eAST_REAR_OUT) {
        EAST_REAR_OUT = eAST_REAR_OUT;
    }



    public Region3D<E, F> getNORTH_EAST_FRONT_OUT() {
        return NORTH_EAST_FRONT_OUT;
    }



    public void setNORTH_EAST_FRONT_OUT(Region3D<E, F> nORTH_EAST_FRONT_OUT) {
        NORTH_EAST_FRONT_OUT = nORTH_EAST_FRONT_OUT;
    }



    public Region3D<E, F> getNORTH_EAST_REAR_OUT() {
        return NORTH_EAST_REAR_OUT;
    }



    public void setNORTH_EAST_REAR_OUT(Region3D<E, F> nORTH_EAST_REAR_OUT) {
        NORTH_EAST_REAR_OUT = nORTH_EAST_REAR_OUT;
    }



    public Region3D<E, F> getSOUTH_EAST_FRONT_OUT() {
        return SOUTH_EAST_FRONT_OUT;
    }



    public void setSOUTH_EAST_FRONT_OUT(Region3D<E, F> sOUTH_EAST_FRONT_OUT) {
        SOUTH_EAST_FRONT_OUT = sOUTH_EAST_FRONT_OUT;
    }



    public Region3D<E, F> getSOUTH_EAST_REAR_OUT() {
        return SOUTH_EAST_REAR_OUT;
    }



    public void setSOUTH_EAST_REAR_OUT(Region3D<E, F> sOUTH_EAST_REAR_OUT) {
        SOUTH_EAST_REAR_OUT = sOUTH_EAST_REAR_OUT;
    }



    public Region3D<E, F> getNORTH_OUT() {
        return NORTH_OUT;
    }



    public void setNORTH_OUT(Region3D<E, F> nORTH_OUT) {
        NORTH_OUT = nORTH_OUT;
    }



    public Region3D<E, F> getNORTH_FRONT_OUT() {
        return NORTH_FRONT_OUT;
    }



    public void setNORTH_FRONT_OUT(Region3D<E, F> nORTH_FRONT_OUT) {
        NORTH_FRONT_OUT = nORTH_FRONT_OUT;
    }



    public Region3D<E, F> getNORTH_REAR_OUT() {
        return NORTH_REAR_OUT;
    }



    public void setNORTH_REAR_OUT(Region3D<E, F> nORTH_REAR_OUT) {
        NORTH_REAR_OUT = nORTH_REAR_OUT;
    }



    public Region3D<E, F> getSOUTH_OUT() {
        return SOUTH_OUT;
    }



    public void setSOUTH_OUT(Region3D<E, F> sOUTH_OUT) {
        SOUTH_OUT = sOUTH_OUT;
    }



    public Region3D<E, F> getSOUTH_FRONT_OUT() {
        return SOUTH_FRONT_OUT;
    }



    public void setSOUTH_FRONT_OUT(Region3D<E, F> sOUTH_FRONT_OUT) {
        SOUTH_FRONT_OUT = sOUTH_FRONT_OUT;
    }



    public Region3D<E, F> getSOUTH_REAR_OUT() {
        return SOUTH_REAR_OUT;
    }



    public void setSOUTH_REAR_OUT(Region3D<E, F> sOUTH_REAR_OUT) {
        SOUTH_REAR_OUT = sOUTH_REAR_OUT;
    }



    public Region3D<E, F> getFRONT_OUT() {
        return FRONT_OUT;
    }



    public void setFRONT_OUT(Region3D<E, F> fRONT_OUT) {
        FRONT_OUT = fRONT_OUT;
    }



    public Region3D<E, F> getREAR_OUT() {
        return REAR_OUT;
    }



    public void setREAR_OUT(Region3D<E, F> rEAR_OUT) {
        REAR_OUT = rEAR_OUT;
    }






}
