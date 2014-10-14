package com.netease.backend.coordinator.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import com.netease.backend.tcc.Procedure;

public class LogUtil {

	public static byte[] serialize(List<Procedure> procList) {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = null;
		byte[] result = null;
		try {
			oo = new ObjectOutputStream(bo);
			oo.writeObject(procList);
			result = bo.toByteArray();
		} catch (IOException e) {
			
		} finally {
			try {
				oo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		return result;
	}
	
	
	public static List<Procedure> deserialize(byte[] procs) {
		return null;
	}
}
