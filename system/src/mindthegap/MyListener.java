package mindthegap;

import javax.jws.Oneway;

import at.feedapi.ActiveTickStreamListener;
import at.feedapi.Session;
import at.shared.ATServerAPIDefines;
import at.utils.jlib.PrintfFormat;


public class MyListener extends ActiveTickStreamListener{


	public MyListener(Session session, boolean processAllUpdates) {
		super(session, processAllUpdates);
		// TODO Auto-generated constructor stub
	}	
	
	protected void OnATStreamTradeUpdate(ATServerAPIDefines.ATQUOTESTREAM_TRADE_UPDATE update)
	{		
		String strSymbol = new String(update.symbol.symbol);
		int plainSymbolIndex = strSymbol.indexOf((byte)0);
		strSymbol = strSymbol.substring(0, plainSymbolIndex);
		//UpdateVolume(strSymbol, update.lastSize);
		StringBuffer sb = new StringBuffer();
		//sb.append(++msgcount);
		sb.append("     ");
		sb.append("RECV: [");
		sb.append(update.lastDateTime.hour);
		sb.append(":");
		sb.append(update.lastDateTime.minute);
		sb.append(":");
		sb.append(update.lastDateTime.second);
		sb.append(":");
		sb.append(update.lastDateTime.milliseconds);
		sb.append("] STREAMTRADE [symbol:");
		sb.append(strSymbol);
		sb.append(" last:");
		
		String strFormat = "%0." + update.lastPrice.precision + "f";
		sb.append(new PrintfFormat(strFormat).sprintf(update.lastPrice.price));
		//sb.append(update.lastPrice.price);
		sb.append(" lastSize:");
		sb.append(update.lastSize);
		sb.append(" volume: ");
		//sb.append(m_mapVolume.get(strSymbol));
		sb.append("]");
		System.out.println(sb.toString());
	}
	protected void OnATStreamQuoteUpdate(ATServerAPIDefines.ATQUOTESTREAM_QUOTE_UPDATE update) 
	{
		String strSymbol = new String(update.symbol.symbol);
		int plainSymbolIndex = strSymbol.indexOf((byte)0);
		strSymbol = strSymbol.substring(0, plainSymbolIndex);
		StringBuffer sb = new StringBuffer();
		sb.append("RECV: [");
		sb.append(update.quoteDateTime.hour);
		sb.append(":");
		sb.append(update.quoteDateTime.minute);
		sb.append(":");
		sb.append(update.quoteDateTime.second);
		sb.append(":");
		sb.append(update.quoteDateTime.milliseconds);
		sb.append("] STREAMQUOTE [symbol:");
		sb.append(strSymbol);
		sb.append(" bid:");
		String strFormat = "%0." + update.bidPrice.precision + "f";
		sb.append(new PrintfFormat(strFormat).sprintf(update.bidPrice.price));
		sb.append(" ask:");
		strFormat = "%0." + update.askPrice.precision + "f";
		sb.append(new PrintfFormat(strFormat).sprintf(update.askPrice.price));
		sb.append(" bidSize:");
		sb.append(update.bidSize);
		sb.append(" askSize:");
		sb.append(update.askSize);
		sb.append("]");
		System.out.println(sb.toString());	
	}
	
}
