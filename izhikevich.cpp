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


// output stream
ofstream out;

// ODE system
void ode(const state_t &x, state_t &dxdt, const real_t /*t*/)
{
    // TODO: encode ODE for RK4 solver
}

/* Generic Parker-Sochacki solver function */
int ps_step(real_t *y[], real_t *co[],              /* in-out */
        real_t y1[], real_t ynew[],                 /* out */
        real_t fp[], real_t eta[],                  /* in */
        void (*first)(real_t *[], real_t *[], real_t []),
        void (*iter)(real_t *[], real_t *[], real_t [], int),
        int stop, int ps_limit, int nv, int err_nv)
{
    int i, p;
    real_t dt = fp[99], dt_pow;

    first(y, co, fp);                   /* Calculate first order terms */

    if (dt == 1) {
        for (i=0; i<nv; i++) {
            y1[i] = y[i][0] + y[i][1];
        }
    } else {
        for (i=0; i<nv; i++) {
            y1[i] = y[i][0] + dt*y[i][1];
        }
        dt_pow = dt*dt;
    }

    for (p=1; p<(ps_limit-1); p++) {    /* Iterations */

        iter(y, co, fp, p);

        /* Update solution */
        if (dt == 1) {
            for(i=0; i<nv; i++) {
                ynew[i] = y1[i] + y[i][p+1];
            }
        } else {
            for(i=0; i<nv; i++) {
                ynew[i] = y1[i] + y[i][p+1]*dt_pow;
            }
            dt_pow *= dt;
        }

        /* Check for solution divergence */
        if ((fabs(y[0][p+1]) > 10.0)) {
            p = -1;
            break;
        }

        /* Test error tolerance on variable value change */
        for (i=0; i<err_nv; i++) {
            if (fabs(ynew[i] - y1[i]) > eta[i]) {
                break;
            }
        }
        if (i == err_nv) break;

        for(i=0; i<nv; i++) {
            y1[i] = ynew[i];
        }
    }

    p++;
    if (stop == 1) {
        if (p == ps_limit) {
            cerr << "PS solve failed." << endl;
        }
    }
    return p;
}

/* PS - first term for Izhikevich model */
void iz_first(real_t *y[], real_t *co[], real_t fp[])
{
    real_t v, u, g_ampa, g_gaba, I, k, /*l,*/
           E_ampa, E_gaba, E, a, b, co_g_ampa, co_g_gaba, chi;

    v =      y[0][0];
    u =      y[1][0];
    g_ampa = y[2][0];
    g_gaba = y[3][0];
    chi =    y[4][0];

    I =         fp[0];
    k =         fp[1];
    E_ampa =    fp[3];
    E_gaba =    fp[4];
    E =         fp[5];
    a =         fp[6];
    b =         fp[7];
    co_g_ampa = fp[8];
    co_g_gaba = fp[9];

    y[0][1] = E*(v*chi - u + E_ampa*g_ampa + E_gaba*g_gaba + I);
    y[1][1] = a*(b*v - u);
    y[2][1] = co_g_ampa*g_ampa;
    y[3][1] = co_g_gaba*g_gaba;
    y[4][1] = k*y[0][1] - y[2][1] - y[3][1];
}

/* PS - iteration function for higher order terms */
void iz_iter(real_t *y[], real_t *co[], real_t fp[], int p)
{
    real_t /*v,*/ k, /*l,*/ E_ampa, E_gaba, b, /*chi,*/ vchi;
    int i;

    k =      fp[1];
    E_ampa = fp[3];
    E_gaba = fp[4];
    b =      fp[7];

    vchi = y[0][0]*y[4][p] + y[4][0]*y[0][p];
    for (i = 1; i < p; i++) {
        vchi += y[0][i] * y[4][p-i];
    }

    y[0][p+1] = co[0][p] * (vchi - y[1][p] + E_ampa*y[2][p] + E_gaba*y[3][p]);
    y[1][p+1] = co[1][p] * (b*y[0][p] - y[1][p]);
    y[2][p+1] = co[2][p] * y[2][p];
    y[3][p+1] = co[3][p] * y[3][p];
    y[4][p+1] = k*y[0][p+1] - y[2][p+1] - y[3][p+1];
}


// TODO: debug output helper
void observe(const state_t &x, const real_t t)
{
    //real_t sol = solution(a, b, c, x0, t);
    //real_t psm_sol = psm(0);

    // re-center power series
    //calc_coefficients(psm(dt));

    // write output
    //out << setw(8) << t;
    //out << setw(16) << sol;
    //out << setw(16) << x[0] << setw(16) << fabs(sol-x[0]);
    //out << setw(16) << psm_sol << setw(16) << fabs(sol-psm_sol);
    //out << endl;
}

int main(int argc, const char* argv[])
{
    // TODO: check parameters
    //if (argc != 7) {
        //cout << "Usage: " << argv[0] << " <a> <b> <c> <x0> <tn> <dt>" << endl;
        //return EXIT_FAILURE;
    //}

    // TODO: parse parameters

    int stop         = 1;
    int ps_limit     = 10;
    const int nv     = 5;
    const int err_nv = nv;

    real_t fp[100];      /* TODO: initialize indices 0-9 and 99 */

    real_t* y  [nv];     /* TODO: initialize */
    real_t* co [nv];     /* TODO: initialize */
    real_t  eta[nv];     /* TODO: initialize */

    for (int i=0; i<nv; i++) {
        y[i]  = (real_t*) calloc(nv, sizeof(real_t)*(ps_limit+1));
        co[i] = (real_t*) calloc(nv, sizeof(real_t)*(ps_limit+1));
    }

    real_t y1  [nv];
    real_t ynew[nv];

    // TODO: initialize RK4 state
    //state_t x(nv);

    // show floating-point width
    //cout << "sizeof(real_t)=" << sizeof(real_t) << endl;

    // stepper
    runge_kutta4<state_t> stp;

    out.open("out.dat");
    int steps = ps_step(y, co,
            y1, ynew, fp, eta, iz_first, iz_iter,
            stop, ps_limit, nv, err_nv);
    // TODO: integrate w/ debug output
    /*size_t steps =*/ //integrate_const(stp, ode, x, t0, tn, dt, observe);
    out.close();

    // show final output
    cout << "steps=" << steps /*<< " x=" << x[0]*/ << endl;

    for (int i=0; i<nv; i++) {
        free(y[i]);
        free(co[i]);
    }

    return EXIT_SUCCESS;
}
