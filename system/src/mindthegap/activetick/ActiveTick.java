package mindthegap.activetick;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import mindthegap.MyListener;
import mindthegap.util.Day;
import mindthegap.util.DayTime;

import at.feedapi.ATCallback;
import at.feedapi.ActiveTickServerAPI;
import at.feedapi.Helpers;
import at.feedapi.Session;
import at.feedapi.ATCallback.*;
import at.shared.ATServerAPIDefines;
import at.shared.ATServerAPIDefines.ATCursorType;
import at.shared.ATServerAPIDefines.*;
import at.utils.jlib.Errors;

public class ActiveTick {
	
	private static Logger logger = Logger.getLogger("mindthegap.activetick");	
	

	public void barRequest(String symbol,DayTime begin,DayTime end){		
		r.barRequest(symbol, begin, end);
	}
	
	

	public void registerBarHistoryEventListener(BarHistoryEventListener bhel) {
		r.registerBarHistoryEventListener(bhel);
	}

	public void addTradetListener(TradeEventListener l){
		s.addTradeListener(l);
	}
	
	public void subscribeTrades(List<String> symbols){		
		
		ATStreamRequestType requestType = (new ATServerAPIDefines()).new ATStreamRequestType();
 		requestType.m_streamRequestType = ATStreamRequestType.StreamRequestSubscribe;
		
		List<ATSYMBOL> syms = new ArrayList<ATSYMBOL>();
		Iterator<String> i = symbols.iterator();
		while(i.hasNext()){
			syms.add(Helpers.StringToSymbol(i.next()));	
		}			
	    r.SendATQuoteStreamRequest(syms, syms.size(),requestType,ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT);	    				
	}	
	
	
	private Requestor r;
	private Streamer s;

	private	ActiveTickServerAPI			m_ATServerAPI = new ActiveTickServerAPI();
	private	at.feedapi.Session			m_ATSession;
	private	long						m_lastRequest;	
	
	private	SessionStatusChangeCallback m_sessionStatusChangeCallback = new SessionStatusChangeCallback();
	private	LoginResponseCallback 		m_loginResponseCallback = new LoginResponseCallback();
	private	RequestTimeoutCallback		m_requestTimeoutCallback = new RequestTimeoutCallback();
	private	ServerTimeUpdateCallback	m_serverTimeUpdateCallback = new ServerTimeUpdateCallback();
	
	private final String APIID = "7d7f0c3671654e88af36624c46bcaf73";
	private final String username = "dia80";
	private final String password = "DV1kelty";
	
	public void connect(){
		
		m_ATServerAPI.ATInitAPI();
		m_ATSession = m_ATServerAPI.ATCreateSession();
		s = new Streamer(m_ATSession);
		ATGUID apiUserId = (new ATServerAPIDefines()).new ATGUID();				
		apiUserId.SetGuid(APIID);
		
		
		//Destroy();	
	
		long errCode = m_ATServerAPI.ATSetAPIUserId(m_ATSession, apiUserId);
		//setup all the callbacks
		m_ATSession.SetServerTimeUpdateCallback(m_serverTimeUpdateCallback);

		if(errCode  == Errors.ERROR_SUCCESS)
			m_ATServerAPI.ATInitSession(m_ATSession, "activetick1.activetick.com", "activetick2.activetick.com", 443, m_sessionStatusChangeCallback);
		logger.log(Level.FINE,m_ATServerAPI.GetAPIVersionInformation());
		
		
		
		MyListener l = new MyListener(m_ATSession, true);			
		r = new Requestor(m_ATSession, l);
	}
	
	class LoginResponseCallback extends ATCallback implements ATLoginResponseCallback
	{
		String strLoginResponseType = "";
		
		public void process(Session session, long requestId, ATLOGIN_RESPONSE response)
		{
			switch(response.loginResponse.m_atLoginResponseType)
			{
			case ATServerAPIDefines.ATLoginResponseType.LoginResponseSuccess: strLoginResponseType = "LoginResponseSuccess"; break;
			case ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidUserid: strLoginResponseType = "LoginResponseInvalidUserid"; break;
			case ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidPassword: strLoginResponseType = "LoginResponseInvalidPassword"; break;
			case ATServerAPIDefines.ATLoginResponseType.LoginResponseInvalidRequest: strLoginResponseType = "LoginResponseInvalidRequest"; break;
			case ATServerAPIDefines.ATLoginResponseType.LoginResponseLoginDenied: strLoginResponseType = "LoginResponseLoginDenied"; break;
			case ATServerAPIDefines.ATLoginResponseType.LoginResponseServerError: strLoginResponseType = "LoginResponseServerError"; break;
			default: strLoginResponseType = "unknown"; break;
			}
			logger.log(Level.INFO,"RECV " + requestId + ": Login Response [" + strLoginResponseType + "]");					
		}
	}


	class ServerTimeUpdateCallback extends ATCallback implements ATServerTimeUpdateCallback
	{
		public void process(SYSTEMTIME serverTime)
		{
			logger.log(Level.FINE,"RECV: Server time update [" + serverTime.hour + ":" + serverTime.minute + 
					":" + serverTime.second + ":" + serverTime.month + ":" + serverTime.day + ":" + serverTime.year);
		}
	}

	class RequestTimeoutCallback extends ATCallback implements ATRequestTimeoutCallback
	{
		public void process(long origRequest)
		{
			logger.log(Level.WARNING,"(" + origRequest + "): Request timed-out\n");			
		}		
	}

	class SessionStatusChangeCallback extends ATCallback implements ATSessionStatusChangeCallback
	{
		public void process(at.feedapi.Session session, ATServerAPIDefines.ATSessionStatusType type)
		{
			String strStatusType = "";
			switch(type.m_atSessionStatusType)
			{
			case ATServerAPIDefines.ATSessionStatusType.SessionStatusConnected: strStatusType = "SessionStatusConnected"; break;
			case ATServerAPIDefines.ATSessionStatusType.SessionStatusDisconnected: strStatusType = "SessionStatusDisconnected"; break;
			case ATServerAPIDefines.ATSessionStatusType.SessionStatusDisconnectedDuplicateLogin: strStatusType = "SessionStatusDisconnectedDuplicateLogin"; break;
			default: break;
			}
			
			System.out.println(strStatusType);

			logger.log(Level.INFO,"RECV Status change [" + strStatusType + "]");			
			
			//if we are connected to the server, send a login request
			if(type.m_atSessionStatusType == ATServerAPIDefines.ATSessionStatusType.SessionStatusConnected)
			{
				
				m_lastRequest = m_ATServerAPI.ATCreateLoginRequest(session, username, password, m_loginResponseCallback);
				boolean rc = m_ATServerAPI.ATSendRequest(session, m_lastRequest, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT, m_requestTimeoutCallback);				
				logger.log(Level.INFO,"SEND (" + m_lastRequest + "): Login request [" + username + "] (rc = " + (char)Helpers.ConvertBooleanToByte(rc) + ")");
			}
		}
	}

	public  void disconnect() {
		
		m_ATServerAPI.ATShutdownSession(m_ATSession);
		m_ATServerAPI.ATShutdownAPI();
		
	}
}
