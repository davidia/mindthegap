require 'date'
require 'sqlite3'

def insert sym,name  
  print "inserting #{sym} (#{name}) - "
  lines = IO.readlines(name)
  puts  " #{lines.count} rows"
  lines.drop(1).reverse.each do |line|
    @stmt.execute([sym] + line.chomp.split(',').take(5))      
  end
end


db = SQLite3::Database.new("mindthegap.db")
db.execute( "drop table  IF EXISTS daily" )

db.execute( "create table daily (symbol TEXT,date DATE,open REAL,high REAL,low REAL,close REAL)" )
db.execute( "CREATE INDEX sym_ind ON daily (symbol)")
db.execute( "CREATE INDEX date_ind ON daily (date)")
@stmt = db.prepare( "insert into daily VALUES (?,?,?,?,?,?)" )

 db.transaction


insert("GSPC",'GSPC.daily.csv')

Dir.entries("daily").sort.each do |file|  
  insert(file.split('.')[0],'daily/' + file) if file =~ /daily.csv/    
end

db.commit