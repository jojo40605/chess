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
        System.out.print(bgColor);

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            // Exactly three spaces for an empty square
            System.out.print("   ");
        } else {
            // Ensure your printPiece helper adds a space before and after the character
            // Example: " P " instead of just "P"
            System.out.print(" ");
            printPiece(piece);
            System.out.print(" ");
        }

        System.out.print(RESET_BG_COLOR);
    }

    private static void printPiece(ChessPiece piece) {
        // High contrast colors for the brown background
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        System.out.print(color + getSymbol(piece));
    }

    private static String getSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        return switch (piece.getPieceType()) {
            case KING   -> isWhite ? "K" : "k";
            case QUEEN  -> isWhite ? "Q" : "q";
            case BISHOP -> isWhite ? "B" : "b";
            case KNIGHT -> isWhite ? "N" : "n";
            case ROOK   -> isWhite ? "R" : "r";
            case PAWN   -> isWhite ? "P" : "p";
        };
    }

    private static void printHeaders(ChessGame.TeamColor perspective) {
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "     "); // Corner spacing

        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int i = 7; i >= 0; i--){ System.out.print(headers[i]);}
        } else {
            for (int i = 0; i < 8; i++) {System.out.print(headers[i]);}
        }

        System.out.println("     " + RESET_BG_COLOR);
    }

    public static void printBoardWithHighlights
            (ChessBoard board, ChessGame.TeamColor perspective,
             ChessPosition center, Collection<ChessMove> moves) {
        Set<ChessPosition> endPositions = new HashSet<>();
        for (ChessMove move : moves) {
            endPositions.add(move.getEndPosition());
        }

        // 1. TOP HEADERS (a b c d...)
        printHeaders(perspective);

        int rowStart = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int rowEnd = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rowStep = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        int colStart = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int colEnd = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int colStep = (perspective == ChessGame.TeamColor.WHITE) ? 1 : -1;

        for (int r = rowStart; (rowStep > 0 ? r <= rowEnd : r >= rowEnd); r += rowStep) {
            // 2. LEFT SIDE LABEL (Number)
            printSideLabel(r);

            for (int c = colStart; (colStep > 0 ? c <= colEnd : c >= colEnd); c += colStep) {
                ChessPosition currentPos = new ChessPosition(r, c);

                String bgColor;
                if (currentPos.equals(center)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                } else if (endPositions.contains(currentPos)) {
                    bgColor = (r + c) % 2 == 0 ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;
                } else {
                    bgColor = (r + c) % 2 == 0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
                }

                printSquare(board, r, c, bgColor);
            }

            // 3. RIGHT SIDE LABEL (Number)
            printSideLabel(r);
            System.out.println(RESET_BG_COLOR); // End of row
        }

        // 4. BOTTOM HEADERS
        printHeaders(perspective);
    }

    // Helper to keep side labels consistent
    private static void printSideLabel(int row) {
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "  " + row + "  " + RESET_BG_COLOR);
    }
}