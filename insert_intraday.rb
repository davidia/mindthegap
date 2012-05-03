require 'date'
require 'sqlite3'

def insert sym,date,name  
  @db.transaction
  print "inserting #{sym} #{date} (#{name}) - "
  lines = IO.readlines(name)
  puts  " #{lines.count} rows"
  lines.each do |line|
    values = line.chomp.split(',').take(5)
    values[0] = DateTime.parse(values[0]).strftime "%s"
    #98p values
    @stmt.execute([sym] + values)      
  end
  @db.commit
end


@db = SQLite3::Database.new("mindthegap.db")
@db.execute( "drop table  IF EXISTS minute" )

@db.execute( "create table minute (symbol TEXT,time INTEGER,open REAL,high REAL,low REAL,close REAL)" )
@db.execute( "CREATE INDEX sym_ind_min ON minute (symbol)")
@db.execute( "CREATE INDEX time_ind_min ON minute (time)")
@stmt = @db.prepare( "insert into minute VALUES (?,?,?,?,?,?)" )

Dir.entries("sp500").sort.each do |file|  
  parts = file.split('.')
  insert(parts[0],parts[1],'sp500/' + file) if file =~ /.csv/    
end

