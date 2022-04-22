package games.core.database.document;

import games.core.database.MongoConst;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.annotation.DocumentFieldUnique;
import games.core.database.model.MUserInfo;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;

@CollectionClass(MongoConst.COLL_USER)
public class UserInfo extends MGDocument implements MongoConst {

    public UserInfo(){

    }

    public UserInfo(int userId){
        this.id = userId;
    }

    public UserInfo(String userName){
        this.userName = userName;
    }
    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int id;

    @DocumentField(FIELD_USER_NAME)
    @DocumentFieldUnique
    private String userName;

    @DocumentField(FIELD_USER_PASS)
    private String userPassword;

    @DocumentField(FIELD_NICK_NAME)
    private String nickName;

    @DocumentField(FIELD_ACCESS_TOKEN)
    @DocumentFieldUnique
    private String token;

    @DocumentField(FIELD_USER_AVATAR)
    private String avatar;

    /**
     * deviceId
     */
    @DocumentField(FIELD_DEVICE_ID)
    private String deviceId;

    @DocumentField(FIELD_MAC_ADDRESS)
    private String macAddress = "";

    @DocumentField(FIELD_IMIE)
    private String imie = "";

    @DocumentField(FIELD_IP_ADDRESS)
    private String ipAddress = "";

    /**
     * fb token
     */
    @DocumentField(FIELD_FB_ID)
    private String fbId;

    @DocumentField(FIELD_NEW_REGISTER)
    private boolean newRegister;

    @DocumentField(FIELD_DAY_REGISTER)
    private int dayRegister = 0;

    @DocumentField(FIELD_GENDER)
    private int gender = 0;

    @DocumentField(FIELD_MAIL)
    private String mail = "";

    @DocumentField(FIELD_USER_PHONE)
    @DocumentFieldUnique
    private String phone = "";

    @DocumentField(FIELD_PLATFORM)
    private int platform = 0;

    @DocumentField(FIELD_VERSION)
    private int version = 0;

    @DocumentField(FIELD_SOURCE)
    private String source = "";

    @DocumentField(FIELD_OTP)
    private String otp = "";

    //thoi gian get
    @DocumentField(FIELD_TIME_GET_OTP)
    private int timeOtp = 0;

    @DocumentField(FIELD_IS_VERIFY_PHONE)
    private boolean isVerifyPhone = false;

    @DocumentField(FIELD_IS_BANNED)
    private boolean isBanned = false;


    @Override
    public UserInfo updateDB() throws IllegalAccessException, NotFoundDocumentInDatabase, NullCollection {
        MUserInfo.getInstance().updateFromMongo(this);
        return this;
    }

    public UserInfo updateDBByUserName() throws NoSuchFieldException, IllegalAccessException {
        MUserInfo.getInstance().updateFromMongoByUniqueField(this, "user_name");
        return this;
    }

    @Override
    public UserInfo saveDB() throws IllegalAccessException, NoSuchFieldException {
        MUserInfo.getInstance().saveToMongo(this);
        this.changed.clear();
        return this;
    }

    //region getter - setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.changed.put(FIELD_DEFAULT_ID, this.id);
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        this.changed.put(FIELD_USER_NAME, this.getUserName());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        this.changed.put(FIELD_ACCESS_TOKEN, this.getToken());
    }

    public String getAvatar() {
        if (avatar == null) avatar = "";
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        this.changed.put(FIELD_USER_AVATAR, this.getAvatar());
    }

    public String getNickName() {
        if (nickName == null) nickName = "";
        return nickName;
    }

    public void setNickName(String nickName) {

        this.nickName = nickName;
        this.changed.put(FIELD_NICK_NAME, this.nickName);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        this.changed.put(FIELD_DEVICE_ID, this.deviceId);
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
        this.changed.put(FIELD_FB_ID, this.fbId);
    }

    public boolean isNewRegister() {
        return newRegister;
    }

    public void setNewRegister(boolean newRegister) {
        this.newRegister = newRegister;
        this.changed.put(FIELD_NEW_REGISTER, this.newRegister);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        this.changed.put(FIELD_MAC_ADDRESS, this.macAddress);
    }

    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
        this.changed.put(FIELD_IMIE, this.imie);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        this.changed.put(FIELD_IP_ADDRESS, this.ipAddress);
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
        this.changed.put(FIELD_GENDER, this.gender);
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
        this.changed.put(FIELD_MAIL, this.mail);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.changed.put(FIELD_USER_PHONE, this.phone);
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
        this.changed.put(FIELD_PLATFORM, this.platform);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
        this.changed.put(FIELD_VERSION, this.version);
    }

    public int getDayRegister() {
        return dayRegister;
    }

    public void setDayRegister(int dayRegister) {
        this.dayRegister = dayRegister;
        this.changed.put(FIELD_DAY_REGISTER, this.dayRegister);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
        this.changed.put(FIELD_SOURCE, this.source);
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
        this.changed.put(FIELD_OTP, this.otp);
    }

    public int getTimeOtp() {
        return timeOtp;
    }

    public void setTimeOtp(int timeOtp) {
        this.timeOtp = timeOtp;
        this.changed.put(FIELD_TIME_GET_OTP, this.timeOtp);
    }

    public boolean getIsVerifyPhone() {
        return isVerifyPhone;
    }

    public void setIsVerifyPhone(boolean isVerifyPhone) {
        this.isVerifyPhone = isVerifyPhone;
        this.changed.put(FIELD_IS_VERIFY_PHONE, this.isVerifyPhone);
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        this.changed.put(FIELD_IS_BANNED, banned);
        isBanned = banned;
    }

    //endregion
}
