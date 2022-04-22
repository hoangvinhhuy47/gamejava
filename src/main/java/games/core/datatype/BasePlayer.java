package games.core.datatype;


import games.core.application.handler.ChannelContextInfo;
import games.core.user.User;

public abstract class BasePlayer
{
    protected User user;
    protected BasePlayer(){

    }

    public User getUser() {
        return user;
    }

    public int getId(){
        return user.getId();
    }

    public void setUser(User user){
        this.user = user;
    }

    /**
     * Định nghĩa khi nào thì nó đang chơi game
     * @return True nếu đang chơi False nếu không chơi
     */
    public abstract boolean isPlaying();

    /*
     * Nó đang ở trong table nhưng ko phải đang chơi. Đang xem thôi
     * */
    public boolean isViewer() {
        return false;
    }

    public ChannelContextInfo getCtxInfo(){
        return user.getCtx();
    }
}
