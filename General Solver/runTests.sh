#!/bin/bash

rm FlameAllTests.dat
rm VanDerPolAllTests.dat
rm PendulumAllTests.dat

for((n = 0; i<1; i++))
do
	for time in {100..100..5}
	do
		for degree in {15..80}
		do
			echo "$degree"
			./FlameEquation .5 "$degree" "$time" 
			cat timeComparisonFlame.dat >> FlameAllTests.dat
			
			./SinglePendulum .5 .1 "$time" "$degree"
			cat timeComparisonPendulum.dat >> PendulumAllTests.dat
			
			./VanDerPolEquation .01 1 1 "$time" .1 "$degree" 
			cat timeComparisonVanDerPol.dat >> VanDerPolAllTests.dat
		done
	done		
done
