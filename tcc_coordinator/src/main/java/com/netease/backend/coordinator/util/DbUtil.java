package com.netease.backend.coordinator.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogRecord;
import com.netease.backend.coordinator.log.LogScanner;
import com.netease.backend.coordinator.log.LogType;
import com.netease.backend.coordinator.log.db.LogScannerImp;
import com.netease.backend.coordinator.monitor.MonitorException;
import com.netease.backend.coordinator.monitor.MonitorRecord;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class DbUtil {
	
	private static final Logger logger = Logger.getLogger(DbUtil.class);
	
	private BasicDataSource localDataSource = null;
	private BasicDataSource systemDataSource = null;
	private static int STREAM_SIZE = 100;
	private CoordinatorConfig config = null;
	
	public DbUtil(CoordinatorConfig config) {
		this.config = config;
	}
	
	public BasicDataSource getLocalDataSource() {
		return localDataSource;
	}

	public void setLocalDataSource(BasicDataSource localDataSource) {
		this.localDataSource = localDataSource;
	}

	public BasicDataSource getSystemDataSource() {
		return systemDataSource;
	}

	public void setSystemDataSource(BasicDataSource systemDataSource) {
		this.systemDataSource = systemDataSource;
	}

	public int getServerId() throws CoordinatorException {
		int serverId = -1;
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		ResultSet localRset = null;
		try {
			localConn = localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("SELECT SERVERID FROM COORDINATOR_INFO");
			localRset = localPstmt.executeQuery();
			if (localRset.next())
				serverId = localRset.getInt("SERVERID");
			else
				throw new CoordinatorException("Cannot fetch local ServerId");
		} catch (SQLException e) {
			logger.error("Read COORDINATOR_INFO table error", e);
			throw new CoordinatorException("Cannot fetch local ServerId");
		} finally {
			try {
				if (localRset != null)
					localRset.close();
				if (localPstmt != null)
					localPstmt.close();
				if (localConn != null)
					localConn.close();
			} catch (SQLException e) {
				logger.error("Read COORDINATOR_INFO table error", e);
			}	
		}
		
		if (serverId == -1) {
			// if have no server id, take one from system db
			Connection sysConn = null;
			PreparedStatement sysPstmt = null;
			ResultSet sysRset = null;
			try {
				sysConn = systemDataSource.getConnection();
				sysPstmt = sysConn.prepareStatement("Insert into SERVER_INFO(SERVER_IP, RDS_IP) values (?, ?)", Statement.RETURN_GENERATED_KEYS);
				
				// set insert value
				sysPstmt.setString(1, config.getServerIp());
				sysPstmt.setString(2, config.getRdsIp());
				
				sysPstmt.executeUpdate();
				sysRset = sysPstmt.getGeneratedKeys();
				if (sysRset.next())
					serverId = sysRset.getInt(1);
				else
					throw new CoordinatorException("Cannot get a new ServerId");
			} catch (SQLException e) {
				logger.error("Write SERVER_INFO table error", e);
				throw new CoordinatorException("Cannot get a new ServerId");
			} finally {
				try {
					if (sysRset != null)
						sysRset.close();
					if (sysPstmt != null)
						sysPstmt.close();
					if (sysConn != null)
						sysConn.close();
				} catch (SQLException e) {
					logger.error("Write SERVER_INFO table error", e);
				}	
			}
			
			// Update new ServerId to local DB
			try {
				localConn = localDataSource.getConnection();
				localPstmt = localConn.prepareStatement("UPDATE COORDINATOR_INFO SET SERVERID = ?");
				
				// set update value
				localPstmt.setInt(1, serverId);
				
				localPstmt.executeUpdate();
			} catch (SQLException e) {
				logger.error("Write COORDINATOR_INFO table error", e);
				throw new CoordinatorException("Cannot update local ServerId");
			} finally {
				try {
					if (localPstmt != null)
						localPstmt.close();
					if (localConn != null)
						localConn.close();
				} catch (SQLException e) {
					logger.error("Write COORDINATOR_INFO table error", e);
				}	
			}
			
		} 
		return serverId;
	}

	public void writeLog(Transaction tx, LogType logType) throws LogException {
		byte[] trxContent = null;
		
		switch(logType) {
		case TRX_BEGIN:
		case TRX_START_EXPIRE:
			trxContent = LogUtil.serialize(tx.getExpireList());
			break;
		case TRX_START_CONFIRM:
			trxContent = LogUtil.serialize(tx.getConfirmList());
			break;
		case TRX_START_CANCEL:
			trxContent = LogUtil.serialize(tx.getCancelList());
			break;
		default:
			trxContent = null;	
		}
		
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		
		try {
			localConn = localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("INSERT INTO COORDINATOR_LOG(TRX_ID, TRX_STATUS, TRX_TIMESTAMP, TRX_CONTENT) VALUES(?,?,?,?)");
			
			// set insert values
			localPstmt.setLong(1, tx.getUUID());
			localPstmt.setInt(2, logType.ordinal());
			localPstmt.setLong(3, tx.getLastTimeStamp());
			localPstmt.setBytes(4, trxContent);
			
			localPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Write log error.", e);
			throw new LogException("Write log error");
		} finally {
			try {
				if (localPstmt != null)
					localPstmt.close();
				if (localConn != null)
					localConn.close();
			} catch (SQLException e) {
				logger.error("Write log error.", e);
			}
			
		}
	}

	public boolean checkExpire(long uuid) throws LogException {
		Connection systemConn = null;
		PreparedStatement systemPstmt = null;
		ResultSet systemRset = null;
		int res = 0;
		try {
			systemConn = systemDataSource.getConnection();
			systemPstmt = systemConn.prepareStatement("INSERT IGNORE INTO EXPIRE_TRX_INFO(TRX_ID, TRX_ACTION)" +
					" VALUES(?,?)");
			
			systemPstmt.setLong(1, uuid);
			systemPstmt.setInt(2, Action.EXPIRE.ordinal());
			
			res = systemPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Check expired error.", e);
			throw new LogException("Check expire error");
		} finally {
			try {
				if (systemPstmt != null)
					systemPstmt.close();
			} catch (SQLException e) {
				logger.error("Check expired error.", e);
			}
		}
		

		// must duplicate key, then check the action
		if (res == 0) {
			try {
				systemPstmt = systemConn.prepareStatement("SELECT TRX_ACTION FROM EXPIRE_TRX_INFO WHERE TRX_ID = ?");
				systemPstmt.setLong(1, uuid);
				systemRset = systemPstmt.executeQuery();
				// if other node confirm or cancel this trx , then checkfailed
				if (systemRset.next()) {
					if (systemRset.getInt(1) != Action.EXPIRE.ordinal()) {
						return false;
					} else { 
						return true;
					}
				} else {
					throw new LogException("Check expire error");
				}
			} catch (SQLException e) {
				logger.error("Check expired error.", e);
				throw new LogException("Check expire error");
			} finally {
				try {
					if (systemRset != null)
						systemRset.close();
					if (systemPstmt != null)
						systemPstmt.close();
					if (systemConn != null)
						systemConn.close();
				} catch (SQLException e) {
					logger.error("Check expired error.", e);
				}
			}
			
		}
		return true;
	}


	public void setCheckpoint(long checkpoint) throws LogException {
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		
		try {
			localConn = this.localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("UPDATE COORDINATOR_INFO SET CHECKPOINT = ?");
			
			// set insert values
			localPstmt.setLong(1, checkpoint);
			
			localPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Update checkpoint error.", e);
			throw new LogException("Update checkpoint error");
		} finally {
			try {
				if (localPstmt != null)
					localPstmt.close();
				if (localConn != null)
					localConn.close();
			} catch (SQLException e) {
				logger.error("Update checkpoint error.", e);
			}
			
		}
	}


	public long getCheckpoint() throws LogException {
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		ResultSet localRset = null;
		long checkpoint = 0;
		try {
			localConn = this.localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("SELECT CHECKPOINT FROM COORDINATOR_INFO");
			
			localRset = localPstmt.executeQuery();
			if (localRset.next()) {
				checkpoint = localRset.getLong(1);
			} else {
				throw new LogException("Read checkpoint error");
			}
		} catch (SQLException e) {
			logger.error("Read checkpoint error.", e);
			throw new LogException("Read checkpoint error");
		} finally {
			try {
				if (localPstmt != null)
					localPstmt.close();
				if (localConn != null)
					localConn.close();
			} catch (SQLException e) {
				logger.error("Read checkpoint error.", e);
			}
			
		}
		return checkpoint;
	}


	public LogScanner beginScan(long startpoint) throws LogException {
		try {
			Connection conn = this.localDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("SELECT TRX_ID, TRX_STATUS, TRX_TIMESTAMP, TRX_CONTENT FROM COORDINATOR_LOG WHERE TRX_TIMESTAMP >= ?");
			pstmt.setLong(1, startpoint);
			pstmt.setFetchSize(STREAM_SIZE);
			ResultSet rset = pstmt.executeQuery();
			
			return new LogScannerImp(conn, pstmt, rset);
		} catch (SQLException e) {
			logger.error("Start read log error.", e);
			throw new LogException("Start read log error");
		} 
	}
	
	public boolean hasNext(ResultSet rset) throws LogException {
		try {
			return rset.next();
		} catch (SQLException e) {
			logger.error("Read log has next error.", e);
			throw new LogException("Read log has next error");
		}
	}

	public LogRecord getNextLog(ResultSet rset) throws LogException {
		try {
			long uuid = rset.getLong("TRX_ID");
			LogType logType = LogType.values()[rset.getInt("TRX_STATUS")];
			long timestamp = rset.getLong("TRX_TIMESTAMP");
			byte[] procs = rset.getBytes("TRX_CONTENT");
			return new LogRecord(uuid, logType, timestamp, procs);
		} catch (SQLException e) {
			logger.error("Read next log error.", e);
			throw new LogException("Read next log error");
		}	
	}


	public void endScan(Connection conn, PreparedStatement pstmt,
			ResultSet rset) throws LogException {
		try {
			if (rset != null)
				rset.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			logger.error("End read log error.", e);
			throw new LogException("Ene read log error");
		}
	}


	public boolean checkActionInRecover(long uuid) throws LogException {
		Connection systemConn = null;
		PreparedStatement systemPstmt = null;
		ResultSet systemRset = null;
		
		int res = 0;
		try {
			// Insert a record to system db, and mark trx action as REGISTED
			systemConn = this.systemDataSource.getConnection();
			systemPstmt = systemConn.prepareStatement("INSERT IGNORE INTO EXPIRE_TRX_INFO(TRX_ID, TRX_ACTION)" +
					" VALUES(?,?)");
			
			systemPstmt.setLong(1, uuid);
			systemPstmt.setInt(2, Action.REGISTERED.ordinal());
			
			res = systemPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Check action in recover error.", e);
			throw new LogException("Check action in recover error");
		} finally {
			try {
				if (systemPstmt != null)
					systemPstmt.close();
			} catch (SQLException e) {
				logger.error("Check action in recover error.", e);
			}
		}
		

		// must duplicate key, then check the action
		if (res == 0) {
			try {
				systemPstmt = systemConn.prepareStatement("SELECT TRX_ACTION FROM EXPIRE_TRX_INFO WHERE TRX_ID = ?");
				systemPstmt.setLong(1, uuid);
				systemRset = systemPstmt.executeQuery();
				
				if (systemRset.next()) {
					// if other node expire this trx , then checkfailed
					if (systemRset.getInt(1) != Action.REGISTERED.ordinal()) {
						return false;
					} else { 
						return true;
					}
				} else {
					throw new LogException("Check action in recover error");
				}
			} catch (SQLException e) {
				logger.error("Check action in recover error.", e);
				throw new LogException("Check action in recover error");
			} finally {
				try {
					if (systemRset != null)
						systemRset.close();
					if (systemPstmt != null)
						systemPstmt.close();
					if (systemConn != null)
						systemConn.close();
				} catch (SQLException e) {
					logger.error("Check action in recover error.", e);
				}
			}
			
		}
		return true;
	}


	public boolean checkLocaLogMgrAlive() {
		// TODO Auto-generated method stub	
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		ResultSet localRset = null;
		try {
			localConn = this.localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("SELECT 1");
			
			
			localRset = localPstmt.executeQuery();
			
			if (localRset.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			logger.error("Check action in recover error.", e);
			return false;
		} finally {
			try {
				if (localRset != null)
					localRset.close();
				if (localPstmt != null)
					localPstmt.close();
				if (localConn != null)
					localConn.close();
			} catch (SQLException e) {
				logger.error("Check action in recover error.", e);
			}
		}
	}


	public void writeHeuristicRec(Transaction tx, Action action,
			HeuristicsException e, boolean isLocal) throws LogException {
		// TODO Auto-generated method stub
		BasicDataSource dataSource = isLocal ? this.localDataSource : this.systemDataSource;
		Connection conn = null;
		PreparedStatement pstmt = null;
		byte[] trxContent = null;
		switch(action) {
		case EXPIRE:
			trxContent = LogUtil.serialize(tx.getExpireList());
			break;
		case CONFIRM:
			trxContent = LogUtil.serialize(tx.getConfirmList());
			break;
		case CANCEL:
			trxContent = LogUtil.serialize(tx.getCancelList());
			break;
		default:
			trxContent = null;	
		}
		
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement("INSERT IGNORE INTO HEURISTIC_TRX_INFO(TRX_ID, TRX_ACTION, TRX_HEURISTIC_CODE, TRX_TIMESTAMP, TRX_CONTENT) VALUES(?,?,?,?,?)");
			pstmt.setLong(1, tx.getUUID());
			pstmt.setInt(2, action.getCode());
			pstmt.setShort(3, e.getCode());
			pstmt.setLong(4, tx.getLastTimeStamp());
			pstmt.setBytes(5, trxContent);
			
			pstmt.executeUpdate();
		} catch (SQLException e1) {
			logger.error("Write heuristic record error.", e1);
			throw new LogException("Write heuristic record error");
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e1) {
				logger.error("Write heuristic record error.", e1);
			}
		}
		
	}


	public void writeMonitorRec(MonitorRecord rec) throws MonitorException {
		// TODO Auto-generated method stub
		Connection systemConn = null;
		PreparedStatement systemPstmt = null;
		
		try {
			systemConn = systemDataSource.getConnection();
			systemPstmt = systemConn.prepareStatement("INSERT INTO SERVER_MONITOR" +
					"(SERVER_ID, TIMESTAMP, CUR_TRX_NUM, CUR_PROCESS_TRX_NUM," +
					" REGISTED_TRX_NUM, CONFIRM_NUM, CANCEL_NUM, EXPIRE_NUM," +
					" AVG_REGISTED_TIME, MAX_REGISTED_TIME, " +
					"AVG_CONFIRM_TIME, MAX_CONFIRM_TIME, AVG_CANCEL_TIME, MAX_CANCEL_TIME)" +
					" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
			
			systemPstmt.setInt(1,  rec.getServerId());
			systemPstmt.setLong(2, rec.getTimestamp());
			systemPstmt.setLong(3, rec.getCurTrxNum());
			systemPstmt.setLong(4, rec.getCurProcessTrxNum());
			systemPstmt.setLong(5, rec.getRegistTrxNum());
			systemPstmt.setLong(6, rec.getConfirmTrxNum());
			systemPstmt.setLong(7, rec.getCancelTrxNum());
			systemPstmt.setLong(8, rec.getExpireTrxNum());
			systemPstmt.setLong(9, rec.getAvgRegistTime());
			systemPstmt.setLong(10, rec.getMaxRegistTime());
			systemPstmt.setLong(11, rec.getAvgConfirmTime());
			systemPstmt.setLong(12, rec.getMaxConfirmTime());
			systemPstmt.setLong(13, rec.getAvgCancelTime());
			systemPstmt.setLong(14, rec.getMaxCancelTime());
			
			systemPstmt.executeQuery();
		} catch (SQLException e) {
			logger.error("Write monitor record error.", e);
			throw new MonitorException("Write monitor record error");
		} finally {
			try {
				if (systemPstmt != null)
					systemPstmt.close();
				if (systemConn != null)
					systemConn.close();
			} catch (SQLException e) {
				logger.error("Write monitor record error.", e);
			}
		}
	}
}
