package com.darcy.window.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowIpRangeInputData {

  private String group_key;
  private String proxy_ip;
  private String domain;
  private String instance_id;
  private long request_time;

}
