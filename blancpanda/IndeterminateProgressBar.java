/**
 * 
 */
package blancpanda;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

/**
 * @author Kaoru
 * 
 */
public class IndeterminateProgressBar {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndeterminateProgressBar.show();
	}
	public static void show(){
		UIManager.put("ProgressBar.repaintInterval", new Integer(150));
		UIManager.put("ProgressBar.cycleTime", new Integer(1050));
		final JProgressBar aJProgressBar = new JProgressBar(0, 100);
		aJProgressBar.setIndeterminate(true);
		JFrame theFrame = new JFrame();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = theFrame.getContentPane();
		contentPane.add(aJProgressBar, BorderLayout.CENTER);
		theFrame.setSize(300, 100);
		theFrame.setVisible(true);		
	}
}
