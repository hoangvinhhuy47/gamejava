package games.core.common.money.exception;

public class MoneyLessThanZero extends Exception {
    MoneyLessThanZero(){
        super();
    }
    MoneyLessThanZero(String message){
        super(message);
    }
}
