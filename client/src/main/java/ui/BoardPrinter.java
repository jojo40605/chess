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


}