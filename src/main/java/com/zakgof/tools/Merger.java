package com.zakgof.tools;

import java.util.*;
import java.util.Map.Entry;

import com.annimon.stream.function.Function;
import com.annimon.stream.function.Supplier;


public class Merger<C extends Comparable<C>> {
  
  private Comparator<C> comparator;


  public Merger(Comparator<C> comparator) {
    this.comparator = comparator;
  }

  private class Source<T> {
    public Source(Supplier<T> stream, Function<T, C> metric) {
      this.stream = stream;
      this.metric = metric;
    }    
    private final Function<T, C> metric;
    private final Supplier<T> stream;
  }

  private class Wrapper<T> implements Comparable<Wrapper<T>> {
    T object;
    Source<T> source;

    public Wrapper(T obj, Source<T> src) {
      this.object = obj;
      this.source = src;
    }

    @Override
    public int compareTo(Wrapper<T> that) {
      return comparator.compare(metric(), that.metric());
    }

    public C metric() {
      return source.metric.apply(object);
    }

  }
  
  public <T> Wrapper<T> wrapFromStream(Source<T> src) {
    T obj = src.stream.get();
    if (obj != null)
      return new Wrapper<T>(obj, src);
    return null;
  }

  public <T> void addSource(Function<T, C> metric, Supplier<T> stream) {
    sources.add(new Source<T>(stream, metric)); 
  }

  private final PriorityQueue<Wrapper<?>> queue = new PriorityQueue<>();
  private final List<Source<?>> sources = new ArrayList<>();
  
  
  public void run() {
    for (Source<?> src : sources)
      feedQueue(src);    
  }

  private <T> void feedQueue(Source<T> src) {
    Wrapper<T> w = wrapFromStream(src);
    if (w != null)
      queue.add(w);
  }

  public Entry<C, Object> next() {    
    Wrapper<?> w = queue.poll();
    if (w == null)
      return null;
    feedQueue(w.source);            
    return new Entry<C, Object>() {

      @Override
      public C getKey() {
        return w.metric();
      }

      @Override
      public Object getValue() {
        return w.object;
      }

      @Override
      public Object setValue(Object value) {
        return null;
      }
    };
  }

}