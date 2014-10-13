package com.netease.backend.coordinator.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogRecord;
import com.netease.backend.coordinator.log.LogType;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class DbUtil {
	public BasicDataSource localDataSource = null;
	public BasicDataSource systemDataSource = null;
	private Logger logger = LoggerFactory.getLogger(DbUtil.class);
	private static int STREAM_SIZE = 100;
	
	public DbUtil() {
		
	}
	

	public void setLocalDataSource(BasicDataSource localDataSource) {
		this.localDataSource = localDataSource;
	}


	public void setSystemDataSource(BasicDataSource systemDataSource) {
		this.systemDataSource = systemDataSource;
	}


	public int getServerId(CoordinatorConfig config) throws CoordinatorException {
		int serverId = -1;
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		ResultSet localRset = null;
		try {
			localConn = localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("SELECT SERVER_ID FROM COORDINATOR_INFO");
			localRset = localPstmt.executeQuery();
			serverId = localRset.getInt("SERVER_ID");
		} catch (SQLException e) {
			logger.debug("Read COORDINATOR_INFO table error", e);
			throw new CoordinatorException("Cannot fetch local ServerId");
		} finally {
			try {
				localRset.close();
				localPstmt.close();
				localConn.close();
			} catch (SQLException e) {
				logger.debug("Read COORDINATOR_INFO table error", e);
			}	
		}
		
		if (serverId == -1) {
			// if have no server id, take one from system db
			Connection sysConn = null;
			PreparedStatement sysPstmt = null;
			ResultSet sysRset = null;
			try {
				sysConn = systemDataSource.getConnection();
				sysPstmt = sysConn.prepareStatement("Insert into SERVER_INFO(SERVER_IP, RDS_IP) values (?, ?)");
				
				// set insert value
				sysPstmt.setString(0, config.getServerIp());
				sysPstmt.setString(1, config.getRdsIp());
				
				sysPstmt.executeUpdate();
				sysRset = sysPstmt.getGeneratedKeys();
				sysRset.next();
				serverId = sysRset.getInt("SERVER_ID");
			} catch (SQLException e) {
				logger.debug("Write SERVER_INFO table error", e);
				throw new CoordinatorException("Cannot get a new ServerId");
			} finally {
				try {
					sysRset.close();
					sysPstmt.close();
					sysConn.close();
				} catch (SQLException e) {
					logger.debug("Write SERVER_INFO table error", e);
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
				logger.debug("Write COORDINATOR_INFO table error", e);
				throw new CoordinatorException("Cannot update local ServerId");
			} finally {
				try {
					localPstmt.close();
					localConn.close();
				} catch (SQLException e) {
					logger.debug("Write COORDINATOR_INFO table error", e);
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
			localConn = this.localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("INSERT INTO COORDINATOR_LOG(TRX_ID, TRX_STATUS, TRX_TIMESTAMP, TRX_CONTENT) VALUES(?,?,?,?)");
			
			// set insert values
			localPstmt.setLong(0, tx.getUUID());
			localPstmt.setInt(1, logType.ordinal());
			localPstmt.setLong(2, tx.getLastTimeStamp());
			localPstmt.setBytes(3, trxContent);
			
			localPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.debug("Write log error.", e);
			throw new LogException("Write log error");
		} finally {
			try {
				localPstmt.close();
				localConn.close();
			} catch (SQLException e) {
				logger.debug("Write log error.", e);
			}
			
		}
	}

	public boolean checkExpire(long uuid) throws LogException {
		Connection systemConn = null;
		PreparedStatement systemPstmt = null;
		ResultSet systemRset = null;
		int res = 0;
		try {
			systemConn = this.systemDataSource.getConnection();
			systemPstmt = systemConn.prepareStatement("INSERT IGNORE INTO EXPIRE_TRX_INFO(TRX_ID, TRX_ACTION)" +
					" VALUES(?,?)");
			
			systemPstmt.setLong(0, uuid);
			systemPstmt.setInt(1, Action.EXPIRED.ordinal());
			
			res = systemPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.debug("Check expired error.", e);
			throw new LogException("Check expire error");
		} finally {
			try {
				systemPstmt.close();
			} catch (SQLException e) {
				logger.debug("Check expired error.", e);
			}
		}
		

		// must duplicate key, then check the action
		if (res == 0) {
			try {
				systemPstmt = systemConn.prepareStatement("SELECT TRX_ACTION FROM EXPIRE_TRX_INFO WHERE TRX_ID = ?");
				systemPstmt.setLong(0, uuid);
				systemRset = systemPstmt.executeQuery();
				// if other node confirm or cancel this trx , then checkfailed
				if (systemRset.getInt(0) != Action.EXPIRED.ordinal()) {
					return false;
				} else { 
					return true;
				}
			} catch (SQLException e) {
				logger.debug("Check expired error.", e);
				throw new LogException("Check expire error");
			} finally {
				try {
					systemRset.close();
					systemPstmt.close();
					systemConn.close();
				} catch (SQLException e) {
					logger.debug("Check expired error.", e);
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
			localPstmt.setLong(0, checkpoint);
			
			localPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.debug("Update checkpoint error.", e);
			throw new LogException("Update checkpoint error");
		} finally {
			try {
				localPstmt.close();
				localConn.close();
			} catch (SQLException e) {
				logger.debug("Update checkpoint error.", e);
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
			checkpoint = localRset.getLong(0);
		} catch (SQLException e) {
			logger.debug("Read checkpoint error.", e);
			throw new LogException("Read checkpoint error");
		} finally {
			try {
				localPstmt.close();
				localConn.close();
			} catch (SQLException e) {
				logger.debug("Read checkpoint error.", e);
			}
			
		}
		return checkpoint;
	}


	public void beginScan(long startpoint, Connection conn,
			PreparedStatement pstmt, ResultSet rset) throws LogException {
		try {
			conn = this.localDataSource.getConnection();
			pstmt = conn.prepareStatement("SELECT TRX_ID, TRX_STATUS, TRX_TIMESTAMP, TRX_CONTENT FROM COORDINATOR_LOG WHERE TRX_TIMESTAMP >= ?");
			pstmt.setLong(0, startpoint);
			pstmt.setFetchSize(STREAM_SIZE);
			rset = pstmt.executeQuery();
		} catch (SQLException e) {
			logger.debug("Start read log error.", e);
			throw new LogException("Start read log error");
		} 
	}
	
	public boolean hasNext(ResultSet rset) throws LogException {
		try {
			return rset.next();
		} catch (SQLException e) {
			logger.debug("Read log has next error.", e);
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
			logger.debug("Read next log error.", e);
			throw new LogException("Read next log error");
		}	
	}


	public void endScan(Connection conn, PreparedStatement pstmt,
			ResultSet rset) throws LogException {
		try {
			rset.close();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.debug("End read log error.", e);
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
			
			systemPstmt.setLong(0, uuid);
			systemPstmt.setInt(1, Action.REGISTERED.ordinal());
			
			res = systemPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.debug("Check action in recover error.", e);
			throw new LogException("Check action in recover error");
		} finally {
			try {
				systemPstmt.close();
			} catch (SQLException e) {
				logger.debug("Check action in recover error.", e);
			}
		}
		

		// must duplicate key, then check the action
		if (res == 0) {
			try {
				systemPstmt = systemConn.prepareStatement("SELECT TRX_ACTION FROM EXPIRE_TRX_INFO WHERE TRX_ID = ?");
				systemPstmt.setLong(0, uuid);
				systemRset = systemPstmt.executeQuery();
				// if other node expire this trx , then checkfailed
				if (systemRset.getInt(0) != Action.REGISTERED.ordinal()) {
					return false;
				} else { 
					return true;
				}
			} catch (SQLException e) {
				logger.debug("Check action in recover error.", e);
				throw new LogException("Check action in recover error");
			} finally {
				try {
					systemRset.close();
					systemPstmt.close();
					systemConn.close();
				} catch (SQLException e) {
					logger.debug("Check action in recover error.", e);
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
			return true;
		} catch (SQLException e) {
			logger.debug("Check action in recover error.", e);
			return false;
		} finally {
			try {
				localRset.close();
				localPstmt.close();
				localConn.close();
			} catch (SQLException e) {
				logger.debug("Check action in recover error.", e);
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
		case EXPIRED:
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
			pstmt.setLong(0, tx.getUUID());
			pstmt.setInt(1, action.getCode());
			pstmt.setShort(2, e.getCode());
			pstmt.setLong(3, tx.getLastTimeStamp());
			pstmt.setBytes(4, trxContent);
			
			pstmt.executeUpdate();
		} catch (SQLException e1) {
			logger.debug("Write heuristic record error.", e);
			throw new LogException("Write heuristic record error");
		} finally {
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e1) {
				logger.debug("Write heuristic record error.", e);
			}
		}
		
	}

	

	
}
