package mindthegap.util;

import java.util.Formatter;

public class Day implements Comparable<Day>{
	
	private final int day,month,year;

	public Day(int year, int month, int day) {
		
		if(year < 2000 || year > 2013)
			throw new IllegalArgumentException("Year = " + year);
		
		if(month < 1 || month > 12)
			throw new IllegalArgumentException("Month = " + month);
		
		
		this.day = day;
		this.month = month;
		this.year = year;
	}
	
	public Day(int packedDate){
		year  = (int) (packedDate >> 9 & (0x07 << 8 | 0xFF )) ;
		month = (int) (packedDate >> 5 & 0x0F) ;
		day   = (int) (packedDate      & 0x1f) ;
		
	}
	
	public int getPacked(){
		return (year << 9) | (month << 5) | day;
		//return day;
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}
	
	private String stringRep = null;
	
	public String toString(){
		if(stringRep == null){
			StringBuilder sb = new StringBuilder();
			Formatter formatter = new Formatter(sb);
			formatter.format("%d-%02d-%02d", year,month,day);
			stringRep = sb.toString();
		}
		return stringRep;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + year;
		result = prime * result + month;
		result = prime * result + day;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Day other = (Day) obj;
		if (day != other.day)
			return false;
		if (month != other.month)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
	
	

	@Override
	public int compareTo(Day o) {
		if(o.year != year)
			return year - o.year;
		if(o.month != month)
			return month - o.month;
		return day - o.day;
		
	}
}
