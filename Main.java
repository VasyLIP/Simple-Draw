import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.IOException;


class SimpleGraphicsEditor {
    enum FigureType {
        CIRCLE, SQUARE, PEN
    }

    JFrame frame; // Główne okno aplikacji
    JMenuBar menuBar; // Pasek menu
    JMenu fileMenu, drawMenu; // Menu "File" i "Draw"
    JMenuItem open, save, saveAs, quit, circle, square, pen, color, clear; // Elementy menu
    JFileChooser fileChooser; // Komponent do wyboru pliku
    JColorChooser colorChooser; // Komponent do wyboru koloru
    JPanel drawingArea; // Kontener do rysowania figur
    Color drawingColor; // Kolor używany do rysowania
    boolean drawingMode; // Tryb rysowania (true - rysowanie włączone, false - rysowanie wyłączone)
    FigureType currentFigure; // Aktualnie wybrana figura
    File currentFile; // Aktualnie otwarty plik
    JLabel statusBar; // Etykieta wyświetlająca stan programu
    List<Figure> savedFigures = new ArrayList<>(); // Lista zapisanych figur


    public SimpleGraphicsEditor() {
        frame = new JFrame("Simple Draw");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menuBar = new JMenuBar();

        fileChooser = new JFileChooser();
        colorChooser = new JColorChooser();

        drawingArea = new DrawingPanel();
        drawingArea.setBackground(Color.WHITE);
        drawingColor = Color.BLACK;
        drawingMode = false;
        currentFigure = FigureType.PEN;
        currentFile = null;

        initializeMenu();

        statusBar = new JLabel();
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

        frame.getContentPane().add(drawingArea, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        updateStatusBar();
    }

    private void initializeMenu() {
        initializeFileMenu(); // operacje na plikach
        initializeDrawMenu();// rysowanie figur

        menuBar.add(fileMenu);
        menuBar.add(drawMenu);
        frame.setJMenuBar(menuBar);
    }



    private void initializeFileMenu() {
        fileMenu = new JMenu("File");

        open = addMenuItem(fileMenu, "Open", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), this::openActionPerformed);
        save = addMenuItem(fileMenu, "Save", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), this::saveActionPerformed);
        saveAs = addMenuItem(fileMenu, "Save As", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), this::saveAsActionPerformed);
        quit = addMenuItem(fileMenu, "Quit", KeyEvent.VK_Q, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), this::quitActionPerformed);
    }


    private void openActionPerformed (ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    currentFile = file;
                    try {
                        Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            interpretLine(line);
                        }
                        scanner.close();
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }
                }
            }

    private void saveActionPerformed(ActionEvent e)  {
                if (currentFile != null) {
                    saveToFile(currentFile);
                } else {
                    saveAsActionPerformed(e);
                }
            }
    private void quitActionPerformed(ActionEvent e)  {
                int option = JOptionPane.showConfirmDialog(frame, "Czy chcesz zapisać zmiany przed zamknięciem?");
                // do wyboru trzy opcji
                if (option == JOptionPane.YES_OPTION) {
                    if (currentFile != null) {
                        saveToFile(currentFile);
                    } else {
                        saveAsActionPerformed(e);
                    }
                } else if (option == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                System.exit(0);
            }

    private void initializeDrawMenu() {
        drawMenu = new JMenu("Draw");
        drawMenu.setMnemonic(KeyEvent.VK_D); //mnemonik

        JMenu figureMenu = new JMenu("Figure");
        figureMenu.setMnemonic(KeyEvent.VK_F);

        circle = addMenuItem(figureMenu, "Circle", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), this::circleActionPerformed);
        square = addMenuItem(figureMenu, "Square", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), this::squareActionPerformed);
        pen = addMenuItem(figureMenu, "Pen", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), this::penActionPerformed);

        drawMenu.add(figureMenu);

        color = addMenuItem(drawMenu, "Color", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), this::colorActionPerformed);
        clear = addMenuItem(drawMenu, "Clear", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), this::clearActionPerformed);
    }


    //pomocnicza metoda
    private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setMnemonic(mnemonic);
        menuItem.setAccelerator(accelerator); //Skrót klawiaturowy
        menuItem.addActionListener(actionListener);
        menu.add(menuItem);
        return menuItem;
    }

    private void circleActionPerformed(ActionEvent e) {
        Point mousePosition = this.drawingArea.getMousePosition();

        int radius = 20;

        Color randomColor = generateRandomColor();

        CircleFigure circle = new CircleFigure(mousePosition.x-radius/2, mousePosition.y-radius/2, radius, randomColor);

        savedFigures.add(circle);
        drawingArea.repaint();
            }


    private void squareActionPerformed(ActionEvent e) {
                Point mousePosition = this.drawingArea.getMousePosition();

                int sideLength = 30;

                Color randomColor = generateRandomColor();

                SquareFigure square = new SquareFigure(mousePosition.x-sideLength/2, mousePosition.y-sideLength/2, sideLength, randomColor);


                savedFigures.add(square);
                drawingArea.repaint();
            }


    private void penActionPerformed(ActionEvent e) {
                currentFigure = FigureType.PEN;
                updateStatusBar();
            }

    private void colorActionPerformed(ActionEvent e) {
                drawingColor = JColorChooser.showDialog(frame, "Wybrany kolor", drawingColor);
            }

    private void clearActionPerformed(ActionEvent e) {
                savedFigures.clear();
                drawingArea.repaint();
            }

    private void updateStatusBar() {
        String mode = switch (currentFigure) {
            case CIRCLE -> "Circle";
            case SQUARE -> "Square";
            case PEN -> "Pen";
        };

        String fileStatus = currentFile != null ? "Modified" : "New";

        statusBar.setText("Drawing Mode: " + mode + " | File Status: " + fileStatus);
    }


    class DrawingPanel extends JPanel {
        // rysowanie figur oraz interakcja z nimi

        private Point startPoint;
        private Graphics2D g2d;

        public DrawingPanel() {
            setDoubleBuffered(true);
            //wyeliminowanie efektu migotania podczas rysowania.

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    if (e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1) {
                        deleteFigure(e.getPoint());
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (currentFigure == FigureType.PEN && startPoint != null) {
                        drawLine(startPoint.x, startPoint.y, e.getX(), e.getY());
                        startPoint = e.getPoint();
                    }
                }
            });
        }

        private void drawLine(int x1, int y1, int x2, int y2) {
            if (g2d == null) {
                g2d = (Graphics2D) getGraphics();
                g2d.setColor(drawingColor);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g2d.drawLine(x1, y1, x2, y2);
        }

        private void deleteFigure(Point point) {
            if (drawingArea.contains(point)) { // czy na panelu
                if (g2d == null) { //tak to
                    g2d = (Graphics2D) getGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                int size = 50;
                int x = point.x - size / 2;
                int y = point.y - size / 2;
                g2d.setColor(getBackground());
                g2d.fillRect(x, y, size, size);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Figure figure : savedFigures) {
                if (figure != null) {
                    figure.draw(g);
                }
            }
        }
    }


    public interface Figure {
        void draw(Graphics g);
    }

    static class CircleFigure implements Figure {
        private final int x;
        private final int y;
        private final int radius;
        private final Color color;

        public CircleFigure(int x, int y, int radius, Color color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }

        public void draw(Graphics g) {
            g.setColor(color);
            int diameter = radius * 2;
            int circleX = x - radius;
            int circleY = y - radius;
            g.fillOval(circleX, circleY, diameter, diameter);
        }
    }

    static class SquareFigure implements Figure {
        private final int x;
        private final int y;
        private final int side;
        private final Color color;

        public SquareFigure(int x, int y, int side, Color color) {
            this.x = x;
            this.y = y;
            this.side = side;
            this.color = color;
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect(x, y, side, side);
        }
    }

    private static final Random random = new Random();
    public void interpretLine(String line) {
        try {
            String[] tokens = line.split(" ");
            if (tokens.length < 3)
                return;

            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);

            switch (tokens[0]) {
                case "drawCircle" -> {
                    if (tokens.length == 4) {
                        int radius = Integer.parseInt(tokens[3]);
                        drawCircle(x, y, radius);
                    }
                }
                case "drawSquare" -> {
                    if (tokens.length == 4) {
                        int side = Integer.parseInt(tokens[3]);
                        drawSquare(x, y, side);
                    }
                }
                case "setColor" -> {
                    if (tokens.length == 4) {
                        int red = Integer.parseInt(tokens[1]);
                        int green = Integer.parseInt(tokens[2]);
                        int blue = Integer.parseInt(tokens[3]);
                        setColor(red, green, blue);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error reading number from file: " + e.getMessage());
        }
    }


    private Color generateRandomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return new Color(red, green, blue);
    }

    public void setColor(int red, int green, int blue) {
        if (red >= 0 && red <= 255 && green >= 0 && green <= 255 && blue >= 0 && blue <= 255) {
            drawingColor = new Color(red, green, blue);
        } else {
            System.err.println("Color values must be between 0 and 255");
        }
    }


    public void drawCircle(int x, int y, int radius) {
        if (radius < 0) {
            System.err.println("Invalid circle parameters: Radius must be a positive number");
            return;
        }
        Color color = generateRandomColor();
        CircleFigure circle = new CircleFigure(x, y, radius, color);
        savedFigures.add(circle);
        drawingArea.repaint();
    }
    public void drawSquare(int x, int y, int side) {
        if (side < 0) {
            System.err.println("Invalid square parameters: Side value must be a positive number");
            return;
        }
        Color color = generateRandomColor();
        SquareFigure square = new SquareFigure(x, y, side, color);
        savedFigures.add(square);
        drawingArea.repaint();
    }



    public void saveToFile(File file) {
        int width = drawingArea.getWidth();
        int height = drawingArea.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        for (Figure figure : savedFigures) {
            if (figure != null) {
                figure.draw(g2d);
            }
        }

        g2d.dispose();

        try {
            String filePath = file.getPath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                filePath += ".png";
                file = new File(filePath);
            }
            ImageIO.write(bufferedImage, "png", file); // Change to "jpg" for JPEG
            currentFile = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAsActionPerformed(ActionEvent e) {
        int returnVal = fileChooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveToFile(currentFile);
        }
    }

    public static void main(String[] args) {
        new SimpleGraphicsEditor();
    }

}
