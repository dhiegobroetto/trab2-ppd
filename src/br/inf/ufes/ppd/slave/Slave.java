package br.inf.ufes.ppd.slave;

import java.util.UUID;

public class Slave {

	protected UUID slaveKey;
	protected String fileName;
	protected String slaveName;

	public Slave(UUID slaveKey, String slaveName, String fileName) {
		this.slaveKey = slaveKey;
		this.slaveName = slaveName;
		this.fileName = fileName;
	}


	public UUID getSlaveKey() {
		return slaveKey;
	}

	public String getFileName() {
		return fileName;
	}

	public String getSlaveName() {
		return slaveName;
	}

}
