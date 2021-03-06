package com.zakgof.tools;

import com.zakgof.tools.generic.Functions;

public class ArrayUtil {
  
  public static <K> K[] insert(K[] array, K value, int pos) {
    @SuppressWarnings("unchecked")
    K[] result = Functions.newArray((Class<K>)array.getClass().getComponentType(), array.length + 1);
    System.arraycopy(array, 0, result, 0, pos);
    result[pos] = value;
    System.arraycopy(array, pos, result, pos + 1, array.length - pos);
    return result;
  }
  
  public static <K> K[] remove(K[] array, int pos) {
    @SuppressWarnings("unchecked")
    K[] result = Functions.newArray((Class<K>)array.getClass().getComponentType(), array.length - 1);
    System.arraycopy(array, 0, result, 0, pos);    
    System.arraycopy(array, pos + 1, result, pos, array.length - pos - 1);
    return result;
  }

}
