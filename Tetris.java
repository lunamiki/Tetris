
/**
 * Tetris.java
 * Make a Tetris Game
 * @author Susie Shen, Period 3 
 * @version 4/24/17
 */
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
public class Tetris extends JPanel implements ActionListener
{
    private final Point[][][] Tetraminos = {
            // I-Piece
            {
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
            },

            // J-Piece
            {
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
                { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
                { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) }
            },
            
            // L-Piece
            {
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
                { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) },
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
                { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }
            },
            
            // O-Piece
            {
                { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }
            },

            // S-Piece
            {
                { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
                { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
                { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
                { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }
            },
            
            // T-Piece
            {
                { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
                { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
                { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
                { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }
            },
            
            // Z-Piece
            {
                { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
                { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) },
                { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
                { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) }
            }
    }; //tetris pieces 
    private final Color[] tetraminoColors = {
        Color.cyan, Color.blue, Color.orange, Color.yellow, Color.green, Color.pink, Color.red
    };
    private static int size = 25; //default size
    
    private static int score = 0;
    private static int rowsCleared = 0;
    private static int rows = 0;
    private static int level = 1;
    private static boolean fourRows = false; //four rows cleared at same time
    private static int rowsNextLevel = 5; // rows to clear until next level
    
    private static int currentPiece; 
    private static int currentRotation;
    private static int nextPiece; 
    private static int nextRotation;
    private static Point origin = new Point(4, -4); //spawn point
    private static Point location = new Point(4,-4); //current location
    private static Color[][] grid = new Color[10][22];
    
    private static JFrame frame = new JFrame();
    private static JButton pauseButton;
    private static JButton newGameButton;
    private static AtomicBoolean paused = new AtomicBoolean(false);
    private static Thread threadObject;
    private static boolean finished = false;
    private static int [] speed = {800,717,633,550,467,383,300,217,133,100,83,67,50,33,17}; 
    public Tetris()
    {
        //create board
        setLayout(null);
        this.pauseButton = new JButton("Pause");
                pauseButton.addActionListener(this);
                this.pauseButton.setFocusable(false);
        add(pauseButton);
        this.pauseButton.setBounds(325, 26*16, 120, 30);
        this.newGameButton = new JButton("Reset");
                newGameButton.addActionListener(this);
                this.newGameButton.setFocusable(false);
        add(newGameButton);
        this.newGameButton.setBounds(325, 26*18, 120, 30);
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 22; j++)
            {
                this.grid[i][j] = Color.black;
            }
        }
        spawnPiece(); //spawns first piece
        nextPiece();
        
        Runnable runnable = new Runnable(){
            @Override 
            public void run()
            {
                while(true)
                {
                    if(paused.get() == true)
                    {
                        synchronized(threadObject)
                        {
                            // Pause Button
                            try 
                            {
                                threadObject.wait();
                            } 
                            catch (InterruptedException e) 
                            {
                            }
                        }
                    }
                    try
                    {
                        dropDown();
                        if(level < 11)
                        {
                            threadObject.sleep(speed[level-1]);
                        }
                        else if(level > 10 && level < 14)
                        {
                            threadObject.sleep(speed[10]);
                        }
                        else if(level > 13 && level < 17)
                        {
                            threadObject.sleep(speed[11]);
                        }
                        else if(level > 16 && level < 20)
                        {
                            threadObject.sleep(speed[12]);
                        }
                        else if(level > 20 && level < 30)
                        {
                            threadObject.sleep(speed[13]);
                        }
                        else
                        {
                            threadObject.sleep(speed[14]);
                        }
                        //time between frames
                    } 
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        threadObject = new Thread(runnable);
        threadObject.start();
    }
    public void actionPerformed(ActionEvent evt)
    {
        Object src = evt.getSource();
        if(src == pauseButton)
        {
            if(!paused.get())
            {
                pauseButton.setText("Resume");
                paused.set(true);
            }
            else
            {
                pauseButton.setText("Pause");
                paused.set(false);
                // Resume
                synchronized(threadObject)
                {
                    threadObject.notify();
                }
            }
        }
        else if(src == newGameButton)
        {
            finished = true;
        }
        if(finished == true)
        {
            finished = false;
            score = 0; 
            rowsCleared = 0; 
            level = 1; 
            rows = 0;
            rowsNextLevel = 5;
            for(int i = 0; i < 10; i++)
            {
                for(int j = 0; j < 21; j++)
                {
                    this.grid[i][j] = Color.black;
                }
            }
            spawnPiece(); //spawns first piece
            nextPiece();
            pauseButton.setText("Pause");
            paused.set(false);
            // Resume
            synchronized(threadObject)
            {
                threadObject.notify();
            }
            repaint();
        }
    }
    public void nextPiece()
    {
        this.nextPiece = (int)(Math.random()*7);
        this.nextRotation = (int)(Math.random()*4);
    }
    public void spawnPiece()
    {
        this.currentPiece = this.nextPiece;
        this.currentRotation = this.nextRotation;
        this.location.x = this.origin.x;
        this.location.y = this.origin.y;
    }
    private void drawPiece(Graphics g)
    {
        g.setColor(tetraminoColors[currentPiece]);
        for(Point p: Tetraminos[currentPiece][currentRotation])
        {
            g.fill3DRect((p.x + location.x)*(this.size+1), (p.y + location.y)*(this.size+1), this.size, this.size, true);
        }
    }
    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(Color.white);
        g.fillRect(0,0,600,26*24);
        
        //draw board
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 21; j++)
            {
                g.setColor(grid[i][j]);
                g.fill3DRect(26*i,26*j,25,25,true);
            }
        }
        drawPiece(g); // draws tetris piece
        
        g.setColor(Color.black);
        g.fill3DRect(300,26*4,26*6,26*6,true);
        //draw next piece
        g.setColor(tetraminoColors[nextPiece]); 
        for(Point p: Tetraminos[nextPiece][nextRotation])
        {
            g.fill3DRect((p.x)*(this.size+1) + 325, (p.y)*(this.size+1) + 26*5, this.size, this.size, true);
        }
        
        g.setColor(Color.black);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 24)); 
        g.drawString("Score: " + score, 325,26*12);
        g.drawString("Cleared: " + rows, 325,26*13);
        g.drawString("Level: " + level, 325,26*14);
        g.drawString("Goal: " + rowsNextLevel, 325,26*15);
        
        g.drawString("Next:", 325,26*3);
    }
    public static void main(String [] args)
    {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(500, 26*21);
        frame.setLocation(0, 0);
        Tetris tetris = new Tetris();
        frame.add(tetris);
        //movement
        frame.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }
            
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) 
                {
                     case KeyEvent.VK_UP:
                        tetris.rotate();
                        break;
                     case KeyEvent.VK_DOWN:
                        tetris.dropDown();
                        tetris.incrementScore(true);
                        break;
                     case KeyEvent.VK_LEFT:
                        tetris.move(-1);
                        break;
                     case KeyEvent.VK_RIGHT:
                        tetris.move(+1);
                        break;
                     case KeyEvent.VK_SPACE:
                        tetris.incrementScore(false);
                        break; //hard drop
                } 
            }
            
            public void keyReleased(KeyEvent e) {
            }
        });
    }
    private void incrementScore(boolean type)
    {
        if(type == true && gameOver() == false)
        {
            this.score++;
        }
        else if(type == false && gameOver() == false)
        {
            int num = hardDrop();
            this.score += 2*num;
        }
    }
    private void rotate()
    {
        int rotate = this.currentRotation; //temp variable
        if(rotate < 3)
        {
            rotate = this.currentRotation + 1;
        }
        else
        {
            rotate = 0;
        }
        boolean move = true;
        for(Point p: Tetraminos[currentPiece][rotate]) //checks if temp collides
        {
            if(collision(location.x, location.y, p) == true)
            {
                move = false;
            }
        }
        if(move == true)//sets new rotation
        {
            this.currentRotation = rotate;
        }
    }
    private boolean collision(int x, int y, Point p)
    {
        if(p.x + x < 0)//out of bounds
        {
            return true;
        }
        if(p.x + x > 9) //out of bounds
        {
            return true;
        }
        if((p.y + location.y) > 18) //at bottom row
        {
            return true;
        }
        if((p.y + y) >= 0 && grid[p.x + x][p.y + y] != Color.black)//collides
        {
            return true;
        }
        return false;
    }
    private void move(int i)
    {
        if(i > 0)
        {
             boolean move = true;
             for(Point p: Tetraminos[currentPiece][currentRotation])
             {
                 if(collision(location.x + 1, location.y, p) == true)
                 {
                     move = false;
                 }
             }
             if(move == true)
             {
                 location.x++;
                 repaint();
             }
        }
        else if(i < 0)
        {
            boolean move = true;
            for(Point p: Tetraminos[currentPiece][currentRotation])
            {
                if(collision(location.x - 1, location.y, p) == true)
                {
                     move = false;
                }
            }
            if(move == true)
            {
                location.x--;
                repaint();
            }
        }
    }
    public int hardDrop()
    {
        int distance = 0;
        boolean test = false;
        boolean [] colision = new boolean[4];
        int pos = 0;
        while(test == false)
        {
            for(Point p: Tetraminos[currentPiece][currentRotation])
            {
                colision[pos] = collision(location.x, location.y + 1, p);
                pos++;
            }
            for(boolean i: colision)
            {
                if(i == true)
                {
                    test = true;
                }
            }
            if(test == true)
            {
                for(Point p: Tetraminos[currentPiece][currentRotation])
                {   
                    if(p.y + location.y > -1)
                    {
                        grid[p.x + location.x][p.y + location.y] = tetraminoColors[currentPiece];
                    }
                }
                clearRow();
                boolean end = gameOver();
                if(end == true)
                {
                    paused.set(true); 
                }
                else
                {
                    spawnPiece();
                    nextPiece();
                }
            }
            else
            {
                location.y++;
                distance++;
                pos = 0; 
            }
        }
        repaint();
        return distance;
    }
    public void dropDown()
    {
        boolean test = false;
        boolean [] colision = new boolean[4];
        int pos = 0;
        for(Point p: Tetraminos[currentPiece][currentRotation])
        {
            colision[pos] = collision(location.x, location.y + 1, p);
            pos++;
        }
        for(boolean i: colision)
        {
            if(i == true)
            {
                test = true;
            }
        }
        if(test == true)
        {
            for(Point p: Tetraminos[currentPiece][currentRotation])
            {   
                if(p.y + location.y > -1)
                {
                    grid[p.x + location.x][p.y + location.y] = tetraminoColors[currentPiece];
                }
            }
            clearRow();
            boolean end = gameOver();
            if(end == true)
            {
                //game over screen
            }
            else
            {
                spawnPiece();
                nextPiece();
            }
        }
        else
        {
            location.y++;
        }
        repaint();
    }
    public boolean gameOver()
    {
        for(int c = 0; c < grid.length; c++)
        {
            if(grid[c][0] != Color.black)
            {
                return true;
            }
        }
        return false;
    }
    public void clearRow()
    {
        boolean clear;
        int rowsCleared = 0;
        for(int x = 0; x < grid[0].length; x++)
        {
            clear = false;
            for(int y = 0; y < grid.length; y++)
            {
                if(grid[y][x] == Color.black)
                {
                    clear = true;
                    break;
                }
            }
            if(!clear)
            {
                deleteRow(x);
                rowsCleared++;
                rows++;
                rowsNextLevel--;
                x--;
            }
        }
        //scoring
        /*
         * 1 - 100 * level
         * 2 - 300 * level 
         * 3 - 500 * level
         * 4 = 800 * level
         * back-to-back bonus ???
         */ 
        
        if(rowsCleared == 1)
        {
            score += 100;
        }
        if(rowsCleared == 2)
        {
            score += 300;
        }
        if(rowsCleared == 3)
        {
            score += 500;
        }
        if(rowsCleared == 4)
        {
            score += 800;
        }
        if(rowsNextLevel<=0)
        {
            level++;
            int extra = Math.abs(rowsNextLevel);
            rowsNextLevel = level*5-extra;
            
        }
    }
    public void deleteRow(int x)
    {
        for(int i = x-1; i > 0; i--)
        {
            for(int j = 0; j < 10; j++)
            {
                grid[j][i+1] = grid[j][i];
            }
        }
        repaint();
    }
}
