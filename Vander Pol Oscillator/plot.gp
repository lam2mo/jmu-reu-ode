reset
fn = "out.dat"

set autoscale xfixmin
set autoscale xfixmax

set term png
set output "out.png"
set key left box

#set logscale y

plot fn using 2:3 title 'x vs x' ' with linespoints, \
     #fn using 2:3 title 'No Stepper' with linespoints

