import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

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

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		List<Process> processList = new ArrayList<Process>();

		Scanner in = new Scanner(new File("processes.in"));
		PrintWriter writer = new PrintWriter("processes.out", "UTF-8");

		int pcount = Integer.parseInt(retrieveParam(in.nextLine(), "processcount"));
		int runfor = Integer.parseInt(retrieveParam(in.nextLine(), "runfor"));
		Algorithm alg = Algorithm.byAbbreviation(retrieveParam(in.nextLine(), "use"));

		int quantum = -1;
		String line = in.nextLine(); // either quantum or first process

		// determine what line represents
		if (line.indexOf(quantum) > -1) {
			quantum = Integer.parseInt(retrieveParam(line, "quantum"));
			line = in.nextLine(); // now move to first process
		} // postcondition: line represents the first process

		// read processes
		while (line.indexOf("process") == 0) {
			Process p = new Process();
			p.name = retrieveParam(line, "name");
			p.arrival = Integer.parseInt(retrieveParam(line, "arrival"));
			p.burst = Integer.parseInt(retrieveParam(line, "burst"));
			processList.add(p);
			line = in.nextLine();
		} // done reading processes

		in.close(); // done reading

		// write alg info
		writer.printf("%d processes%n", pcount);
		writer.printf("Using %s%n", alg.getName());
		if (alg == Algorithm.ROUND_ROBIN)
			writer.printf("Quantum %d%n", quantum);
		writer.println();

		// simulation
		int elapsedTime = 0;
		Process selected = null;
		List<Process> loadedProcesses = new ArrayList<Process>();
		PriorityQueue<Process> processQueue = new PriorityQueue<Process>((Process p1, Process p2) -> {
			return Integer.compare(p1.arrival, p2.arrival);
		});
		for (Process p : processList)
			processQueue.add(p);

		// while we have time and processes exist
		for (; elapsedTime < runfor; elapsedTime++) {
			if (!(!processQueue.isEmpty() || !loadedProcesses.isEmpty())) {
				writer.printf("Time %d: Idle%n", elapsedTime);
				continue;
			}

			while (!processQueue.isEmpty() && processQueue.peek().arrival <= elapsedTime)
				loadedProcesses.add(processQueue.poll());

			// announce arrivals and completions
			for (int i = 0; i < loadedProcesses.size(); i++) {
				Process p = loadedProcesses.get(i);
				if (p.arrival == elapsedTime) {
					writer.printf("Time %d: %s arrived%n", elapsedTime, p.name);
				}
				if (p.burst == 0) {
					writer.printf("Time %d: %s finished%n", elapsedTime, p.name);
					p.complete = elapsedTime;
					p.burst = -1;
					loadedProcesses.remove(p);
					selected = null; // important for detecting idleness
				}
			}

			switch (alg) {
			case FIRST_COME_FIRST_SERVED:
				if (selected == null || selected.burst <= 0) {
					Collections.sort(loadedProcesses, (Process p1, Process p2) -> {
						return Integer.compare(p1.arrival, p2.arrival);
					});
					selected = selectFirst(selected, loadedProcesses, elapsedTime);
					if (selected != null)
						writer.printf("Time %d: %s selected (burst %d)%n", elapsedTime, selected.name, selected.burst);
				}
				break;
			case SHORTEST_JOB_FIRST:
				Collections.sort(loadedProcesses, (Process p1, Process p2) -> {
					return Integer.compare(p1.burst, p2.burst);
				});
				Process previous = selected;
				selected = selectFirst(selected, loadedProcesses, elapsedTime);
				if (previous != selected)
					writer.printf("Time %d: %s selected (burst %d)%n", elapsedTime, selected.name, selected.burst);
				break;
			case ROUND_ROBIN:
				if (selected == null || (elapsedTime - selected.selected) % quantum == 0) {
					Collections.sort(loadedProcesses, (Process p1, Process p2) -> {
						return Integer.compare(p1.lastRan, p2.lastRan);
					});
					selected = selectFirst(selected, loadedProcesses, elapsedTime);
					if (selected != null)
						writer.printf("Time %d: %s selected (burst %d)%n", elapsedTime, selected.name, selected.burst);
				}
				break;
			}

			// tick.
			for (Process p : loadedProcesses)
				if (selected != null && p != selected)
					p.wait++;
			if (selected != null) {
				selected.lastRan = elapsedTime;
				selected.burst--;
			} else {
				writer.printf("Time %d: Idle%n", elapsedTime);
			}
		}

		// handle completion during last tick
		for (int i = 0; i < loadedProcesses.size(); i++) {
			Process p = loadedProcesses.get(0);
			if (p.burst == 0) {
				writer.printf("Time %d: %s finished%n", elapsedTime, p.name);
				p.complete = elapsedTime;
				p.burst = -1;
				loadedProcesses.remove(p);
				selected = null; // important for detecting idleness
			}
		}

		// completion message
		if (loadedProcesses.isEmpty())
			writer.printf("Finished at time %d%n%n", elapsedTime);
		else
			writer.printf("Ran out of time at time %d%n%n", elapsedTime);

		// wait/turnaround time
		for (Process p : processList)
			writer.printf("%s wait %d turnaround %d%n", p.name, p.wait, p.complete - p.arrival);

		writer.flush();
		writer.close();
	}

	private static Process selectFirst(Process selected, List<Process> loadedProcesses, int elapsedTime) {
		if (!loadedProcesses.isEmpty() && loadedProcesses.get(0) != selected) {
			Process nextProcess = loadedProcesses.get(0);
			boolean isNewProcess = (selected == null || selected != nextProcess);
			selected = nextProcess;
			if (isNewProcess)
				selected.selected = elapsedTime;
		}
		return selected;
	}

	/**
	 * Returns the next "word" (substring surrounded with spaces) following the provided identifier.
	 * 
	 * @param line
	 *            String to extract parameter value from.
	 * @param name
	 *            Name of parameter.
	 * @throws IllegalArgumentException
	 * 			  String does not contain the name of the parameter.
	 */
	static String retrieveParam(String line, String name) {
		int indexOfIdentifier = line.indexOf(name);

		if (indexOfIdentifier < 0)
			throw new IllegalArgumentException(String.format("Parameter name %s not found", name));

		int startIndex = indexOfIdentifier + name.length() + 1;
		int endIndex = line.indexOf(" ", startIndex);
		if (endIndex == -1)
			endIndex = line.length();
		return line.substring(startIndex, endIndex);
	}
}

class Process {
	String name;
	int arrival, burst, wait, complete, lastRan, selected;

	Process() {
		wait = 0;
	}
}