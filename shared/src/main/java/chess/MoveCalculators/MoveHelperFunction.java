package chess.MoveCalculators;

import chess.*;

public final class MoveHelperFunction {

    private MoveHelperFunction() {}

    public static boolean inBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public static boolean isEmpty(ChessBoard board, ChessPosition pos) {
        return board.getPiece(pos) == null;
    }

    public static boolean isEnemy(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        ChessPiece target = board.getPiece(pos);
        return target != null && target.getTeamColor() != piece.getTeamColor();
    }
}