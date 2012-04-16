require 'thread'
threads = []
semaphore = Mutex.new
syms = IO.readlines('sp500sym.txt').map{|s|s.chomp}
dates = IO.readlines('trade-dates.txt').map{|s|s.chomp}

pairs = []
syms.each{|sym| dates.each{|d| pairs << [sym,d]} }

(1..2).each do |tid|
  t = Thread.new(tid) do |i|  
    sleep(5 * Random.rand) 
    while true do
      begin      
        pair = nil
        semaphore.synchronize do
          return if pairs.empty?
          pair = pairs.pop     
        end

        file = "sp500/%s.%s.csv" % pair
        sym = pair[0]
        date = pair[1]
        start = date + "093000"
        ennd = date +  "160000"
        proc = 'wget -O %s "http://localhost:5000/barData?symbol=%s&historyType=0&intradayMinutes=1&beginTime=%s&endTime=%s"' % [file,sym,start,ennd]
        puts proc
        
        Process.wait(spawn(proc))
      rescue
        if fail > 1000 then
          exit
        end
        fail+=1
      end
    end
  end
  threads << t
end

threads.each {|t| t.join}