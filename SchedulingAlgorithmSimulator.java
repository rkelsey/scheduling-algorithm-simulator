import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchedulingAlgorithmSimulator {

	enum Algorithm {
		FIRST_COME_FIRST_SERVED("fcfs", "First-Come-First-Served"), SHORTEST_JOB_FIRST("sjf",
				"Preemptive Shortest Job First"), ROUND_ROBIN("rr", "Round-Robin");

		private final String abbreviation;
		private final String name;

		private Algorithm(String abbreviation, String name) {
			this.abbreviation = abbreviation;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getAbbreviation() {
			return abbreviation;
		}

		public static Algorithm byAbbreviation(String abbreviation) {
			for (Algorithm algorithm : values()) {
				if (abbreviation.equals(algorithm.getAbbreviation()))
					return algorithm;
			}
			return null;
		}

	}

	public static void main(String[] args) throws FileNotFoundException {
		List<Process> processList = new ArrayList<Process>();

		String fileName = "processes.in";
		Scanner in = new Scanner(new File(fileName));

		int pcount = Integer.parseInt(retrieveParam(in.nextLine(), "processcount"));
		int runfor = Integer.parseInt(retrieveParam(in.nextLine(), "runfor"));
		Algorithm alg = Algorithm.byAbbreviation(retrieveParam(in.nextLine(), "use"));

		int quantum = -1;
		if (alg == Algorithm.ROUND_ROBIN)
			quantum = Integer.parseInt(retrieveParam(in.nextLine(), "quantum"));

		// read processes
		String line = in.nextLine();
		while (line.indexOf("process") == 0) {
			Process p = new Process();
			p.name = retrieveParam(line, "name");
			p.arrival = Integer.parseInt(retrieveParam(line, "arrival"));
			p.burst = Integer.parseInt(retrieveParam(line, "burst"));
			processList.add(p);
			line = in.nextLine();
		} // done reading processes

		// actually get the joj here
		System.out.printf("%d processes%n", pcount);
		System.out.printf("Using %s%n", alg.getName());
		if (alg == Algorithm.ROUND_ROBIN)
			System.out.printf("Quantum %d%n", quantum);

		int elapsedTime = 0;
		Process selected = null;
		List<Process> loadedProcesses = new ArrayList<Process>();
		PriorityQueue<Process> processQueue = new PriorityQueue<Process>((Process p1, Process p2) -> {
			return Integer.compare(p1.arrival, p2.arrival);
		});
		for (Process p : processList)
			processQueue.add(p);

		while (!processQueue.isEmpty() || !loadedProcesses.isEmpty()) {

			while (!processQueue.isEmpty() && processQueue.peek().arrival <= elapsedTime)
				loadedProcesses.add(processQueue.poll());

			for (int i = 0; i < loadedProcesses.size(); i++) {
				Process p = loadedProcesses.get(0);
				// announce arrivals and completion
				if (p.arrival == elapsedTime) {
					System.out.printf("Time %d: %s arrived%n", elapsedTime, p.name);
				}
				if (p.burst == 0) {
					System.out.printf("Time %d: %s finished%n", elapsedTime, p.name);
					p.complete = elapsedTime;
					p.burst = -1;
					loadedProcesses.remove(p);
				}
			}

			// current process finished or we haven't started, select a new one
			if (selected == null || selected.burst <= 0) {
				switch (alg) {
				case FIRST_COME_FIRST_SERVED:
					Collections.sort(loadedProcesses, (Process p1, Process p2) -> {
						return Integer.compare(p1.arrival, p2.arrival);
					});
					for (Process p : loadedProcesses) {
						if (p.arrival <= elapsedTime) {
							selected = p;
							System.out.printf("Time %d: %s selected (burst %d)%n", elapsedTime, selected.name,
									selected.burst);
							break;
						}
					}
					break;
				case ROUND_ROBIN:
					Collections.sort(loadedProcesses, (Process p1, Process p2) -> {
						return Integer.compare(p2.lastRan, p1.lastRan);
					});
					if (elapsedTime % quantum == 0) {
						for (Process p : loadedProcesses) {
							if (p.arrival <= elapsedTime) {
								selected = p;
								System.out.printf("Time %d: %s selected (burst %d)%n", elapsedTime, selected.name,
										selected.burst);
								break;
							}
						}
					}
					break;
				}

			}
			// handle preemptive algorithm
			if (alg == Algorithm.SHORTEST_JOB_FIRST) {
				Collections.sort(loadedProcesses, (Process p1, Process p2) -> {
					return Integer.compare(p1.burst, p2.burst);
				});

				if (!loadedProcesses.isEmpty()) {
					if (loadedProcesses.get(0) != selected) {
						selected = loadedProcesses.get(0);
						System.out.printf("Time %d: %s selected (burst %d)%n", elapsedTime, selected.name,
								selected.burst);
					}
				}
			}

			// tick.
			for (Process p : loadedProcesses)
				if (selected != null && p != selected)
					p.wait++;
			if (selected != null) {
				selected.lastRan = elapsedTime;
				selected.burst--;
			}
			elapsedTime++;
		}

		System.out.printf("Finished at time %d%n%n", elapsedTime);

		for (Process p : processList)
			System.out.printf("%s wait %d turnaround %d%n", p.name, p.wait, p.complete - p.arrival);
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
	int arrival, burst, wait, complete, lastRan;

	Process() {
		wait = 0;
	}
}