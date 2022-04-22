package games.Poker.CARDS;

import message.PokerProto;

public class PokerCard {
    public String cardnumber;
    public String cardtype;

    @Override // Override để dùng ArrayList.remove(Obj)
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof PokerCard)) return false;
        if (obj == this) return true;
        PokerCard o = (PokerCard) obj;
        return o.cardnumber == this.cardnumber && o.cardtype == this.cardtype;
    }

    public PokerCard () {
        cardnumber = "";
        cardtype = "";
    }
    public PokerCard (String cardnumber, String cardtype){
        this.cardnumber = cardnumber;
        this.cardtype = cardtype;
    }
    public String getCardnumber() {
        return cardnumber;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }
}
