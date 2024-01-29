public class Position {
    private int x;
    private int y;
    private int visits;

    public Position(int x, int y){
        this.x = x;
        this.y = y;
        this.visits = 0;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    public int getVisits(){
        return this.visits;
    }
    public void addVisit(){
        this.visits++;
    }
}
