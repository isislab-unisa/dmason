/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.util.management.inspection;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import it.isislab.dmason.experimentals.util.visualization.globalviewer.Display;
import sim.display.GUIState;
import sim.portrayal.inspector.PropertyInspector;
import sim.util.Interval;
import sim.util.Properties;
import sim.util.gui.LabelledList;
import sim.util.gui.PropertyField;

/**
 * Replicates the visuals of SimpleInspector, even though its values are
 * updated through a distributed system.
 * @see sim.portrayal.SimpleInspector
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini
 *
 */
public class DistributedInspector extends JPanel
{
	/** The object being inspected */
    Object object;
    /** The property list displayed -- this may change at any time */
    LabelledList propertyList;
    /** The generated object properties -- this may change at any time */
    Properties properties;
    /** Each of the property fields in the property list, not all of which may exist at any time. */
    PropertyField[] members = new PropertyField[0];
    /** The number of items presently in the propertyList */
    int count = 0;
    
    GUIState guiState;
    
    public void updateInspector()
    {
    	if (properties.isVolatile())  // need to rebuild each time, YUCK
    	{
    		remove(propertyList);
    		generateProperties();
    		//doEnsuredRepaint(this);
    	}
    	else for( int i = 0 ; i < count ; i++ )
    		if (members[i] != null) 
    			members[i].setValue(properties.betterToString(properties.getValue(i)));
    }
    
    public DistributedInspector(Properties properties, GUIState guiState)
    {
    	// Create an update button
    	JButton btnUpdate = new JButton("Update");
    	btnUpdate.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { updateInspector(); } });
    	JPanel pnlUpdate = new JPanel();
    	pnlUpdate.setLayout(new FlowLayout(SwingConstants.RIGHT));
    	pnlUpdate.add(btnUpdate);
    	this.setLayout(new BorderLayout());
    	this.add(pnlUpdate, BorderLayout.NORTH);
    	
    	// Create property inspectors for the model
    	this.guiState = guiState;
    	this.properties = properties;
    	
    	generateProperties();
    }
    
    /**
     * If no inspectable class is available, just show the Graphics checkbox.
     * @param guiState
     */
    public DistributedInspector(GUIState guiState)
    {
    	Properties p = Properties.getProperties(null);
    	this.properties = p;
    	this.guiState = guiState;
    	generateProperties();
    }

	private void generateProperties()
	{
		if (propertyList != null) 
            remove(propertyList);
        propertyList = new LabelledList(null);
        
        { // ADDS A CHECKBOX TO TRACE/UNTRACE THE GRAPHIC
	        JCheckBox isTracing = new JCheckBox();
	        
	        isTracing.setName("-GRAPHICS");
    		isTracing.setToolTipText("Check this box if you want to trace agents' positions");
    		isTracing.addItemListener(new ItemListener() {				
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					if (e.getStateChange() == ItemEvent.SELECTED)
						((Display)guiState).startTracing(((JCheckBox)e.getItem()).getName());
					else
						((Display)guiState).stopTracing(((JCheckBox)e.getItem()).getName());
				}
			});
	        
	    	propertyList.add(null,
					new JLabel("Graphic"),
					null, 
					isTracing, 
					null);	
        }
        
        count = properties.numProperties();
        
        members = new PropertyField[count + 1];
		
        for (int i = 0; i < count; i++)
        {
        	if (!properties.isHidden(i))  // don't show if the user asked that it be hidden
            {
        		members[i] = makePropertyField(i);
        		
        		JCheckBox isTracing = new JCheckBox();
        		isTracing.setName(properties.getName(i));
        		isTracing.setToolTipText("Check this box if you want to trace " + properties.getName(i));
        		isTracing.addItemListener(new ItemListener() {
					
					@Override
					public void itemStateChanged(ItemEvent e)
					{
						if (e.getStateChange() == ItemEvent.SELECTED)
							((Display)guiState).startTracing(((JCheckBox)e.getItem()).getName());
						else
							((Display)guiState).stopTracing(((JCheckBox)e.getItem()).getName());
					}
				});
        		
        		propertyList.add(null,
        				new JLabel(properties.getName(i) + " "),
        				PropertyInspector.getPopupMenu(properties,i, this.guiState, null /*makePreliminaryPopup(i)*/), 
        				members[i], 
        				isTracing);
            }
        	else 
        		members[i] = null;
        }
        add(propertyList);
        revalidate();
	}
	
	PropertyField makePropertyField(final int index)
	{
		Class<?> type = properties.getType(index);
		final Properties props = properties;            // see UNUSUAL BUG note below
		return new PropertyField(
				null,
				properties.betterToString(properties.getValue(index)),
				properties.isReadWrite(index),
				properties.getDomain(index),
				(properties.isComposite(index) ?
						//PropertyField.SHOW_VIEWBUTTON : 
						PropertyField.SHOW_TEXTFIELD :
							(type == Boolean.TYPE || type == Boolean.class ?
									PropertyField.SHOW_CHECKBOX :
										(properties.getDomain(index) == null ? PropertyField.SHOW_TEXTFIELD :
											(properties.getDomain(index) instanceof Interval) ? 
													PropertyField.SHOW_SLIDER : PropertyField.SHOW_LIST ))))
		{
			// The return value should be the value you want the display to show instead.
			@Override
			public String newValue(final String newValue)
			{
				// UNUSUAL BUG: if I say this:
				// Properties props = properties;
				// ...or...
				// Properties props = SimpleInspector.this.properties
				// ... then sometimes props is set to null even though clearly
				// properties is non-null above, since it'd be impossible to return a
				// PropertyField otherwise.  So instead of declaring it as an instance
				// variable here, we declare it as a final closure variable above.

				// the underlying model could still be running, so we need
				// to do this safely
				//synchronized(SimpleInspector.this.state.state.schedule)
				synchronized(guiState.state.schedule)
				{
					// try to set the value
					if (props.setValue(index, newValue) == null)
						java.awt.Toolkit.getDefaultToolkit().beep();
					// refresh the controller -- if it exists yet
//					if (SimpleInspector.this.state.controller != null)
//						SimpleInspector.this.state.controller.refresh();
					if (guiState.controller != null)
					{
						guiState.controller.refresh();	
					}
					// set text to the new value
					return props.betterToString(props.getValue(index));
				}
			}
		};
    }
}
