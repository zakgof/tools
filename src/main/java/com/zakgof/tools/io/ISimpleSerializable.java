package com.zakgof.tools.io;

import java.io.IOException;

public interface ISimpleSerializable {
  void writeState(SimpleOutputStream out) throws IOException;
  void readState(SimpleInputStream in) throws IOException;
}
