package com.zakgof.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.zakgof.tools.generic.IFunction;
import com.zakgof.tools.generic.IProvider;

public class Merger<C extends Comparable<C>> {

  private class Source<T> {
    public Source(IProvider<T> stream, IFunction<T, C> metric) {
      this.stream = stream;
      this.metric = metric;
    }    
    private final IFunction<T, C> metric;
    private final IProvider<T> stream;
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
      return -source.metric.get(object).compareTo(that.source.metric.get(that.object));
    }

  }
  
  public <T> Wrapper<T> wrapFromStream(Source<T> src) {
    T obj = src.stream.get();
    if (obj != null)
      return new Wrapper<T>(obj, src);
    return null;
  }

  public <T> void addSource(IFunction<T, C> metric, IProvider<T> stream) {
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

  public Object next() {    
    Wrapper<?> w = queue.poll();
    if (w == null)
      return null;
    feedQueue(w.source);            
    return w.object;     
  }

}