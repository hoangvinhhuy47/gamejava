package games.system;

import games.core.common.data.DataCommonManager;
import games.core.constant.Platform;
import games.core.constant.Version;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 9/8/17.
 */
public class VersionManager {
    private static VersionManager instance = new VersionManager();

    public static VersionManager getInstance() {
        if (instance == null) {
            instance = new VersionManager();
        }
        return instance;
    }

    private Logger logger;

    private VersionManager() {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        this.setVersions(DataCommonManager.getInstance().getVersions());
    }

    List<Version> versions = new ArrayList<>();

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public boolean checkActiveVersion(int versionId, Platform platform) {
        Version currentVersion = null;
        for (Version version : versions)
        {
            if (version.getPlatform() == platform && version.getVersion() == versionId)
            {
                currentVersion = version;
                break;
            }
        }
        if (currentVersion == null) {
            return false;
        } else {
            if (currentVersion.isActive()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public Version maxVersionByPlatform(Platform platform) {
        Version currentVersion = null;
        int versionIdCur = 0;
        for (Version version : versions) {
            if (version.getPlatform() == platform && version.isActive()) {
                if (version.getVersion() > versionIdCur) {
                    currentVersion = version;
                    versionIdCur = version.getVersion();
                }
            }
        }
        return currentVersion;
    }
}
