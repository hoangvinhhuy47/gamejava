package games.tx.TXMongoManager;

import games.tx.TaiXiuGame;
import org.bson.Document;

import java.util.ArrayList;

public class GetDataFromMongo {

    private String id;
    private int FinalResult;
    private int Dice1;
    private int Dice2;
    private int Dice3;
    private boolean tai_xiu;

    private int FinalResultForGraph3;
    private int FinalResultForGraph4;

    public String getId() {
        return this.id;
    }

    public int getFinalResult() {
        return this.FinalResult;
    }

    public int getDice1() {
        return this.Dice1;
    }

    public int getDice2() {
        return this.Dice2;
    }

    public int getDice3() {
        return this.Dice3;
    }

    public int getFinalResultForGraph3() {
        return FinalResultForGraph3;
    }

    public int getFinalResultForGraph4() {
        return this.FinalResultForGraph4;
    }

    public void GetDataFromMongoDB_one()
    {
        ConnectMongoDB connect = new ConnectMongoDB();
        connect.EstablishConnectionToMongoDB();

        ArrayList<Document> documents = (ArrayList<Document>) connect.getCollection().find().into(new ArrayList<Document>());
        for (Document doc : documents) {
            if (Integer.parseInt(((String) doc.get("_id")).substring(1)) == connect.getCollection().count()) {
                this.id = (String) doc.get("_id");
                this.FinalResult = (int) doc.get("final");
                this.Dice1 = (int) doc.get("dice1");
                this.Dice2 = (int) doc.get("dice2");
                this.Dice3 = (int) doc.get("dice3");
                this.tai_xiu = (boolean) doc.get("tai_xiu");
            }
        }
    }

//    public void GetDataFromMongoDB ()
//    {
//        ConnectMongoDB connect = new ConnectMongoDB();
//        connect.EstablishConnectionToMongoDB();
//        ArrayList<Document> documents = (ArrayList<Document>) connect.getCollection().find().into(new ArrayList<Document>());
//        for (Document doc : documents) {
//            for (int i = 17; i > -1; i--) {
//                if (Integer.parseInt(((String)doc.get("_id")).substring(1))  == connect.getCollection().count() - i) {
//                    this.id = (String)doc.get("_id");
//                    this.FinalResult = (int)doc.get("final");
//                    this.Dice1 = (int)doc.get("dice1");
//                    this.Dice2 = (int)doc.get("dice2");
//                    this.Dice3 = (int)doc.get("dice3");
//                    this.tai_xiu = (boolean)doc.get("tai_xiu");
//                    System.out.println(id + " " + FinalResult + " " + Dice1 + " " + Dice2 + " " + Dice3 + " " + tai_xiu);
//                    // -------------------------
//                    TaiXiuGame.instance.FinalResult.add(FinalResult);
//                    TaiXiuGame.instance.Dice1Result.add(Dice1);
//                    TaiXiuGame.instance.Dice2Result.add(Dice2);
//                    TaiXiuGame.instance.Dice3Result.add(Dice3);
//                    TaiXiuGame.instance.SumTaiXIu.add(tai_xiu);
//                    this.FinalResultForGraph3 = (int)doc.get("final");
//                    TaiXiuGame.instance.FinalResultGrapth3.add(FinalResultForGraph3);
//                    this.FinalResultForGraph4 = (int)doc.get("final");
//                    TaiXiuGame.instance.FinalResultGrapth4.add(FinalResultForGraph4);
//                }
//            }
//            for (int i = 79; i > 17; i--){
//                if (Integer.parseInt(((String)doc.get("_id")).substring(1))  == connect.getCollection().count() - i){
//                    this.FinalResultForGraph3 = (int)doc.get("final");
//                    TaiXiuGame.instance.FinalResultGrapth3.add(FinalResultForGraph3);
//                    this.FinalResultForGraph4 = (int)doc.get("final");
//                    TaiXiuGame.instance.FinalResultGrapth4.add(FinalResultForGraph4);
//                }
//            }
//            for (int i = 119; i > 79; i--){
//                if (Integer.parseInt(((String)doc.get("_id")).substring(1))  == connect.getCollection().count() - i){
//                    this.FinalResultForGraph4 = (int)doc.get("final");
//                    TaiXiuGame.instance.FinalResultGrapth4.add(FinalResultForGraph4);
//                }
//            }
//        }
//    }
}
