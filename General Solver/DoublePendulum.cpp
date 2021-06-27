#include "psmGeneralFunctions.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>

using namespace std;

ofstream out;

class DoublePendulum: public PSM{
	public:
	
		void updateNthCoefficient(const vector<double> &params, vector<vector<double>> &y, int n){
			/*We have the equation x' = sin(x)
			 * We rewrite as the system:
			 * y0' = y1
			 * y1' = y1 y2 
			 * y2' = -y1^2
			 * Where y1 = sin(y0) y2 = cos(y0)
			 * 
			 * Assuming we know the coefficients up to n
			 * we update the n+1 coefficients
			 */
			 if(n+1>= y[0].size()){
				 //Add a flag like this to avoid segmentation fault
				 return;
			 }
			 			 
			 //First equation
			 y[0][n+1] = y[1][n]/(n+1);//The nth value is known
			 
			 //Second Equation
			 y[1][n+1] = nthCoefficientProduct(y[1],y[2],n)/(n+1);
			 
			 //Third Equation
			 y[2][n+1] = -nthCoefficientProduct(y[1],y[1],n)/(n+1);
		
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

	DoublePendulum flame;
	double step = .1;
	vector<double> params = {0};
	vector<double> initialConditions = {.5, sin(.5),cos(.5)};
	double end = 2;
	int n = 10;
	PSM::Solution sol = flame.findSolution(params,initialConditions,step, end, n, 1);
	
	flame.writeToFile("doublePendulum.dat",sol);
	
}
