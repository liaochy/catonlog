package com.sohu.goldmine;

import com.taobao.metamorphosis.client.MetaClientConfig;
import com.taobao.metamorphosis.utils.ZkUtils.ZKConfig;

public class Help {
	public static MetaClientConfig initMetaConfig() {
		final MetaClientConfig metaClientConfig = new MetaClientConfig();
		final ZKConfig zkConfig = new ZKConfig();
		zkConfig.zkConnect = JobConfig.getString("metaq.zk.host");
		zkConfig.zkRoot = JobConfig.getString("metaq.zk.root");
		metaClientConfig.setZkConfig(zkConfig);
		return metaClientConfig;
	}
}