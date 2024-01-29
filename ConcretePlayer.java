public class ConcretePlayer implements Player{
    private boolean playerOne;
    private int numOfWins;

    public ConcretePlayer(boolean p1){
        this.playerOne = p1;
        this.numOfWins = 0;
    }

    @Override
    public boolean isPlayerOne() {
        return this.playerOne;
    }

    @Override
    public int getWins() {
        return this.numOfWins;
    }
    public void addWin(){
        this.numOfWins++;
    }
}
