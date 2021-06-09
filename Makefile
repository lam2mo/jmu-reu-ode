CXX=g++
CXXFLAGS=-g -O0 -Wall -std=c++11
LDFLAGS=

TARGETS=solve_p1_fp64 solve_p1_fp32 \
        solve_p2_fp64 solve_p2_fp32 \
		coeff ODEViewer.class \
        solve_p3 solve_p4 \
		izhikevich_fp64 izhikevich_fp32

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

solve_p3: solve_p3.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

solve_p4: solve_p4.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

izhikevich_fp64: izhikevich.cpp
	$(CXX) $(CXXFLAGS) -Dreal_t=double -o $@ $^ $(LDFLAGS)

izhikevich_fp32: izhikevich.cpp
	$(CXX) $(CXXFLAGS) -Dreal_t=float  -o $@ $^ $(LDFLAGS)

coeff: coeff.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LDFLAGS)

clean:
	rm -rf $(TARGETS) *.dSYM *.class

