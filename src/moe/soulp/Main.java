package moe.soulp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nexon.auth.NexonAuthentication;

public class Main {
	private static String mabinogi = "mabinogi.exe";
	private static final String option = "code:1622 ver:321 logip:111.87.32.89 logport:11000 chatip:111.87.32.92 chatport:8002 setting:\"file://data/features.xml=Regular, Japan\" /P:";
	private static InputStreamReader isr = new InputStreamReader(System.in);
	private static BufferedReader br = new BufferedReader(isr);

	public static void main(String[] args) throws IOException {
		String id = null;
		String password = null;
		String otp = "";
		boolean isOTP = false;

		for (short i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h")) {
				System.out.println("java -jar Mabinogi.jar [options...]");
				System.out.println();
				System.out.println("-id ...                   NEXON ID");
				System.out.println("-password ... | -p ...    Password");
				System.out.println("-otp [...]                OneTimePassword");
				System.out.println("-help | -h                HELP");
				System.exit(0);
			}
			if (args[i].equalsIgnoreCase("-id")) {
				id = args[++i];
			}
			if (args[i].equalsIgnoreCase("-password") || args[i].equalsIgnoreCase("-p")) {
				password = args[++i];
			}
			if (args[i].equalsIgnoreCase("-otp")) {
				isOTP = true;
				if (i < args.length - 1 && args[i + 1].matches("^\\d{6}$")) {
					otp = args[++i];
				}
			}
		}

		if (id == null || id.isEmpty()) {
			System.out.print("NEXON ID: ");
			id = br.readLine();
		}

		if (password == null || password.isEmpty()) {
			System.out.print("Password: ");
			char[] pass = System.console().readPassword();
			password = "";
			for (short i = 0; i < pass.length; i++) {
				password += pass[i];
			}
		}

		if (isOTP && otp.isEmpty()) {
			System.out.print("OTP: ");
			otp = br.readLine();
		}

		// 認証
		NexonAuthentication nexonAuth = new NexonAuthentication();
		String npp = nexonAuth.login(id, password, otp);

		if (npp != null) {
			// 起動
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec(mabinogi + " " + option + npp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
