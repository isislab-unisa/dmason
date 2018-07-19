package it.isislab.dmason.sim.app.DWoims3D;


import java.io.Serializable;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import sim.portrayal3d.SimplePortrayal3D;

public abstract class RemoteObstacle<E> extends SimplePortrayal3D implements Serializable,RemotePositionedAgent<E>{

    E pos;
    String id;

    public RemoteObstacle() {
        // TODO Auto-generated method stub
    }

    public RemoteObstacle(DistributedState<E> state){
        int i = state.nextId();
        this.id = state.getType().toString()+"-"+i;
    }

    /**
     *
     */
    private static final long serialVersionUID = -804328013149854126L;

    @Override
    public E getPos() {
        // TODO Auto-generated method stub
        return pos;
    }

    @Override
    public void setPos(E pos) {
        // TODO Auto-generated method stub
        this.pos=pos;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public void setId(String id) {
        // TODO Auto-generated method stub
        this.id=id;
    }

    public boolean equals(Object obj){
        if(this==obj)
            return true;
        if (obj==null)
            return false;
        if(getClass()!=obj.getClass())
            return false;
        RemoteObstacle other = (RemoteObstacle) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (pos == null) {
            if (other.pos != null)
                return false;
        } else if (!pos.equals(other.pos))
            return false;
        return true;
    }
}
