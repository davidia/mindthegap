#Scan for gaps in daily data
#and write to log
# output
# symbol date %gap

require 'date'

 
PROFIT = 0.013
STOP   = 0.009

OPEN = 0
HIGH = 1
LOW = 2
CLOSE= 3

TIME_STOP = 100000

# 1 minute later than open
ENTER_TIME = 93000

# ENTRY conditions
GAP = 0.046

def report dir,price,time,result,reason,pnl
  puts "%s %f %d %s %s %f" % [dir > 0 ? 'BUY' : 'SELL',price,time,result,reason,100*pnl]
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


res = Hash.new {|h,k| h[k]=[]}
setup  = Hash.new {|h,k| h[k]=[]}

Dir.entries("sp-daily").each do |file|
  if file =~ /daily.csv/
    sym = file.split('.').first
    lines = IO.readlines('sp-daily/' + file)
    last_c = last_l = last_o = nil
    lines.drop(1).reverse.each do |line|
      line = line.chomp
      date,open,high,low,close = line.split ','

      open = open.to_f
      high = high.to_f
      low = low.to_f
      close = close.to_f

      if last_c
        
        date = date.split('-').join('')
        gap = open/last_c - 1 - 0*iret[date]
        if (gap- iret[date]) < -GAP and gap.abs < 0.20  and (last_c/last_o > 1.01)
          setup[date] << [sym,gap,iret[date]]
          puts 'trading %s -> %s %f' % [sym,date,gap]
          minute_data = IO.readlines('sp500/' + [sym,date,'csv'].join('.') )

          dir = -gap <=> 0

          target = PROFIT + (last_c/last_o-1) * 0.12

          exit_side = dir == 1 ? HIGH : LOW
          stop_side = dir == 1 ? LOW : HIGH
          open = nil
          minute_data.each do |minline|
            els = minline.split(',')          
            time = els[0][8..-1].to_i
            prices = els[1..-1].map{|e| e.to_f}

            if time >= ENTER_TIME
              if !open
                open = prices[OPEN]
                report dir,open,time,"","OPEN",0
              end

              if (dir * (prices[stop_side]-open) / open) <= -STOP
                p = open * (1-dir*STOP)
                pnl = -STOP    
                report -dir,p,time,"LOSE","STOP",pnl
                res[date] << pnl                
                break
              end

              if (dir * (prices[exit_side]-open) / open) >= target
                p = prices[exit_side]
                pnl = target               
                report -dir,p,time,"WIN","PROFIT",pnl
                res[date] << pnl                
                break
              end

              if time >= TIME_STOP
                p = prices[CLOSE]
                pnl = dir * (p-open)/open 
                report -dir,p,time,pnl > 0 ? "WIN" : "LOSE","TIMEOUT",pnl
                res[date] << pnl
                break
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

trades = res.values.flatten
puts "%d trades for %f PROFIT %f per trade" % [trades.length,100*trades.reduce(:+),100*trades.reduce(:+)/trades.length]
p res.keys.sort
p res.sort
p setup.sort