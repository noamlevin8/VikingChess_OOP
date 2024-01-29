import java.util.Comparator;

public class ComperBySquares implements Comparator<ConcretePiece> {
    private ConcretePlayer p;

    public ComperBySquares(ConcretePlayer p){
        this.p = p;
    }

    @Override
    public int compare(ConcretePiece p1, ConcretePiece p2){
        Integer p1Squares = p1.getSquares();
        Integer p2Squares = p2.getSquares();

        String p1Num = p1.getName().substring(1);
        String p2Num = p2.getName().substring(1);
        Integer p1N = Integer.parseInt(p1Num);
        Integer p2N = Integer.parseInt(p2Num);

        int teamComper = 1;
        if (p1.getOwner() == this.p){
            teamComper = -1;
        }
        int squaresComper = p2Squares.compareTo(p1Squares);
        int numComper = p1N.compareTo(p2N);

        if (squaresComper == 0) {
            if (numComper == 0) {
                return teamComper;
            }
            return numComper;
        }
        return squaresComper;
    }
}
