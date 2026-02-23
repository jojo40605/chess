package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoveCalculator implements MoveCalculator {

    private static final int[][] DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {0, 1}, {-1, 0}, {0, -1}
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition from, ChessPiece piece) {
        Collection<ChessMove> moves = new ArrayList<>();
        MoveHelperFunction.addSlidingMoves(moves, board, from, piece, DIRECTIONS);
        return moves;
    }
}