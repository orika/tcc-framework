package com.netease.backend.coordinator.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdForCoordinatorException;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogType;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;

public class DbUtil {
	public static BasicDataSource localDataSource = null;
	public static BasicDataSource systemDataSource = null;
	private Logger logger = LoggerFactory.getLogger(DbUtil.class);
	
	public DbUtil() {
		
	}
	
	public static void init(CoordinatorConfig config) {
		localDataSource.setDriverClassName(null);
		localDataSource.setUsername(null);
		localDataSource.setPassword(null);
		localDataSource.setUrl(null);
		localDataSource.setMaxActive(null);
		localDataSource.setMaxIdle(null);
		localDataSource.setMaxWait(null);
		
		systemDataSource.setDriverClassName(null);
		systemDataSource.setUsername(null);
		systemDataSource.setPassword(null);
		systemDataSource.setUrl(null);
		systemDataSource.setMaxActive(null);
		systemDataSource.setMaxIdle(null);
		systemDataSource.setMaxWait(null);
	} 
	
	public int getServerId(CoordinatorConfig config) throws IdForCoordinatorException {
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
			logger.error("Read COORDINATOR_INFO table error", e);
			throw new IdForCoordinatorException("Cannot fetch local ServerId");
		} finally {
			try {
				localRset.close();
				localPstmt.close();
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
				sysPstmt = sysConn.prepareStatement("Insert into SERVER_INFO(SERVER_IP, RDS_IP) values (?, ?)");
				
				// set insert value
				sysPstmt.setString(0, config.getServerIp());
				sysPstmt.setString(1, config.getRdsIp());
				
				int s = sysPstmt.executeUpdate();
				sysRset = sysPstmt.getGeneratedKeys();
				sysRset.next();
				serverId = sysRset.getInt("SERVER_ID");
			} catch (SQLException e) {
				logger.error("Write SERVER_INFO table error", e);
				throw new IdForCoordinatorException("Cannot get a new ServerId");
			} finally {
				try {
					sysRset.close();
					sysPstmt.close();
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
				
				int rs = localPstmt.executeUpdate();
			} catch (SQLException e) {
				logger.error("Write COORDINATOR_INFO table error", e);
				throw new IdForCoordinatorException("Cannot update local ServerId");
			} finally {
				try {
					localPstmt.close();
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
			trxContent = LogUtil.getPayLoad(tx.getExpireList());
			break;
		case TRX_START_CONFIRM:
			trxContent = LogUtil.getPayLoad(tx.getConfirmList());
			break;
		case TRX_START_CANCEL:
			trxContent = LogUtil.getPayLoad(tx.getCancelList());
			break;
		default:
			trxContent = null;	
		}
		
		Connection localConn = null;
		PreparedStatement localPstmt = null;
		
		try {
			localConn = DbUtil.localDataSource.getConnection();
			localPstmt = localConn.prepareStatement("INSERT INTO COORDINATOR_LOG(TRX_ID, TRX_STATUS, TRX_TIMESTAMP, TRX_CONTENT) VALUES(?,?,?,?)");
			
			// set insert values
			localPstmt.setLong(0, tx.getUUID());
			localPstmt.setInt(1, logType.ordinal());
			localPstmt.setLong(2, tx.getLastTimestamp());
			localPstmt.setBytes(3, trxContent);
			
			localPstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Write log error.", e);
			throw new LogException("Write log error");
		} finally {
			try {
				localPstmt.close();
				localConn.close();
			} catch (SQLException e) {
				logger.error("Write log error.", e);
			}
			
		}
	}

	public boolean checkExpire(long uuid) {
		Connection systemConn = null;
		PreparedStatement systemPstmt = null;
		ResultSet systemRset = null;
		
		try {
			systemConn = DbUtil.systemDataSource.getConnection();
			systemPstmt = systemConn.prepareStatement("INSERT INTO EXPIRE_TRX_INFO(TRX_ID, TRX_ACTION)" +
					" VALUES(?,?)");
			
			systemPstmt.setLong(0, uuid);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
}
