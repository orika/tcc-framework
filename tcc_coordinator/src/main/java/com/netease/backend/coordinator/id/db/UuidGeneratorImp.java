package com.netease.backend.coordinator.id.db;

import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.id.UUIDGenerator;

public class UuidGeneratorImp implements UUIDGenerator {
	private static UuidGeneratorImp uuidGen = null;
	private IdForCoordinator serverIdDist;
	private SequenceIdGenerator seqGen;
	
	private UuidGeneratorImp() {
		this.serverIdDist = new ServerIdDistributor();
		seqGen = new SequenceIdGenerator();
	}
	
	public void init() {
		if (uuidGen == null) {
			uuidGen = new UuidGeneratorImp();
		}
	}
	
	public synchronized static UUIDGenerator getInstance() {
		return uuidGen;
	}
	
	
	@Override
	public long next() {
		// TODO Auto-generated method stub
		int serverId = this.serverIdDist.get();
		long seqId = this.seqGen.nextSeqId();
		long uuid = (serverId << 48) | (seqId & 0xffffffffffffL); 
		return uuid;
	}

}
