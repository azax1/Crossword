package crossword;

import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
class CrosswordPanel extends JPanel
{	
	public JTextField[][] textFields;

    void setCrossword(char[][] array, final Crossword[] crossword)
    {
        removeAll();
        int w = array.length;
        int h = array[0].length;
        setLayout(new GridLayout(w, h));
        textFields = new JTextField[w][h];

        for (int y = 0; y < w; y++)
        {
        		for (int x = 0; x < h; x++) // sweep left-right, then up-down
            {
                char c = array[x][y];
                if (c != 0)
                {
                    JTextFieldWithState t = new JTextFieldWithState(String.valueOf(c));
                		textFields[x][y] = t;
                		t.setFont(t.getFont().deriveFont(20.0f));
                		
                    final int[] coord = { x, y };
                    final JTextFieldWithState[] ts = { t };
                		t.addFocusListener(new FocusListener() {

                			// updates which clues are displayed
						@Override
						public void focusGained(FocusEvent e) {
                    			(crossword[0]).switchToSquare(coord[0], coord[1]);
						}

						@Override
						public void focusLost(FocusEvent e) {
							ts[0].wasEmpty = ! ts[0].isFilled();
						}
 
                    });
                    
                    // allows for a single character write into box, then run away to next unfilled box
                    t.getDocument().addDocumentListener(new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {
							(crossword[0]).tab(coord[0], coord[1], ts[0].wasEmpty);
						}
						@Override
						public void removeUpdate(DocumentEvent e) { } // allow to overwrite after deleting
						@Override
						public void changedUpdate(DocumentEvent e) { } // this kind of update is stupid
					
                    });
                    
                    // allows for arrow presses to trigger across / down mode
                    // allows for backspacing to jump back one square when useful
                    t.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent e) { }
						
						@Override
						public void keyPressed(KeyEvent e) { }

						@Override
						public void keyReleased(KeyEvent e) {
							switch(e.getKeyCode()) {
								case KeyEvent.VK_RIGHT:
								case KeyEvent.VK_KP_RIGHT:
									if (crossword[0].currentDir != Crossword.ACROSS) {
										(crossword[0]).setDirection(Crossword.ACROSS);
									}
									else {
										crossword[0].tab(coord[0], coord[1], false);
									}
									break;
								case KeyEvent.VK_LEFT:
								case KeyEvent.VK_KP_LEFT:
									if (crossword[0].currentDir != Crossword.ACROSS) {
										(crossword[0]).setDirection(Crossword.ACROSS);
									}
									else {
										crossword[0].detab(coord[0], coord[1], false);
									}
									break;
								case KeyEvent.VK_DOWN:
								case KeyEvent.VK_KP_DOWN:
									if (crossword[0].currentDir != Crossword.DOWN) {
										(crossword[0]).setDirection(Crossword.DOWN);
									}
									else {
										crossword[0].tab(coord[0], coord[1], false);
									}
									break;
								case KeyEvent.VK_UP:
								case KeyEvent.VK_KP_UP:
									if (crossword[0].currentDir != Crossword.DOWN) {
										(crossword[0]).setDirection(Crossword.DOWN);
									}
									else {
										crossword[0].detab(coord[0], coord[1], false);
									}
									break;
								case KeyEvent.VK_BACK_SPACE:
									(crossword[0]).detab(coord[0], coord[1], false);
									break;
								default:
									break;
							}
							ts[0].selectAll();
						}
                    		
                    });
                    add(t);
                }
                else
                {
                    add(new JLabel());
                }
            }
        }
        getParent().validate();
        repaint();
    }
}
