import storm
import re

# Cleans information retreived from ServiceBus to contain only information from 
# the fitness tracker. Then splits the data by pipes to return as an array
class CleanInputBolt(storm.BasicBolt):
	def process(self, tup):
		data = tup.values[0]
		temp = re.search(r'(?=\{)(.*?)(?=\})', data)
		# Non JSON method
		match = temp.group(0).replace("{", "")
		if match:
			storm.emit([match])

CleanInputBolt().run()

# (?!\{)(.*?)(?=\}) Not including braces
# (?=\{)(.*?)(?:\}) Including braces

# Testing purposes
def test():
	#string = "@^Fstring^H3http://schemas.microsoft.com/2003/10/Serialization/{"calorie":1.0,"distance":2.0,"runDownStep":3.0,"runStep":4.0,"runUpStep":5.0,"speed":6.0,"stepStatus":7.0,"totalStep":8.0,"walkDownStep":9.0,"walkStep":10.0,"walkUpStep":11.0,"walkingFrequency":12.0}^A"
	match = re.search(r'(?=\{)(.*?)(?:\})', string)
	print match.group()

#test()
