package com.netease.backend.coordinator.id.db;

import com.netease.backend.coordinator.id.IdAtLastDown;

public class IdAstLastDownImp implements IdAtLastDown {

	private SequenceIdGenerator seqGen = null;
	@Override
	public long get() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(long uuid) {
		this.seqGen.setSeqId(uuid & SequenceIdGenerator.sequenceIdMask );

	}


	public void setSeqGen(SequenceIdGenerator seqGen) {
		this.seqGen = seqGen;
	}

}
