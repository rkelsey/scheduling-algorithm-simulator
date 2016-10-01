import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchedulingAlgorithmSimulator {

	enum Algorithm {
		FIRST_COME_FIRST_SERVED("fcfs"),
		SHORTEST_JOB_FIRST("sjf"),
		ROUND_ROBIN("rr");
		
		private final String name;
		
		private Algorithm(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public static Algorithm byName(String name) {
			for(Algorithm algorithm : values()) {
				if(name.equals(algorithm.getName()))
					return algorithm;
			}
			return null;
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		int pcount, runfor, quantum;
		Algorithm alg;
		ArrayList<Process> processes = new ArrayList<Process>();
		
		
		String fileName = "processes.in";
		Scanner in = new Scanner(new File(fileName));
		
		pcount = Integer.parseInt(retrieveParam(in.nextLine(), "processcount"));
		runfor = Integer.parseInt(retrieveParam(in.nextLine(), "runfor"));
		retrieveParam(in.nextLine(), "use");
		
		if (alg.equals("rr"))
			quantum = Integer.parseInt(retrieveParam(in.nextLine(), "quantum"));
		
		// read processes
		String line = in.nextLine();
		while (line.indexOf("process") == 0) {
			Process p = new Process();
			p.name = retrieveParam(line, "name");
			p.arrival = Integer.parseInt(retrieveParam(line, "arrival"));
			p.burst = Integer.parseInt(retrieveParam(line, "burst"));
			processes.add(p);
			line = in.nextLine();
		} // done reading processes
		
	}

	/**
	 * Removes all characters of a string proceeding the first instance of '#' include the '#'
	 * itself.
	 */
	static String trimComment(String str) {
		return str.split("#")[0];
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	static Integer retrieveInt(String line) {
		Pattern r = Pattern.compile("(.*) (\\d+)#?(.*)?");
		Matcher m = r.matcher(line);

		if (!m.find())
			throw new IllegalArgumentException();
		else
			return Integer.parseInt(m.group(1));
	}

	static String retrieveParam(String line, String name) {
		int startIndex = line.indexOf(name) + name.length() + 1;
		int endIndex = line.indexOf(" ", startIndex);
		if (endIndex == -1)
			endIndex = line.length();
		return line.substring(startIndex, endIndex);
	}
}

class Process {
	String name;
	int arrival, burst;
}
