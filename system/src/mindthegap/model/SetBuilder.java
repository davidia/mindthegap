package mindthegap.model;

import java.util.*;

import mindthegap.util.Day;

import weka.core.*;

public class SetBuilder {
	
	private Map<Day,Instances> instanceMap;
	private Map<Day,List<MetaData>> metaDataMap;
	
	public Engine engine;

	public SetBuilder(Map<Day, Instances> instanceMap,Map<Day,List<MetaData>> metaDataMap,int max,int test,int train,int stride) {		
		engine=new Engine(stride, train, test,max);		
		this.instanceMap = instanceMap;
		this.metaDataMap = metaDataMap;
	}
	
	public class Range{
		final int start,end;

		public Range(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
		
	}
	
	public class Engine{
		
		private final int stride;
		private final int train;
		private final int test;
		private final int offset;
		
		public Engine(int stride, int train, int test, int max) {			
			this.stride = stride;
			this.train = train;
			this.test = test;			
			this.offset =  max - train;
		}
		
		public Range getTrainSet(int index){
			int start = index * stride + offset;
			int end = start + train -1;		
			return new Range(start, end);			
		}
		
		public Range getTestSet(int index){
			int start = index * stride + train + offset;
			int end = start + test -1;		
			return new Range(start, end);			
		}	
		
		public int size(){		
			return (instanceMap.size()-test-train-offset) / stride;		
		}
		
	}
	
	public int size(){
		return engine.size();			
	}
	
	public Instances getTrainSet(int index){		
		return instanceSubset(engine.getTrainSet(index));			
	}
	
	public Instances getTestSet(int index){			
		return instanceSubset(engine.getTestSet(index));			
	}
	
	public List<MetaData> getTrainMetaData(int index){
		return metaDataSubset(engine.getTrainSet(index));			
	}
	
	public List<MetaData> getTestMetaData(int index){		
		return metaDataSubset(engine.getTestSet(index));
	}

	private Instances instanceSubset(Range range) {
		Instances instances = null;		
		int j = 0;
		for(Instances i : instanceMap.values()){
			if(j == range.getStart()){
				instances = new Instances(i);
			} if(j>range.getStart() && j<range.getEnd()) {
				for(Instance ii : i){
					instances.add(ii);
				}
				//instances = Instances.mergeInstances(instances, i);
			}			
			j++;			
		}
		return instances;
	}
	
	private List<MetaData> metaDataSubset(Range range) {
		List<MetaData> metaData = null;		
		int j = 0;
		for(List<MetaData> m : metaDataMap.values()){
			if(j == range.getStart()){
				metaData = m;
			} if(j>range.getStart() && j<range.getEnd()) {
				metaData.addAll(m);
			}			
			j++;			
		}
		return metaData;
	}
	

}
