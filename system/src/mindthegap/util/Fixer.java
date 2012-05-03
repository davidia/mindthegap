package mindthegap.util;

import java.io.*;
import java.util.*;

import mindthegap.data.OHLC;


import at.shared.TickerPlant.Symbol;

import com.almworks.sqlite4java.*;


public class Fixer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SQLiteConnection conn;

		try {

			conn = new SQLiteConnection(new File(
					"/home/david/trading/mindthegap/mindthegap.db"));
			conn.open();
			
			SQLiteStatement st = conn.prepare("SELECT DISTINCT symbol FROM minute");										
			SQLiteStatement delete = conn.prepare("delete from minute where symbol=?");
			
						
			while (st.step()) {				
				String symbol = st.columnString(0);
				
				System.out.println(symbol);
				conn.exec("create table '"+symbol+"' (time INTEGER,open REAL,high REAL,low REAL,close REAL)");
				conn.exec("INSERT INTO '"+symbol+"' SELECT time,open,high,low,close FROM minute where symbol='"+symbol+"'");
				conn.exec("CREATE INDEX "+symbol+"_ind ON '"+symbol+"' (time)");
				delete.reset().bind(1,symbol).step();				
			}
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
