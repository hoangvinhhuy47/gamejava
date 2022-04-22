package games.core.constant;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class Version {
    private int	id;
    private int version;
    private String url;
    private Platform platform;
    private boolean active;

    public Version() {

    }

    public Version(String[] strings) {
        try {
            if (strings.length >= 5) {
                id = Integer.parseInt(strings[0]);
                version = Integer.parseInt(strings[1]);
                url = strings[2];
                int platformId = Integer.parseInt(strings[3]);
                platform = Platform.valueOf(platformId);
                active = Boolean.parseBoolean(strings[4]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Version - ").append(id);
        builder.append(" - version = ").append(version);
        builder.append(" - url = ").append(url);
        builder.append(" - platform = ").append(platform.toString());
        builder.append(" - active = ").append(active);
        return builder.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
