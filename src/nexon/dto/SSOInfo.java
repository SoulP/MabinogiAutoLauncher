package nexon.dto;

public class SSOInfo {
    public boolean ShowFisshingAlert;
    public boolean LoginSuccess;
    public boolean IsOtpUser;
    public boolean IsCaptchaUser;
    public boolean HasError;
    public boolean IsBlocked;
    public boolean RequirePasswordChange;
    public boolean IsMobileID;
    public boolean IsSimpleID;
    public boolean IsNHNTransferID;
    public String NHNTransferURL;
    public String  CaptchaUrl;
    public boolean LoginFailure;
    public boolean OtpFailure;
    public boolean IsOldOtp;
    public boolean IsSymantecOtp;
    public boolean IsHyphenMail;
    public String Jwt;
}
