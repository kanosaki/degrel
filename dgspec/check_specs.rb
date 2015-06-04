#!/usr/bin/env ruby

require 'yaml'
require 'pp'

Dir["**/*.dgspec"].each do |f|
  puts "Checking: #{f}"
  begin
    spec = YAML.load_file(f)
    pp spec
  rescue => e
    puts "Invalid yaml format ! #{e}"
    next
  end
  puts "Description not found" until spec["description"]
end
