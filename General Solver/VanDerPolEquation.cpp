#include "psmGeneralFunctions.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>

using namespace std;


class VanDerPolEquation: public PSM{
	public:
	
		void updateNthCoefficient(vector<double> params, vector<vector<double>> &y, int n){
			/*We have the equation x''+a(x^2-1)x'+x = 0
			 * We rewrite as the system:
			 * y0'' = -ay1 y2 -y0 
			 * y1 = y0^2-1
			 * y2 = y0'
			 * 
			 * Assuming we know the coefficients up to n
			 * we update the n+1 coefficients
			 */
			 //Set parameters in simpler notation
			 double a = params[0];
			 			 
			 //Third equation
			 y[0][n+1] = y[2][n]/(n+1);//The nth value is known
			 
			 //Second Equation
			 y[1][n+1] = nthCoefficientProduct(y[0],y[0],n+1);
			 //We never need to substract 1 as this information is in the initial conditions
			 
			 //First equation
			 y[0][n+2] = (-a*y[1][n]*y[2][n]-y[0][n])/(n+2);
			 
			 //Update rest of values
			 y[2][n+1] = (n+2)*y[0][n+2];			
		}	
};



int main(){

	VanDerPolEquation VanDerPol;
	double step = .1;
	vector<double> params = {1};
	vector<double> initialConditions = {1,0,0};
	double end = .5;
	int n = 10;
	vector<vector<double>> sol = VanDerPol.findSolution(params,initialConditions,step, end, n, 1);
	
	
	for(int i = 0; i*step<=end; i++){
		cout<<setw(15)<<i*step<<setw(15)<<sol[0][i]<<setw(15)<<sol[1][i]<<setw(15)<<sol[0][i]*sol[0][i]-1<<"\n";
	}
	

}
