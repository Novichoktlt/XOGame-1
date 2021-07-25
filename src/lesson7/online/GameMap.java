package lesson7.online;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class GameMap extends JPanel {

    private static final int EMPTY_DOT = 0;
    //todo enum?
    private static final int HUMAN_DOT = 1;
    //todo player2?
    private static final int AI_DOT = 2;

    //todo enum?
    private static final int STATE_DRAW = 0;
    private static final int VERTICAL_WIN = 1;
    private static final int HORIZONTAL_WIN = 2;
    private static final int DIAGONAL_WIN = 3;
    private static final int REVERSE_DIAGONAL_WIN = 4;
    private int stateWin; //todo initialize in start

    private int currentStateGameOver;

    public final Random RANDOM = new Random();
    //todo not the most clear names, make enum
    public static final int GAME_MODE_HVH = 0;
    public static final int GAME_MODE_HVA = 1;
    public static int turnsCount = 0; //todo naming??

    private int gameMode;
    private int fieldSizeX;
    private int fieldSizeY;
    private int winLength;
    private int[][] field;
    private int cellWidth;
    private int cellHeight;
    private int dotI; //todo naming??
    private int dotJ; //todo naming??
    //todo enum
    private boolean isGameOver;
    private boolean isGameStarted;

    GameMap() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                update(e);
            }
        });
        isGameStarted = false;
    }

    void start(int gameMode, int fieldSizeX, int fieldSizeY, int winLength, Color colorMap) {
        this.gameMode = gameMode;
        this.fieldSizeX = fieldSizeX;
        this.fieldSizeY = fieldSizeY;
        this.winLength = winLength;
        setBackground(colorMap); //todo naming??
        turnsCount = 0; //todo why is it static??
        field = new int[fieldSizeX][fieldSizeY];
        isGameOver = false;
        isGameStarted = true;
        repaint();
    }

    private void update(MouseEvent e) {
        if (!isGameStarted) return;
        if (isGameOver) return; //todo move to upper if
        int cellX = e.getX() / cellWidth;
        int cellY = e.getY() / cellHeight;
        System.out.println("X: " + cellX + ", Y:" + cellY); //todo use logger
        if (isValidCell(cellX, cellY) || !isEmptyCell(cellX, cellY)) {
            return;
        }
        //todo refactor ifs and use ternary operator (see original version commented out)
        if (gameMode == GAME_MODE_HVH) {
            player(cellY, cellX, turnsCount % 2 == 0 ? AI_DOT : HUMAN_DOT);
        }
        else {
            aiTurn();
            repaint(); //todo just have one repaint at the end of the method.
            if (checkWin(AI_DOT)) {
                setGameOver(stateWin);
                return; //todo replace with "else" below
            }
            if (isFullMap()) {
                setGameOver(STATE_DRAW);
            }
        }

//        if (gameMode == GAME_MODE_HVH && turnsCount % 2 == 0) {
//            player(cellY, cellX, AI_DOT);
//        } else {
//            player(cellY, cellX, HUMAN_DOT);
//        }
//        if (gameMode == GAME_MODE_HVA && !isGameOver) {
//            aiTurn();
//            repaint();
//            if (checkWin(AI_DOT)) {
//                setGameOver(stateWin);
//                return;
//            }
//            if (isFullMap()) {
//                setGameOver(STATE_DRAW);
//            }
//        }
    }

    private void setGameOver(int gameOverState) {
        currentStateGameOver = gameOverState;
        isGameOver = true;
        repaint();
    }
    //todo better name
    private void player(int cellY, int cellX, int characterSymbol) {
        field[cellY][cellX] = characterSymbol;
        if (checkWin(characterSymbol)) {
            setGameOver(stateWin);
            return;
        }
        if (isFullMap()) {
            setGameOver(STATE_DRAW);
            return;
        }
        repaint(); //todo I guess we will always need repaint in update().
    }

    private void render(Graphics g) throws IOException {
        if (!isGameStarted) return;
        int width = getWidth();
        int height = getHeight();
        cellWidth = width / fieldSizeX; //todo should be init in constructor if size is static
        cellHeight = height / fieldSizeY;
        //todo extract method
        g.setColor(Color.BLACK);
        for (int i = 1; i < fieldSizeY; i++) {
            int y = i * cellHeight;
            g.drawLine(0, y, width, y);
        }
        for (int i = 1; i < fieldSizeX; i++) {
            int x = i * cellWidth;
            g.drawLine(x, 0, x, height);
        }
        //todo move to constants.
        Image cross = ImageIO.read(Objects.requireNonNull(GameMap.class.getResourceAsStream("resources//cross.png")));
        Image zero = ImageIO.read(Objects.requireNonNull(GameMap.class.getResourceAsStream("resources//zero.png")));
        //todo extract method
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (isEmptyCell(x, y)) { //todo enum+switch
                    continue;
                }
                if (field[y][x] == HUMAN_DOT) {
                    g.drawImage(cross, x * cellWidth + 10, y * cellHeight + 10, cellWidth - 20, cellHeight - 20, null);
                } else if (field[y][x] == AI_DOT) {
                    g.drawImage(zero, x * cellWidth + 10, y * cellHeight + 10, cellWidth - 20, cellHeight - 20, null);
                } else {
                    throw new RuntimeException("Ошибка при отрисовке X: " + x + " Y: " + y);
                }
            }
        }
        turnsCount++; //todo VERY IMPLICIT AND FRAGILE LOGIC!!!! BE EXPLICIT!
        if (isGameOver) {
            showGameOverState(g);
        }
    }

    private void showGameOverState(Graphics g) throws IOException {
        switch (currentStateGameOver) {
            case HORIZONTAL_WIN -> {
                Image HORIZONTAL = ImageIO.read(Objects.requireNonNull(GameMap.class.getResourceAsStream("resources//HORIZONTAL.png")));
                g.drawImage(HORIZONTAL, dotI * cellWidth - 30, dotJ * cellHeight + cellHeight / 2 - cellHeight / 8, cellWidth * winLength + 60, cellHeight / 4, null);
            }
            case VERTICAL_WIN -> {
                Image VERTICAL = ImageIO.read(Objects.requireNonNull(GameMap.class.getResourceAsStream("resources//VERTICAL.png")));
                g.drawImage(VERTICAL, dotI * cellWidth + cellWidth / 2 - cellWidth / 8, dotJ * cellHeight - 30, cellWidth / 4, cellHeight * winLength + 60, null);
            }
            case DIAGONAL_WIN -> {
                Image DIAGONAL = ImageIO.read(Objects.requireNonNull(GameMap.class.getResourceAsStream("resources//DIAGONAL.png")));
                for (int s = 0; s < winLength; s++) //todo better use brackets.
                    g.drawImage(DIAGONAL, (dotI + s) * cellWidth - 15, (dotJ + s) * cellHeight - 15, cellWidth + 15, cellHeight + 15, null);
            }
            case REVERSE_DIAGONAL_WIN -> {
                Image REVERSE_DIAGONAL = ImageIO.read(Objects.requireNonNull(GameMap.class.getResourceAsStream("resources//REVERSE_DIAGONAL.png")));
                for (int s = 0; s < winLength; s++)
                    g.drawImage(REVERSE_DIAGONAL, (dotI + s) * cellWidth - 15, (dotJ - s) * cellHeight - 15, cellWidth + 15, cellHeight + 15, null);
            }
            case STATE_DRAW -> {
                g.setColor(Color.WHITE);
                g.fillRect(120, 170, 250, 100);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Times New Roman", Font.ITALIC, 70));
                g.drawString("Ничья!", 150, getHeight() / 2);
            }
            default -> throw new RuntimeException("Непредвиденная ошибка: " + currentStateGameOver);
        }
    }

    private void aiTurn() {
        //todo methods can be merged in one
        if (turnAIWinCell()) {
            return;
        }
        if (turnHumanWinCell()) {
            return;
        }
        int x;
        int y;
        do { //todo replace with a more efficient algo
            x = RANDOM.nextInt(fieldSizeX);
            y = RANDOM.nextInt(fieldSizeY);
        } while (!isEmptyCell(x, y));
        field[y][x] = AI_DOT;
    }

    private boolean turnAIWinCell() {
        for (int i = 0; i < fieldSizeY; i++) {
            for (int j = 0; j < fieldSizeX; j++) {
                if (isEmptyCell(j, i)) {
                    field[i][j] = AI_DOT;
                    if (checkWin(AI_DOT)) {
                        return true;
                    }
                    field[i][j] = EMPTY_DOT;
                }
            }
        }
        return false;
    }

    private boolean turnHumanWinCell() {
        for (int i = 0; i < fieldSizeY; i++) {
            for (int j = 0; j < fieldSizeX; j++) {
                if (isEmptyCell(j, i)) {
                    field[i][j] = HUMAN_DOT;
                    if (checkWin(HUMAN_DOT)) {
                        field[i][j] = AI_DOT;
                        return true;
                    }
                    field[i][j] = EMPTY_DOT;
                }
            }
        }
        return false;
    }

    private boolean checkWin(int characterSymbol) {
        //todo better use hor/ver instead of x/y
        for (int i = 0; i < fieldSizeX; i++) {
            for (int j = 0; j < fieldSizeY; j++) {
                if (checkLine(i, j, 1, 0, winLength, characterSymbol)) {
                    stateWin = HORIZONTAL_WIN;
                    dotI = i; //todo get rid of copy paste
                    dotJ = j;
                    return true;
                }
                if (checkLine(i, j, 0, 1, winLength, characterSymbol)) {
                    stateWin = VERTICAL_WIN;
                    dotI = i;
                    dotJ = j;
                    return true;
                }
                if (checkLine(i, j, 1, 1, winLength, characterSymbol)) {
                    stateWin = DIAGONAL_WIN;
                    dotI = i;
                    dotJ = j;
                    return true;
                }
                if (checkLine(i, j, 1, -1, winLength, characterSymbol)) {
                    stateWin = REVERSE_DIAGONAL_WIN;
                    dotI = i;
                    dotJ = j;
                    return true;
                }
            }
        }
        return false;
    }

    //todo better parameters and method name
    private boolean checkLine(int x, int y, int vx, int vy, int len, int characterSymbol) {
        final int farX = x + (len - 1) * vx;
        final int farY = y + (len - 1) * vy;
        if (isValidCell(farX, farY)) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (field[y + i * vy][x + i * vx] != characterSymbol) {
                return false;
            }
        }
        return true;
    }
    //todo can be simply replaced with turns count.
    private boolean isFullMap() {
        for (int i = 0; i < fieldSizeX; i++) {
            for (int j = 0; j < fieldSizeY; j++) {
                if (field[i][j] == EMPTY_DOT) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidCell(int x, int y) {
        return x < 0 || x >= fieldSizeX || y < 0 || y >= fieldSizeY;
    }

    private boolean isEmptyCell(int x, int y) {
        return field[y][x] == EMPTY_DOT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            render(g);
        } catch (IOException e) { //todo why are you catching this one?
            e.printStackTrace();
        }
    }
}
