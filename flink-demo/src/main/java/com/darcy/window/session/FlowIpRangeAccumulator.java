package com.darcy.window.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowIpRangeAccumulator {

  private boolean first_element;
  private boolean pre_element;

  private long current_date;
  private long start_date;
  private long end_date;
  private long current_request_count;

  private boolean next_element;
  private long next_date;
  private long next_request_count;

  public FlowIpRangeAccumulator(boolean first_element) {
    this.first_element = first_element;
  }

}