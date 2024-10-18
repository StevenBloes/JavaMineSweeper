import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;


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
    private boolean isFirstClick, hasWon;
    private ArrayList<Color> flowerColors;
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

    void newGame(){

        setEnabled(false);
        setSize(new Dimension(30 * columns,30 * (rows + 1)));

        neededForWin = (rows * columns) - bombs;
        flagCount = 0;
        hasWon = false;
        isFirstClick = true;

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

        if (playField != null){
            remove(playField);
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

        setEnabled(true);
        isFirstClick = true;
    }

    private void gameWon(){

        hasWon = true;
        for(ArrayList<FieldButton> row : fieldButtons){
            for(FieldButton button : row){
                button.reveal();
            }
        }

        if(JOptionPane.showConfirmDialog(this,
                "<html><b>GAME WON</b><br>Start a new game?</html>",
                "GAME WON",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE)
                == JOptionPane.OK_OPTION){
            SwingUtilities.invokeLater(this::newGame);
        }
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
            SwingUtilities.invokeLater(this::newGame);
        }
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
        easyItem.addActionListener((e) -> {
            rows = 10; columns = 10; bombs = 10;
            newGame();
            setLocationRelativeTo(null);
        });

        JRadioButtonMenuItem mediumItem = new JRadioButtonMenuItem("Medium");
        mediumItem.addActionListener((e) -> {
            rows = 16; columns = 16; bombs = 40;
            newGame();
            setLocationRelativeTo(null);
        });

        JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("Expert");
        hardItem.addActionListener((e) -> {
            rows = 16; columns = 30; bombs = 99;
            newGame();
            setLocationRelativeTo(null);
        });

        JRadioButtonMenuItem customItem = new JRadioButtonMenuItem("Aangepast");
        customItem.addActionListener((e) -> new CustomisationDialog(this));

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

    private void initObjects(){
        fieldButtons = new ArrayList<>();
        counterLbl = new JLabel("Click to start game");
        counterLbl.setHorizontalAlignment(JLabel.RIGHT);

        setLayout(new BorderLayout());
        add(counterLbl, BorderLayout.SOUTH);

        initMenu();
        newGame();

        flowerColors = new ArrayList<>();
        flowerColors.add(new Color(250,220,0));
        flowerColors.add(new Color(240,200,0));
        flowerColors.add(new Color(250,150,0));
        flowerColors.add(new Color(250,100,50));
        flowerColors.add(new Color(200,100,200));
        flowerColors.add(new Color(200,50,150));
        flowerColors.add(new Color(100,100,255));
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
                if(hasWon){
                    setText("✿");
                    Collections.shuffle(flowerColors);
                    setForeground(flowerColors.get(0));
                    setBackground(new Color(150,210,150));
                } else {
                    setText("\uD83D\uDCA3");
                    setForeground(Color.RED);
                    setBackground(new Color(250,190,180));
                }
                setBorder(BorderFactory.createLineBorder(BorderColor, 1));

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

    private class CustomisationDialog extends JDialog{

        private JLabel colLbl, rowLbl, bombLbl;
        private NumberField colField, rowField, bombField;
        private JButton doneButton, cancelButton;

        private final Window window;

        CustomisationDialog(Window window){

            super(window, "Customisation");
            this.window = window;
            window.setEnabled(false);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    window.setEnabled(true);
                }
            });

            createObjects();
            createLayout();
        }

        private void createObjects() {
            colLbl = new JLabel("Kolommen");
            colLbl.setFont(getFont().deriveFont(Font.BOLD, 13));
            rowLbl = new JLabel("Rijen");
            rowLbl.setFont(getFont().deriveFont(Font.BOLD, 13));
            bombLbl = new JLabel("Bommen");
            bombLbl.setFont(getFont().deriveFont(Font.BOLD, 13));

            colField = new NumberField(columns, 3);
            rowField = new NumberField(rows, 3);
            bombField = new NumberField(bombs, 3);

            doneButton = new JButton(" Toepassen ");
            doneButton.setFont(getFont().deriveFont(Font.BOLD, 15));
            doneButton.setBorder(BorderFactory.createEtchedBorder());
            doneButton.setBackground(new Color(150, 230, 150));
            doneButton.addActionListener((e) -> {

                if((colField.getNumber() * rowField.getNumber()) > (bombField.getNumber() + 8)){
                    columns = colField.getNumber();
                    rows = rowField.getNumber();
                    bombs = bombField.getNumber();
                    newGame();
                    window.setLocationRelativeTo(null);
                    this.dispose();
                    window.setEnabled(true);
                    window.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this, "Maximum aantal bommen overschreden!", "Bomb Overload", JOptionPane.WARNING_MESSAGE);
                }
            });

            cancelButton = new JButton(" Annuleren ");
            cancelButton.setFont(getFont().deriveFont(Font.BOLD, 15));
            cancelButton.setBorder(BorderFactory.createEtchedBorder());
            cancelButton.setBackground(new Color(250, 85, 75));
            cancelButton.addActionListener((e) -> {
                this.dispose();
                window.setEnabled(true);
                window.requestFocus();
            });
        }

        private void createLayout(){
            Container pane = getContentPane();

            GroupLayout layout = new GroupLayout(pane);
            pane.setLayout(layout);

            pane.setEnabled(false);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(colLbl)
                            .addComponent(colField))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(rowLbl)
                            .addComponent(rowField))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(bombLbl)
                            .addComponent(bombField))
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(doneButton)
                            .addComponent(cancelButton))
            );
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                            .addComponent(colLbl)
                            .addComponent(colField))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(rowLbl)
                            .addComponent(rowField))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(bombLbl).addComponent(bombField))
                    .addGap(30)
                    .addGroup(layout.createParallelGroup()
                            .addComponent(doneButton)
                            .addComponent(cancelButton))
            );

            layout.linkSize(colLbl, rowLbl, bombLbl);
            layout.linkSize(doneButton, cancelButton);

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    static class NumberField extends JTextField {

        private String value;
        private final boolean needsNumber = true;

        public NumberField(int value, int columns){
            super(String.valueOf(value), columns);
            setListeners();
        }

        public int getNumber(){
            return Integer.parseInt(getText());
        }

        private void setListeners(){

            this.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    super.focusLost(e);
                    if(!getText().equals("")){
                        try {
                            int a = Integer.parseInt(getText());
                            if(a < 1){
                                JOptionPane.showMessageDialog(null,
                                        "<html>Gelieve een positief geheel getal groter dan 0 in te geven.</html>");
                                setText(value);
                            }
                        } catch (Exception ex1){
                            JOptionPane.showMessageDialog(null,
                                    "<html>Gelieve een positief geheel getal groter dan 0 in te geven.</html>");
                            setText(value);
                        }
                    } else if (needsNumber){
                        setText(value);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    super.focusGained(e);
                    value = getText();
                }
            });
        }
    }
}
