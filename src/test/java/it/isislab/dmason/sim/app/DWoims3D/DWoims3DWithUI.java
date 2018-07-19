package it.isislab.dmason.sim.app.DWoims3D;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;

public class DWoims3DWithUI extends GUIState {

    public Display3D display;
    public JFrame displayFrame;
    public static String name;
    public static String title;

    ContinuousPortrayal3D woimsPortrayal = new ContinuousPortrayal3D();

    public DWoims3DWithUI(SimState state) {
        super(state);
        // TODO Auto-generated constructor stub
    }

    public void init(Controller c)
    {
        super.init(c);


        // make the displayer
        display = new Display3D(600, 600, this);
        display.setBackdrop(Color.black);

        //WireFrameBoxPortrayal3D wireFramePortrayal = new WireFrameBoxPortrayal3D(-0.5,-0.5,-0.5,DParticles3D.gridWidth, DParticles3D.gridHeight, DParticles3D.gridLenght, Color.blue);


        display.translate(-100,-100,-100);
        display.scale(1.0/200);

        display.attach( woimsPortrayal, "Woims!" );
        displayFrame = display.createFrame();
        displayFrame.setTitle(title);
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
        // uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
		/* display.attach( trailsPortrayal, "Trails" ); */



    }

    @Override
    public void quit()
    {
        super.quit();

        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    public DWoims3DWithUI(GeneralParam args,String prefix)
    {
        super(new DWoims3D(args, prefix));
        name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()))+""+(String.valueOf(args.getZ()));
        title=String.valueOf("Woims"+args.getI())+""+(String.valueOf(args.getJ()))+""+(String.valueOf(args.getZ()));
    }

    public DWoims3DWithUI(GeneralParam args,List<EntryParam<String, Object>> simParams,String topicPrefix)
    {
        super(new DWoims3D(args, simParams, topicPrefix));
        name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()))+""+(String.valueOf(args.getZ()));
        title=String.valueOf("Woims"+args.getI())+""+(String.valueOf(args.getJ()))+""+(String.valueOf(args.getZ()));
    }

    public static String getName() { return "Peer: <"+name+">"; }

    @Override
    public void start()
    {
        super.start();
        setupPortrayals();
    }

    @Override
    public void load(SimState state)
    {
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals()
    {
        DWoims3D woims = (DWoims3D)state;

        woimsPortrayal.setField(woims.environment);
        display.createSceneGraph();
        display.reset();
        //display.repaint();
    }


}

