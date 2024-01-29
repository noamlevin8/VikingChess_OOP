import java.util.Comparator;

public class ComperBySteps implements Comparator<ConcretePiece> {

    @Override
    public int compare(ConcretePiece p1, ConcretePiece p2){
        if (p1.getSteps().size() < p2.getSteps().size()) {
            return -1;
        }
        if (p1.getSteps().size() == p2.getSteps().size()){
            String p1Num = p1.getName().substring(1);
            String p2Num = p2.getName().substring(1);
            Integer p1N = Integer.parseInt(p1Num);
            Integer p2N = Integer.parseInt(p2Num);
            int numComper = p1N.compareTo(p2N);
            return numComper;
        }

        return 1;
    }
}
