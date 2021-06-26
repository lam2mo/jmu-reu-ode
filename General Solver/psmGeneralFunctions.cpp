#include "psmGeneralFunctions.h"
#include <vector>
#include <cmath>

using namespace std;

double PSM::nthCoefficientProduct(vector<double>& x, vector<double>& y, int n){
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
	
	vector<vector<double>> coefficients(nn);
	
	//Add initial conditions
	for(int i = 0; i<nn; i++){
		coefficients[i][0] = initialConditions[i];
	}
	/*Calculate solution*/
	for(int n = 0; n<nn-1; n++){
		//This function has to be defined for every problem
		PSM::updateNthCoefficient(parameters,coefficients,n);
	}
	return coefficients;
}

	
vector<vector<double>> PSM::findSolution(vector<double> parameters, vector<double> initialConditions, double step, double end, int n, bool forward){
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

	//Start Stepping
	for(int i = 1; step*i<= end; i++){
		vector<vector<double>> coeff = PSM::computeCoefficients(parameters,currentInitialConditions,n);
		if(forward){
			currentInitialConditions = PSM::evaluateAll(coeff,step);
		}
		else{
			currentInitialConditions = PSM::evaluateAll(coeff,-step);
		}
		for(int j = 0; j<numberOfEquations; j++){
			solution[i].push_back(currentInitialConditions[i]);
		}	
	}
	return solution;
}

int main(){
	
}

	

