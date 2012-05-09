package mindthegap.util;

import java.util.Formatter;

public class Time {
	private final int minutes;
	
	public Time(int hour,int minute){
		
		if(hour < 0 || hour > 23)
			throw new IllegalArgumentException("hour = " + hour);
		
		if(minute < 0 || minute > 59)
			throw new IllegalArgumentException("minute = " + minute);
		
		minutes = minute + 60 * hour;		
	}
	
	public Time(int minutes){
		this.minutes = minutes;
	}
	
	public int getHour(){
		return minutes / 60;
	}
	
	public int getMinute(){
		return minutes % 60;
	}
	
	public Time add(int minutes){
		return new Time(this.minutes + minutes);
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + minutes;
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
		Time other = (Time) obj;
		if (minutes != other.minutes)
			return false;
		return true;
	}

	public boolean after(Time time) {
		return minutes > time.minutes;
	}
	
	public boolean before(Time time) {
		return minutes < time.minutes;
	}

	private String stringRep = null;
	
	public String toString(){
		if(stringRep == null){
			StringBuilder sb = new StringBuilder();
			Formatter formatter = new Formatter(sb);
			formatter.format("%02d:%02d:00", getHour(),getMinute());
			stringRep = sb.toString();
		}
		return stringRep;
	}

	public int asMinutes() {
		// TODO Auto-generated method stub
		return minutes;
	}



}
