package de.evoila.cf.autoscaler.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class MetricReader {

	public static final String PATH = "."+File.separator+"src"+File.separator+"test"+File.separator+"testData.txt";
	
	private static final String CPU = "cpu";
	private static final String RAM = "ram";
	private static final String LATENCY = "latency";
	private static final String REQUEST = "request";
	private static final String METRIC = "metric";
	private static final String MAX = "Max";
	private static final String MEAN = "Mean";
	private static final String MIN = "Min";
	private static final String COUNT = "Count";
	
	
	private List<ValuePair<Integer>> cpuPairs;
	private List<ValuePair<Long>> ramPairs;
	private List<Integer> latencyValues;
	private List<Integer> requestValues;
	
	private int metricCount;
	
	private int cpuMax;
	private int cpuMean;
	private int cpuMin;
	
	private long ramMax;
	private long ramMean;
	private long ramMin;
	
	private int latencyMax;
	private int latencyMean;
	private int latencyMin;
	
	private int requestMax;
	private int requestMean;
	private int requestMin;
	
	private File file;
	
	public MetricReader() { }
	
	public void readFromFile(String path) {
		BufferedReader br = null;
		try {
			file = new File(path);
			FileInputStream fInput = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fInput));
			StringBuilder sb = new StringBuilder();
			String line;
			while ( (line = br.readLine()) != null) {
				sb.append(line);
			}
			
			String data = sb.toString();
			
			cpuPairs = getCpuValuesFromString(data);
			ramPairs = getRamValuesFromString(data);
			latencyValues = getLatencyValuesFromString(data);
			requestValues = getRequestValuesFromString(data);
			
			metricCount = parsePolicyValue(METRIC, COUNT, data);
			
			cpuMax = parsePolicyValue(CPU, MAX, data);
			cpuMean = parsePolicyValue(CPU, MEAN, data);
			cpuMin = parsePolicyValue(CPU, MIN, data);
			
			ramMax = parsePolicyValue(RAM, MAX, data) * 1024 * 1024;
			ramMean = computeRamMean();
			ramMin = parsePolicyValue(RAM, MIN, data) * 1024 * 1024;
			
			latencyMax = parsePolicyValue(LATENCY, MAX, data);
			latencyMean = parsePolicyValue(LATENCY, MEAN, data);
			latencyMin = parsePolicyValue(LATENCY, MIN, data);
			
			requestMax = parsePolicyValue(REQUEST, MAX, data);
			requestMean = parsePolicyValue(REQUEST, MEAN, data);
			requestMin = parsePolicyValue(REQUEST, MIN, data);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private List<ValuePair<Integer>> getCpuValuesFromString(String data) {
		List<ValuePair<Integer>> pairs = new LinkedList<ValuePair<Integer>>();
		try {
			String hlp = data;
			hlp = hlp.substring(hlp.indexOf("cpu"), hlp.length());
			hlp = hlp.substring(hlp.indexOf('[')+1, hlp.indexOf(']'));
			hlp = freeFromEmptySpaces(hlp);
			
			String valuePair = "";
			int instanceIndex = -1;
			int value = -1;
			while (hlp.indexOf(')') > 0) {
				valuePair = hlp.substring(hlp.indexOf('(')+1,hlp.indexOf(')'));
				instanceIndex = Integer.parseInt(valuePair.substring(0,valuePair.indexOf(':')));
				value = Integer.parseInt(valuePair.substring(valuePair.indexOf(':')+1, valuePair.length()));
				pairs.add(new ValuePair<Integer>(instanceIndex, value));
			
				hlp = hlp.substring(hlp.indexOf(')')+1,hlp.length());
			}
			
		} catch (IndexOutOfBoundsException ex) { 
			System.out.println("Could not find cpu values. Use following syntax where Y are the instance indices and X are the values: {cpu [(Y:X),(Y:X),(Y:X),...]}");
		} catch (NumberFormatException ex) {
			System.out.println("Parsing error while trying to parse the cpu values.");
		}
		
		return pairs;
	}
	
	private List<ValuePair<Long>> getRamValuesFromString(String data) {
		List<ValuePair<Long>> pairs = new LinkedList<ValuePair<Long>>();
		try {
			String hlp = data;
			hlp = hlp.substring(hlp.indexOf("ram"), hlp.length());
			hlp = hlp.substring(hlp.indexOf('[')+1, hlp.indexOf(']'));
			hlp = freeFromEmptySpaces(hlp);
			
			String valuePair = "";
			int instanceIndex = -1;
			long value = -1;
			while (hlp.indexOf(')') > 0) {
				valuePair = hlp.substring(hlp.indexOf('(')+1,hlp.indexOf(')'));
				instanceIndex = Integer.parseInt(valuePair.substring(0,valuePair.indexOf(':')));
				value = Long.parseLong(valuePair.substring(valuePair.indexOf(':')+1, valuePair.length()));
				pairs.add(new ValuePair<Long>(instanceIndex, value));
			
				hlp = hlp.substring(hlp.indexOf(')')+1,hlp.length());
			}
			
		} catch (IndexOutOfBoundsException ex) { 
			System.out.println("Could not find ram values. Use following syntax where Y are the instance indices and X are the values: {ram [(Y:X),(Y:X),(Y:X),...]}");
		} catch (NumberFormatException ex) {
			System.out.println("Parsing error while trying to parse the ram values.");
		}
		
		return pairs;
	}
	
	private List<Integer> getLatencyValuesFromString(String data) {
		List<Integer> values = new LinkedList<Integer>();
		try {
			String hlp = data;
			hlp = hlp.substring(hlp.indexOf("latency"), hlp.length());
			hlp = hlp.substring(hlp.indexOf('[')+1, hlp.indexOf(']'));
			hlp = freeFromEmptySpaces(hlp);

			int value = -1;
			while (hlp.indexOf(',') > 0) {
				value = Integer.parseInt(hlp.substring(0,hlp.indexOf(',')));
				values.add(value);
				
				if (hlp.indexOf(',') > 0)
					hlp = hlp.substring(hlp.indexOf(',')+1,hlp.length());
			}
			value = Integer.parseInt(hlp.substring(0,hlp.length()));
			values.add(value);
			
		} catch (IndexOutOfBoundsException ex) { 
			System.out.println("Could not find latency values. Use following syntax where X are the values: {latency [X,X,X,...]}");
		} catch (NumberFormatException ex) {
			System.out.println("Parsing error while trying to parse the latency values.");
		}
		
		return values;
	}
	
	private List<Integer> getRequestValuesFromString(String data) {
		List<Integer> values = new LinkedList<Integer>();
		try {
			String hlp = data;
			hlp = hlp.substring(hlp.indexOf("requests"), hlp.length());
			hlp = hlp.substring(hlp.indexOf('[')+1, hlp.indexOf(']'));
			hlp = freeFromEmptySpaces(hlp);

			int value = -1;
			while (hlp.indexOf(',') > 0) {
				value = Integer.parseInt(hlp.substring(0,hlp.indexOf(',')));
				values.add(value);
				
				if (hlp.indexOf(',') > 0)
					hlp = hlp.substring(hlp.indexOf(',')+1,hlp.length());
			}
			value = Integer.parseInt(hlp.substring(0,hlp.length()));
			values.add(value);
			
		} catch (IndexOutOfBoundsException ex) { 
			System.out.println("Could not find request values. Use following syntax where X are the values: {requests [X,X,X,...]}");
		} catch (NumberFormatException ex) {
			System.out.println("Parsing error while trying to parse the request values.");
		}
		
		return values;
	}
	
	private int parsePolicyValue(String component, String policy, String data) {
		String hlp = data;
		int policyValue = -1;
		
		try {
			hlp = hlp.substring(hlp.indexOf(component + policy), hlp.length());
			hlp = hlp.substring(hlp.indexOf(':')+1, hlp.indexOf('}'));
			hlp = freeFromEmptySpaces(hlp);
			policyValue = Integer.parseInt(hlp);
		} catch (IndexOutOfBoundsException ex) { 
			System.out.println("Could not find "+ component+policy + ". Use following syntax where X is the value: {cpuMax : X}");
		} catch (NumberFormatException ex) {
			System.out.println("Could not parse " + component+policy + " value.");
		}
		return policyValue;
	}
	
	
	private String freeFromEmptySpaces(String input) {
		String output = input;
		return output.replace(" ", "");
	}
	
	private long computeRamMean() {
		long sum = 0;
		for (int i = 0; i < ramPairs.size(); i++) {
			sum += ramPairs.get(i).getValue() * 1024 * 1024;
		}
		return sum / ramPairs.size();
	}

	public List<ValuePair<Integer>> getCpuPairs() {
		return cpuPairs;
	}
	
	public int[] getCpuValues() {
		int[] output = new int[cpuPairs.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = cpuPairs.get(i).getValue();
		}
		return output;
	}
	
	public int getCpuInstanceCount() {
		int output = -1;
		for (int i = 0; i < cpuPairs.size(); i++) {
			output = Math.max(output, cpuPairs.get(i).getInstanceIndex());
		}
		return output;
	}

	public void setCpuPairs(List<ValuePair<Integer>> cpuPairs) {
		this.cpuPairs = cpuPairs;
	}

	public List<ValuePair<Long>> getRamPairs() {
		return ramPairs;
	}
	
	public long[] getRamValues() {
		long[] output = new long[ramPairs.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = ramPairs.get(i).getValue();
		}
		return output;
	}
	
	public int getRamInstanceCount() {
		int output = -1;
		for (int i = 0; i < ramPairs.size(); i++) {
			output = Math.max(output, ramPairs.get(i).getInstanceIndex());
		}
		return output;
	}

	public void setRamPairs(List<ValuePair<Long>> ramPairs) {
		this.ramPairs = ramPairs;
	}

	public int[] getLatencyValues() {
		int[] output = new int[latencyValues.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = latencyValues.get(i);
		}
		return output;
	}

	public void setLatencyValues(List<Integer> latencyValues) {
		this.latencyValues = latencyValues;
	}

	public int[] getRequestValues() {
		int[] output = new int[requestValues.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = requestValues.get(i);
		}
		return output;
	}
	
	public void setRequestValues(List<Integer> requestValues) {
		this.requestValues = requestValues;
	}
	
	public int getMetricCount() {
		return metricCount;
	}

	public void setMetricCount(int metricCount) {
		this.metricCount = metricCount;
	}

	public int getCpuMax() {
		return cpuMax;
	}

	public void setCpuMax(int cpuMax) {
		this.cpuMax = cpuMax;
	}

	public int getCpuMean() {
		return cpuMean;
	}

	public void setCpuMean(int cpuMean) {
		this.cpuMean = cpuMean;
	}

	public int getCpuMin() {
		return cpuMin;
	}

	public void setCpuMin(int cpuMin) {
		this.cpuMin = cpuMin;
	}

	public long getRamMax() {
		return ramMax;
	}

	public void setRamMax(long ramMax) {
		this.ramMax = ramMax;
	}

	public long getRamMean() {
		return ramMean;
	}

	public void setRamMean(long ramMean) {
		this.ramMean = ramMean;
	}

	public long getRamMin() {
		return ramMin;
	}

	public void setRamMin(long ramMin) {
		this.ramMin = ramMin;
	}

	public int getLatencyMax() {
		return latencyMax;
	}

	public void setLatencyMax(int latencyMax) {
		this.latencyMax = latencyMax;
	}

	public int getLatencyMean() {
		return latencyMean;
	}

	public void setLatencyMean(int latencyMean) {
		this.latencyMean = latencyMean;
	}

	public int getLatencyMin() {
		return latencyMin;
	}

	public void setLatencyMin(int latencyMin) {
		this.latencyMin = latencyMin;
	}

	public int getRequestMax() {
		return requestMax;
	}

	public void setRequestMax(int requestMax) {
		this.requestMax = requestMax;
	}

	public int getRequestMean() {
		return requestMean;
	}

	public void setRequestMean(int requestMean) {
		this.requestMean = requestMean;
	}

	public int getRequestMin() {
		return requestMin;
	}

	public void setRequestMin(int requestMin) {
		this.requestMin = requestMin;
	}

	public String toString() {
		return 	"metricCount : " + metricCount + "\n"
				+"cpuPairs : "+ cpuPairs+"\n"
				+"cpuMax : " + cpuMax+"\n"
				+"cpuMean : " + cpuMean+"\n"
				+"cpuMin : " + cpuMin+"\n"
				+"ramPairs : "+ ramPairs+"\n"
				+"ramMax : " + ramMax+"\n"
				+"ramMean : " + ramMean+"\n"
				+"ramMin : " + ramMin+"\n"
				+"latencyValues : "+ latencyValues+"\n"
				+"latencyMax : " + latencyMax+"\n"
				+"latencyMean : " + latencyMean+"\n"
				+"latencyMin : " + latencyMin+"\n"
				+"requestValues : "+ requestValues+"\n"
				+"requestMax : " + requestMax+"\n"
				+"requestMean : " + requestMean+"\n"
				+"requestMin : " + requestMin+"\n";
	}
}
