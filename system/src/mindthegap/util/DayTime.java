package mindthegap.util;

import java.util.Date;

public class DayTime {
	
	private final Day day;
	private final Time time;
	
	
	public int pack(){
		int d = day.getPacked();
		return d << 11 | time.asMinutes();
	}
	
	public static DayTime unpack(int packed){
		int minutes = packed & (0x07 << 8 | 0xff);
		return new DayTime(new Day(packed >> 11),new Time(minutes));
	}
	
	@SuppressWarnings("deprecation")
	public DayTime(Date date){
		day = new Day(date.getYear() + 1900,date.getMonth()+1,date.getDate());
		time = new Time(date.getHours(),date.getMinutes());
	}
	
	public DayTime(int year,int month,int day,int hour,int minute){
		this.day = new Day(year,month,day);
		time = new Time(hour,minute);
	}
	
	public DayTime(Day day, Time time) {
		this.day = day;
		// TODO Auto-generated constructor stub
		this.time = time;
	}

	public Day getDay(){
		return day;
	}
	
	public Time getTime(){
		return time;
	}
	
	public String toString(){
		return day.toString() + " " + time.toString();
	}
	
	public Date toDate(){
		return new Date(day.getYear() - 1900,day.getMonth()-1,day.getDay(),time.getHour(),time.getMinute(),0);
	}
	
	
	@SuppressWarnings("deprecation")
	public static Date getDate(Day day,Time time){
		return new Date(day.getYear() - 1900,day.getMonth()-1,day.getDay(),time.getHour(),time.getMinute(),0);
	}

}
