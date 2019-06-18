package com.darcy.window.session;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;

public class FlowKeyNotNullFilter implements FilterFunction<FlowIpRangeInputData> {

  @Override
  public boolean filter(FlowIpRangeInputData value) throws Exception {
    return StringUtils.isNotBlank(value.getGroup_key()) && StringUtils.isNotBlank(value.getProxy_ip())
        && StringUtils.isNotBlank(value.getDomain()) && StringUtils.isNotBlank(value.getInstance_id());
  }
}