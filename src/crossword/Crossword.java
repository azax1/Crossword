package crossword;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Code adapted from https://stackoverflow.com/questions/21755117/
 * what-is-the-best-way-to-create-gui-for-a-crossword-puzzle-java
 *
 * @author Aryeh Zax
 * @version 05/25/18
 */

public class Crossword
{
	private static String fileName;
	private CrosswordPanel panel;
	
	public  static final char WHITESPACE_CHAR = 8193;
	public static final String WHITESPACE_STRING = String.valueOf(WHITESPACE_CHAR);
	
	// dimension of grid (standard = 15 x 15)
	private static final int SIZE = 15;
	
	// type of square (-1 = black square, n = start of clue n, 0 = generic fill-able square)
	private int[][] type;
	private final static int BLACK = -1;
	
	// stores clues
	private String[] acrossClues;
	private String[] downClues;
	
	// displays both relevant clues
	private JLabel across;
	private JLabel down;
	
	boolean currentDir;
	public static final boolean ACROSS = true;
	public static final boolean DOWN = false;
	
    public static void main(String[] args)
    {
    		fileName = args[0];
	    	final Crossword[] crossword = { new Crossword() }; // hack to pass objects into dynamically defined class
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                (crossword[0]).createAndShowGUI();
            }
        });
    }

    /**
     * Update the banner text to read what the across and down clues are for the given square.
     */
    public void updateBanner(int x, int y)
    {
    		panel.textFields[x][y].grabFocus();
	    panel.textFields[x][y].selectAll();
	    	
	    	// find the down clue this is part of
	    	int w = x;
	    	int z = y;
	    	while (type[w][z] <= 0 || type[w][z] >= downClues.length || downClues[type[w][z]] == null)
	    		w--;
	    	int downClue = type[w][z];
	    	down.setText(downClue + "-Down: " + downClues[downClue]);
	    	
	    	// find the across clue this is part of
	    	w = x;
	    	while (type[w][z] <= 0 || type[w][z] >= acrossClues.length || acrossClues[type[w][z]] == null)
	    		z--;
	    	int acrossClue = type[w][z];
	    	across.setText(acrossClue + "-Across: " + acrossClues[acrossClue]);
    }
    
    private void createAndShowGUI()
    {
    		currentDir = ACROSS;
    		
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.getContentPane().setLayout(new BorderLayout());

        JPanel container = new JPanel(new FlowLayout());
        panel = new CrosswordPanel();
        final CrosswordPanel[] panels = { panel };
        container.add(panel);
        f.getContentPane().add(container, BorderLayout.CENTER);
        
        across = new JLabel();
        down = new JLabel();
        across.setText("across");
        down.setText("down");
        
        JPanel panel = new JPanel();
        panel.add(across);
        panel.add(down);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        
        f.getContentPane().add(panel, BorderLayout.NORTH);
        
        f.setSize(800, 800);
        try {
			generate(panels[0]);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void generate(CrosswordPanel panel) throws FileNotFoundException
    {
	    	Scanner sc = new Scanner(new File(fileName));
	    	sc.nextLine();
	    	acrossClues = new String[sc.nextInt() + 1];
	    	sc.nextLine();
	    	sc.nextLine();
	    	while(true)
	    	{
	    		int index = sc.nextInt();
	    		acrossClues[index] = sc.nextLine();
	    		if (index == acrossClues.length - 1)
	    			break;
	    	}
	    	
	    	sc.nextLine();
	    	sc.nextLine();
	    	downClues = new String[sc.nextInt() + 1];
	    	sc.nextLine();
	    	sc.nextLine();
	    	while(true)
	    	{
	    		int index = sc.nextInt();
	    		downClues[index] = sc.nextLine();
	    		if (index == downClues.length - 1)
	    			break;
	    	}
	    	
	    	sc.nextLine();
	    	
	    	int w = SIZE;
	    	int h = SIZE;
	    	type = new int[w][h];
	
	    	int j = 0;
	    	while (sc.hasNextLine())
	    	{
	    		String l = sc.nextLine();
	    		String[] line = l.split(" ");
	    		for (int k = 1; k < line.length; k++)
	    			type[j][Integer.parseInt(line[k])] = -1;
	    		j += 1;
	    	}
	    	
	    	sc.close();
	    	
	    	int num = 1;
	    	for (int i = 0; i < type.length; i++)
	    		for (int k = 0; k < type.length; k++)
	       			if (type[i][k] != -1 && (i == 0 || k == 0 || type[i - 1][k] == -1 || type[i][k - 1] == -1))
	       				type[i][k] = num++;
	    	
	        char[][] crossword = new char[w][h];
	        for (int x = 0; x < w; x++)
	        {
	        		for (int y = 0; y < h; y++)
	            {
	                if (type[x][y] == -1)
	                    crossword[x][y] = 0;
	                else
	                		crossword[x][y] = WHITESPACE_CHAR;
	            }
	        }
	    panel.setCrossword(crossword, new Crossword[] { this });
    }
    
    /**
     * Sets the current direction of travel to be the given direction.
     */
    public void setDirection(boolean newDir) {
    		currentDir = newDir;
    }
    
    /**
     * Moves the cursor to the next empty square, which depends on whether we are currently working down or across.
     * TODO make this work like NYT: go to next clue rather than next square when at end of clue
     */
    public void tab(int x, int y, boolean wasEmpty) {
		int w = x + (currentDir == DOWN ? 1 : 0);
		int z = y + (currentDir == ACROSS ? 1 : 0);
		if (!wasEmpty && w < SIZE && z < SIZE && type[w][z] != -1) {
			// go to immediate next square (you're rewriting a clue that was wrong)
			// no code actually has to go here, we're just bypassing the else block
		}
		else {
			// go to next empty square (you're writing in a clue fresh)
	    		JTextField[][] tfs = panel.textFields;
	    		w = x;
	    		z = y;
	    		
			while (type[w][z] == BLACK || (tfs[w][z].getText().length() > 0
				   && ! WHITESPACE_STRING.equals(tfs[w][z].getText()))) {
				if (currentDir == ACROSS) {
					z++;
					w += z / SIZE;
					z %= SIZE;
					
					if (w == SIZE && z == 0) {
						w = 0;
						z = 0;
						currentDir = DOWN;
					}
				}
				else {
					w++;
					z += w / SIZE;
					w %= SIZE;
					
					if (w == 0 && z == SIZE) {
						w = 0;
						z = 0;
						currentDir = ACROSS;
					}
				}
				
				if (w == x && z == y) { // puzzle filled in
					break;
				}
			}
		}
    		updateBanner(w, z);
	}
    
    /**
     * Go to the immediately preceding square, if it is directly left / above and filled in; otherwise remain stationary.
     * Used when deleting from a box.
     */
    public void detab(int x, int y) {
    		int w = x - (currentDir == DOWN ? 1 : 0);
		int z = y - (currentDir == ACROSS ? 1 : 0);
    		if (w >= 0 && z >= 0 && type[w][z] != -1) {
    			if (! (WHITESPACE_STRING.equals(panel.textFields[w][z].getText())
    				|| (panel.textFields[w][z].getText().length() == 0))) {
    				updateBanner(w, z);
    			}
    		}
    }
}

/**
 * Simple JTextField extension that tracks whether the field was empty just before it gained focus.
 */
class JTextFieldWithState extends JTextField {
	boolean wasEmpty;
	
	public JTextFieldWithState(String s) {
		super(s);
		wasEmpty = true;
	}
}

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

        for (int x = 0; x < h; x++)
        {
            for (int y = 0; y < w; y++)
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
                    			(crossword[0]).updateBanner(coord[0], coord[1]);
						}

						@Override
						public void focusLost(FocusEvent e) {
							ts[0].wasEmpty = ts[0].getText().length() == 0;
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
								case KeyEvent.VK_LEFT:
								case KeyEvent.VK_KP_LEFT:
									(crossword[0]).setDirection(Crossword.ACROSS);
									break;
								case KeyEvent.VK_DOWN:
								case KeyEvent.VK_KP_DOWN:
								case KeyEvent.VK_UP:
								case KeyEvent.VK_KP_UP:
									(crossword[0]).setDirection(Crossword.DOWN);
									break;
								case KeyEvent.VK_BACK_SPACE:
									(crossword[0]).detab(coord[0], coord[1]);
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