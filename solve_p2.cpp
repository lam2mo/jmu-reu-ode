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
real_t a, b, c, x0;
double t0 = 0.0;
double tn, dt;
const int N = 10;
real_t ac[N+1];

// output stream
ofstream out;

// ODE system
void ode(const state_t &x, state_t &dxdt, const real_t /*t*/)
{
    dxdt[0] = a*pow(x[0],2.0) + b*x[0] + c;
}

// power series solution at x
void calc_coefficients(real_t x)
{
    ac[0] = x;
    ac[1] = a * ac[0]*ac[0] + b * ac[0] + c;

    for (int i = 2; i <= N; i++) {
        ac[i] = 0.0;
        for (int k = 0; k < i; k++) {
            ac[i] += ac[k] * ac[i-1-k];
        }
        ac[i] = (a * ac[i] + b * ac[i-1]) / i;
    }
}

// power series solution using Horner's method
real_t psm(const real_t t)
{
    real_t x = t * ac[N];
    for (int j = N-1; j > 0; j--) {
        x = t * (x + ac[j]);
    }
    x += ac[0];

    calc_coefficients(x);
    return x;
}

// debug output helper
void observe(const state_t &x, const real_t t)
{
    // power series solution
    real_t sol = psm(t);

    //out << "t=" << setw(5) << t << "  x=" << setw(8) << x[0]
         //<< "  sol=" << setw(8) << sol
         //<< "  err=" << setw(12) << fabs(sol-x[0]) << endl;
    out << setw(8) << t << setw(16) << x[0] << setw(16) << sol
         << setw(16) << fabs(sol-x[0]) << endl;
}

int main(int argc, const char* argv[])
{
    // check parameters
    if (argc != 7) {
        cout << "Usage: " << argv[0] << " <a> <b> <c> <x0> <tn> <dt>" << endl;
        return EXIT_FAILURE;
    }

    // parse parameters
    a  = stod(argv[1], NULL);
    b  = stod(argv[2], NULL);
    c  = stod(argv[3], NULL);
    x0 = stod(argv[4], NULL);
    tn = stod(argv[5], NULL);
    dt = stod(argv[6], NULL);
    // TODO: add stepper and t0?

    // initialize state
    state_t x(1);
    x[0] = x0;

    // precalculate coefficients
    calc_coefficients(x0);

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
