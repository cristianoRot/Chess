package Chess;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChessBoardViewer extends JFrame {
    private JPanel[][] squares = new JPanel[8][8];
    private JLabel[][] pieces = new JLabel[8][8];
    private ArrayList<String> moves;

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

    public ChessBoardViewer(ArrayList<String> moves) {
        this.moves = moves;

        setTitle("Chess");
        setSize(640, 640);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(8, 8));

        setupBoard();
        setInitialPieces();
        updateBoard();

        setVisible(true);
    }

    private void setupBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel square = new JPanel(new BorderLayout());
                square.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                squares[row][col] = square;
                pieces[row][col] = new JLabel("", SwingConstants.CENTER);
                square.add(pieces[row][col]);
                add(square);
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

    public char getPiece(char[] box)
    {
        return this.pieces[box[0]][box[1]].getText().charAt(0);
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
