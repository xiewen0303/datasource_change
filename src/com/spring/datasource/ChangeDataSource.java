package com.spring.datasource;

import java.sql.Connection;

/**
 * 
 * @author  作者：wind
 * @version 创建时间：2017-11-2 下午2:35:31
 */
public class ChangeDataSource {
	
	public static void main(String[] args) throws Exception {
		
		DataSourceContext.setDatasourceKey(DataSourceType.DS_KEY_DEFAULT);
		
		DataSourceHandler dataSourceHandler = new DataSourceHandler();
		Connection conn = dataSourceHandler.getConnection();
		System.out.println(conn);
	}
}