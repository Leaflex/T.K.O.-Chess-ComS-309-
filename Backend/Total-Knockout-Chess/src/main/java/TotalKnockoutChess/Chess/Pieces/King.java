package TotalKnockoutChess.Chess.Pieces;

import TotalKnockoutChess.Chess.ChessGameTile;

public class King extends ChessPiece {
    private static final long serialVersionUID = 0L;

    private boolean checked, checkMated, canCastle;

    private Coordinate coordinate;

    public King(String color, Coordinate coordinate) {
        super(color);
        checked = false;
        checkMated = false;
        canCastle = true;
        this.coordinate = coordinate;
    }

    // Getter/Setter for the coordinate of the piece
    public Coordinate getCoordinate() {
        return coordinate;
    }
    public void setCoordinate(Coordinate updatedCoordinate) {
        coordinate = updatedCoordinate;
    }

    public String calculateAvailableMoves(ChessGameTile[][] board, Coordinate currentPosition, King king) {
        String moves = "";
//        Code to add every move as an available move for testing
        for(Coordinate c : Coordinate.values()){
            moves += c.toString() + " ";
        }

        // If checkmated
        if(checkMated) { return moves; }
        // If checked
        else if(checked){

        }

        return moves;
    }

    /**
     * Method that checks for attacks on this piece. Looks for available moves for the attacks and updates this piece's fields.
     */
    public void scan(){
        String attackedFrom = "";

        // TODO Survey all possible attack points and add to attackedFrom variable, if needed update checked field

        // TODO if (checked)
        // TODO For each attack, calculate if king can move out of check "calculateAvailableMoves" OR
        //  if any friendly piece can take or block the attacker piece. Update checkMated field accordingly



    }

    @Override
    public final String toString() {
        return color + "King";
    }


    // Getter/Setter for checked field
    public boolean isChecked(){ return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }


    // Getter/Setter for checkMated field
    public boolean isCheckMated(){ return checkMated; }
    public void setCheckMated(boolean checkMated){ this.checkMated = checkMated; }


    // Getter/Setter for canCastle field
    public boolean canCastle(){ return canCastle; }
    public void setCanCastle(boolean canCastle){ this.canCastle = canCastle; }
}
