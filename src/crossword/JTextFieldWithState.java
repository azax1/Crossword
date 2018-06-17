package crossword;

import javax.swing.JTextField;

/**
 * Simple JTextField extension that supports tracking whether the field was empty just before it gained focus.
 * TODO come up with a better name now that there is more functionality
 */
@SuppressWarnings("serial")
class JTextFieldWithState extends JTextField {
	boolean wasEmpty;
	
	public JTextFieldWithState(String s) {
		super(s);
		wasEmpty = true;
	}
	
	/**
	 * Returns true unless all the current contents of the square are whitespace.
	 * Returns true if the data field is empty.
	 */
	public boolean isFilled() {
		return this.getText().trim().length() > 0;
	}
}