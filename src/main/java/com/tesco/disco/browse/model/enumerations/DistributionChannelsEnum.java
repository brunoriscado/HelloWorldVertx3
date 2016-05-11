package com.tesco.disco.browse.model.enumerations;

import org.elasticsearch.common.lang3.StringUtils;

/**
	* Created by bruno on 04/05/16.
	*/
public enum DistributionChannelsEnum {
				GHS("ghs");

				private String channelName;

				private DistributionChannelsEnum(String channelName) {
								this.channelName = channelName;
				}

				public String getChannelName() {
								return channelName;
				}

				public static DistributionChannelsEnum getByChannelName(String channelName) {
								for (int i = 0; i < DistributionChannelsEnum.values().length; i++) {
												if (StringUtils.isNotBlank(channelName) &&
																				channelName.equals(DistributionChannelsEnum.values()[i].getChannelName())) {
																return DistributionChannelsEnum.values()[i];
												}
								}
								return null;
				}
}
