#!/bin/bash

make -q solve_p2_fp64
rm -f out.dat coeff.dat out.png
./solve_p2_fp64 $@ >out.dat 2>coeff.dat
gnuplot plot.gp
#open out.png

