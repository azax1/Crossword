package crossword;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Code adapted from https://stackoverflow.com/questions/21755117/
 * what-is-the-best-way-to-create-gui-for-a-crossword-puzzle-java
 *
 * @author Aryeh Zax
 * @version 06/17/18
 */

public class Crossword
{
	private static String fileName;
	CrosswordPanel panel;
	
	public static final char WHITESPACE_CHAR = 8193;
	public static final String WHITESPACE_STRING = String.valueOf(WHITESPACE_CHAR);
	
	// dimension of grid (standard = 15 x 15)
	static int SIZE;
	
	// type of square (-1 = black square, n = start of clue n, 0 = generic fill-able square)
	int[][] type;
	private final static int BLACK = -1;
	
	// stores clues
	private String[] acrossClues;
	private String[] downClues;
	
	// if n is a blah clue, blahStarts[n] stores { x, y }, the coordinates of
	// the black square immediately before that clue's beginning
	private int[][] acrossStarts;
	private int[][] downStarts;
	
	// stores the number of the currently selected clue
	private int acrossIndex;
	private int downIndex;
	
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
    	 * Returns the starting squares of the two clues (across, then down) that cross (x, y).
    	 */
    	int[][] getSquares(int x, int y) {
    		int[][] ret = new int[2][2];
    		int w = Math.min(SIZE - 1, x);
	    	int z = Math.min(SIZE - 1,  y);
	    	while (type[w][z] >= acrossClues.length || acrossClues[type[w][z]] == null)
	    		w--;
	    	ret[0] = new int[] { w, z }; // the across clue
	    	
	    	w = Math.min(SIZE - 1, x);
	    	while (type[w][z] >= downClues.length || downClues[type[w][z]] == null)
	    		z--;
	    	ret[1] = new int[] { w, z }; // the down clue
	    	
	    	return ret;
    	}
    	
    	/**
    	 * Returns the numbers of the two clues (across, then down) that cross (x, y).
    	 */
    	private int[] getClues(int x, int y) {
    		int[][] squares = getSquares(x, y);
    		int[] ret = new int[2];
    		ret[0] = type[squares[0][0]][squares[0][1]]; // the across clue
    		ret[1] = type[squares[1][0]][squares[1][1]]; // the across clue
	    	return ret;
    	}
    	
    /**
     * Update the banner text to read what the across and down clues are for the given square.
     * Also updates the across and down indices, and highlights the relevant squares.
     */
    public void switchToSquare(int x, int y)
    {
    		// de-highlight
    		for (int w = 0; w < SIZE; w++) {
    			for (int z = 0; z < SIZE; z++) {
    				if (type[w][z] != -1) {
    					panel.textFields[w][z].setBackground(Color.WHITE);
    				}
    			}
    		}
    		
    		try {
				Thread.sleep(20);
			} catch (InterruptedException e) { }
    	
    		panel.textFields[x][y].grabFocus();
	    panel.textFields[x][y].selectAll();
	    	
	    int[] wz = getClues(x, y);
	    	acrossIndex = wz[0];
	    	downIndex = wz[1];
	    	
	    	down.setText(downIndex + "-Down: " + downClues[downIndex]);
	    	across.setText(acrossIndex + "-Across: " + acrossClues[acrossIndex]);
	    
	    	// highlight
	    int[][] squares = getSquares(x, y);
	    	for (int w = squares[0][0]; w < SIZE && type[w][y] != -1; w++) {
	    		panel.textFields[w][y].setBackground(Color.YELLOW);
	    	}
	    	for (int z = squares[1][1]; z < SIZE && type[x][z] != -1; z++) {
	    		panel.textFields[x][z].setBackground(Color.YELLOW);
	    	}
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
			generate(panels[0]); // TODO why is this wrapped in an array?
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void generate(CrosswordPanel panel) throws FileNotFoundException
    {
	    	Scanner sc = new Scanner(new File(fileName));
	    	sc.nextLine();
	    	acrossClues = new String[sc.nextInt() + 1];
	    	acrossStarts = new int[acrossClues.length][2];
	    	for (int i = 0; i < acrossStarts.length; i++) { acrossStarts[i] = null; }
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
	    downStarts = new int[downClues.length][2];
    		for (int i = 0; i < downStarts.length; i++) { downStarts[i] = null; }
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
	    	
	    	SIZE = Integer.parseInt(sc.nextLine());
	    	
	    	int w = SIZE;
	    	int h = SIZE;
	    	type = new int[w][h];
	
	    	int j = 0;
	    	while (sc.hasNextLine())
	    	{
	    		String l = sc.nextLine();
	    		String[] line = l.split(" ");
	    		for (int k = 1; k < line.length; k++)
	    			type[Integer.parseInt(line[k])][j] = BLACK;
	    		j += 1;
	    	}
	    	
	    	sc.close();
	    	
	    	int num = 1; // clue number

		for (int k = 0; k < type[0].length; k++) {
    			for (int i = 0; i < type.length; i++) { // need to sweep left-to-right, then up-down
       			if (type[i][k] != BLACK) {
       				if (i == 0 || type[i - 1][k] == BLACK) { // across clue
       					acrossStarts[num] = new int[]{ i - 1, k };
       					type[i][k] = num;
       					
       					// code repetition is ugly, but whether or not you increment num is hard
       					if (k == 0 || type[i][k - 1] == BLACK) { // down clue too
	       					downStarts[num] = new int[]{ i, k - 1 };
	       				}
       					num++;
       				}
       				else if (k == 0 || type[i][k - 1] == BLACK) { // down clue only
       					downStarts[num] = new int[]{ i, k - 1 };
       					type[i][k] = num++;
       				}
       			}
	    		}
	    	}
	    	
        char[][] crossword = new char[w][h];
        for (int x = 0; x < w; x++)
        {
        		for (int y = 0; y < h; y++)
            {
                if (type[x][y] == BLACK)
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
     * Behavior depends slightly on filled-in status to determine whether we are erasing pre-filled squares or
     * because they are likely incorrect or wish to skip over them because they are likely correct.
     */
    public void tab(int x, int y, boolean skipFilled) {
		JTextField[][] tfs = panel.textFields;
    		// first, try to just move one square "forward"
    		// this is correct when we just overwrote something and there's a fillable square in front of us
		int w = x + (currentDir == ACROSS ? 1 : 0);
		int z = y + (currentDir == DOWN ? 1 : 0);
		if (w >= SIZE || z >= SIZE) {  // can't just go forward, abort
			w = x;
			z = y;
		}
		else if (type[w][z] != BLACK &&
				! (skipFilled && ((JTextFieldWithState) tfs[w][z]).isFilled())) { // going forward is a go
			switchToSquare(w, z);
			return;
		}
		
		while (w == SIZE || z == SIZE
			  || type[w][z] == BLACK
			  || (tfs[w][z].getText().length() > 0 && ! WHITESPACE_STRING.equals(tfs[w][z].getText()))) {
			if (w == SIZE || z == SIZE || type[w][z] == BLACK) { // end of clue;  jump to start of next clue (by number)
				int[][] arr = (currentDir == ACROSS ? acrossStarts : downStarts);
				int index = (currentDir == ACROSS ? acrossIndex : downIndex) + 1;
				while (index < arr.length && arr[index] == null) { index++; }
				if (index != arr.length) { // there's a later clue of the same type
					w = arr[index][0] + (currentDir == ACROSS ? 1 : 0);
					z = arr[index][1] + (currentDir == DOWN ? 1 : 0);
					int[] clues = getClues(w, z);
					acrossIndex = clues[0];
					downIndex = clues[1];
				}
				else { // no later clue; change dir and start over at beginning of puzzle
					currentDir = !currentDir;
					int[] coords = (currentDir == ACROSS ? acrossStarts : downStarts)[1];
					w = coords[0] + (currentDir == ACROSS ? 1 : 0);
					z = coords[1] + (currentDir == DOWN ? 1 : 0);
					
					int[] indices = getClues(w, z);
					acrossIndex = indices[0];
					downIndex = indices[1];
				}
			}
			else {
				w += (currentDir == ACROSS ? 1 : 0);
				z += (currentDir == DOWN ? 1 : 0);
			}
			
			if (w == x && z == y) { // puzzle filled in
				break;
			}
		}
    		switchToSquare(w, z);
	}
    
    /**
     * Go to the immediately preceding square, if it is directly left / above and filled in; otherwise remain stationary.
     * Used when navigating with up / left arrow keys or deleting from a box.
     */
    public void detab(int x, int y, boolean skipFilled) {
    		int w = x - (currentDir == ACROSS ? 1 : 0);
		int z = y - (currentDir == DOWN ? 1 : 0);
    		if (w >= 0 && z >= 0 && type[w][z] != BLACK) {
    			if (skipFilled
    				|| ! (WHITESPACE_STRING.equals(panel.textFields[w][z].getText())
    				|| (panel.textFields[w][z].getText().length() == 0))) {
    				switchToSquare(w, z);
    			}
    		}
    }
}