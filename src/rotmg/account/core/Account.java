package rotmg.account.core;

public class Account {

	public static final String NETWORKNAME = "rotmg";
	public static final String WEBUSERID = "";
	public static final String WEBPLAYPLATFORMNAME = "rotmg";

	public String signedRequest = "";
	public String kabamId = "";

	public boolean isVerifiedEmail = true;
	public String platformToken = "";
	public String userDisplayName = "";

	public String entryTag = "";
	public boolean rememberMe = true;
	public String paymentProvider = "";
	public String paymentData = "";
	public String userId = ""; // Email
	public String password = "";
	public String token = "";
	public String secret = "";
	public Object credentials;
	public String gameNetworkUserId = "";
	public String gameNetwork = "";
	public String playPlatform = "";
	String userName = "";
	boolean isRegistered;
	String requestPrefix = "";
	boolean isVerified = true;

	String moneyUserId = "";

	String moneyAccessToken = "";

	public Account() {
		super();
	}

	public Account(String email, String password) {
		this.userId = email;
		this.password = password;
		this.secret = password; //TODO what's the difference between secret and password?
	}

	public String getUserName() {
		return this.userId;
	}

	public String getUserId() {
		if (this.userId == null) {
			//this.userId = GUID.create();
		}
		return this.userId;
	}

	public String getPassword() {

		if (this.password != null) {
			return this.password;
		} else {
			return "";
		}

	}

	public String getToken() {
		return "";
	}

	public boolean isRegistered() {
		return true;
	}

	public void reportIntStat(String param1, int param2) {
	}

	public String getRequestPrefix() {
		return "/credits";
	}

	public String gameNetworkUserId() {
		return WEBUSERID;
	}

	public String gameNetwork() {
		return NETWORKNAME;
	}

	public String playPlatform() {
		return WEBPLAYPLATFORMNAME;
	}

	public String getEntryTag() {
		if (this.entryTag != null) {
			return this.entryTag;
		} else {
			return "";
		}
	}

	public String getSecret() {
		return "";
	}

	public void verify(boolean param1) {
		this.isVerifiedEmail = param1;
	}

	public boolean isVerified() {
		return this.isVerifiedEmail;
	}

	public String getPlatformToken() {

		if (this.platformToken != null) {
			return this.platformToken;
		} else {
			return "";
		}

	}

	public void setPlatformToken(String param1) {
		this.platformToken = param1;
	}

	public String getMoneyAccessToken() {
		return this.signedRequest;
	}

	public String getMoneyUserId() {
		return this.kabamId;
	}

	public String getUserDisplayName() {
		return this.userDisplayName;
	}

	public void setUserDisplayName(String param1) {
		this.userDisplayName = param1;
	}

	public boolean getRememberMe() {
		return this.rememberMe;
	}

	public void setRememberMe(boolean param1) {
		this.rememberMe = param1;
	}

	public String getPaymentProvider() {
		return this.paymentProvider;
	}

	public void setPaymentProvider(String param1) {
		this.paymentProvider = param1;
	}

	public String getPaymentData() {
		return this.paymentData;
	}

	public void setPaymentData(String param1) {
		this.paymentData = param1;
	}

	@Override
	public String toString() {
		return "Email : '" + userId + "', Password : '" + password + "'";
	}

	public boolean contains(String contains) {
		return userId.toLowerCase().contains(contains.toLowerCase()) || password.toLowerCase().contains(contains.toLowerCase());
	}

	@Override
	public boolean equals(Object a) {
		if (a instanceof Account) {
			Account aa = (Account) a;
			return aa.userId.equals(this.userId) && aa.password.equals(this.password);
		}
		return false;

	}

}
