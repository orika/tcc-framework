package com.netease.backend.coordinator.id.db;

import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.id.UUIDGenerator;

public class UuidGeneratorImp implements UUIDGenerator {
	private IdForCoordinator serverIdDist = null;
	private SequenceIdGenerator seqGen = new SequenceIdGenerator();
	
	public UuidGeneratorImp(IdForCoordinator serverIdDist) {
		this.serverIdDist = serverIdDist;
	}

	public IdForCoordinator getServerIdDist() {
		return serverIdDist;
	}

	public SequenceIdGenerator getSeqGen() {
		return seqGen;
	}

	public void setServerIdDist(IdForCoordinator serverIdDist) {
		this.serverIdDist = serverIdDist;
	}

	public void setSeqGen(SequenceIdGenerator seqGen) {
		this.seqGen = seqGen;
	}

	@Override
	public long next() {
		// use serverId for high 16 bit 
		// sequenceId for low 48 bit
		int serverId = this.serverIdDist.get(); 
		long seqId = this.seqGen.nextSeqId();
		long uuid = (serverId << 48) | (seqId & SequenceIdGenerator.sequenceIdMask); 
		return uuid;
	}

	@Override
	public void init(long lastUUID) {
		seqGen.setSeqId(lastUUID & SequenceIdGenerator.sequenceIdMask);
	}
}
