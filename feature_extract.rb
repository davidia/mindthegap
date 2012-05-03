#Scan for gaps in daily data
#and write to log
# output
# symbol date %gap

require 'date'
# require 'sqlite3'

# db = SQLite3::Database.new("features.db")
# db.execute( "drop table  IF EXISTS features" )
# db.execute( "create table features (date DATE,sym TEXT,gap REAL,indexGap REAL,R1 REAL, R2 REAL, G1 REAL, MktCap REAL, GapIndex REAL,target REAL)" )
# stmt = db.prepare( "insert into features VALUES (?,?,?,?,?,?,?,?,?,?)" )
features=[]

CLOSE= 3

TIME_STOP = 93400


ENTER_TIME = 93000

# ENTRY conditions
GAP = -0.020

last_last_o = nil

def report dir,price,time,result,reason,pnl
 # puts "%s %f %d %s %s %f" % [dir > 0 ? 'BUY' : 'SELL',price,time,result,reason,100*pnl]
end

names =[%w{Target Gap Index R-1 R-2 G-1 MktCap GapIndex}]


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

gaps = Hash.new { |hash, key| hash[key] =[]  }

Dir.entries("daily").sort.each do |file|
  if file =~ /daily.csv/
    sym = file.split('.').first    
    lines = IO.readlines('daily/' + file)
    last_c = nil
    lines.drop(1).reverse.each do |line|
      line = line.chomp
      date,open,high,low,close = line.split ','
      date = date.split('-').join('')

      open = open.to_f      
      close = close.to_f
      
      if last_c 
        gaps[date] << open / last_c - 1
      end
      last_c = close
    end
  end
end

mktCap={}
IO.readlines('mktCap').each do |l|
  (sym,cap) = l.chomp.split(" ")
  mktCap[sym] = cap.to_f
end

gaps.each {|k,v| v.sort!}



Dir.entries("daily").sort.each do |file|
  if file =~ /daily.csv/
    sym = file.split('.').first
    intraday_name = 'sp-daily/' + file;
    lines = IO.readlines('daily/' + file)
    last_c = last_l = last_o = nil
    last_last_c = nil
    
    lines.drop(1).reverse.each do |line|
      line = line.chomp
      date,open,high,low,close = line.split ','
      d2 = date.split('-').join('')
      open = open.to_f
      high = high.to_f
      low = low.to_f
      close = close.to_f

      if last_last_c    
       
        gap = open/last_c - 1         
        if gap < GAP and gap > -0.2
          minute_data_file = 'sp500/' + [sym,d2,'csv'].join('.')           
          if !File.exists? minute_data_file
            puts "missing " + minute_data_file
            proc = 'wget -O %s "http://localhost:5000/barData?symbol=%s&historyType=0&intradayMinutes=1&beginTime=%s093000&endTime=%s163000"' % [minute_data_file,sym,d2,d2]
            puts proc                 
            Process.wait(spawn(proc))   
          end       

          minute_data = IO.readlines(minute_data_file)

          feature = []          

          feature << date
          feature << sym
          feature << gap
          feature << iret[d2]
          feature << last_o/last_c -1
          feature << last_last_o/last_last_c -1 
          feature << last_c/last_last_c -1              
          feature << mktCap[sym]     
          feature << gaps[d2].index {|g2| g2>=gap}
          
          minute_data.each do |minline|
            els = minline.split(',')          
            time = els[0][8..-1].to_i
            prices = els[1..-1].map{|e| e.to_f}

            if time >= ENTER_TIME
              if !open
                open = prices[OPEN]                
              end
          
              if time >= TIME_STOP
                p = prices[CLOSE]
                pnl = (p-open)/open
                feature << pnl
                features << feature if pnl.abs < 0.1
                #stmt.execute(feature) if pnl.abs < 0.1   and feature.all? { |e| e  }          
                break
              end
            end
          end        
        end
      end
      last_o = open
      last_h = high
      last_l = low
      last_last_c = last_c
      last_last_o = last_o
      last_c = close    
    end
  end  
end

# st = targets.sort

# a = st[st.count/3]
# b = st[2*st.count/3]
 

# targets.each_with_index do |t,i|
#   next if t.abs > 0.1
#   print (t < a ? -1 : t < b ? 0 : 1).to_s + " "
#   features[i].each_with_index {|f,i| print " %d:%f" % [i+1,f] if f}
#   print "\n"
# end

features.each{|f| t = f.last; f[-1] = t < -0.005 ? -1 : t > 0.005 ? 1 : 0 }
#features.each{|f| t = f.last; f[-1] = t < 0 ? -1 : 1}

puts %w{date sym gap index g-1 g-2 r-1 mktCap dist_post target}.join(',')
features.each do |f|
  puts f.join(',')
end