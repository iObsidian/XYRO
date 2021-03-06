package rotmg.account.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import rotmg.account.core.Account;

public class AppEngine {

	public static final String getCharListAsString(Account account) {
		return getWebsite(getCharListURL(account));
	}

	public static final String getCharListAsString() {
		return getWebsite(getCharListURL());
	}

	public static final String getCharListURL(Account account) {
		return getCharListURL() + "?guid=" + account.getUserId() + "&password=" + account.getPassword();
	}

	public static final String getCharListURL() {
		return "http://www.realmofthemadgod.appspot.com/char/list";
	}

	public static final String getWebsite(String url) {

		StringBuilder input = new StringBuilder();

		try {
			URL websiteURL = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(websiteURL.openStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				input.append(inputLine);
			}

			in.close();

		} catch (IOException e) {
			System.err.println("Error reading website : " + url);
			e.printStackTrace();
		}

		return input.toString();

	}

}
