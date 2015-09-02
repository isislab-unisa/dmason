package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
@Deprecated
public class FileTree {
//
//    public static void main(String[] args) {
//        new FileTree();
//    }

    private DNode r;
    
    public FileTree(DNode root) {
    	r=root;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                }

                JFrame frame = new JFrame(" Dendrogram");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        private DefaultTreeModel model;
        private JTree tree;

        public TestPane() {
            setLayout(new BorderLayout());

            tree = new JTree();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Dendo");
            model = new DefaultTreeModel(root);

            tree.setModel(model);
            tree.setRootVisible(true);
            tree.setShowsRootHandles(true);

            add(new JScrollPane(tree));

//            JButton load = new JButton("Load");
  //          add(load, BorderLayout.SOUTH);

//            load.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {

                    DefaultMutableTreeNode root2 = (DefaultMutableTreeNode) model.getRoot();
                    root2.removeAllChildren();
                    model.reload();
                  //  File rootFile = (File) root.getUserObject();

                   // addFiles(rootFile, model, root);
                    navigate(r,model,root);

                    tree.expandPath(new TreePath(root));

//                }
//            });

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }
        
        protected void navigate(DNode myRoot, DefaultTreeModel model, DefaultMutableTreeNode root)
        {
        	for (int i = 0 ; i< myRoot.getChildrenCount() ; i++) {
        		 DNode n = (DNode) myRoot.getChildren().get(i);
        		 DefaultMutableTreeNode child = new DefaultMutableTreeNode(n.getCommunity().toString()+" resolution: "+n.getResolution());
        		 model.insertNodeInto(child, root, root.getChildCount());
        		 if(n.getChildrenCount()>1)
        			 navigate(n,model,child);
			}
        }

        protected void addFiles(File rootFile, DefaultTreeModel model, DefaultMutableTreeNode root) {

            for (File file : rootFile.listFiles()) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
                model.insertNodeInto(child, root, root.getChildCount());
                if (file.isDirectory()) {
                    addFiles(file, model, child);
                }
            }

        }
    }
}