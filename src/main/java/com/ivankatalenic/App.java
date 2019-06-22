package com.ivankatalenic;

import javax.swing.*;
import java.awt.*;

/**
 * Hello world!
 *
 */
public class App {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUI();
			}
		});
	}
	
    private static void createGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("GUI Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TextEditorModel model = 
        		new TextEditorModel("Gle malu vocku poslije kise,\n" 
        				+ "puna je kapi pa ih njise.\n" 
        				+ "I blijesti suncem obasjana,\n" 
        				+ "Cudesna raskos njenih grana.");
        JComponent editor = new TextEditor(model);
        frame.getContentPane().add(editor, BorderLayout.CENTER);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
}

