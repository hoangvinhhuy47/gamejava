package games.payment.object;

public class NewUserCardResponse {
    private int code = 0;//200 success
    private int status = 0;//theo docs: 1 : card hop le, 2 : card ko hop le......
    private int verifyStatus = 0;
    private double price = 0;//muc tien cua card nay

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(int verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    @Override
    public String toString() {
        String pattern =
                "NewUserCardResponse: " +
                " HttpCode %s, " +
                " RequestStatus: %s, " +
                " VerifyStatus: %s, " +
                " Price: %s";
        return String.format(pattern, getCode(), getStatus(), getVerifyStatus(), getPrice());
    }
}
