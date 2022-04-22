package games.core.user.object;

/**
 * Created by tuanhoang on 8/29/17.
 */
public class InfoUserItem {
    private int id = 0;
    private String name = "";
    private int gender = 0;

    InfoUserItem() {

    }

    public InfoUserItem(String[] strs) {
        try {
            if (strs.length >= 3) {
                id = Integer.parseInt(strs[0]);
                name = strs[1];
                gender = Integer.parseInt(strs[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //region getter - setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    //endregion
}
