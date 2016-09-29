package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextDialog extends JDialog {
	private JTextArea textArea;
	private JButton okButton;
	
	public TextDialog() {
		textArea = new JTextArea(30, 50);
		okButton = new JButton("Done");
		okButton.addActionListener(listener);
		okButton.setAlignmentX(CENTER_ALIGNMENT);

		textArea.setLineWrap(true);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		getContentPane().add(okButton);
		pack();
		
		setModalityType(ModalityType.APPLICATION_MODAL);
	}
	
	public String show(String title, String text, boolean editable) {
		setTitle(title);
		textArea.setText(text);
		textArea.setEditable(editable);
		setLocationRelativeTo(null);
		setVisible(true);
		
		return textArea.getText();
	}
	
	private ActionListener listener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	};
}
