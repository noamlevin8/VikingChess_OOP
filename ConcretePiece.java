import java.util.ArrayList;

public abstract class ConcretePiece implements Piece{
    private ConcretePlayer player;
    private String type;
    private ArrayList<Position> steps;
    private String name;
    private Position pos;
    private int squares;

    public ConcretePiece(ConcretePlayer player, String type, String name, Position pos){
        this.player = player;
        this.type = type;
        this.steps = new ArrayList<>();
        this.name = name;
        this.pos = new Position(pos.getX(), pos.getY());
        this.steps.add(new Position(pos.getX(), pos.getY()));
        this.squares = 0;
    }
    @Override
    public ConcretePlayer getOwner(){
        return this.player;
    }
    @Override
    public String getType(){
        return this.type;
    }
    public ArrayList<Position> getSteps(){
        return this.steps;
    }
    public String getName(){
        return this.name;
    }
    public void addSquares() {
        Position s1 = this.steps.get(this.steps.size() - 1);
        Position s2 = this.steps.get(this.steps.size() - 2);
        if (s1.getX() == s2.getX()) {
            this.squares += Math.abs(s1.getY() - s2.getY());
        } else {
            this.squares += Math.abs(s1.getX() - s2.getX());
        }
    }
    public int getSquares(){
        return this.squares;
    }
}
