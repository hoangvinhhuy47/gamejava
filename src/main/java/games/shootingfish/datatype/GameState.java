package games.shootingfish.datatype;

import libs.util.CRandom;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by cuong nguyen cao on 8/15/17.
 */
public class GameState {
    public enum StateType{
        NORMAL(1, 300, 500), //300, 500
        SPEED(2, 10),
        BOSS1(3, 67),
        BOSS2(4, 105),
        BOSS3(5, 71),
        BOSS4(6, 67),
        BOSS5(7, 77),

        //game 2
        BOSS1_2(13, 67),
        BOSS3_2(15, 71),
        BOSS4_2(16, 67),
        BOSS5_2(17, 77),
        BOSS11(18, 70),

        BOSS12(19,125),
        BOSS13(20,65),
        BOSS14(21,59),
        BOSS15(22,38),
        BOSS17(24,98),
        BOSS18(25,38),
        BOSS18_1(26,38),
        BOSS18_2(27,38);

        int id = 0;
        int time = 0, maxTime = 0;
        StateType(int id, int minTime, int maxTime){
            this.id = id;
            this.time = minTime;
            this.maxTime = maxTime;
        }
        StateType(int id, int time){
            this.id = id;
            this.time = time;
        }
    }

    //bg id cua man nay
    private int bgId = 0;

    private State initState;
    private static CRandom cRandom = new CRandom();
    private State currentState;

    public GameState(int roomId){
        initState = new State(StateType.NORMAL);
        bgId = 0;

        switch (roomId) {
            case RoomType.VIP:
                State boss1_2 = new State(StateType.BOSS1_2).setNextState(initState);
                State boss4_2 = new State(StateType.BOSS4_2).setNextState(initState);
                State boss5_2 = new State(StateType.BOSS5_2).setNextState(initState);
                State boss3_2 = new State(StateType.BOSS3_2).setNextState(initState);
                State boss11 = new State(StateType.BOSS11).setNextState(initState);
                initState.setNextState(boss1_2).setNextState(boss4_2)
                        .setNextState(boss5_2).setNextState(boss3_2).setNextState(boss11);
                //initState.setNextState(boss5_2);
                break;
            case RoomType.SUPER_VIP:
                State boss12 = new State(StateType.BOSS12).setNextState(initState);
                State boss13 = new State(StateType.BOSS13).setNextState(initState);
                State boss14 = new State(StateType.BOSS14).setNextState(initState);
                State boss15 = new State(StateType.BOSS15).setNextState(initState);
                State boss17 = new State(StateType.BOSS17).setNextState(initState);
                State boss18 = new State(StateType.BOSS18).setNextState(initState);
                State boss18_1 = new State(StateType.BOSS18_1).setNextState(initState);
                State boss18_2 = new State(StateType.BOSS18_2).setNextState(initState);
                initState.setNextState(boss12).setNextState(boss13).setNextState(boss14)
                        .setNextState(boss15).setNextState(boss17).setNextState(boss18)
                        .setNextState(boss18_1).setNextState(boss18_2);
                break;
            default:
                State boss1 = new State(StateType.BOSS1).setNextState(initState);
                //State boss2 = new State(StateType.BOSS2).setNextState(initState);
                State boss4 = new State(StateType.BOSS4).setNextState(initState);
                State boss5 = new State(StateType.BOSS5).setNextState(initState);
                State boss3 = new State(StateType.BOSS3).setNextState(initState);
                initState.setNextState(boss1).setNextState(boss3).setNextState(boss4).setNextState(boss5);//.setNextState(boss2)
                //initState.setNextState(boss5);
        }

        currentState = initState;
    }

    public int getId(){
            return currentState.stateType.id;
    }

    public GameState nextState(){
        //currentState = new State(StateType.NORMAL);//;//
        //currentState = new State(StateType.BOSS1);
        currentState = currentState.getNextState();

        int newBg = 0;
        do {
            newBg = cRandom.randInt(0, 7);
        } while (newBg == bgId);
        bgId = newBg;

        if (currentState.stateType.maxTime != 0) currentState.randTimeDuring();
        return this;
    }
    public int getTimeDuring(){
        return currentState.timeDuring;
    }

    public StateType getStateType(){
        return currentState.stateType;
    }

    public void reset(){
        currentState = initState;
        currentState.initTimeDuring();
    }

    private class State{

        StateType stateType;



        State(StateType stateType){
            this.stateType = stateType;
            initTimeDuring();
        }
        Set<State> nextStates = new HashSet<>(10);
        int timeDuring;

        void initTimeDuring(){
            if (stateType.maxTime == 0) setTimeDuring(stateType.time);
            else randTimeDuring();
        }

        void randTimeDuring(){
            if (stateType.time < stateType.maxTime)
            this.timeDuring = cRandom.randInt(stateType.time, stateType.maxTime);
            else this.timeDuring = stateType.time;
        }

        void setTimeDuring(int timeDuring) {
            this.timeDuring = timeDuring;
        }
        State setNextState(State state){
            this.nextStates.add(state);
            return this;
        }

        State getNextState() {
            //Nếu số nextState > 10 thì random 1 trong số đó
            if (nextStates.size() > 1) return getNextStateRandom();
            //Ngược lại trả về thằng đầu tiền
            if (nextStates.size() == 1) return (State) nextStates.toArray()[0];
            //Nếu nextState = {} trả về thằng initState
            return initState;
        }


        public State getNextStateRandom(){
            //Random từ 0 đến số phần tử của nextStates - 1
            int rand = cRandom.randInt(0, nextStates.size() - 1);
            Iterator<State> iterator = nextStates.iterator();
            int i = 0;
            while (iterator.hasNext()){
                if (i == rand) return iterator.next();
                iterator.next();
                i++;
            }
            return initState;
        }


    }

    public int getBgId() {
        return bgId;
    }

    public void setBgId(int bgId) {
        this.bgId = bgId;
    }


    public static void main(String[] args) {
        GameState gameState = new GameState(RoomType.NORMAL);
        String s = gameState.getStateType().toString();
        while (!s.equals("BOSS2")){
            System.out.println(" == " + s);
            s = gameState.nextState().getStateType().toString();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(gameState.getStateType());
    }
}
