// this class is used to manage the user data
// including user log in and user sign up
import java.security.MessageDigest;
import java.util.*;
import java.io.*;

public class logIn {
	// store all users and keys in a hashmap
	private HashMap<String, String> user_table = new HashMap<String, String>();
	private List<String> userKeyList = new ArrayList<String>();
	private boolean rightPassword = false;
	private String userName = null;

	// set up some exist username and password
	logIn() {
		this.initialLogIn();
	}
	// check username and password
	logIn(String userName, String passWord) {
		// read user_pass.txt in the hash table
		this.readIn();
		if (user_table.containsKey(userName)) {
			String checkPass = user_table.get(userName);
			if (checkPass.equals(encodeKey(passWord))) {
				System.out.println(userName + " logs in");
				this.userName = userName;
				this.rightPassword = true;
			}
			else {
				System.out.println(userName + " enters wrong password");
				this.rightPassword = false;
			}
		}
		else {
			System.out.println(userName + " enters wrong userName");
			this.rightPassword = false;
		}
	}

	public boolean checkRightPassword() {
		return this.rightPassword;
	}

	public String checkUserName() {
		return this.userName;
	}

	private void readIn() {
		// read user_pass.txt in the hash table
		try {
			int c = 0;
			char checkC = (char)c;
			StringBuilder buildNP = new StringBuilder();
			String userLogInName = null, userLogInPass = null;
			FileReader fileReader = new FileReader("user_pass.txt");
			// write name & password into hashtable
			while ((c = fileReader.read()) != -1) {
				// c = 32 bloank, c = 10 enter
				checkC = (char)c;
				if (c != 32 && c != 10) {
					buildNP.append(checkC);
				}
				else if (c == 32) {
					userLogInName = buildNP.toString();
					//System.out.println(userLogInName);
					buildNP.setLength(0);
				}
				else {
					userLogInPass = buildNP.toString();
					//System.out.println(userLogInPass);
					buildNP.setLength(0);
					user_table.put(userLogInName, userLogInPass);
				}
				//System.out.println(checkC);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("File copy error");
		} catch (Exception e) {
				throw new RuntimeException(e);
		}
	}

	// put all these user-key pair into table
	public void initialLogIn() {
		System.out.println("test");
		try {
			userKeyList.add("columbia");	userKeyList.add(encodeKey("116bway"));
			userKeyList.add("seas");		userKeyList.add(encodeKey("winterisover"));
			userKeyList.add("csee4119");	userKeyList.add(encodeKey("lotsofassignments"));
			userKeyList.add("foobar");		userKeyList.add(encodeKey("passpass"));
			userKeyList.add("windows");		userKeyList.add(encodeKey("withglass"));
			userKeyList.add("google");		userKeyList.add(encodeKey("partofalphabet"));
			userKeyList.add("facebook");	userKeyList.add(encodeKey("wastetime"));
			userKeyList.add("wikipedia");	userKeyList.add(encodeKey("donation"));
			userKeyList.add("network");		userKeyList.add(encodeKey("seemsez"));
		} catch (Exception e) {
			System.out.println("Set list failed");
		}

		try {
			FileWriter fileWriter = new FileWriter("user_pass.txt");
			for (int i = 0; i < userKeyList.size(); i = i+2) {
					System.out.println(userKeyList.get(i));
					fileWriter.write( userKeyList.get(i) + " " );
					fileWriter.write( userKeyList.get(i+1) + "\n");
				}
			fileWriter.flush();
			System.out.println("Write in new file");
			fileWriter.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("File copy error");
		}

	}
	
	// encoding key string to SHA1
	private static String encodeKey(String str) {
		try {
				MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
				messageDigest.update(str.getBytes());
				return getFormattedText(messageDigest.digest());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}
	
	private static String getFormattedText(byte[] bytes) {
		int len = bytes.length;
		StringBuilder buf = new StringBuilder(len);
		for (int j = 0; j < len; j++) {
			buf.append(bytes[j] + 128); 			
		}
		return buf.toString();
	}

	public static void main(String args[]){
		try {
			if (args.length != 2) {
				logIn lgin1 = new logIn();
			}
			else {
				logIn lgin2 = new logIn(args[0], args[1]);
			}
		} catch (Exception e) {
			System.out.println("Error");
		}
	}
	
}
