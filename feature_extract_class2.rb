#Scan for gaps in daily data
#and write to log
# output
# symbol date %gap

require 'date'
require 'sqlite3'

db = SQLite3::Database.new("features.db")
db.execute( "drop table  IF EXISTS features" )
db.execute( "create table features (date DATE,sym TEXT,gap REAL,indexGap REAL,G1 REAL, R1 REAL, MktCap REAL, GapIndex REAL,target REAL)" )
stmt = db.prepare( "insert into features VALUES (?,?,?,?,?,?,?,?,?)" )

features=[]

OPEN = 0
HIGH = 1
LOW  = 2
CLOSE= 3

TIME_STOP = 100000
ENTER_TIME = 93000

# ENTRY conditions
GAP = -0.020

EXIT = 0.015


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

      rdate = Date.new(d2[0..3].to_i,d2[4..5].to_i,d2[6..7].to_i)

      if last_last_c    
       
        gap = open/last_c - 1  
        percentile = gaps[d2].index{|g2| g2>=gap} / gaps[d2].count.to_f            
        if gap < GAP and  percentile > 0.02  and mktCap[sym] and mktCap[sym] > 5 and rdate > Date.new(2011,6,1)
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
          feature << last_o/last_last_c -1 
          feature << last_c/last_last_c -1              
          feature << mktCap[sym]     
          feature << percentile
          
          minute_data.each do |minline|
            els = minline.split(',')          
            time = els[0][8..-1].to_i
            prices = els[1..-1].map{|e| e.to_f}

            if time >= ENTER_TIME
              if !open
                open = prices[OPEN]                
              end
            

              if  prices[LOW]/open - 1 <= -EXIT                
                feature << -2
                features << feature  
                break
              end

              if prices[HIGH]/open - 1 >= EXIT                
                feature << 2
                features << feature  
                break
              end
              
              if time >= TIME_STOP 
                t = prices[CLOSE] / open - 1
                
                #feature << (t < -EXIT ? -1 : t < EXIT ? 0 : 1)     
                feature << (t < 0 ? -1 : 1)     
                #feature << 0
                features << feature if feature.all?{|f| !f}
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


#features.each{|f| t = f.last; f[-1] = t < 0 ? -1 : 1}

# CSV
# puts %w{date sym gap wday index g-1 r-1 mktCap dist_post target}.join(',')
# features.each do |f|
#   puts f.join(',')_e
# end

# SQLite3
features.sort_by {|f| f[0]}
features.each {|f| stmt.execute(f)}