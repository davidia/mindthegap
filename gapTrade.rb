#Scan for gaps in daily data
#and write to log
# output
# symbol date %gap

require 'date'

 
PROFIT = 0.016
STOP   = 0.016

OPEN = 0
HIGH = 1
LOW = 2
CLOSE= 3

TIME_STOP = 93400

# 1 minute later than open
ENTER_TIME = 93000

# ENTRY conditions
GAP = 0.02
PREV_RET_FLOOR = 0.000
PREV_RET_PNL_BOOST = 0.2
INDEX_ADJ = 0.0
MAX_DAILY_TRADES = 1

def report dir,price,time,result,reason,pnl
 # puts "%s %f %d %s %s %f" % [dir > 0 ? 'BUY' : 'SELL',price,time,result,reason,100*pnl]
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

Dir.entries("daily").sort.each do |file|
  if file =~ /daily.csv/
    sym = file.split('.').first
    intraday_name = 'sp-daily/' + file;
    lines = IO.readlines('daily/' + file)
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
        gap = open/last_c - 1 - INDEX_ADJ*iret[date]
        if (gap- iret[date]) < -GAP and gap.abs < 0.20  and (last_c/last_o - 1 > PREV_RET_FLOOR) 
          minute_data_file = 'sp500/' + [sym,date,'csv'].join('.') 
          if !File.exists? minute_data_file
            puts "missing " + minute_data_file
            proc = 'wget -O %s "http://localhost:5000/barData?symbol=%s&historyType=0&intradayMinutes=1&beginTime=%s093000&endTime=%s163000"' % [minute_data_file,sym,date,date]
            puts proc 
                
            Process.wait(spawn(proc))   
          end       
          res[date] << { sym: sym, gap: gap, index:iret[date]}
          #setup[date] << [sym,gap,iret[date]]
          #puts 'trading %s -> %s %f' % [sym,date,gap]

          minute_data = IO.readlines(minute_data_file)

          dir = -gap <=> 0

          target = PROFIT + (last_c/last_o-1) * PREV_RET_PNL_BOOST

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
                res[date][-1][:pnl] = pnl
                break
              end

              if (dir * (prices[exit_side]-open) / open) >= target
                p = prices[exit_side]
                pnl = target               
                report -dir,p,time,"WIN","PROFIT",pnl
                res[date][-1][:pnl] = pnl
                break
              end

              if time >= TIME_STOP
                p = prices[CLOSE]
                pnl = dir * (p-open)/open 
                report -dir,p,time,pnl > 0 ? "WIN" : "LOSE","TIMEOUT",pnl
                res[date][-1][:pnl] = pnl
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

sorted_dates = res.keys.sort

hist = {}

all_trades = []
sorted_dates.each do |d|
  trades = res[d]  
  st = trades.sort_by {|t1| (t1[:gap].abs)}
  traded = st.take(MAX_DAILY_TRADES).map{|t| t[:pnl]}
  p traded
  all_trades << traded
  avg_pnl = traded.reduce(:+) / traded.count
  hist[d] = avg_pnl
end

r = 0
pnlout = []
sorted_dates.each do |d|
  r += hist[d]
  # dt = Date.new(d[0..3].to_i,d[4..5].to_i,d[6..7].to_i)
  pnlout << "%s %f" % [d,r]
end

all_trades = all_trades.flatten
open('pnl','w') { |f| pnlout.each{|p| f << p+"\n"}}

#draw down
mx = 0
v= 0
max_drawdown = sorted_dates.map{|d| v+=hist[d];mx = [v,mx].max; mx - v}.max
duration = 0
max_drawdown_duration = sorted_dates.map{|d| v+=hist[d];mx = [v,mx].max; v < mx ?  duration+=1 : duration = 0}.max

puts "%d trading days" % [hist.count]
puts "%d trades"  % [all_trades.count]
puts "%2.2f%% Profit"  % [100 * hist.values.reduce(:+) ]
puts "%.2f%% Avg Daily Profit"  % [100 * hist.values.reduce(:+) / hist.values.count.to_f]
puts "%.2f%% Avg Profit Per Trade"  % [100 * all_trades.reduce(:+) / all_trades.count.to_f]
puts "%2.2f%% Max Drawdown"  % [100 *max_drawdown]
puts "%d Max Drawdown Duration"  % [max_drawdown_duration]

#trades = res.values.flatten
#puts "%d trades for %f PROFIT %f per trade" % [trades.length,100*trades.reduce(:+),100*trades.reduce(:+)/trades.length]
# p res.keys.sort
# p res.sort
# p setup.sort

