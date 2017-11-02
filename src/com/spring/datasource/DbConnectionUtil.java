package com.spring.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import com.mchange.v2.c3p0.ComboPooledDataSource;
public class DbConnectionUtil {

	private static Logger logger = Logger.getLogger(DataSourceHandler.class);
	
	private static DataSource dataSource = null;
	
//	static{
//		//初始化数据源(默认连接)
//		//读取配置文件
//		ResourceBundle bundle = PropertyResourceBundle.getBundle("conf/database/jdbc");
////		String dbInfo = "jdbc:mysql://localhost:3306/xianling_tool?characterEncoding=utf8;root;zl";
//		String dbInfo = bundle.getString("query");
//		try {
//			String[] str = dbInfo.split(";");
//			
//			ComboPooledDataSource cpds = new ComboPooledDataSource();
//			cpds.setDriverClass("com.mysql.jdbc.Driver");
//			cpds.setAcquireIncrement(1);
//			cpds.setInitialPoolSize(1);
//			cpds.setMaxPoolSize(5);
//			cpds.setMinPoolSize(1);
//			cpds.setMaxIdleTime(1000);
//			
//			//JDBCURL
//			cpds.setJdbcUrl(str[0].trim());
//			//账号
//			cpds.setUser(str[1].trim());
//			//密码
//			cpds.setPassword(str[2].trim());
//			
//			dataSource = cpds;
//		} catch (Exception e) {
//			logger.error("", e);
//		}
//	}
	/**
	 * 根据serverId获取game_db_url
	 * @param dataSource 
	 * @param serverId
	 * @return
	 */
	public static String queryGameDbUrlByServerId(DataSource dataSource, String serverId){
		Connection conn = null;
		Statement st = null;
		try {
			conn = dataSource.getConnection();
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select game_db_url from game_server where server_id = '"+serverId+"'");
			String str = null;
			while ( rs.next() ) {
				str = rs.getString("game_db_url");
			}
			return str;
		} catch (SQLException e) {
			logger.error("", e);
		}finally{
			try{
				if( st != null ) st.close();
				if( conn != null ) conn.close();
			}catch (SQLException e) {
				logger.error("", e);
			}
		}
		return null;
	}
	/**
	 * 根据serverId获取log_db_url
	 * @param serverId
	 * @return
	 */
	public static String queryLogDbUrlByServerId(DataSource dataSource,String serverId){
		Connection conn = null;
		Statement st = null;
		try {
			conn = dataSource.getConnection();
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select log_db_url from game_server where server_id = '"+serverId+"'");
			
			String str = null;
			while ( rs.next() ) {
				str = rs.getString("log_db_url");
			}
			return str;
		} catch (SQLException e) {
			logger.error("", e);
		}finally{
			try{
				if( st != null ) st.close();
				if( conn != null ) conn.close();
			}catch (SQLException e) {
				logger.error("", e);
			}
		}
		return null;
	}
	
	/**
	 * 根据serverId获取charge_db_url
	 * @param serverId
	 * @return
	 */
	public static String queryChargeDbUrlByServerId(String serverId){
		Connection conn = null;
		Statement st = null;
		try {
			conn = dataSource.getConnection();
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select charge_db_url from game_server where server_id = '"+serverId+"'");
			
			String str = null;
			while ( rs.next() ) {
				str = rs.getString("charge_db_url");
			}
			
			return str;
		} catch (SQLException e) {
			logger.error("", e);
		}finally{
			try{
				if( st != null ) st.close();
				if( conn != null ) conn.close();
			}catch (SQLException e) {
				logger.error("", e);
			}
		}
		return null;
	}
	
	public static boolean checkDbConneciton(String dbUrl){
		try {
			dbUrl = "jdbc:mysql://" + dbUrl;
			String[] str = dbUrl.split(";");
			
			ComboPooledDataSource cpds = new ComboPooledDataSource();
			cpds.setDriverClass("com.mysql.jdbc.Driver");
			cpds.setAcquireIncrement(1);
			cpds.setInitialPoolSize(1);
			cpds.setMaxPoolSize(5);
			cpds.setMinPoolSize(1);
			cpds.setMaxIdleTime(1000);
			//JDBCURL
			cpds.setJdbcUrl(str[0].trim());
			//账号
			cpds.setUser(str[1].trim());
			//密码
			cpds.setPassword(str[2].trim());
			
			Connection conn = cpds.getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println( DbConnectionUtil.queryGameDbUrlByServerId("1015") );
//		System.out.println( DbConnectionUtil.queryLogDbUrlByServerId("1015") );
		System.out.println(checkDbConneciton("192.168.0.212:3306/chuanqi_ly;linyu;com.123"));
		System.out.println("-------------------------------------");
		System.out.println(checkDbConneciton("192.168.0.212:3306/chuanqi_ly;linyu;com.123a"));
	}

}
