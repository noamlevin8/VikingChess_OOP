import java.util.ArrayList;

public class Pawn extends ConcretePiece{
    final private String full = "♟";
    final private String empty = "♙";
    private int kills;
    public Pawn(ConcretePlayer player, String type, String name, Position pos) {

        super(player, type, name, pos);
        this.kills = 0;
    }
    @Override
    public ConcretePlayer getOwner(){
        return super.getOwner();
    }
    @Override
    public String getType(){
        return super.getType();
    }
    @Override
    public ArrayList<Position> getSteps(){return super.getSteps();}
    public void addKill(){
        this.kills++;
    }
    public int getKills(){
        return this.kills;
    }
}
