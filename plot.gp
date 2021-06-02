reset
fn = "out.dat"

set autoscale xfixmin
set autoscale xfixmax

set term png
set output "out.png"
set key left box

set logscale y

plot fn using 1:2 title 'Runge-Kutta' with linespoints, \
     fn using 1:3 title 'Power Series' with linespoints

