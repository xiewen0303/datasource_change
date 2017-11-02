package com.spring.datasource;

/**
 * 数据源类型
 * @author  作者：wind
 * @version 创建时间：2017-11-2 下午3:07:48
 */
public enum DataSourceType {
	/**
	 * 默认数据库
	 */
	DS_KEY_DEFAULT("query"),
	/**
	 * 游戏库
	 */
	DS_KEY_GAMESERVER("gameserver"),
	/**
	 * 日志库
	 */
	DS_KEY_LOG2DB_SERVER("logStatistics"),
	/**
	 * 充值库
	 */
	DS_KEY_CHARGE("charge"),
	/**
	 * 插件统计库
	 */
	DS_KEY_PLUGIN("plugin");
	
	private String value;

	public String getValue() {
		return value;
	}
	
	private DataSourceType(String value) {
		this.value = value;
	}
}
