package mindthegap.activetick;

import java.util.List;

import mindthegap.data.Intraday;
import mindthegap.data.OHLC;


public interface BarHistoryEventListener {	
	void handleBarHistoryEvent(Intraday data);

}
