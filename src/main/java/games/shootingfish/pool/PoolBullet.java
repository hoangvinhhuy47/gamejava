package games.shootingfish.pool;

import games.shootingfish.object.Bullet;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by WINDNCC on 7/12/2017.
 */
public class PoolBullet extends PoolObject<Bullet> {
    public PoolBullet(){
        //Init pool
        dataQueue = new LinkedBlockingQueue<>();
        outPool = new LinkedBlockingQueue<>();
        maxId = 50;
        for (int i = 1; i<=maxId; i++){
            dataQueue.add(new Bullet(i));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PoolBullet poolBullet = new PoolBullet();
        HashMap<Integer, Bullet> bulletHashMap = new HashMap<>();
        for (int i = 1; i<40; i++){
            Bullet poolBulletf = poolBullet.shiftBullet();
            poolBulletf.setPlayerId(i);
            bulletHashMap.put(poolBulletf.getId(), poolBulletf);
        }
long s = System.nanoTime();
        Collection<Bullet> ss = bulletHashMap.values();
            for (Bullet bullet : ss){
                if (bullet.getPlayerId() == 40){
                    System.out.println(bullet.getPlayerId());
                    System.out.println("hehe");
                }
            }
        long e = System.nanoTime();
        System.out.println(e-s);
    }
    public Bullet shiftBullet() throws InterruptedException {
//        System.out.println("PoolBullet.shiftBullet - out = " + outPool.size() + " - queue = " + dataQueue.size());
        if (dataQueue.isEmpty()) return createBullet();
        return shiftSync();
    }
    private Bullet createBullet() throws InterruptedException {
        maxId++;
        Bullet bullet =  new Bullet(maxId);
        outPool.offer(bullet, 100, TimeUnit.MILLISECONDS);
        return bullet;
    }

    public boolean putBullet(Bullet bullet){
        try {
//            System.out.println("PoolBullet.putBullet - out = " + outPool.size() + " - queue = " + dataQueue.size());
            return putSync(bullet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    Bullet shiftSync() throws InterruptedException {
        Bullet object = dataQueue.poll(100, TimeUnit.MILLISECONDS);
        return object;
    }

    @Override
    boolean putSync(Bullet object) throws InterruptedException {
        dataQueue.offer(object, 100, TimeUnit.MILLISECONDS);
        return true;
    }

}
