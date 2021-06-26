#include "psmGeneralFunctions.h"
#include <vector>
#include <cmath>
#include <iostream>
using namespace std;

bool debug = 1;


double PSM::nthCoefficientProduct(vector<double> x, vector<double> y, int n){
	/* This method computes the nth coefficient of x*y, runs in O(n)
	 */
	double sum = 0;
	for(int i  = 0; i<=n; i++){
		sum+=x[i]*y[n-i];
	}
	return sum;
}

double PSM::eval(vector<double> coeff, double x){
	/* Evaluates a Polynomial given its coefficients
	 * Inputs:
	 * coeff: coefficients of the polynomial to be evaluated in increasing order
	 * x    : number to be evaluated
	 * Output:
	 * p(x) 
	 */
	
	//Horners Algorithm
	double val = 0;
	for(int i = coeff.size()-1; i>=0; i--){
		val = val*x+coeff[i];
	}
	return val;
}

vector<double> PSM::evaluateAll(vector<vector<double>> coeff, double x){
	/* Evaluates a set of polynomials at x
	 */
	int n = coeff.size();
	vector<double> sol(n);
	for(int i = 0; i<n; i++){
		sol[i] = PSM::eval(coeff[i],x);
	}
	return sol;
}

vector<double> PSM::calculateDerivative(vector<double> f){
	/*This method calculates the derivative of a polynomial*/
	vector<double> deriv(f.size()-1,0);
	for(int i = 0; i< deriv.size(); i++){
		deriv[i] = f[i+1]*(i+1);
	}
	return deriv;
}



//nn means degree
vector<vector<double>> PSM::computeCoefficients(vector<double> parameters, vector<double> initialConditions, int nn){
	/* Inputs
	 * parameters: parameters of the equation
	 * initialConditions: initial conditions
	 * nn : order of the polynomial approximation

	 * Output:
	 * coefficients of the nth polynomial approximation in increasing order
	 * 
	 * Runs in O(n^2), memory O(n)
	 */
     //Initial Conditions
	int k = initialConditions.size();
	vector<vector<double>> coefficients(k,vector<double> (nn,0));
	
	//Add initial conditions
	for(int i = 0; i<k; i++){
		coefficients[i][0] = initialConditions[i];
	}
	/*Calculate solution*/
	for(int n = 0; n<nn-1; n++){
		//This function has to be defined for every problem
		updateNthCoefficient(parameters,coefficients,n);
	}
	return coefficients;
}



vector<vector<double>> PSM::findSolution(vector<double> parameters, vector<double> initialConditions, double step, double end, int n, bool forward ){
	/*This function is the general PSM  solver
	 * Input:
	 * parameters: vector of parameters of the ODE
	 * initialConditions: vector of initial conditions, it's a vector for systems of ODEs 
	 * step: size of the step
	 * end : end of the interval
	 * forward: direction in which we should move
	 */
	int numberOfEquations = initialConditions.size();
	vector<vector<double>> solution(numberOfEquations); //We might want to include any auxiliary variables in here as well
	
	//Initialize initial conditions
	vector<double> currentInitialConditions(numberOfEquations);
	for(int i = 0; i<numberOfEquations; i++){
		solution[i].push_back(initialConditions[i]);
		currentInitialConditions[i] = initialConditions[i];
	}
	
	if(debug){
		cout<<"Set Initial Conditions\n";
	}

	//Start Stepping
	for(int i = 1; step*i<= end; i++){
		if(debug){
			cout<<"Iteration "<<i<<"\n";
			cout<<"Position "<<step*i<<"\n";
		}
		vector<vector<double>> coeff = PSM::computeCoefficients(parameters,currentInitialConditions,n);
		if(debug){
			cout<<"Computed Coefficients\n";
			for(int j = 0; j <coeff.size(); j++){
				cout<<coeff[0][i]<<" ";
			}
			cout<<"\n";
		}
		if(forward){
			currentInitialConditions = PSM::evaluateAll(coeff,step);
		}
		else{
			currentInitialConditions = PSM::evaluateAll(coeff,-step);
		}
		if(debug){
			cout<<"Computed new initial conditions\n";
		}
		for(int j = 0; j<numberOfEquations; j++){
			solution[j].push_back(currentInitialConditions[j]);
		}	
	}
	return solution;
}



