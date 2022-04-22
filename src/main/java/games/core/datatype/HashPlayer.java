package games.core.datatype;

import libs.util.data.HashByIntSync;


public class HashPlayer<P extends BasePlayer> extends HashByIntSync<P> {

    @Override
    public P put(P player) {
        return super.put(player.getId(), player);
    }

    public P remove(P player) {
        return super.remove(player.getId());
    }

    public boolean isExist(P player){
        return containsKey(player.getId()) && containsValue(player);
    }
}
