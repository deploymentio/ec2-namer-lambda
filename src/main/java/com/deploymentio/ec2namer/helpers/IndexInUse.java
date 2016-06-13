package com.deploymentio.ec2namer.helpers;

public class IndexInUse implements Comparable<IndexInUse> {
	
	private String instanceId;
	private int index;

	public IndexInUse(String instanceId, int index) {
		this.instanceId = instanceId;
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexInUse other = (IndexInUse) obj;
		if (index != other.index)
			return false;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		return true;
	}

	@Override
	public int compareTo(IndexInUse o) {
		return Integer.compare(index, o.index);
	}
}
