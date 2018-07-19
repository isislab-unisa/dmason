package it.isislab.dmason.sim.app.DWoims3D;

import java.io.Serializable;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;

import com.sun.j3d.utils.geometry.Sphere;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import sim.engine.SimState;
import sim.util.Double3D;

public class DObstacle3D extends RemoteObstacle{

    private static final long serialVersionUID = 1;

    double diameter;
    Double3D pos;
    protected Color3f obstacleColor = new Color3f(192f/255,255f/255,192f/255);

    public DObstacle3D(DistributedState dm,Double3D position )
    {
        super(dm);
        pos=position;
    }

    public void setDiameter(double diam){
        diameter=diam;
    }

    public double getDiameter(){
        return diameter;
    }

    @Override
    public void step(SimState arg0) {
        // TODO Auto-generated method stub

    }

    public TransformGroup getModel(Object obj, TransformGroup j3dModel)
    {
        if(j3dModel==null)
        {
            j3dModel = new TransformGroup();
            Sphere s = new Sphere((float)diameter/2);
            Appearance appearance = new Appearance();
            appearance.setColoringAttributes(new ColoringAttributes(obstacleColor, ColoringAttributes.SHADE_GOURAUD));
            Material m= new Material();
            m.setAmbientColor(obstacleColor);
            m.setEmissiveColor(0f,0f,0f);
            m.setDiffuseColor(obstacleColor);
            m.setSpecularColor(1f,1f,1f);
            m.setShininess(128f);
            appearance.setMaterial(m);

            s.setAppearance(appearance);
            j3dModel.addChild(s);
            clearPickableFlags(j3dModel);
        }
        return j3dModel;
    }

}
