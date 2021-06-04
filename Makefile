CXX=g++
CXXFLAGS=-g -O2 -Wall -std=c++11
LDFLAGS=

TARGETS=solve_p1_fp64 solve_p1_fp32 \
        solve_p2_fp64 solve_p2_fp32 \
		coeff ODEViewer.class

all: $(TARGETS)

ODEViewer.class: ODEViewer.java
	javac $<

solve_p1_fp64: solve_p1.cpp
	$(CXX) $(CXXFLAGS) -Dreal_t=double -o $@ $^ $(LDFLAGS)

solve_p1_fp32: solve_p1.cpp
	$(CXX) $(CXXFLAGS) -Dreal_t=float  -o $@ $^ $(LDFLAGS)

solve_p2_fp64: solve_p2.cpp
	$(CXX) $(CXXFLAGS) -Dreal_t=double -o $@ $^ $(LDFLAGS)

solve_p2_fp32: solve_p2.cpp
	$(CXX) $(CXXFLAGS) -Dreal_t=float  -o $@ $^ $(LDFLAGS)

coeff: coeff.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

clean:
	rm -rf $(TARGETS) *.dSYM *.class

