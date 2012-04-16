require 'set'

def ret sym,date,min   
  minute_data = IO.readlines('sp500/' + [sym,date,'csv'].join('.'))   
  a = minute_data[1].split(',')[1].to_f
  b = minute_data[min].split(',')[4].to_f  
  a/b - 1
end

#build tables of gaps
table = []
syms = []
dates = Set.new
dir = 'sp-daily'
Dir.entries(dir).each do |file|
  if file =~ /.csv/
    lines = IO.readlines(dir + '/' + file)    
    next if lines.count != 67
    sym = file.split('.').first
    syms << sym    
    last_c = nil
    row = []
    lines.drop(1).reverse.each do |line|
      date,open,high,low,close,vol,adj_close = line.split ','
      open = open.to_f
      close = close.to_f
      date = date.split('-').join('')
      dates << date
      if last_c
        gap = (open/last_c - 1)        
        #puts "%s %s %f %f" % [sym,date,open,last_c] if gap.abs > 0
        #gap = 0 if gap.abs > 0.3
        row << gap
      end      
      last_c = close
    end
    table << row
  end
end
dates = dates.to_a.sort
table = table.transpose

# unadjusted

table.each_with_index do |day,i|
  day.each_with_index do |e,j|
    puts "%d %d %f" % [i,j, [[e,0.1].min,-0.1].max]
  end
  puts "\n"
end


# percentile
# table.each_with_index do |day,i| 
#   sorted = day.sort
#   percentiles = day.map{ |e| sorted.index(e)/sorted.length.to_f }
#   percentiles.each_with_index do |p,j|          
#     puts "%d %d %f" % [i,j,p]    
#   end
#   puts "\n"
# end


# percentile x gap = return
# table.each_with_index do |day,i| 
#   sorted = day.sort
#   percentiles = day.map{ |e| sorted.index(e)/sorted.length.to_f }
#   percentiles.each_with_index do |p,j| 
#   begin
#     r = ret(syms[j],dates[i],10)
#     puts "%f %f %f" % [day[j],p,r]#[r.abs,0.03].min * (r <=> 0)] if p < 0.031
#   rescue
#     $stderr.puts [syms[j],dates[i],30]
#   end
#   end
#   puts "\n"
# end

# t=[]
# table.each_with_index do |day,i|
#   day.each_with_index do |e,j|
#     begin
#       r = ret(syms[j],dates[i], 90)
#       t << [day[j],r]
#     rescue
#       $stderr.puts [syms[j],dates[i],90]
#     end
#   end
# end
# bin_size = 0.01
# t.group_by{ |e| (e[0] / bin_size).round }.each do |k,v|
#   rs = v.map{|a| a[1]}
#   puts "%f %f %f" % [k*bin_size,rs.reduce(:+)/rs.count,Math.sqrt(rs.count)]
# end
  