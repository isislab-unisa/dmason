package dmason.util.SystemManagement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.util.gui.HTMLBrowser;
import dmason.sim.engine.DistributedState;

/**
 * @author Michele Carillo, Ada Mancuso, Flavio Serrapica, Carmine Spagnuolo, Francesco Raia 
 */
public class ModelPanel extends JPanel {
	JTabbedPane tabpane;
	public ModelPanel(JTabbedPane tabpane) {
		this.tabpane=tabpane;
		initComponents();
	}
	HTMLBrowser browser;
	private void initComponents() {

		scrollPaneModel = new JScrollPane();
		panelInModel = new JPanel();
		scrollPaneHTML = new JScrollPane();
		panelInHTML = new JPanel();
		panelInHTML.setLayout(new BorderLayout());
		
		browser = new HTMLBrowser(defaultText)
        {
        	public Dimension getPreferredSize() { return new Dimension(300, 300); }
        	public Dimension getMinimumSize() { return new Dimension(300,300); }
        };
       
        panelInHTML.add(browser,BorderLayout.CENTER);


		//======== this ========

		//======== scrollPaneModel ========
		{

			//======== panelInModel ========
			{

				GroupLayout panelInModelLayout = new GroupLayout(panelInModel);
				panelInModel.setLayout(panelInModelLayout);
				panelInModelLayout.setHorizontalGroup(
					panelInModelLayout.createParallelGroup()
						.addGap(0, 330, Short.MAX_VALUE)
				);
				panelInModelLayout.setVerticalGroup(
					panelInModelLayout.createParallelGroup()
						.addGap(0, 343, Short.MAX_VALUE)
				);
			}
			scrollPaneModel.setViewportView(panelInModel);
		}

		//======== scrollPaneHTML ========
		{

			//======== panelInHTML ========
			{

				GroupLayout panelInHTMLLayout = new GroupLayout(panelInHTML);
				panelInHTML.setLayout(panelInHTMLLayout);
				panelInHTMLLayout.setHorizontalGroup(
					panelInHTMLLayout.createParallelGroup()
						.addGap(0, 315, Short.MAX_VALUE)
				);
				panelInHTMLLayout.setVerticalGroup(
					panelInHTMLLayout.createParallelGroup()
						.addGap(0, 343, Short.MAX_VALUE)
				);
			}
			scrollPaneHTML.setViewportView(panelInHTML);
		}

		
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(scrollPaneModel, GroupLayout.PREFERRED_SIZE, 332, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(browser, GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
		);
		layout.setVerticalGroup(
			layout.createParallelGroup()
				.addComponent(scrollPaneModel, GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
				.addComponent(browser, GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
		);
		
		
		
	}
	Inspector modelInspector;
	

	private JScrollPane scrollPaneModel;
	private JPanel panelInModel;
	private JScrollPane scrollPaneHTML;
	private JPanel panelInHTML;
	final String defaultText ="<html><body bgcolor='white'>Info simulation not founded!</body></html>";
	public HashMap<String, Object> getAllParameters()
	{
		return hash;
	}
	HashMap<String, Object> hash;
	
	public void updateHTML(String selectedSimulation) {
		hash = new HashMap<String, Object>();
		panelInModel.removeAll();
		 try {
			 DistributedState d=
					 (DistributedState) Class.forName(selectedSimulation).getConstructor().
			 newInstance();
			 
			browser.setText(GUIState.getInfo(Class.forName(selectedSimulation)));
			Field[] fields =Class.forName(selectedSimulation).getFields();
			Method[] methods=Class.forName(selectedSimulation).getMethods();
			HashMap<String, Method> hash_methods=new HashMap<String, Method>();
			
			for (int j = 0; j < methods.length; j++) {
			
				hash_methods.put(methods[j].getName(), methods[j]);
			}
			
			
			for (int i = 0; i < methods.length; i++) {
				
				
				if(methods[i].getName().startsWith("get"))
				{
					String name2=methods[i].getName().substring(3, methods[i].getName().length());
					if(hash_methods.containsKey("set"+name2)
							&& !hash_methods.containsKey("hide"+name2)
							&& !hash_methods.containsKey("global"+name2))
					{
						Object o=methods[i].invoke(d);
						if(o!=null)
							hash.put(name2,o);
					}
				}
			}
			
			panelInModel.setLayout(new GridLayout(hash.size(),2));
			for (String s:hash.keySet()) {
				panelInModel.add(new JLabel(s));
				final String  name_field=s;
				final JTextField valueField=new JTextField(hash.get(s)+"");
				valueField.getDocument().addDocumentListener(new DocumentListener() {					
					@Override
					public void removeUpdate(DocumentEvent e)
					{
						hash.put(name_field, valueField.getText());
					}
					
					@Override
					public void insertUpdate(DocumentEvent e)
					{
						hash.put(name_field, valueField.getText());
					}
					
					@Override
					public void changedUpdate(DocumentEvent e)
					{
						hash.put(name_field, valueField.getText());
					}
				});
				panelInModel.add(valueField);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			browser.setText("<html><body bgcolor='white'>Could not load simulation parameters!</body></html>");
			e.printStackTrace();
		}
		 
	}
}
