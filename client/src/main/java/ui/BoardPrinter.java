package ui;

import chess.*;
import static ui.EscapeSequences.*;

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
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR + "\n");
    }

    private static void printSquare(ChessBoard board, int row, int col) {
        if ((row + col) % 2 != 0) {
            System.out.print(SET_BG_COLOR_WHITE);
        } else {
            System.out.print(SET_BG_COLOR_BLACK);
        }

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));

        if (piece == null) {
            // Use the EMPTY constant from EscapeSequences for consistent width
            System.out.print(EMPTY);
        } else {
            printPiece(piece);
        }
    }

    private static void printPiece(ChessPiece piece) {
        // We still set a text color, but the icon itself is the main visual
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
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
        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "   ");
        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int i = 7; i >= 0; i--) System.out.print(headers[i]);
        } else {
            for (int i = 0; i < 8; i++) System.out.print(headers[i]);
        }
        System.out.print("   " + RESET_BG_COLOR + "\n");
    }
}