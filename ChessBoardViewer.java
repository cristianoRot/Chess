package Chess;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChessBoardViewer extends JFrame {
    private JPanel[][] squares = new JPanel[8][8];
    private JLabel[][] pieces = new JLabel[8][8];
    private ArrayList<String> moves;

    private JPanel boardPanel;

    private static final String[][] initialPosition = {
            {"r", "n", "b", "q", "k", "b", "n", "r"},
            {"p", "p", "p", "p", "p", "p", "p", "p"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"P", "P", "P", "P", "P", "P", "P", "P"},
            {"R", "N", "B", "Q", "K", "B", "N", "R"},
    };

    public ChessBoardViewer(ArrayList<String> moves, boolean visible) {
        this.moves = moves;

        setTitle("Chess");
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        boardPanel = new JPanel(new GridLayout(8, 8));
        setupBoard();
        setInitialPieces();
        updateBoard();

        JPanel westLabels = new JPanel(new GridLayout(8, 1));
        JPanel eastLabels = new JPanel(new GridLayout(8, 1));
        JPanel northLabels = new JPanel(new GridLayout(1, 8));
        JPanel southLabels = new JPanel(new GridLayout(1, 8));

        for (int i = 0; i < 8; i++) {
            JLabel rankLabelWest = new JLabel(String.valueOf(8 - i), SwingConstants.CENTER);
            JLabel rankLabelEast = new JLabel(String.valueOf(8 - i), SwingConstants.CENTER);
            westLabels.add(rankLabelWest);
            eastLabels.add(rankLabelEast);
        }

        for (int i = 0; i < 8; i++) {
            JLabel fileLabelNorth = new JLabel(String.valueOf((char)('a' + i)), SwingConstants.CENTER);
            JLabel fileLabelSouth = new JLabel(String.valueOf((char)('a' + i)), SwingConstants.CENTER);
            northLabels.add(fileLabelNorth);
            southLabels.add(fileLabelSouth);
        }

        // Rimuovere le righe che causano l'errore:
        // add(new JPanel(), BorderLayout.NORTH_WEST);
        // add(new JPanel(), BorderLayout.NORTH_EAST);
        // add(new JPanel(), BorderLayout.SOUTH_WEST);
        // add(new JPanel(), BorderLayout.SOUTH_EAST);


        add(westLabels, BorderLayout.WEST);
        add(eastLabels, BorderLayout.EAST);
        add(northLabels, BorderLayout.NORTH);
        add(southLabels, BorderLayout.SOUTH);
        add(boardPanel, BorderLayout.CENTER);


        setVisible(visible);
    }

    private void setupBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel square = new JPanel(new BorderLayout());
                square.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                squares[row][col] = square;
                pieces[row][col] = new JLabel("", SwingConstants.CENTER);
                pieces[row][col].setFont(new Font("SansSerif", Font.BOLD, 30));
                square.add(pieces[row][col]);
                boardPanel.add(square);
            }
        }
    }

    private void setInitialPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                pieces[row][col].setText(initialPosition[row][col]);
            }
        }
    }

    public void updateBoard() {
        setInitialPieces();
        for (String move : moves) {
            if (!move.matches(".*[a-h][1-8]-[a-h][1-8]")) continue;

            String from = move.substring(move.indexOf('-') - 2, move.indexOf('-'));
            String to = move.substring(move.indexOf('-') + 1);

            int fromCol = from.charAt(0) - 'a';
            int fromRow = 8 - Character.getNumericValue(from.charAt(1));
            int toCol = to.charAt(0) - 'a';
            int toRow = 8 - Character.getNumericValue(to.charAt(1));

            String piece = pieces[fromRow][fromCol].getText();
            pieces[fromRow][fromCol].setText("");
            pieces[toRow][toCol].setText(piece);
        }
    }

    public char getPiece(int[] box)
    {
        String pieceText = this.pieces[box[1]][box[0]].getText();
        if (pieceText.isEmpty()) {
            return '\0';
        }
        return pieceText.charAt(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = pieces[row][col].getText();
                sb.append(piece.isEmpty() ? "." : piece);
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}