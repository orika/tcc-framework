/**
 * @Description:			Used for scan log in recovery
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */

package com.netease.backend.coordinator.log;

import com.netease.backend.tcc.common.LogException;


public interface LogScanner {
	/**
	 * check if has next log
	 * @return true is has next log
	 * @throws LogException
	 */
	boolean hasNext() throws LogException;
	
	/**
	 * fetch next log record
	 * @return log record
	 * @throws LogException
	 */
	LogRecord next() throws LogException;
	
	/**
	 * end scan and destory the scanner connection
	 * @throws LogException
	 */
	void endScan() throws LogException;
}
