package com.netease.backend.coordinator.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bo.close();
				if (oo != null)
					oo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new IllegalArgumentException("can not serialize proclist");
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Procedure> deserialize(byte[] procs) {
		ByteArrayInputStream bo = new ByteArrayInputStream(procs);
		ObjectInputStream oo = null;
		try {
			oo = new ObjectInputStream(bo);
			return (List<Procedure>) oo.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bo.close();
				if (oo != null)
					oo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new IllegalArgumentException("can not deserialize proclist");
	}
}
