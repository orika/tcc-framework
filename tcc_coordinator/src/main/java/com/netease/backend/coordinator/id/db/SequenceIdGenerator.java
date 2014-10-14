package com.netease.backend.coordinator.id.db;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceIdGenerator {
	private AtomicLong seqId;
	public static long sequenceIdMask = 0xffffffffffffL;
	
	public SequenceIdGenerator() {
		setSeqId(0);
	}
	
	public long getSeqId() {
		return this.seqId.get();
	}
	
	public void setSeqId(long seqId) {
		this.seqId.set(seqId);
	}
	
	public long nextSeqId() {
		return this.seqId.addAndGet(1);
	}
}
