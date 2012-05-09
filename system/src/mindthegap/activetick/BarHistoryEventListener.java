package mindthegap.activetick;

import java.util.List;

import mindthegap.data.Intraday;


public interface BarHistoryEventListener {	
	void handleBarHistoryEvent(Intraday data);

}
