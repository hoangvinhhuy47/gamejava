package games.api;

public enum ApiAction {
    GET_CCU("ccu"),
    ABC("abc");

    String action;
    ApiAction(String action){
        this.action = action;
    }

}
