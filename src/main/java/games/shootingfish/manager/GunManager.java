package games.shootingfish.manager;

import games.shootingfish.data.DataManager;
import games.shootingfish.datatype.HashByInt;
import games.shootingfish.object.Gun;

/**
 * Created by WINDNCC on 7/12/2017.
 */
public class GunManager {
    public static GunManager instance;

    private HashByInt<Gun> hashGun;

    private static final int DEFAULT_GUN = 5;

    //moi lan ban 1 vien dan thi duoc tang % len
    public static final float PERCENT_SUPER_ONE_BULLET = 0.1f;

    public static final int COUNT_BULLET_SUPER = 20;

    public static final float MUL_GOLD_SUPER_GUN = 2.0f;

    public  static final int COUNT_SO_NEED = 5;//can 5 cai so de nhan duoc free bullet

    public static final int MAX_MONEY_FOR_FREE_BULLET = 100000;//toi da tien cho free bullet, cai nay chia price hien tai thi duoc maxBulletFree

    public static final int MIN_MONEY_FOR_FREE_BULLET = 50000;//=>min bullet free

    public static final int TIME_BULLET_FREE = 121000;//minisecond

    public static GunManager getInstance() {
        if (instance == null) instance = new GunManager();
        return instance;
    }
    private GunManager(){
        hashGun = new HashByInt<>(30);
        DataManager.getInstance().getDataGun().forEach((gunId, gunProperties) ->
            this.hashGun.put(gunId, new Gun(gunId, gunProperties.getMultiple()))
        );
    }

    public Gun getGun(int id){
        return hashGun.getOrDefault(id, hashGun.getOrDefault(DEFAULT_GUN, null));
    }
}
