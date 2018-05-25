package crossword;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
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
 * @version 06/21/16
 */

public class Crossword
{
	private static final String FILE_NAME = "./src/crossword/032317.txt";
	private CrosswordPanel panel;
	private int[][] type; // type of square (-1 = black square, n = start of clue n, 0 = other)
	private String[] acrossClues;
	private String[] downClues;
	private JLabel across;
	private JLabel down;
	
    public static void main(String[] args)
    {
    	final Crossword[] c = { new Crossword() };
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                (c[0]).createAndShowGUI();
            }
        });
    }

    public void updateSquare(JTextField t)
    {
    	t.selectAll();
    	int x = 0;
    	int y = 0;
    	JTextField[][] tfs = panel.textFields;
    	for(x = 0; x < 15; x++)
    	{
    		for(y = 0; y < 15; y++)
    		{
    			if (t == tfs[x][y])
    				break;
    		}
    		if (y != 15)
    			break;
    	}
    	int w = y;
    	int z = x;
    	while (type[w][z] <= 0 || type[w][z] >= downClues.length || downClues[type[w][z]] == null)
    		w--;
    	int downClue = type[w][z];
    	down.setText(downClue + "-Down: " + downClues[downClue]);
    	
    	w = y;
    	while (type[w][z] <= 0 || type[w][z] >= acrossClues.length || acrossClues[type[w][z]] == null)
    		z--;
    	int acrossClue = type[w][z];
    	across.setText(acrossClue + "-Across: " + acrossClues[acrossClue]);
    }
    
    private void createAndShowGUI()
    {
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
	    	Scanner sc = new Scanner(new File(FILE_NAME));
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
	    	
	    	int w = 15;
	    	int h = 15;
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
	        for (int x=0; x<w; x++)
	        {
	        	for (int y=0; y<h; y++)
	            {
	                if (type[y][x] == -1) // don't ask about the indexing. really, don't
	                    crossword[x][y] = 0;
	                else
	                	crossword[x][y] = 8193; // em whitespace
	            }
	        }
	
	        Crossword[] junk = { this };
	        panel.setCrossword(crossword, junk);
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

        for (int y=0; y<h; y++)
        {
            for (int x=0; x<w; x++)
            {
                char c = array[x][y];
                if (c != 0)
                {
                    textFields[x][y] = new JTextField(String.valueOf(c));
                    textFields[x][y].setFont(textFields[x][y].getFont().deriveFont(20.0f));
                    (textFields[x][y]).addFocusListener(new FocusListener() {

						@Override
						public void focusGained(FocusEvent e) {
							Object source = e.getSource();
                    		(crossword[0]).updateSquare((JTextField) source);
						}

						@Override
						public void focusLost(FocusEvent e) { }
 
                    });
                    
                    textFields[x][y].getDocument().addDocumentListener(new DocumentListener() {

						@Override
						public void insertUpdate(DocumentEvent e) {	update(e); }
						@Override
						public void removeUpdate(DocumentEvent e) { }
						@Override
						public void changedUpdate(DocumentEvent e) { update(e); }
						
						private void update(DocumentEvent e) {
					    	try {
								Robot robot = new Robot(); // bad hack to tab over to next square (only works for across clues)
								robot.keyPress(KeyEvent.VK_TAB);
							} catch (AWTException f) {
								f.printStackTrace(); // rude, but whatever
							}
						}
					
                    });
                    
                    add(textFields[x][y]);
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

    char[][] getCrossword()
    {
        int w = textFields.length;
        int h = textFields[0].length;
        char crossword[][] = new char[w][h];
        for (int y=0; y<h; y++)
        {
            for (int x=0; x<w; x++)
            {
                if (textFields[x][y] != null)
                {
                    String s = textFields[x][y].getText();
                    if (s.length() > 0)
                    {
                        crossword[x][y] = s.charAt(0);
                    }

                }
            }
        }
        return crossword;
    }
}