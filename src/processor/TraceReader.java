package processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TraceReader {
	private final BufferedReader br;
	private final String PATH = "traces/"; //E:/DOCUMENTS/EPFL/git/CS4223-Assignment2/
	
	public static class Command {
		private final int type;
		private final String message;
		
		public Command(int t, String m) {
			type = t;
			message = m;
		}

		public int getType() {
			return type;
		}

		public String getMessage() {
			return message;
		}
		
	}
	
	public TraceReader(String file) throws FileNotFoundException {
		br = new BufferedReader(new FileReader(PATH + file + ".data"));
	}
	
	public Command getNextCommand() {
		try {
			String l = br.readLine();
			if (l == null) return new Command(3, "");
			else {
				String[] splitted = l.split(" ");
				return new Command(Integer.parseInt(splitted[0]), splitted[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Command(3, "");
		}

	}
	
	public void close() {
		try {
			br.close();
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
