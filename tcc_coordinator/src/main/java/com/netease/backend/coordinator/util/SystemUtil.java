package com.netease.backend.coordinator.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemUtil {

	public static String getIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}
}
