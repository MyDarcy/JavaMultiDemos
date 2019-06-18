package com.darcy.window.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowIpRangeOutputData {

  private long request_date;
  private long start_date;
  private long end_date;
  private long request_count;

  private boolean last_element;
  private boolean next_element;

}
