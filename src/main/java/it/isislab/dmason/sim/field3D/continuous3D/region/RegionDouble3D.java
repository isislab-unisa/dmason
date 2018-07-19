package it.isislab.dmason.sim.field3D.continuous3D.region;


import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field3D.region.Region3D;
import it.isislab.dmason.util.Util;
import sim.util.Double3D;

public class RegionDouble3D extends Region3D<Double, Double3D> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RegionDouble3D(Double upl_xx, Double upl_yy, Double upl_zz, Double down_xx, Double down_yy, Double down_zz) {
        super(upl_xx, upl_yy, upl_zz, down_xx, down_yy, down_zz);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Region3D<Double, Double3D> clone() {
        // TODO Auto-generated method stub
        RegionDouble3D r = new RegionDouble3D(upl_xx, upl_yy, upl_zz, down_xx, down_yy, down_zz);
        for(String agent_id : this.keySet())
        {
            EntryAgent<Double3D> e = this.get(agent_id);
            r.put(e.r.getId(), new EntryAgent<Double3D>(((RemotePositionedAgent<Double3D>)(Util.clone(e.r))),e.l));
        }

        return r;
    }

    @Override
    public boolean isMine(Double x, Double y, Double z) {
        // TODO Auto-generated method stub
        return (x>=upl_xx) && (y >= upl_yy) && (z>=upl_zz) && (x < down_xx) && (y< down_yy) && (z<down_zz);
    }

    @Override
    public boolean addAgents(EntryAgent<Double3D> e) {
        // TODO Auto-generated method stub
        if(e == null || e.l == null || e.r == null) return false;

        if(this.containsKey(e.r.getId()) && this.get(e.r.getId()).equals(e)) return true;

        this.put(e.r.getId(),e);
        return true;
    }
}
