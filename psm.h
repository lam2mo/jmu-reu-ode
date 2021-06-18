#ifndef PSM_H
#define PSM_H

#include <vector>

using namespace std;

template<class T>
T eval(vector<T> coeff, T x)
{
    /* Evaluates a Polynomial given its coefficients
     * Inputs:
     * coeff: coefficients of the polynomial to be evaluated in increasing order
     * x    : number to be evaluated
     * Output:
     * p(x)
     */

    //Horners Algorithm
    T val = 0;
    for(int i = coeff.size()-1; i>=0; i--){
        val = val*x+coeff[i];
    }
    return val;
}

template<class T>
vector<T> derivative(vector<T> f)
{
    vector<T> deriv(f.size()-1,0);
    for(int i = 0; i< deriv.size(); i++){
        deriv[i] = f[i+1]*(i+1);
    }
    return deriv;
}

#endif

