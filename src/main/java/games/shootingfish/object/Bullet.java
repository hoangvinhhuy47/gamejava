package games.shootingfish.object;

import message.FishShootingProtos.BulletResponseBean;
import message.IResponseBean;

/**
 * Created by WINDNCC on 7/11/2017.
 */
public class Bullet extends GameObject implements IResponseBean<BulletResponseBean.Builder> {
    private float posX; // tọa độ x
    private float posY;// tọa độ y
    private float angle; //góc bắn
    private int gunId; //Loại súng bắn ra
    private int playerId; //Đạn được bắn bởi thằng nào


    private boolean isSuperBUllet = false;
    public Bullet(){

    }
    public Bullet(int id){
        this.id = id;
    }
    public Bullet(int id, int posX, int posY,int angle, int gunId, int playerId){
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.angle = angle;
        this.gunId = gunId;
        this.playerId = playerId;
    }

    public float getAngle() {
        return angle;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public int getGunId() {
        return gunId;
    }

    public void setGunId(int gunId) {
        this.gunId = gunId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public boolean isSuperBUllet() {
        return isSuperBUllet;
    }

    public void setSuperBUllet(boolean superBUllet) {
        isSuperBUllet = superBUllet;
    }

    @Override
    public String toString() {
        return String.format("Bullet Id: %s", this.getId());
    }

    @Override
    public BulletResponseBean.Builder parseResponseBeanBuilder() {
        BulletResponseBean.Builder responseBeanBuilder = BulletResponseBean.newBuilder();
        responseBeanBuilder.setBulletId(this.id)
                .setGunId(this.gunId)
                .setPlayerId(this.playerId)
                .setPosX(this.posX)
                .setPosY(this.posY)
                .setAngle(this.angle).setIsSuper(isSuperBUllet);
        return responseBeanBuilder;
    }
}
