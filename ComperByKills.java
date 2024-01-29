import java.util.Comparator;
import java.lang.Integer;


public class ComperByKills implements Comparator<ConcretePiece> {
    private ConcretePlayer p;

    public ComperByKills(ConcretePlayer p){
        this.p = p;
    }

    @Override
    public int compare(ConcretePiece p1, ConcretePiece p2){
        Integer p1Kills = ((Pawn)p1).getKills();
        Integer p2Kills = ((Pawn)p2).getKills();

        String p1Num = p1.getName().substring(1);
        String p2Num = p2.getName().substring(1);
        Integer p1N = Integer.parseInt(p1Num);
        Integer p2N = Integer.parseInt(p2Num);

        int teamComper = 1;
        if (p1.getOwner() == this.p){
            teamComper = -1;
        }
        int killsComper = p2Kills.compareTo(p1Kills);
        int numComper = p1N.compareTo(p2N);

        if (killsComper == 0) {
            if (numComper == 0) {
                return teamComper;
            }
            return numComper;
        }
        return killsComper;
    }
}
