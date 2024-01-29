import java.util.Comparator;

public class ComperByVisits implements Comparator<Position> {
    @Override
    public int compare(Position p1, Position p2){
        Integer p1Visits = p1.getVisits();
        Integer p2Visits = p2.getVisits();

        int visitsComper = p2Visits.compareTo(p1Visits);
        int xComper = ((Integer) p1.getX()).compareTo(((Integer) p2.getX()));
        int yComper = ((Integer) p1.getY()).compareTo(((Integer) p2.getY()));

        if (visitsComper == 0) {
            if (xComper == 0) {
                return yComper;
            }
            return xComper;
        }
        return visitsComper;
    }
}
