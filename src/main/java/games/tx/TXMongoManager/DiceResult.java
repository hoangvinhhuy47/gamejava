package games.tx.TXMongoManager;

public class DiceResult {
    private String id;

    private int FinalResult;
    private int Dice1;
    private int Dice2;
    private int Dice3;
    private boolean tai_xiu;
    // -----------------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    // ----------------------------
    public int getFinalResult() {
        return FinalResult;
    }

    public int getDice1() {
        return Dice1;
    }

    public int getDice2() {
        return Dice2;
    }

    public int getDice3() {
        return Dice3;
    }

    public boolean gettai_xiu(){
        return tai_xiu;
    }

    public void setFinalResult(int finalResult) {
        FinalResult = finalResult;
    }

    public void setDice1(int dice1) {
        Dice1 = dice1;
    }

    public void setDice2(int dice2) {
        Dice2 = dice2;
    }

    public void setDice3(int dice3) {
        Dice3 = dice3;
    }

    public void setTai_xiu(boolean tai_xiu) {
        this.tai_xiu = tai_xiu;
    }
}

