package games.core.user;

public class TestSMS {
    public static void main(String[] args) {
        SMSService.getInstance().sendOTP("0967600793", "123543");
    }
}
