package mindthegap;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.yaml.snakeyaml.Yaml;

import weka.core.Instances;

import mindthegap.activetick.ActiveTick;
import mindthegap.backtest.Trader;
import mindthegap.data.DataSource;
import mindthegap.model.FeatureBuilder;
import mindthegap.model.MetaData;
import mindthegap.model.Model;
import mindthegap.model.SetBuilder;
import mindthegap.util.Trade;

public class MindTheGap {

	// ActiveTick at = new ActiveTick();

	private Map<String, Object> config;

	public MindTheGap(Map<String, Object> config) {
		this.config = config;
	}

	private static Date getDate(int y, int m, int d) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(y, m - 1, d);
		return gc.getTime();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//
//		InputStream input = null;
//		try {
//			input = new FileInputStream(new File("config/config.yaml"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Yaml yaml = new Yaml();
//		Map<String, Object> config = (Map<String, Object>) yaml.load(input);
//
//		MindTheGap mtg = new MindTheGap(config);

		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.UK);

		ActiveTick at = new ActiveTick();
		at.connect();
	
		DataSource dataSource = new DataSource(at,
				"/home/david/trading/mindthegap/sp500",
				"/home/david/trading/mindthegap/mindthegap.db");
		
		int test   =  1;
		int train  = 10;
		int maxTest = 50;
		int stride =  1;
		FeatureBuilder fb = new FeatureBuilder(dataSource);
		fb.createFeatures(30);
		SetBuilder setBuilder = new SetBuilder(fb.getInstances(), fb.getMetaData(),maxTest, test, train, stride);
		
		Trader trader = new Trader(dataSource);
		Model model = new Model();
		
		List<Trade> trades = new ArrayList<>(); 
		
		for(int i=0; i < setBuilder.size(); i++){
			
			model.train(setBuilder.getTrainSet(i));
			Instances testSet = setBuilder.getTestSet(i);
			List<Double> classes = model.classify(testSet);
			
			List<MetaData> metaData = setBuilder.getTestMetaData(i);
			
			
//			System.out.println(classes);
//			for(int j=0; j<classes.size();j++){
//				MetaData md = metaData.get(j);				
//				formatter.format("%s %s %1.3f => %1.3f\n",md.getSymbol(),md.getDate(),classes.get(j),testSet.get(j).value(2));
//			}
			
			trades.addAll(trader.simulate(metaData, classes));
			
		}
//		System.out.println(sb);
//
				int wins = 0, losses = 0;

		double ret = 0;
		for (Trade t : trades) {
			ret += t.getReturn();
			if (t.getReturn() > 0)
				wins++;
			if (t.getReturn() < 0)
				losses++;
			System.out.println(t);
		}

		double tradeCount = trades.size();

	
		formatter.format("Trades: %d\n", trades.size());
		formatter.format("Wins: %d (%2.1f%%)\n", wins, 100 * wins / tradeCount);
		formatter.format("Losses: %d (%2.1f%%)\n", losses, 100 * losses
				/ tradeCount);
		formatter.format("Return: %2.1f%% => %2.2f%% per trade\n", ret * 100,
				ret * 100 / tradeCount);
		System.out.println(sb);
		
		//System.out.println(fb.report());
		
		at.disconnect();

	}
}
