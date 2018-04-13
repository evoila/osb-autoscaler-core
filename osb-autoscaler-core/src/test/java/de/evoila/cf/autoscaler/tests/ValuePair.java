package de.evoila.cf.autoscaler.tests;

public class ValuePair<T> {
	
	private int instanceIndex;
	private T value;
	
	public ValuePair(int instanceIndex, T value) {
		this.instanceIndex = instanceIndex;
		this.value = value;
	}

	public int getInstanceIndex() {
		return instanceIndex;
	}

	public void setInstanceIndex(int instanceIndex) {
		this.instanceIndex = instanceIndex;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	public String toString() {
		return instanceIndex+":"+value;
	}
}
