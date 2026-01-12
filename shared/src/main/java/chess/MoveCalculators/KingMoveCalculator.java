package chess.MoveCalculators;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;

public class KingMoveCalculator implements MoveCalculator {

    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    @Override
    public Collection<ChessMove> calculateMoves(
            ChessBoard board,
            ChessPosition from,
            ChessPiece piece
    ) {
        Collection<ChessMove> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            if (!MoveHelperFunction.inBounds(row, col)) {
                continue;
            }

            ChessPosition to = new ChessPosition(row, col);

            if (MoveHelperFunction.isEmpty(board, to)
                    || MoveHelperFunction.isEnemy(board, to, piece)) {
                moves.add(new ChessMove(from, to, null));
            }
        }

        return moves;
    }
}
