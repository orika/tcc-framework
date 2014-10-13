package com.netease.backend.coordinator.id.db;

import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.id.UUIDGenerator;

public class UuidGeneratorImp implements UUIDGenerator {
	private IdForCoordinator serverIdDist;
	private SequenceIdGenerator seqGen;
	
	public UuidGeneratorImp() {
		
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
		// TODO Auto-generated method stub
		int serverId = this.serverIdDist.get(); 
		long seqId = this.seqGen.nextSeqId();
		long uuid = (serverId << 48) | (seqId & SequenceIdGenerator.sequenceIdMask); 
		return uuid;
	}
}
