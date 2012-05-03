Dir.entries("pages").sort.each do |file|
  if file =~ /[A-Z]/
  	IO.readlines("pages/" + file).each do |l|      
  		if md = (/yfs_j10_.{2,5}>(.*?)</.match(l) )
        puts "%s %s" % [file,md[1].chop]
        break
      end
  	end
  end
end