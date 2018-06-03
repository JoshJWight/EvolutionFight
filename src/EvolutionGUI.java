import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class EvolutionGUI extends JFrame{
	private EvolutionController controller;
	
	private JTextField saveField;
	private JTextField readField;
	
	public EvolutionGUI(final EvolutionController controller){
		this.controller = controller;
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        System.exit(0);
		    }
		});
		this.setTitle("Evolution Sim");
		this.setSize(500, 500);
		
		
		this.setLayout(new GridLayout(5, 2));
		
		JButton genButton = new JButton("Generate");
		genButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						controller.generate();
					}
				}.start();
			}
		});
		this.add(genButton,0,0);
		
		JButton showButton = new JButton("Show One");
		showButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						controller.show();
					}
				}.start();
			}
		});
		this.add(showButton,0,1);
		JButton finishButton = new JButton("Finish Gen");
		finishButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						controller.finish();
					}
				}.start();
			}
		});
		this.add(finishButton,1,0);
		
		JButton autoButton = new JButton("Toggle Auto");
		autoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						if(controller.auto) {
							controller.auto = false;
						} else {
							controller.startAuto();
						}
					}
				}.start();
			}
		});
		this.add(autoButton,1,1);
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						controller.save(saveField.getText());
					}
				}.start();
			}
		});
		this.add(saveButton,2,0);
		
		saveField = new JTextField(new Date().toString());
		this.add(saveField,2,1);
		
		JButton readButton = new JButton("Read");
		readButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						controller.read(readField.getText());
					}
				}.start();
			}
		});
		this.add(readButton,3,0);
		
		readField = new JTextField();
		this.add(readField,3,1);
		
		JButton bestButton = new JButton("Show Best");
		bestButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run() {
						controller.showBest();
					}
				}.start();
			}
		});
		this.add(bestButton,4,0);
		
		this.setVisible(true);
	}
	
}
