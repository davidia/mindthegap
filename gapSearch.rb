#Scan for gaps in daily data
#and write to log
# output
# symbol date %gap

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
    last_c = nil
    lines.drop(1).reverse.each do |line|
      line = line.chomp
      date,open,high,low,close = line.split ','

      open = open.to_f
      high = high.to_f
      low = low.to_f
      close = close.to_f

      if last_c
        gap = open/last_c - 1     
        date = date.split('-').join('')
        #puts "%s %s %f %f" % [sym,date,gap,close/open - 1]


        puts "%f %f %f" % [gap,gap- iret[date],close/open - 1 ]
      end

      last_o = open
      last_h = high
      last_l = low
      last_c = close
    end    
  end
end
