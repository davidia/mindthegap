#Scan for gaps in daily data
#and write to log
# output
# symbol date %gap

require 'date'

STOP = 0.02
PROFIT = 0.01

OPEN = 0
HIGH = 1
LOW = 2
CLOSE= 3

TIME_STOP = 110000

# 1 minute later than open
ENTER_TIME = 93000

# ENTRY conditions
GAP = 0.025

def report dir,price,time,result,reason,pnl
  puts "%s %f %d %s %s %f" % [dir > 0 ? 'BUY' : 'SELL',price,time,result,reason,100*pnl]
end

data={}
(-20..20).each do |g|
  data[g] = {}
  (1..90).each do |t|
    data[g][t] = [0]
end
end

iret = Hash.new
lines = IO.readlines('GSPC.daily.csv')
last_c = nil
lines.each do |line|
line = line.chomp
      date,open,high,low,close = line.split ','
      open = open.to_f
      close = close.to_f
      if last_c
        gap = open/last_c - 1     
        date = date.split('-').join('')
        iret[date] = close/open - 1
      end
      last_c = close
end

Dir.entries("sp-daily").each do |file|
  if file =~ /daily.csv/
    sym = file.split('.').first
    lines = IO.readlines('sp-daily/' + file)
    last_c = last_l = last_h = nil
    lines.drop(1).reverse.each do |line|
      line = line.chomp
      date,open,high,low,close = line.split ','

      open = open.to_f
      high = high.to_f
      low = low.to_f
      close = close.to_f

      if last_c
        date = date.split('-').join('')
        gap = open/last_c - 1 - iret[date]     
        
        if gap.abs > GAP and gap.abs < 0.1 and (open < last_l or open > last_h)
          minute_data = IO.readlines('sp500/' + [sym,date,'csv'].join('.') )

          open = nil
          minute_data.each do |minline|
            els = minline.split(',')          
            time = els[0][8..-1].to_i
            h = els[0][8..9].to_i
            m = els[0][10..11].to_i
            prices = els[1..-1].map{|e| e.to_f}

            if time >= ENTER_TIME
              if !open
                open = prices[OPEN]
              elsif time <= TIME_STOP
                p = prices[CLOSE]
                pnl =  (p-open)/open 
                data [ ((gap*1000/5).truncate) ][ h*60 + m - (9*60+30)] << pnl                        
              end
            end
          end
        end
      end
      last_o = open
      last_h = high
      last_l = low
      last_c = close
    end    
  end
end



data.each do |k,v|  
  v.each do |k2,v2|
    a=v2.reduce(:+)/v2.length
    puts "%d %d %f" % [k,k2,a]
  end
  puts "\n"
end