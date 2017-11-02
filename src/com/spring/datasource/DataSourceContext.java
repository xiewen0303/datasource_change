package com.spring.datasource;

/**
 * 设置数据源key
 * @author  作者：wind
 * @version 创建时间：2017-11-2 下午2:57:15
 */
public class DataSourceContext {
	
	private static ThreadLocal<String> datasourceKey = new ThreadLocal<String>();
	private static ThreadLocal<String> serverId = new ThreadLocal<String>();
	
	/**
	 * 获取当前设置的服务器ID
	 * @return
	 */
	public static ThreadLocal<String> getServerId() {
		return serverId;
	}

	/**
	 * 设置服务器Id
	 * @param serverId
	 */
	public static void setServerId(ThreadLocal<String> serverId) {
		DataSourceContext.serverId = serverId;
	}

	/**
	 * 设置当前上下文的数据源key
	 * @param key {@link DataSourceType}
	 */
	public static void setDatasourceKey(DataSourceType key){
		datasourceKey.set(key.getValue());
	}
	
	/**
	 * 获取当前上下文的数据源key
	 */
	public static String getDatasourceKey(){
		return datasourceKey.get();
	}
}
