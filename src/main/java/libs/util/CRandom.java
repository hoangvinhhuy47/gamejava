package libs.util;
import java.util.concurrent.ThreadLocalRandom;

public class CRandom {
    public int randInt(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }
    public long randLong(long min, long max){
        return ThreadLocalRandom.current().nextLong(min, max+1);
    }
    public boolean randByPercent(int percent) {
        int rand = this.randInt(0, 100);
        if (rand < percent) {
            return true;
        }
        return false;
    }
}
