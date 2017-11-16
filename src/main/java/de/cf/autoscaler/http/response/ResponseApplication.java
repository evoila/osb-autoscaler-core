package de.cf.autoscaler.http.response;

import de.cf.autoscaler.api.binding.Binding;
import de.cf.autoscaler.applications.ScalableApp;

/**
 * Class for serializing a {@linkplain ScalableApp} to a JSON-String.
 * @author Marius Berger
 *
 */
public class ResponseApplication {

	private Binding binding;
	private ResponseScaling scaling;
	private ResponseCpu cpu;
	private ResponseRam ram;
	private ResponseLatency latency;
	private ResponseRequests requests;
	private ResponseLearning learning;
	
	public ResponseApplication(ScalableApp app) {
		binding = new Binding(app.getBinding());
		scaling = new ResponseScaling(app);
		cpu = new ResponseCpu(app);
		ram = new ResponseRam(app);
		latency = new ResponseLatency(app);
		requests = new ResponseRequests(app);
		learning = new ResponseLearning(app);
	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

	public ResponseScaling getScaling() {
		return scaling;
	}

	public void setScaling(ResponseScaling scaling) {
		this.scaling = scaling;
	}

	public ResponseCpu getCpu() {
		return cpu;
	}

	public void setCpu(ResponseCpu cpu) {
		this.cpu = cpu;
	}

	public ResponseRam getRam() {
		return ram;
	}

	public void setRam(ResponseRam ram) {
		this.ram = ram;
	}

	public ResponseLatency getLatency() {
		return latency;
	}

	public void setLatency(ResponseLatency latency) {
		this.latency = latency;
	}

	public ResponseRequests getRequests() {
		return requests;
	}

	public void setRequests(ResponseRequests requests) {
		this.requests = requests;
	}

	public ResponseLearning getLearning() {
		return learning;
	}

	public void setLearning(ResponseLearning learning) {
		this.learning = learning;
	}
}
