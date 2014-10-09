package com.netease.backend.coordinator.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import com.netease.backend.tcc.Procedure;

public class LogUtil {

	public static byte[] getPayLoad(List<Procedure> procList) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = new ObjectOutputStream(bo);
		oo.writeObject(procList);
		byte[] result = bo.toByteArray();
		oo.close();
		return result;
	}
}
