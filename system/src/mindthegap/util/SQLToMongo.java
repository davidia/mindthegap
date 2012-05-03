package mindthegap.util;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

import mindthegap.data.OHLC;

import com.almworks.sqlite4java.*;
import com.mongodb.*;

public class SQLToMongo {

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
			coll.drop();
			coll = db.getCollection("intraday");

			coll.ensureIndex("symbol");
			BasicDBObject doc = new BasicDBObject();
			doc.put("symbol", "AAPL");
			doc.put("date", "20110326");
			doc.put("bars", "20110326");

			conn = new SQLiteConnection(new File(
					"/home/david/trading/mindthegap/mindthegap.db"));
			conn.open();

			SQLiteStatement tables = conn
					.prepare("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");

			while (tables.step()) {
				String table = tables.columnString(0);

				System.out.println(table);

				// skip non data tables
				if (table.length() > 4)
					continue;

				SQLiteStatement tabledata = conn.prepare("SELECT * FROM '"
						+ table + "' ORDER BY time");

				Day last = null;
				BasicDBObject currentDoc = null;
				List<BasicDBObject> bars = new ArrayList<>();
				Set<Integer> minutes = new TreeSet<>();
				while (tabledata.step()) {
					Date date = new Date(1000 * tabledata.columnLong(0));
					DayTime dayTime = new DayTime(date);

					BasicDBObject bar = new BasicDBObject();

					int minute = dayTime.getTime().asMinutes();

					if (!minutes.contains(minute)) {
						bar.put("minute", minute);
						bar.put("open", tabledata.columnDouble(1));
						bar.put("high", tabledata.columnDouble(2));
						bar.put("low", tabledata.columnDouble(3));
						bar.put("close", tabledata.columnDouble(4));
						minutes.add(minute);
						bars.add(bar);
					}

					if (!dayTime.getDay().equals(last)) {
						if (currentDoc != null) {
							currentDoc.put("date", dayTime.getDay().toString());
							coll.insert(currentDoc);
						}
						minutes = new TreeSet<>();
						currentDoc = new BasicDBObject();
						bars = new ArrayList<>();
						currentDoc.put("date", dayTime.getDay().toString());
						currentDoc.put("symbol", table);
						currentDoc.put("bars", bars);
					}
					last = dayTime.getDay();
				}
			}
		} catch (SQLiteException | UnknownHostException | MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
