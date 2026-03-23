package ui;

import chess.*;
import static ui.EscapeSequences.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

public class BoardPrinter {

    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        printHeaders(perspective);

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int r = 1; r <= 8; r++) {
                printRow(board, r, perspective);
            }
        } else {
            for (int r = 8; r >= 1; r--) {
                printRow(board, r, perspective);
            }
        }

        printHeaders(perspective);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
    }

    private static void printRow(ChessBoard board, int row, ChessGame.TeamColor perspective) {
        // 1. Print the left-side row label
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "  " + row + "  " + RESET_BG_COLOR);

        // 2. Loop through columns based on perspective
        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int col = 8; col >= 1; col--) {
                String bgColor = getStandardColor(row, col);
                printSquare(board, row, col, bgColor);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                String bgColor = getStandardColor(row, col);
                printSquare(board, row, col, bgColor);
            }
        }

        // 3. Print the right-side row label and newline
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "  " + row + "  " + RESET_BG_COLOR + "\n");
    }

    // Helper to keep the code clean
    private static String getStandardColor(int row, int col) {
        if ((row + col) % 2 == 0) {
            return SET_BG_COLOR_BLACK; // Or your preferred dark square color
        } else {
            return SET_BG_COLOR_WHITE; // Or your preferred light square color
        }
    }

    private static void printSquare(ChessBoard board, int row, int col, String bgColor) {
        // 1. Set the background color first
        System.out.print(bgColor);

        // 2. Get the piece and print its icon (or a space)
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            System.out.print("   "); // Empty square
        } else {
            printPiece(piece); // Your existing function that prints 'P', 'K', etc.
        }

        // 3. Reset the background so it doesn't bleed
        System.out.print(EscapeSequences.RESET_BG_COLOR);
    }

    private static void printPiece(ChessPiece piece) {
        // High contrast colors for the brown background
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
        System.out.print(color + getSymbol(piece));
    }

    private static String getSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        return switch (piece.getPieceType()) {
            case KING -> isWhite ? WHITE_KING : BLACK_KING;
            case QUEEN -> isWhite ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> isWhite ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> isWhite ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> isWhite ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    private static void printHeaders(ChessGame.TeamColor perspective) {
        // Each header is exactly 3 spaces wide to match the " ♔ " icons
        String[] headers = {" a  ", " b  ", " c ", " d  ", " e  ", "f  ", " g  ", " h  "};

        // Start with 5 spaces of grey to align with the "  row  " border
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "     ");

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int i = 7; i >= 0; i--) {System.out.print(headers[i]);}
        } else {
            for (int i = 0; i < 8; i++) {System.out.print(headers[i]);}
        }

        // End with 5 spaces of grey for the right border
        System.out.print("    " + RESET_BG_COLOR + "\n");
    }

    public static void printBoardWithHighlights(ChessBoard board, ChessGame.TeamColor perspective, ChessPosition center, Collection<ChessMove> moves) {
        // Fix "Doesn't like Set" error with this:
        Set<ChessPosition> endPositions = new HashSet<>();
        for (ChessMove move : moves) {
            endPositions.add(move.getEndPosition());
        }

        // Determine loop directions (Same as your standard printBoard)
        int rowStart = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int rowEnd = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rowStep = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        for (int r = rowStart; (rowStep > 0 ? r <= rowEnd : r >= rowEnd); r += rowStep) {
            for (int c = 1; c <= 8; c++) {
                // Fix "Doesn't like currentPos" by creating it here
                ChessPosition currentPos = new ChessPosition(r, c);

                // Calculate the Highlight Color
                String bgColor;
                if (currentPos.equals(center)) {
                    bgColor = EscapeSequences.SET_BG_COLOR_YELLOW; // The piece you selected
                } else if (endPositions.contains(currentPos)) {
                    // Alternating green shades for moves
                    bgColor = (r + c) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_DARK_GREEN : EscapeSequences.SET_BG_COLOR_GREEN;
                } else {
                    // Standard black/white checkerboard
                    bgColor = (r + c) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_BLACK : EscapeSequences.SET_BG_COLOR_WHITE;
                }

                // Fix "Doesn't like bgColor" by passing it to our updated printSquare
                printSquare(board, r, c, bgColor);
            }
            System.out.println(); // New line after each row
        }
    }
}