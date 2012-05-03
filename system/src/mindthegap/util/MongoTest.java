package mindthegap.util;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

import mindthegap.data.Bar;
import mindthegap.data.Intraday;
import mindthegap.data.OHLC;

import com.almworks.sqlite4java.*;
import com.mongodb.*;

public class MongoTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SQLiteConnection conn;

		Mongo m;
		try {
			m = new Mongo();
			DB db = m.getDB("mindthegap");
			DBCollection coll = db.getCollection("intraday");

			List<String> syms = new ArrayList<>();
			syms.add("PPG");
			syms.add("STZ");
			syms.add("PCS");
			syms.add("JCP");
			syms.add("X");

			BasicDBObject o = new BasicDBObject();
			Date start = new Date();
			for (String symbol : syms) {

				o.put("symbol", symbol);
				o.put("date", "2011-01-07");

				DBCursor dbc = coll.find(o);

				Intraday intraday = null;
				for (DBObject r : dbc) {

					List<DBObject> dbBars = (List<DBObject>) r.get("bars");
					List<Bar> bars = new ArrayList<>();
					for (DBObject bar : dbBars) {
						bars.add(new Bar(new Time((int) bar.get("minute")),
								(double) bar.get("open"), (double) bar
										.get("high"), (double) bar.get("low"),
								(double) bar.get("close")));

					}
					intraday = new Intraday("X", new Day(2011, 1, 1), bars);
				}
			}
			Date end = new Date();
			System.out.println((end.getTime() - start.getTime()));

			// System.out.println(intraday);

			// }
		} catch (UnknownHostException | MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
