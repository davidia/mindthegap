package mindthegap.model;

import weka.core.*;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import mindthegap.data.Bar;
import mindthegap.data.DataSource;
import mindthegap.data.Intraday;
import mindthegap.util.Day;
import mindthegap.util.Time;

public class FeatureBuilder {

	//Map<Date, Instances> byDate = new TreeMap<Date, Instances>();
	
	private final Map<Day, Instances> instanceMap = new TreeMap<>();
	private final Map<Day, List<MetaData>> metaDataMap = new TreeMap<>();
	
	private final DataSource dataSource;
	
	Attribute gap = new Attribute("gap");
	Attribute prevRet = new Attribute("prevRet");
	Attribute prevClose = new Attribute("prevClose");
	Attribute prevRelation;
	Attribute yestLow = new Attribute("yestLow");
	Attribute indexRet = new Attribute("index");
	Attribute mktCap = new Attribute("mktCap");
	Attribute dividend = new Attribute("dividend");
	Attribute target = new Attribute("target");
	
	ArrayList<Attribute> attributes = new ArrayList<>();

	public FeatureBuilder(DataSource dataSource) {
		this.dataSource = dataSource;
		
		List<String> my_nominal_values = new ArrayList<String>(3); 
		my_nominal_values.add("body"); 
		my_nominal_values.add("tail"); 
		my_nominal_values.add("below"); 
		
		prevRelation = new Attribute("prevRelation",my_nominal_values);
		
		attributes.add(gap);
		attributes.add(prevRet);
		attributes.add(prevClose);
		attributes.add(prevRelation);
		attributes.add(yestLow);
		attributes.add(indexRet);
		attributes.add(mktCap);
		attributes.add(dividend);
		attributes.add(target);
	}
	
	private double getPrevReturn(String symbol,int index){		
		double c1 = dataSource.getDay(symbol, index - 1).close();
		double c2 = dataSource.getDay(symbol, index - 2).close();		
		return(c2/c1 - 1);		
	}
	
//	private double getPrevClose(String symbol,int index){		
//		
//		return dataSource.getDay(symbol, index - 1).close();							
//	}
	
	
	private double getPrevLow(String symbol,int index){		
		double open = dataSource.getDay(symbol, index).open();
		double low = dataSource.getDay(symbol, index - 1).low();				
		return(open/low - 1);		
	}
	
	private double getPrevClose(String symbol,int index){		
		double open = dataSource.getDay(symbol, index).open();
		double close = dataSource.getDay(symbol, index - 1).close();				
		return(open/close - 1);		
	}
	
	private String getPrevRelation(String symbol,int index){		
		Bar y = dataSource.getDay(symbol, index - 1);
		double o = dataSource.getDay(symbol, index).open();		
		double open = y.open();
		double low = y.low();
		if( o > open)
			return "body";
		if( o > low)
			return "tail";
		return "below";
	}
	
	private double getIndexGap(int index){		
		double c = dataSource.getIndex(index-1).close();
		double o = dataSource.getIndex(index).open();		
		return(o/c - 1);		
	}
	
	private double getGap(String symbol,int index){		
		double prevClose = dataSource.getDay(symbol, index - 1).close();
		double open = dataSource.getDay(symbol, index).open();
		return(open/prevClose - 1);
	}
	
	public String report(){
		StringBuilder sb = new StringBuilder();
		for(Day d : metaDataMap.keySet()){
			sb.append(d);
			sb.append(" ");
			sb.append(metaDataMap.get(d).size());
			sb.append("\n");
		}
		return sb.toString();
	}
	

	private class Check implements Runnable{

		
		
		public Check(String symbol, Day day) {

			this.symbol = symbol;
			this.day = day;
		}

		private String symbol;
		private Day day;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			dataSource.ensureIntraday(symbol,day); 
		}
		
	}
	


	
	public void createFeatures(int itemsPerDay){
		List<Collection<String>> candidates = filterCandidates(itemsPerDay);
		
		//ExecutorService es = Executors.newFixedThreadPool(6);
		System.out.println("Beginning prefetch");
		for (int i = 0; i < candidates.size(); i++) {
			for(String symbol : candidates.get(i)){
				dataSource.ensureIntraday(symbol, dataSource.getDates().get(i));
			}					
		}
		
		dataSource.waitForDownloads();
		
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		
		
		System.out.println("Prefetch queued");
//		es.shutdown();
//		try {
//			es.awaitTermination(30, TimeUnit.SECONDS);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		System.out.println("Prefetch done");
		
		// TODO add previous days low

		
		
		for (int i = 0 ; i < candidates.size();i++) {
			
			createInstances(i, candidates.get(i));
		//	es.submit(new Lame(d, syms));
		}
		
//		try {
//			es.awaitTermination(30, TimeUnit.SECONDS);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
//	
//	private class Lame implements Runnable{
//		private Day day;
//		private List<String> syms;
//
//		public Lame(Day day, List<String> syms) {
//		
//			this.day = day;
//			this.syms = syms;
//		}
//
//
//
//		@Override
//		public void run() {
//			createInstances(day, syms);
//			
//		}
//				
//	}

	private void createInstances(int index, Collection<String> syms) {
		Day d = dataSource.getDates().get(index);
		Instances instances = new Instances(d.toString(), attributes, syms.size());
		instances.setClass(target);
		List<MetaData> metaData = new ArrayList<MetaData>();
			
		for(String symbol : syms){
			Instance instance = new DenseInstance(attributes.size());
			
			try{
			instance.setValue(gap, getGap(symbol,index));
			instance.setValue(prevRet, getPrevReturn(symbol, index));
			instance.setValue(yestLow, getPrevLow(symbol, index));
			instance.setValue(prevClose, getPrevClose(symbol, index));
			instance.setValue(prevRelation, getPrevRelation(symbol, index));
			instance.setValue(indexRet, getIndexGap(index));
			instance.setValue(dividend, dataSource.getDividend(symbol,d) / dataSource.getDay(symbol, index-1).close() );
			instance.setValue(mktCap, dataSource.getMarketCap(symbol));
			//instance.setValue(target, getReturn(symbol,d,30));
			instance.setValue(target, getMax(symbol,d,60));
			instances.add(instance);
			metaData.add(new MetaData(symbol, d));
			}catch(Exception e){
				System.out.println("skipped instance: " + symbol + " " + d);
				e.printStackTrace();
			}
		}
		instanceMap.put(d,instances);
		metaDataMap.put(d,metaData);
	}

	
	private double getMax(String symbol, Day d, int minutes) throws Exception {
		Time start = new Time(9,30);
		Time end = start.add(minutes-1);
		Intraday intraday = dataSource.getIntraday(symbol, d);		
		
		if(intraday == null)
			throw new Exception("no intraday data");
		
		double max = 0;
		
		for (Bar bar : intraday) {
			
			if(bar.getDayTime().getTime().before(start)){
				continue;
			}
			
			
			
			if(bar.getDayTime().getTime().after(end)){
				break;			
			}
			
			max = Math.max(max, bar.high());
			
		}
		
		Bar first = intraday.get(start);
		
		if(first == null)
			throw new Exception("no bar for " + start);
		
		double open  = first.open();
		
		
		return max / open - 1;		
	}
	

	private double getReturn(String symbol, Day d, int minutes) throws Exception {
		Time start = new Time(9,30);
		Time end = start.add(minutes-1);
		Intraday intraday = dataSource.getIntraday(symbol, d);		
		
		if(intraday == null)
			throw new Exception("no intraday data");
		
		Bar first = intraday.get(start);
		
		if(first == null)
			throw new Exception("no bar for " + start);
		
		Bar last = intraday.get(end);
		
		
		if(last == null)
			throw new Exception("no intraday data for " + end);
		
		double open  = first.open();
		double close = last.close();		
		
		return close / open - 1;		
	}

	public List<Collection<String>> filterCandidates(int perDay) {

		List<Collection<String>> candidates = new ArrayList<>();
		List<Day> dates = dataSource.getDates();
		
		
		//skip first two
		ArrayList<String> list = new ArrayList<String>();
		candidates.add(list);	
		candidates.add(list);
		
		
		for (int i = 2; i < dates.size(); i++) {
			TreeMap<Double, String> map = new TreeMap<Double, String>();
			for(String symbol : dataSource.getSymbols()){
				
				if(getGap(symbol,i) > -0.2)
					map.put(getGap(symbol, i), symbol);
				
//				try{
//				if(dataSource.getDividend(symbol, dates.get(i)) != 0 ) {
//					
//				}
//				}catch (NullPointerException e) {
//					// TODO: handle exception
//				}
			}			
			
			list = new ArrayList<String>();
						
			int gapCount = 0;
			//rely on fact TreeMap keys are ascending
			//System.out.println(date);
			for (String symbol: map.values()) {							
				list.add(symbol);
				//System.out.println(symbol + " " + getGap(symbol,date));
				
				gapCount++;
				if (gapCount == perDay)
					break;			
			}			
			candidates.add(list);			
		}
		
		return candidates;
	}

	public Map<Day, List<MetaData>> getMetaData() {
		return metaDataMap;
	}

	public Map<Day, Instances> getInstances() {
		return instanceMap;
	}

	public void dump(String string) {
		Instances instances = null;				
		for(Instances i : instanceMap.values()){
			
			if(instances == null){
				instances = new Instances(i);
			} else{
				for(Instance ii : i){
					instances.add(ii);
				}
			}
		}
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		try {
			saver.setFile(new File(string));
			saver.writeBatch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
