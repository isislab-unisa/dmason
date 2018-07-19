package it.isislab.dmason.sim.app.DWoims3D;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import sim.engine.SimState;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.util.Bag;
import sim.util.Double3D;

public class DWoim3D extends RemoteWoim<Double3D> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final double CENTROID_DISTANCE = 20 * DWoims3D.DIAMETER;
    public static final double AVOID_DISTANCE = 16 * DWoims3D.DIAMETER;
    public static final double COPY_SPEED_DISTANCE = 40 * DWoims3D.DIAMETER;

    public static final double OBSTACLE_AVOID_COEF = 1.05;
    public static final double OBSTACLE_FAST_AVOID_COEF = 1.5;

    public static final double MAX_DISTANCE = Math.max( CENTROID_DISTANCE, Math.max( AVOID_DISTANCE, COPY_SPEED_DISTANCE ) );

    public static final double ADJUSTMENT_RATE = 0.025;
    public static final double MIN_VELOCITY = 0.25;
    public static final double MAX_VELOCITY = 0.75;
    public static final float SKIP = 4.0f;

    protected double orientation;
    protected Vector3D velocity = new Vector3D( 0, 0, 0 );
    protected Vector3D acceleration = new Vector3D( 0, 0, 0 );

    double ond;
    double ondSpeed;
    public Vector3D woimPosition=new Vector3D(0,0,0);
    final static int numLinks = 12;
    Vector3d[] lastPos = new Vector3d[numLinks];
    Vector3d[] lastPosRel = new Vector3d[numLinks];
    java.awt.Color[] colors = new java.awt.Color[numLinks];

    //Bag nearbyWoims;
    //double[] distSqrTo;

    public DWoim3D(){

    }


    public DWoim3D(DistributedState<Double3D> dm, Double3D position){
        super(dm);
        pos=position;
        ond = dm.random.nextDouble()*2*Math.PI;
        ondSpeed = 0.05 + dm.random.nextDouble()*0.15;
        for( int i = 0 ; i < colors.length ; i++ )
            colors[i] = new java.awt.Color(63 + (int)(192*(colors.length-i)/colors.length),0,0);
        //((float)(63f+(192.0*(colors.length-i))/colors.length)/255.0f, 0f, 0f );
        velocity = new Vector3D(0.05,0.05, 0.05);
        computePositions();
    }




    public final double distanceSquared( final Vector3D loc1, final Vector3D loc2 )
    {
        return( (loc1.x-loc2.x)*(loc1.x-loc2.x)+(loc1.y-loc2.y)*(loc1.y-loc2.y)+(loc1.z-loc2.z)*(loc1.z-loc2.z) );
    }

    public final double distanceSquared( final Vector3D loc1, final Double3D loc2 )
    {
        return( (loc1.x-loc2.x)*(loc1.x-loc2.x)+(loc1.y-loc2.y)*(loc1.y-loc2.y)+(loc1.z-loc2.z)*(loc1.z-loc2.z) );
    }

    public final double distanceSquared( final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
    {
        return ((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2));
    }

    void preprocessWoims( final DWoims3D state, Double3D pos, Bag nearbyWoims, double[] distSqrTo )
    {

		/*
          if( nearbyWoims == null )
          return;
		 */

        distSqrTo = new double[nearbyWoims.numObjs];
        for( int i = 0 ; i < nearbyWoims.numObjs ; i++ )
        {
            if(!(nearbyWoims.objs[i] instanceof DWoim3D))
            {
                continue;
            }
            DWoim3D p = (DWoim3D)(nearbyWoims.objs[i]);
            distSqrTo[i] = distanceSquared(
                    pos.x,pos.y,pos.z,p.pos.x,p.pos.y,p.pos.z);
        }
    }

    public Vector3D towardsFlockCenterOfMass( final DWoims3D state, Bag nearbyWoims, double[] distSqrTo )
    {
        if( nearbyWoims == null )
            return new Vector3D( 0, 0, 0 );
        Vector3D mean = new Vector3D( 0, 0, 0 );
        int n = 0;
        for( int i = 0 ; i < nearbyWoims.numObjs ; i++ )
        {
            if( nearbyWoims.objs[i] != this &&
                    distSqrTo[i] <= CENTROID_DISTANCE * CENTROID_DISTANCE &&
                    distSqrTo[i] > AVOID_DISTANCE * AVOID_DISTANCE )
            {
                DWoim3D p = (DWoim3D)(nearbyWoims.objs[i]);
                mean = mean.add(new Double3D(p.pos.x,p.pos.y,p.pos.z));
                n++;
            }
        }
        if( n == 0 )
            return new Vector3D( 0, 0, 0 );
        else
        {
            mean = mean.amplify( 1.0 / n );
            mean = mean.subtract( woimPosition );
            return mean.normalize();
        }
    }


    public Vector3D awayFromCloseBys( final DWoims3D state, Bag nearbyWoims, double[] distSqrTo )
    {
        if( nearbyWoims == null )
            return new Vector3D( 0, 0, 0 );
        Vector3D away = new Vector3D( 0, 0, 0 );
        for( int i = 0 ; i < nearbyWoims.numObjs ; i++ )
        {
            if(!(nearbyWoims.objs[i] instanceof DWoim3D))continue;
            if( nearbyWoims.objs[i] != this &&
                    distSqrTo[i] <= AVOID_DISTANCE * AVOID_DISTANCE )
            {
                DWoim3D p = (DWoim3D)(nearbyWoims.objs[i]);
                Vector3D temp = woimPosition.subtract(new Double3D(p.pos.x,p.pos.y,p.pos.z));
                temp = temp.normalize();
                away = away.add( temp );
            }
        }
        return away.normalize();
    }

    public Vector3D matchFlockSpeed( final DWoims3D state, Bag nearbyWoims, double[] distSqrTo )
    {
        if( nearbyWoims == null )
            return new Vector3D( 0, 0, 0 );
        Vector3D mean = new Vector3D( 0, 0, 0 );
        int n = 0;
        for( int i = 0 ; i < nearbyWoims.numObjs ; i++ )
        {
            if( nearbyWoims.objs[i] != this &&
                    distSqrTo[i] <= COPY_SPEED_DISTANCE * COPY_SPEED_DISTANCE &&
                    distSqrTo[i] > AVOID_DISTANCE * AVOID_DISTANCE )
            {
                mean = mean.add( ((DWoim3D)(nearbyWoims.objs[i])).velocity );
                n++;
            }
        }
        if( n == 0 )
            return new Vector3D( 0, 0, 0 );
        else
        {
            mean = mean.amplify( 1.0 / n );
            return mean.normalize();
        }
    }

    public Vector3D avoidObstacles( final SimState state )
    {
        double[][] info = DWoims3D.obstInfo;
        if( info == null || info.length == 0 )
            return new Vector3D( 0, 0, 0 );

        Vector3D away = new Vector3D( 0, 0, 0 );
        for( int i = 0 ; i < info.length ; i++ )
        {
            double dist = Math.sqrt( (woimPosition.x-info[i][1])*(woimPosition.x-info[i][1]) +
                    (woimPosition.y-info[i][2])*(woimPosition.y-info[i][2]) +
                    (woimPosition.z-info[i][3])*(woimPosition.z-info[i][3]) );
            if( dist <= info[i][0]+AVOID_DISTANCE )
            {
                Vector3D temp = woimPosition.subtract( new Vector3D( info[i][1], info[i][2], info[i][3] ) );
                temp = temp.normalize();
                away = away.add( temp );
            }
        }
        return away.normalize();
    }

    public Vector3D randomDirection( final DWoims3D state )
    {
        Vector3D temp = new Vector3D( 1.0 - 2.0 * state.random.nextDouble(),
                1.0 - 2.0 * state.random.nextDouble(),
                1.0 - 2.0 * state.random.nextDouble() );
        return temp.setLength( MIN_VELOCITY + state.random.nextDouble()*(MAX_VELOCITY-MIN_VELOCITY) );
    }


    public Vector3D niceUndulation( final DWoims3D state )
    {
        ond += ondSpeed;
        if( ond > 7 )
            ond -= 2*Math.PI;
        double angle = Math.cos( ond );
        Vector3D temp = velocity;
        double velA = Math.atan2( temp.y, temp.x );
        velA = velA + (Math.PI / 2)*angle;
        return new Vector3D( Math.cos(velA), Math.sin(velA), 0 );
    }

    private void computePositions() {
        double centerx, centery, centerz;

        // the head!
        centerx = pos.x + 1.0/2.0;
        centery = pos.y + 1.0/2.0;
        centerz = pos.z + 1.0/2.0;
        lastPos[0] = new Vector3d( centerx, centery, centerz );
        //System.out.println(lastPos[0]);
        Vector3d temp  = new Vector3d();
        //System.out.println(temp);

        Vector3d velocity3d = new Vector3d(velocity.x, velocity.y, velocity.z);

        //System.out.println(velocity3d);
        for( int i = 1 ; i < numLinks ; i++ )
        {
            //System.out.println(" first lastPost "+lastPos[i]);
            if( lastPos[i] == null )
            {
                temp.scale(-1.0, velocity3d);
                temp.normalize();
                centerx = lastPos[i-1].x+temp.x;
                centery = lastPos[i-1].y+temp.y;
                centerz = lastPos[i-1].z+temp.z;
                lastPos[i] = new Vector3d( centerx, centery, centerz );
                //System.out.println("lastPost "+lastPos[i]);
            }
            else
            {
                temp.sub(lastPos[i-1], lastPos[i] );
                //System.out.println("temp first sub result "+temp);
                temp.scale(SKIP/temp.length());
                //System.out.println("temp scale result "+temp);
                temp.sub(lastPos[i-1], temp);
                //System.out.println("temp second sub result"+temp);
                lastPos[i] = new Vector3d( temp.x, temp.y, temp.z );
                //System.out.println("lastPost "+lastPos[i]);
            }

        }
        for( int i = 0 ; i < lastPosRel.length ; i++ )
        {
            lastPosRel[i] = new Vector3d( lastPos[i].x-lastPos[0].x,
                    lastPos[i].y-lastPos[0].y,
                    lastPos[i].z-lastPos[0].z );
        }

    }


    @Override
    public void step(SimState state) {
        // TODO Auto-generated method stub
        DWoims3D bd = (DWoims3D)state;
        Bag nearbyWoims = bd.environment.getNeighborsWithinDistance( pos, MAX_DISTANCE );
        double[] distSqrTo = new double[nearbyWoims.numObjs];
        //System.out.println(pos.x);
        Double3D temp = new Double3D(pos.x,pos.y,pos.z);
        woimPosition.x = pos.x;
        woimPosition.y = pos.y;
        woimPosition.z = pos.z;
        preprocessWoims( bd, temp, nearbyWoims, distSqrTo );


        Vector3D vel = new Vector3D( 0, 0, 0 );
        vel = vel.add( avoidObstacles(bd).amplify( 1.5 ) );
        vel = vel.add( towardsFlockCenterOfMass(bd,nearbyWoims,distSqrTo).amplify(0.5) );
        //System.out.println(vel);
        vel = vel.add( matchFlockSpeed(bd,nearbyWoims,distSqrTo).amplify(0.5) );
        //System.out.println(vel);
        vel = vel.add( awayFromCloseBys(bd,nearbyWoims,distSqrTo).amplify(1.5) );
        //System.out.println(vel);
        if( vel.length() <= 1.0 )
        {
            vel = vel.add( niceUndulation(bd).amplify(0.5) );
            //System.out.println(vel);
            vel = vel.add( randomDirection(bd).amplify(0.25) );
            //System.out.println(vel);
        }

        double vl = vel.length();
        if( vl < MIN_VELOCITY )
            vel = vel.setLength( MIN_VELOCITY );
        else if( vl > MAX_VELOCITY )
            vel = vel.setLength( MAX_VELOCITY );
        vel = new Vector3D( (1-ADJUSTMENT_RATE)*velocity.x + ADJUSTMENT_RATE*vel.x,
                (1-ADJUSTMENT_RATE)*velocity.y + ADJUSTMENT_RATE*vel.y,
                (1-ADJUSTMENT_RATE)*velocity.z + ADJUSTMENT_RATE*vel.z );
        //System.out.println(vel);
        velocity = vel;
        Double3D desiredPosition = new Double3D( woimPosition.x+vel.x*DWoims3D.TIMESTEP,
                woimPosition.y+vel.y*DWoims3D.TIMESTEP,
                woimPosition.z+vel.z*DWoims3D.TIMESTEP );
        //System.out.println("Step "+bd.schedule.getSteps()+":  agent "+this.id+" position: "+bd.environment.getObjectLocation(this)+" desired position: "+desiredPosition);
        //bd.setObjectLocation(this, desiredPosition);
        Double w=bd.environment.width;
        Double h=bd.environment.height;
        Double l=bd.environment.length;
        Double3D loc = new Double3D((desiredPosition.x+w)%w,(desiredPosition.y+h)%h,(desiredPosition.z+l)%l);
        try {
            bd.environment.setDistributedObjectLocation(loc, this, bd);
            //System.out.println( bd.schedule.getSteps() +" _ "+ bd.environment.cellType +" - " +bd.environment.allObjects.size());
        } catch (DMasonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //		if(bd.schedule.getSteps()%12==0 && bd.schedule.getSteps()!=0){
        //			//System.out.println("Step "+bd.schedule.getSteps()+":  agent "+this.id+" position: "+bd.environment.getObjectLocation(this));
        //
        //			System.out.println(this.id);
        //		}

    }

    public TransformGroup createModel(Object obj)
    {
        TransformGroup globalTG = new TransformGroup();
        for(int i=0; i< numLinks; ++i)
        {
            // we set the number of divisions to 6 and it's quite a bit faster and
            // less memory-hungry.  The default is 15.  This is a sort of goofy way of
            // doing things.
            SpherePortrayal3D s = new SpherePortrayal3D(colors[i], 4.0f , 6);
            s.setCurrentFieldPortrayal(getCurrentFieldPortrayal());
            TransformGroup localTG = s.getModel(obj, null);

            localTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            globalTG.addChild(localTG);
        }
        globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        return globalTG;
    }

    public TransformGroup getModel(Object obj, TransformGroup transf)
    {
        computePositions();
        if(transf==null) return createModel(obj);
        for(int i=0; i<transf.numChildren(); ++i)
        {
            Transform3D tmpT3d = new Transform3D();
            //System.out.println("last "+lastPosRel[i]);
            tmpT3d.setTranslation(lastPosRel[i]);
            ((TransformGroup)transf.getChild(i)).setTransform(tmpT3d);
        }
        return transf;
    }

}
