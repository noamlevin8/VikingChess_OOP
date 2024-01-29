import java.util.ArrayList;

public class King extends ConcretePiece{
    final private String type = "♔";
    public King(ConcretePlayer player, String type, String name, Position pos) {
        super(player, type, "K7", pos);
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
}
