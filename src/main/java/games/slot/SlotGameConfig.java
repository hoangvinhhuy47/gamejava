package games.slot;

import games.slot.data.SlotLine;
import games.slot.data.SlotReel;
import games.slot.data.SlotReward;
import libs.util.data.HashByInt;

public class SlotGameConfig<L extends SlotLine, R extends SlotReward> {
    private HashByInt<L> lines;
    private HashByInt<R> rewards;
    private HashByInt<SlotReel> reels;
    public SlotGameConfig(HashByInt<L> lines, HashByInt<R> rewards, HashByInt<SlotReel> reels){
        this.lines = lines;
        this.rewards = rewards;
        this.reels = reels;
    }

    public HashByInt<L> getLines() {
        return lines;
    }

    public HashByInt<R> getRewards() {
        return rewards;
    }

    public HashByInt<SlotReel> getReels() {
        return reels;
    }
}
