#!/bin/bash

make -q VanderPolOscillatorGeneral
rm -f out.dat coeff.dat out.png
./VanderPolOscillatorGeneral $@ >out.dat 2>coeff.dat
gnuplot plot.gp
#open out.png

