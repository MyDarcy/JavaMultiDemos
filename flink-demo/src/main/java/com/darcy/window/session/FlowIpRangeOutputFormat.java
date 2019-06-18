package com.darcy.window.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowIpRangeOutputFormat {

  private String group_key;
  private String proxy_ip;
  private String domain;
  private String instance_id;

  private String request_date;
  private String start_date;
  private String end_date;
  private long request_count;

  private String window_start_date;
  private String window_end_date;

  private boolean last_element;
  private boolean next_element;

}
