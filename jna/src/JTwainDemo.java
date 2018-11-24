// JTwainDemo.java

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import net.javajeff.jtwain.ImageListener;
import net.javajeff.jtwain.JNATwain;
import net.javajeff.jtwain.JTwainException;

/**
 *  This class defines the GUI for the JTwainDemo application.
 *
 *  @author Jeff Friesen
 */

@SuppressWarnings("serial")
public class JTwainDemo extends JFrame
{
   /**
    *  Each acquired image is displayed in an ImageArea panel.
    */

   ImageArea ia = new ImageArea ();

   /**
    *  To support the display of images that can't be fully displayed without
    *  scrolling, the ImageArea panel is placed in a JScrollPane.
    */

   JScrollPane jsp;

   /**
    *  Construct JTwainDemo's GUI and start event-handling thread.
    *
    *  @param title text to display in JTwainDemo's title bar
    */

   public JTwainDemo (String title)
   {
      // Place title in the title bar of JTwainDemo's main window.

      super (title);

      // Exit the application if user selects Close from system menu.

      setDefaultCloseOperation (EXIT_ON_CLOSE);

      // Create the application's File menu.

      JMenu menu = new JMenu ("File");

      ActionListener al;

      // Build the Acquire... menu item.

      JMenuItem mi = new JMenuItem ("Acquire...");
      al = new ActionListener ()
           {
               @Override
			public void actionPerformed (ActionEvent e)
               {
                   new SwingWorker<Image, Object>() {
                       @Override
                       protected Image doInBackground() throws Exception {
                           ImageListener imageListener = new ScannerListener();
                           return JNATwain.acquire(true, imageListener);
                       }

                       @Override
                       protected void done() {
                           super.done();

                           try {

                               ia.setImage(get());

                               jsp.getHorizontalScrollBar().setValue(0);
                               jsp.getVerticalScrollBar().setValue(0);
                           } catch (Exception e2) {
                               e2.printStackTrace();
                               JOptionPane.showMessageDialog(JTwainDemo.this,
                                       e2.getMessage());
                           }
                       }


               }.execute();
                  
               }
           };

      mi.addActionListener (al);
      menu.add (mi);

      // Build the Select Source... menu item.

      mi = new JMenuItem ("Select Source...");
      al = new ActionListener ()
           {
               @Override
			public void actionPerformed (ActionEvent e)
               {
                  try
                  {
                      // Display dialog box of sources names to user. The user
                      // may select any source as the new default source -- as
                      // long as the user closes the dialog box by clicking on
                      // Ok.

                      JNATwain.selectSourceAsDefault ();
                  }
                  catch (JTwainException e2)
                  {
                      JOptionPane.showMessageDialog (JTwainDemo.this,
                                                     e2.getMessage ());
                  }
               }
           };

      mi.addActionListener (al);
      menu.add (mi);

      menu.addSeparator ();

      // Build the Exit menu item.

      mi = new JMenuItem ("Exit");
      mi.addActionListener (new ActionListener ()
                            {
                                @Override
								public void actionPerformed (ActionEvent e)
                                {
                                   dispose ();
                                }
                            });
      menu.add (mi);

      // Create the main window's menu bar and add the file menu to that menu
      // bar.

      JMenuBar mb = new JMenuBar ();
      mb.add (menu);
      setJMenuBar (mb);

      // Place the ImageArea panel component inside a JScrollPane and add the
      // JScrollPane to the main window's content pane.

      jsp = new JScrollPane (ia);
	getContentPane ().add (jsp);

      // Pack all components to their preferred sizes.

      pack ();

      // Display the GUI and start the event-handling thread.

      setVisible (true);
   }

   /**
    *  Application entry point.
    *
    *  @param args array of command-line arguments.
    */

   public static void main (String [] args)
   {
      new JTwainDemo ("JTwain Demo");
   }
}
