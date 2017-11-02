package com.spring.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 多数据源路由
 * @author  作者：wind
 * @version 创建时间：2017-11-2 下午2:45:43
 */
public class DataSourceHandler extends AbstractRoutingDataSource {
	private Logger logger = Logger.getLogger(DataSourceHandler.class);
	
	private PropertyResourceBundle jdbcProperty = (PropertyResourceBundle) PropertyResourceBundle.getBundle("config/jdbc");
	private static final String JDBCURL_PREFIX_MYSQL = "jdbc:mysql://";
	
	private String driverClass ="com.mysql.jdbc.Driver";
	private int acquireIncrement = 1;
	private int initialPoolSize = 1;
	private int minPoolSize = 1;
	private int maxPoolSize = 30;
	private int maxIdleTime = 1000;
	private int checkoutTimeout = 30000;
	
	private int idleConnectionTestPeriod = 30; //每多少秒检查所有连接池中的空闲连接
	private int acquireRetryAttempts = 2;	  //从数据库获取新连接失败后重复尝试的次数
	private int acquireRetryDelay = 1000;		  //两次连接中间隔时间，单位毫秒
	
	//11@@DataSourceType 或 DataSourceType
	private Map<Object,DataSource> dataSources = new ConcurrentHashMap<Object, DataSource>();
	
	
	//初始化数据
	public void init(){
		Set<String> keys = jdbcProperty.keySet();
		for(String key : keys){
			initDatasource(key,jdbcProperty.getString(key));
		}
	}
	
	/**
	 * 清理内存数据源
	 */
	public void cleanDataSources(){
		Iterator<Entry<Object, DataSource>> it = dataSources.entrySet().iterator();
		while(it.hasNext()){
			Entry<Object, DataSource> entry=it.next();
			ComboPooledDataSource cpds = (ComboPooledDataSource)entry.getValue();
			if(cpds != null){
				cpds.close();
			}
		}
		dataSources.clear();
		init();
	}
	
	/**
	 * 重置指定服务器数据源
	 */
	public void cleanDataSources(String serverId){
		closeConncetion(serverId + "@@" + DataSourceType.DS_KEY_GAMESERVER);
		closeConncetion(serverId + "@@" + DataSourceType.DS_KEY_LOG2DB_SERVER);
	}
	
	private void closeConncetion(String key){
		synchronized (dataSources) {
			try {
				ComboPooledDataSource cpds = (ComboPooledDataSource)dataSources.get(key);
				if(cpds!=null){
					cpds.close();
					dataSources.remove(key);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private DataSource initDatasource(String key,String dsCfg){
		DataSource dataSource = getDataSource(dsCfg);
		dataSources.put(key, dataSource);
		return dataSource;
	}
	
	private DataSource getDataSource(String dsCfg) {
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(driverClass);
			cpds.setAcquireIncrement(acquireIncrement);
			cpds.setInitialPoolSize(initialPoolSize);
			cpds.setMaxPoolSize(maxPoolSize);
			cpds.setMinPoolSize(minPoolSize);
			cpds.setMaxIdleTime(maxIdleTime);
			cpds.setCheckoutTimeout(checkoutTimeout);
			
//			//设置mysql连接失效处理
			cpds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
			cpds.setAcquireRetryAttempts(acquireRetryAttempts);
			cpds.setAcquireRetryDelay(acquireRetryDelay);
			
			String[] str = dsCfg.split(";");
			
			cpds.setJdbcUrl(str[0]);
			cpds.setUser(str[1]);
			cpds.setPassword(str[2]);
			System.out.println(str[0]);
			System.out.println(str[1]);
			System.out.println(str[2]);
//			cpds.setJdbcUrl("jdbc:mysql://172.24.16.56:3306/hqg_tools?characterEncoding=utf8");
			cpds.setUser("root");
			cpds.setPassword("root");
			
			
		} catch (Exception e) {
			logger.error("jdbc驱动错误,driverClass:"+driverClass);
			e.printStackTrace();
		}
		return cpds;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}
	
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}
	
	@Override
	protected DataSource determineTargetDataSource() {
		Object dbKey = determineCurrentLookupKey();
		if(dbKey == null){
			logger.error("DatasourceKey is null");
			return null;
		}
		DataSource dataSource = dataSources.get(dbKey);
		if(dataSource != null){
			return dataSource;
		}
		String dbKeyType = dbKey.toString();
		//dbKey格式为1@@gameServer or 1@@log2db，拆分为serverId和标识
		String[] keys =  dbKeyType.split("@@");
		String serverId = "";
		String dbUrl = "";
		if(keys.length >= 2){
			serverId = keys[0];
		}
		
		if(DataSourceType.DS_KEY_DEFAULT.getValue().equals(dbKeyType) ){
			dbUrl = jdbcProperty.getString("query");
		}else if(DataSourceType.DS_KEY_GAMESERVER.getValue().equals(dbKeyType)){
			dbUrl = DbConnectionUtil.queryGameDbUrlByServerId(getDefaultDataSource(),serverId);
		}else if(DataSourceType.DS_KEY_LOG2DB_SERVER.getValue().equals(dbKeyType)){
			dbUrl = DbConnectionUtil.queryLogDbUrlByServerId(getDefaultDataSource(),serverId);
		}else if(DataSourceType.DS_KEY_PLUGIN.getValue().equals(dbKeyType)){
			dbUrl = ResourceBundle.getBundle("config/charge-jdbc").getString("plugin");
		}else if(DataSourceType.DS_KEY_CHARGE.getValue().equals(dbKeyType)){
			dbUrl = ResourceBundle.getBundle("config/charge-jdbc").getString("charge");//读取配置
		}else{
			logger.error("类型不支持,dbKeyType:"+dbKeyType);
			return null;
		}
		initDatasource(dbKeyType, JDBCURL_PREFIX_MYSQL+dbUrl);
		//logger.debug("==================="+((ComboPooledDataSource)dataSource).getJdbcUrl());
		return dataSources.get(dbKeyType);
	}
	
	private DataSource getDefaultDataSource(){
		DataSource defaultDataSource = dataSources.get(DataSourceType.DS_KEY_DEFAULT.getValue());
		if(defaultDataSource != null){
			return defaultDataSource;
		}
		init();
		return dataSources.get(DataSourceType.DS_KEY_DEFAULT.getValue());
	}
	
	@Override
	protected Object determineCurrentLookupKey() {
		return DataSourceContext.getDatasourceKey();
	}

	public void setJdbcProperty(PropertyResourceBundle jdbcProperty) {
		this.jdbcProperty = jdbcProperty;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	
	public void setCheckoutTimeout(int checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}

	public boolean containsDatasource(String key){
		return dataSources.containsKey(key);
	}

	public void setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
	}

	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	public void setAcquireRetryDelay(int acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}
}
