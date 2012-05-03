package mindthegap.model;

import weka.core.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mindthegap.data.Bar;
import mindthegap.data.DataSource;
import mindthegap.data.Intraday;
import mindthegap.data.OHLC;
import mindthegap.util.Day;
import mindthegap.util.Time;

public class FeatureBuilder {

	//Map<Date, Instances> byDate = new TreeMap<Date, Instances>();
	
	private final Map<Day, Instances> instanceMap = new TreeMap<>();
	private final Map<Day, List<MetaData>> metaDataMap = new TreeMap<>();
	
	private final DataSource dataSource;

	public FeatureBuilder(DataSource dataSource) {
		this.dataSource = dataSource;
		
		
		attributes.add(gap);
		attributes.add(prevRet);
		attributes.add(yestLow);
		attributes.add(index);
		attributes.add(mktCap);
		attributes.add(target);
	}
	
	private double getPrevReturn(String symbol,Day d){
		int index = dataSource.indexForDate(d);
		double c1 = dataSource.getDay(symbol, index - 1).close();
		double c2 = dataSource.getDay(symbol, index - 2).close();		
		return(c2/c1 - 1);		
	}
	
	private double getPrevLow(String symbol,Day d){
		int index = dataSource.indexForDate(d);
		double open = dataSource.getDay(symbol, index).open();
		double low = dataSource.getDay(symbol, index - 1).low();
				
		return(open/low - 1);		
	}
	
	private double getIndexGap(Day d){
		int index = dataSource.indexForDate(d);
		double c = dataSource.getIndex(index-1).close();
		double o = dataSource.getIndex(index).open();		
		return(o/c - 1);		
	}
	
	private double getGap(String symbol,Day d){
		int index = dataSource.indexForDate(d);
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
			dataSource.check(symbol,day); 
		}
		
	}
	
	Attribute gap = new Attribute("gap");
	Attribute prevRet = new Attribute("prevRet");
	Attribute yestLow = new Attribute("yestLow");
	Attribute index = new Attribute("index");
	Attribute mktCap = new Attribute("mktCap");
	Attribute target = new Attribute("target");
	
	ArrayList<Attribute> attributes = new ArrayList<>();

	
	public void createFeatures(int itemsPerDay){
		Map<Day, List<String>> candidates = filterCandidates(itemsPerDay);
		
		//ExecutorService es = Executors.newFixedThreadPool(6);
		System.out.println("Beginning prefetch");
		for(Day day : candidates.keySet()){
			for(String symbol : candidates.get(day)){
				dataSource.check(symbol, day);
				//es.submit(new Check(symbol, day));				
			}
		}
		
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

		
		
		for (Day d : candidates.keySet()) {
			List<String> syms = candidates.get(d);
			createInstances(d, syms);
		//	es.submit(new Lame(d, syms));
		}
		
//		try {
//			es.awaitTermination(30, TimeUnit.SECONDS);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
	
	private class Lame implements Runnable{
		private Day day;
		private List<String> syms;

		public Lame(Day day, List<String> syms) {
		
			this.day = day;
			this.syms = syms;
		}



		@Override
		public void run() {
			createInstances(day, syms);
			
		}
				
	}

	private void createInstances(Day d, List<String> syms) {
		Instances instances = new Instances(d.toString(), attributes, syms.size());
		instances.setClass(target);
		List<MetaData> metaData = new ArrayList<MetaData>();
			
		for(String symbol : syms){
			Instance instance = new DenseInstance(attributes.size());
			
			try{
			instance.setValue(gap, getGap(symbol,d));
			instance.setValue(prevRet, getPrevReturn(symbol, d));
			instance.setValue(yestLow, getPrevLow(symbol, d));
			instance.setValue(index, getIndexGap(d));
			instance.setValue(mktCap, dataSource.getMarketCap(symbol));
			instance.setValue(target, getReturn(symbol,d,60));
			instances.add(instance);
			metaData.add(new MetaData(symbol, d));
			}catch(Exception e){
				System.out.println("skipped instance: " + symbol + " " + d);
				//e.printStackTrace();
			}
		}
		instanceMap.put(d,instances);
		metaDataMap.put(d,metaData);
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

	public Map<Day, List<String>> filterCandidates(int perDay) {

		Map<Day, List<String>> candidates = new TreeMap<>();
		List<Day> dates = dataSource.getDates();
		
		for (int i = 2; i < dates.size(); i++) {
			Day date = dates.get(i);
			TreeMap<Double, String> map = new TreeMap<Double, String>();
			for(String symbol : dataSource.getSymbols()){				
				map.put(getGap(symbol, date), symbol);
			}			
			
			ArrayList<String> list = new ArrayList<String>();
						
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
			candidates.put(date,list);			
		}
		
		return candidates;
	}

	public Map<Day, List<MetaData>> getMetaData() {
		return metaDataMap;
	}

	public Map<Day, Instances> getInstances() {
		return instanceMap;
	}
}
