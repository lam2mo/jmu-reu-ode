#/bin/bash
#
# ODE solver - JMU REU 2021
#
# @author Mike Lam
#

PROBLEM="$1"
FP64FILE="${PROBLEM}_fp64.txt"
FP32FILE="${PROBLEM}_fp32.txt"
shift

make -s solve_${PROBLEM}_fp64 solve_${PROBLEM}_fp32
./solve_${PROBLEM}_fp64 $@ >${FP64FILE}
./solve_${PROBLEM}_fp32 $@ >${FP32FILE}
paste ${FP64FILE} ${FP32FILE}
rm -f ${FP64FILE} ${FP32FILE}

