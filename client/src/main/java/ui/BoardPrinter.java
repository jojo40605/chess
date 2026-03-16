package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class BoardPrinter {

    // Board dimensions
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        // Headers (a-h)
        printHeaders(perspective);

        // We loop through rows based on perspective
        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int r = 1; r <= 8; r++) {
                printRow(board, r, perspective);
            }
        } else {
            for (int r = 8; r >= 1; r--) {
                printRow(board, r, perspective);
            }
        }

        // Footer (a-h)
        printHeaders(perspective);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
    }

    private static void printRow(ChessBoard board, int row, ChessGame.TeamColor perspective) {
        // 1. Print Left Border (The Number)
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);

        // 2. Print Squares
        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
        }

        // 3. Print Right Border (The Number)
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR + "\n");
    }

    private static void printSquare(ChessBoard board, int row, int col) {
        // Choose BG Color: (row + col) % 2 != 0 is White in most chess setups
        if ((row + col) % 2 != 0) {
            System.out.print(SET_BG_COLOR_WHITE);
        } else {
            System.out.print(SET_BG_COLOR_BLACK);
        }

        // Get piece at this position
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));

        if (piece == null) {
            System.out.print("   "); // Exactly three standard spaces
        } else {
            printPiece(piece); // Ensure printPiece also results in 3 characters
        }
    }


    }
}