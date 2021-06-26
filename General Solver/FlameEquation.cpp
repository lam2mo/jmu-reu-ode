#include "psmGeneralFunctions.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>

using namespace std;

ofstream out;

class FlameEquation: public PSM{
	public:
	
		void updateNthCoefficient(const vector<double>& params, vector<vector<double>> &y, int n){
			/*We have the equation x' = x^2-x^3
			 * We rewrite as the system:
			 * y0' = y1 - y1 y0
			 * y1 = y0^2
			 * 
			 * Assuming we know the coefficients up to n
			 * we update the n+1 coefficients
			 */
			 if(n+1> y[0].size()){
				 //Add a flag like this to avoid segmentation fault
				 return;
			 }
			 			 
			 //First equation
			 y[0][n+1] = (y[1][n]-nthCoefficientProduct(y[1],y[0],n))/(n+1);//The nth value is known
			 
			 //Second Equation
			 y[1][n+1] = nthCoefficientProduct(y[0],y[0],n+1);
		
		}
		
		void writeToFile(string file,Solution sol1){
			/* Writes to the file
			 * It receives sol1, the output of findSolution
			 */

			out.open(file);
			for(int i = 0; i<sol1.solutions[0].size(); i++){
				out<<setw(15)<<sol1.steps[i]<<setw(15)<<sol1.solutions[0][i]<<"\n";
			}
			out.close();
		}	
};



int main(){

	FlameEquation flame;
	double step = .1;
	vector<double> params = {0};
	vector<double> initialConditions = {.5,.25};
	double end = 4;
	int n = 10;
	PSM::Solution sol = flame.findSolution(params,initialConditions,step, end, n, 1);
	
	flame.writeToFile("flame.dat",sol);
	
}
