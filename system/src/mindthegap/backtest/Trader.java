package mindthegap.backtest;

import java.util.ArrayList;
import java.util.List;

import mindthegap.data.Bar;
import mindthegap.data.DataSource;
import mindthegap.data.Intraday;
import mindthegap.model.MetaData;
import mindthegap.util.Day;
import mindthegap.util.DayTime;
import mindthegap.util.Direction;
import mindthegap.util.Time;
import mindthegap.util.Trade;
import weka.core.Instance;
import weka.core.Instances;

public class Trader {

	DataSource dataSource;

	double stop = 0.015;
	double profit = 0.022;

	public Trader(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<Trade> simulate(List<MetaData> testData, List<Double> classes) {

		List<Trade> trades = new ArrayList<Trade>();

		// It's a buy!
		double open = 0, close;
//
		double max = Double.MIN_VALUE;
		int tradei = 0;
		for (int j = 0; j < classes.size(); j++) {
			double c = classes.get(j);
			if (c > max) {
				max = c;
				tradei = j;
			}
		}
		if(classes.size() > 0 && max > 0.018){
			profit = stop = max;
	//	for(int tradei=0;tradei<testData.size();tradei++){
		//	if(classes.get(tradei) > 0.007){
			MetaData md = testData.get(tradei);
			String symbol = md.getSymbol();
			Intraday intradayData = dataSource.getIntraday(symbol, md.getDate());
			//(symbol, md.getDate(), new Time(9,30), new Time(10,00));
			if(intradayData.size() > 0 && intradayData.get(new Time(9,30)) != null){
				Trade trade = buy(symbol,md.getDate(), intradayData);
				trade.setMeta(new Double(max).toString());
				trades.add(trade);			
			}
		}
		//}
		
		

		return trades;
	}

	private Trade buy(String symbol,Day day, Intraday intradayData) {
		Trade trade = new Trade(symbol, Direction.Buy);	

		for (Bar ohlc : intradayData) {
			if (!trade.isOpen() && ohlc.getTime().after( new Time(9,29) )  ){
				trade.open(new DayTime(day,ohlc.getTime()), ohlc.open());
			}

			if (trade.isOpen()) {
				if (ohlc.low() <= trade.getOpen() * (1 - stop)) {
					trade.close(new DayTime(day,ohlc.getTime()), trade.getOpen() * (1 - stop));
					break;
				}
				if (ohlc.high() >= trade.getOpen() * (1 + profit)) {
					trade.close(new DayTime(day,ohlc.getTime()), trade.getOpen() * (1 + profit));
					break;
				}
				if (ohlc.getTime().after(new Time(10,10))) {
					trade.close(new DayTime(day,ohlc.getTime()), ohlc.close());
					break;
				}
			}
		}
		
		if (trade.isOpen()){
			Bar ohlc = intradayData.last();
			trade.close(new DayTime(day,ohlc.getTime()), ohlc.close());
		}
		
		if(!trade.isClosed()){
			System.out.println("bahh");
		}
		
		return trade;
	}

}
