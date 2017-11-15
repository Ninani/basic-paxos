import fileinput
import re
from time import strptime

dir_path = "./log/"
f_names = ['server-8091_1510710810534.log', 'server-8091_1510711077736.log'] # names of log files
destination_path = dir_path + 'merged.log'
file_paths = [dir_path + f for f in f_names]

lines = list(fileinput.input(file_paths))
# 2017-11-15 02:54:03.924 
t_fmt = '%Y-%m-%d %H:%M:%S.%f'	#timestamp format 
t_pat = re.compile(r'(\d+-\d+-\d+ \d+:\d+:\d+.\d+)') # pattern to extract timestamp

lines.sort()

with open(destination_path, 'wb') as f:
	for l in lines:
	# for l in sorted(lines, key= lambda l: strptime(t_pat.search(l).group(1), t_fmt)):
		f.write(l)