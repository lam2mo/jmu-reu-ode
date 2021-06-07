/**
 * ODE solver - JMU REU 2021
 *
 * @author Mike Lam
 */

// standard headers
#include <cstdlib>
#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <string>
#include <cmath>
#include <functional>
using namespace std;

// Boost ODEint headers (individual files compile faster than the catch-all)
#include <boost/numeric/odeint.hpp>
//#include <boost/numeric/odeint/integrate/integrate_const.hpp>
//#include <boost/numeric/odeint/stepper/runge_kutta4.hpp>
using namespace boost::numeric::odeint;

// floating-point precision control (control via compiler parameter -Dreal_t)
//typedef double real_t;      // 64 bits
//typedef float real_t;       // 32 bits

// solver state
typedef std::vector<real_t> state_t;

// parameters (most read from command line)
real_t b, c, x0;
double t0 = 0.0;
double tn, dt;

// output stream
ofstream out;

// ODE system
void ode(const state_t &x, state_t &dxdt, const real_t /*t*/)
{
    dxdt[0] = b*x[0] + c;
}

// debug output helper
void observe(const state_t &x, const real_t t)
{
    // known closed-form solution
    real_t sol = (x0+c/b) * exp(b*t) - c/b;

    out << setw(8) << t << " " << setw(12) << x[0]
         //<< " " << setw(12) << sol
         << " " << setw(12) << fabs(sol-x[0]) << endl;
}

int main(int argc, const char* argv[])
{
    // check parameters
    if (argc != 6) {
        cout << "Usage: " << argv[0] << " <b> <c> <x0> <tn> <dt>" << endl;
        return EXIT_FAILURE;
    }

    // parse parameters
    b  = stod(argv[1], NULL);
    c  = stod(argv[2], NULL);
    x0 = stod(argv[3], NULL);
    tn = stod(argv[4], NULL);
    dt = stod(argv[5], NULL);
    // TODO: add stepper and t0?

    // initialize state
    state_t x(1);
    x[0] = x0;

    // show floating-point width
    //cout << "sizeof(real_t)=" << sizeof(real_t) << endl;

    // stepper
    runge_kutta4<state_t> stp;

    // integrate w/ debug output
    out.open("out.dat");
    /*size_t steps =*/ integrate_const(stp, ode, x, t0, tn, dt, observe);
    out.close();

    // show final output
    //cout << "steps=" << steps << " x=" << x[0] << endl;

    return EXIT_SUCCESS;
}
