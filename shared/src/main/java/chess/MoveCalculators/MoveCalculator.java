package chess.MoveCalculators;

import chess.*;
import java.util.Collection;

public interface MoveCalculator {

    Collection<ChessMove> calculateMoves(
            ChessBoard board,
            ChessPosition from,
            ChessPiece piece
    );
}
