package it.isislab.dmason.sim.field3D;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import sim.engine.SimState;

/**
 * An interface for all Distributed Fields 3D
 * @param <E> the type of locations
 *
 *
 * @author D'Auria Matteo
 */
public interface DistributedField3D<E> extends it.isislab.dmason.sim.field.DistributedField<E>
{
    public static final int UNIFORM_PARTITIONING_MODE=0;
    public static final int SQUARE_BALANCED_DISTRIBUTION_MODE=1;
    public static final int HORIZONTAL_BALANCED_DISTRIBUTION_MODE=2;
    public static final int NON_UNIFORM_PARTITIONING_MODE=4;
    public static final int THIN_MODE=3;

    /**
     * Provide the shift logic of the agents among the peers
     * @param location The new location of the remote agent
     * @param remoteObject The remote agent to be stepped or the value
     * @param sm SimState of simulation
     * @throws DMasonException the exception
     * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
     */
    public boolean setDistributedObjectLocation(final E location,Object remoteObject,SimState sm) throws DMasonException;


    /**
     * Set a available location to a Remote Agent:
     * it generates the location depending on the field of expertise
     * @return The location assigned to Remote Agent
     */
    public E getAvailableRandomLocation();

    public boolean isToroidal();

    public void setToroidal(boolean isToroidal);

    public boolean verifyPosition(E pos);

}
