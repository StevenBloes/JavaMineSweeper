import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class AppWindow extends JFrame {

    private ArrayList<ArrayList<FieldButton>> fieldButtons;
    private int rows = 10;
    private int columns = 10;
    private int bombs = 10;
    private int neededForWin;
    private int flagCount;
    private JLabel counterLbl;
    private JPanel playField;
    private final Color BorderColor = Color.LIGHT_GRAY;
    private final Color BackGroundColor = new Color(240,240,240);
    private boolean isFirstClick;

    private final Color[] colors = {
            new Color(255, 255, 255),
            new Color(0, 0, 255),
            new Color(0, 130, 0),
            new Color(254, 0, 0),
            new Color(0, 0, 132),
            new Color(132, 0, 0),
            new Color(0, 130, 132),
            new Color(132, 0, 132),
            new Color(119, 119, 119),
    };


    public AppWindow(){
        setTitle("Minesweeper");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        initObjects();
        setLocationRelativeTo(null);
    }

    private void initObjects(){
        fieldButtons = new ArrayList<>();
        counterLbl = new JLabel("Click to start game");
        counterLbl.setHorizontalAlignment(JLabel.RIGHT);

        setLayout(new BorderLayout());
        add(counterLbl, BorderLayout.SOUTH);

        initMenu();
        newGame();
    }

    private void newGame(){

        setSize(new Dimension(30 * columns,30 * (rows + 1)));

        neededForWin = (rows * columns) - bombs;
        flagCount = 0;

        counterLbl.setText(String.format("Cells to uncover: %s | Flags Dropped: %s  ", neededForWin, flagCount));

        fieldButtons = new ArrayList<>();

        ArrayList<Integer> bombLoc = new ArrayList<>();
        while(bombLoc.size() < bombs) {
            int loc = (int)(Math.random() * columns * rows);

            if(!bombLoc.contains(loc)){
                bombLoc.add(loc);
            }
        }

        int counter = 0;

        for (int i = 0; i < rows; ++i){
            ArrayList<FieldButton> row = new ArrayList<>();
            for (int j = 0; j < columns; ++j){
                if(bombLoc.contains(counter)){
                    row.add(new FieldButton(true, j, i));
                } else {
                    row.add(new FieldButton(false, j, i));
                }
                counter = counter + 1;
            }
            fieldButtons.add(row);
        }

        try{
            remove(playField);
        } catch (NullPointerException e){
            System.out.println("No Playfield available");
        }

        playField = new JPanel();

        GridLayout gridLayout = new GridLayout(rows, columns);
        gridLayout.setVgap(0);
        gridLayout.setHgap(0);
        playField.setLayout(gridLayout);

        for (ArrayList<FieldButton> row : fieldButtons){
            for (FieldButton button : row){
                playField.add(button);
            }
        }

        add(playField, BorderLayout.CENTER);

        revalidate();
        repaint();

        isFirstClick = true;
    }

    private void initMenu(){

        JMenuBar menuBar = new JMenuBar();
        JMenu startMenu = new JMenu("Start");
        JMenu difficultyMenu = new JMenu("Moeilijkheid");

        JMenuItem newGameItem = new JMenuItem("Nieuw");
        newGameItem.addActionListener((e)->newGame());
        JMenuItem exitGameItem = new JMenuItem("Afsluiten");
        exitGameItem.addActionListener((e)->dispose());

        JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("Makkelijk", true);
        easyItem.addActionListener((e)->{rows = 10; columns = 10; bombs = 10; newGame();});

        JRadioButtonMenuItem mediumItem = new JRadioButtonMenuItem("Medium");
        mediumItem.addActionListener((e)->{rows = 16; columns = 16; bombs = 40; newGame();});

        JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("Expert");
        hardItem.addActionListener((e)->{rows = 16; columns = 30; bombs = 99; newGame();});

        JRadioButtonMenuItem customItem = new JRadioButtonMenuItem("Aangepast");
        customItem.addActionListener((e)->{
            new CustomisationDialog(this);
        });

        ButtonGroup difficultyGroup = new ButtonGroup();
        difficultyGroup.add(easyItem);
        difficultyGroup.add(mediumItem);
        difficultyGroup.add(hardItem);
        difficultyGroup.add(customItem);

        startMenu.add(newGameItem);
        startMenu.add(exitGameItem);

        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(hardItem);
        difficultyMenu.addSeparator();
        difficultyMenu.add(customItem);

        menuBar.add(startMenu);
        menuBar.add(difficultyMenu);

        setJMenuBar(menuBar);

    }

    private void gameOver(){

        for(ArrayList<FieldButton> row : fieldButtons){
            for(FieldButton button : row){
                button.reveal();
            }
        }

        if(JOptionPane.showConfirmDialog(this,
                "<html><b>GAME OVER</b><br>Start a new game?</html>",
                "GAME OVER",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE)
                == JOptionPane.OK_OPTION){
            newGame();
        }
    }

    private void gameWon(){
        if(JOptionPane.showConfirmDialog(this,
                "<html><b>GAME WON</b><br>Start a new game?</html>",
                "GAME WON",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE)
                == JOptionPane.OK_OPTION){
            newGame();
        }
    }

    private class FieldButton extends JButton {

        boolean isBomb;
        boolean isFlagged = false;
        boolean isActive = true;
        int row, column;

        FieldButton(boolean isBomb, int x, int y){

            super(" ");
            this.isBomb = isBomb;
            this.column = x;
            this.row = y;

            setFocusPainted(false);
            setMargin(new Insets(0,0,0,0));
            setFont(getFont().deriveFont(Font.BOLD, 20));

            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    if(e.getButton()==MouseEvent.BUTTON1){
                        // ensure the first click is always an open space (surrounding bomb count = 0), if not relocate bombs
                        if(isFirstClick){
                            if(getSurroundingBombs() != 0){

                                // list of places where no bombs may occur
                                ArrayList<Integer> excludeLoc = new ArrayList<>();
                                int current = row * columns + column + 1;
                                excludeLoc.add(current);
                                excludeLoc.add(current-1);
                                excludeLoc.add(current+1);
                                excludeLoc.add(current - columns);
                                excludeLoc.add(current - columns - 1);
                                excludeLoc.add(current - columns + 1);
                                excludeLoc.add(current + columns);
                                excludeLoc.add(current + columns - 1);
                                excludeLoc.add(current + columns + 1);

                                // creates bomb locations an add to list
                                ArrayList<Integer> bombLoc = new ArrayList<>();
                                while(bombLoc.size() < bombs) {
                                    int loc = (int)(Math.random() * columns * rows);

                                    if(!bombLoc.contains(loc) && !excludeLoc.contains(loc+1)){
                                        bombLoc.add(loc);
                                    }
                                }

                                // allocate the bombs to their respective fields
                                int counter = 0;

                                for(ArrayList<FieldButton> rows : fieldButtons){
                                    for(FieldButton field : rows){
                                        field.setBomb(bombLoc.contains(counter));
                                        counter = counter + 1;
                                    }
                                }
                            }
                            isFirstClick = false;
                        }

                        if(!getText().equals("\uD83C\uDFF3")){
                            if(isActive){
                                activated();
                            } else {
                                if(getSurroundingBombs() == getSurroundingFlags()){
                                    showSurroundingTiles();
                                } else {
                                    System.out.println("Bombs: " + getSurroundingBombs() + ", Flags: " + getSurroundingFlags());
                                }
                            }
                        }
                    } else if (e.getButton()==MouseEvent.BUTTON3){
                        if(isFirstClick){
                            isFirstClick = false;
                        }
                        if(isActive){
                            if(getText().equals("\uD83C\uDFF3")){
                                isFlagged = false;
                                flagCount = flagCount - 1;
                                setText("");
                            } else {
                                isFlagged = true;
                                flagCount = flagCount + 1;
                                setText("\uD83C\uDFF3");
                            }
                        }
                    }
                    counterLbl.setText(String.format("cells to uncover: %s | Flags Dropped: %s  ",neededForWin, flagCount));
                    counterLbl.repaint();
                }
            });
        }

        void setBomb(boolean isBomb){
            this.isBomb = isBomb;
        }

        void activated(){
            if(isActive && !isFlagged){
                if(isBomb){
                    setText("x");
                    setForeground(Color.RED);
                    setBorder(BorderFactory.createLineBorder(BorderColor, 1));
                    setBackground(new Color(250,190,180));
                    isActive = false;
                    gameOver();
                } else {
                    int bombs = getSurroundingBombs();
                    if(bombs == 0){
                        setForeground(colors[bombs]);
                        setBorder(BorderFactory.createLineBorder(BorderColor, 1));
                        setBackground(BackGroundColor);
                        isActive = false;
                        showSurroundingTiles();

                    } else {
                        setText(String.valueOf(bombs));
                        setForeground(colors[bombs]);
                        setBorder(BorderFactory.createLineBorder(BorderColor, 1));
                        setBackground(BackGroundColor);
                        isActive = false;
                    }
                    neededForWin = neededForWin - 1;

                    if(neededForWin == 0){
                        gameWon();
                    }
                }
            }
        }

        void reveal(){
            isActive = false;
            if(isBomb){
                setText("\uD83D\uDCA3");
                setForeground(Color.RED);
                setBorder(BorderFactory.createLineBorder(BorderColor, 1));
                setBackground(new Color(250,190,180));
            } else {
                int bombs = getSurroundingBombs();
                if (bombs != 0) {
                    setText(String.valueOf(bombs));
                }
                setForeground(colors[bombs]);
                setBorder(BorderFactory.createLineBorder(BorderColor, 1));
                setBackground(BackGroundColor);
            }
        }

        void showSurroundingTiles(){
            try{
                fieldButtons.get(row+1).get(column-1).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row+1).get(column).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row+1).get(column+1).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row).get(column-1).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row).get(column+1).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row-1).get(column-1).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row-1).get(column).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                fieldButtons.get(row-1).get(column+1).activated();
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }
        }

        int getSurroundingBombs(){

            int surroundingBombs = 0;

            try{
                if(fieldButtons.get(row+1).get(column-1).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row+1).get(column).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row+1).get(column+1).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row).get(column-1).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row).get(column+1).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row-1).get(column-1).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row-1).get(column).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row-1).get(column+1).isBomb){
                    surroundingBombs = surroundingBombs + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            return surroundingBombs;
        }

        int getSurroundingFlags(){

            int surroundingFlags = 0;

            try{
                if(fieldButtons.get(row+1).get(column-1).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row+1).get(column).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row+1).get(column+1).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row).get(column-1).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row).get(column+1).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row-1).get(column-1).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row-1).get(column).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            try{
                if(fieldButtons.get(row-1).get(column+1).isFlagged){
                    surroundingFlags = surroundingFlags + 1;
                }
            } catch (IndexOutOfBoundsException exception){
                System.out.print("");
            }

            return surroundingFlags;
        }
    }

    private static class CustomisationDialog extends JDialog{

        CustomisationDialog(Window window){
            super(window, "Customisation");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JLabel colLbl = new JLabel("Kolommen");
            JLabel rowLbl = new JLabel("Rijen");
            JLabel bombLbl = new JLabel("Bommen");

            Container pane = getContentPane();

            GroupLayout layout = new GroupLayout(pane);
            pane.setLayout(layout);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(colLbl))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(rowLbl))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(bombLbl))
            );
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                            .addComponent(colLbl))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(rowLbl))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(bombLbl)));

            layout.linkSize(colLbl, rowLbl, bombLbl);

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

    }

    public static void main(String[] args) {
        try{
            EventQueue.invokeAndWait(AppWindow::new);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}