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
        // Border is 5 chars wide: [space][space][num][space][space]
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "  " + row + "  " + RESET_BG_COLOR);

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "  " + row + "  " + RESET_BG_COLOR + "\n");
    }

    private static void printSquare(ChessBoard board, int row, int col) {
        // Use the new Brown colors
        if ((row + col) % 2 != 0) {
            System.out.print(SET_BG_COLOR_LIGHT_BROWN);
        } else {
            System.out.print(SET_BG_COLOR_BROWN);
        }

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            printPiece(piece);
        }
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
        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};

        // Start with 5 spaces of grey to align with the "  row  " border
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "     ");

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int i = 7; i >= 0; i--) System.out.print(headers[i]);
        } else {
            for (int i = 0; i < 8; i++) System.out.print(headers[i]);
        }

        // End with 5 spaces of grey for the right border
        System.out.print("     " + RESET_BG_COLOR + "\n");
    }
}