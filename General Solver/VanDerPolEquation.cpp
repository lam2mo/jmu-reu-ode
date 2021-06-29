#include "psmGeneralFunctions.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>
#include <string>

using namespace std;

ofstream out;

class VanDerPolEquation: public PSM{
	public:
	
		void updateNthCoefficient(const vector<double> &params, vector<vector<double>> &y, int n){
			/*We have the equation x''+a(x^2-1)x'+x = 0
			 * We rewrite as the system:
			 * y0'' = -ay1 y2 -y0 
			 * y1 = y0^2-1
			 * y2 = y0'
			 * 
			 * Assuming we know the coefficients up to n
			 * we update the n+1 coefficients
			 */
			 if(n+2>= (int)y[0].size()){
				 //Add a flag like this to avoid segmentation fault
				 return;
			 }
			 //Set parameters in simpler notation
			 double a = params[0];
			 			 
			 //Third equation
			 y[0][n+1] = y[2][n]/(n+1);//The nth value is known
			 
			 //Second Equation
			 y[1][n+1] = nthCoefficientProduct(y[0],y[0],n+1);
			 //We never need to substract 1 as this information is in the initial conditions
			 
			 //First equation
			 y[0][n+2] = (-a*nthCoefficientProduct(y[1],y[2],n)-y[0][n])/((n+1)*(n+2));
			 
			 //Update rest of values
			 y[2][n+1] = (n+2)*y[0][n+2];			
		}
		
		void writeToFile(string file,Solution sol1){
			/* Writes to the file
			 * It receives sol1, the output of findSolution
			 */

			out.open(file);
			for(unsigned int i = 0; i<sol1.solutions[0].size(); i++){
				out<<setw(15)<<sol1.steps[i]<<setw(15)<<sol1.solutions[0][i]<<setw(15)<<sol1.solutions[2][i]<<"\n";
			}
			out.close();
		}	
		void writeToFile(string file,Solution sol1, Solution sol2){
			/* Writes to the file
			 * It receives sol1 the negative part and sol2 the positive
			 */

			out.open(file);
			for(int i = sol1.solutions[0].size()-1; i>=0 ;i--){
				out<<setw(15)<<sol1.steps[i]<<setw(15)<<sol1.solutions[0][i]<<setw(15)<<sol1.solutions[2][i]<<"\n";
			}
			for(unsigned int i = 1; i<sol2.solutions[0].size(); i++){
				out<<setw(15)<<sol2.steps[i]<<setw(15)<<sol2.solutions[0][i]<<setw(15)<<sol2.solutions[2][i]<<"\n";
			}
				
			out.close();
		}	
};



int main(int argc, const char* argv[]){
	
	if(argc != 6){
        cout << "Usage: " << argv[0] << " <a> <x0> <px0> <tn> <dt> " << endl;
        return EXIT_FAILURE;
	}
	double a = stod(argv[1],NULL);
	double x0 = stod(argv[2],NULL);
	double px0 = stod(argv[3],NULL);
	double end = stod(argv[4],NULL);
	double step = stod(argv[5],NULL);

	VanDerPolEquation VanDerPol;
	vector<double> params = {a};
	vector<double> initialConditions = {x0,px0,x0*x0-1};
	int n = 10;
	PSM::Solution sol2 = VanDerPol.findSolution(params,initialConditions,step, end, n, 1);
	PSM::Solution sol1 = VanDerPol.findSolution(params,initialConditions,step, end, n, 0);
	
	PSM::Solution sol4 = VanDerPol.findSolutionAdaptive(params,initialConditions,end,1);
	PSM::Solution sol3 = VanDerPol.findSolutionAdaptive(params,initialConditions,end,0);
	
	VanDerPol.writeToFile("vanDerPol.dat",sol1,sol2);
	VanDerPol.writeToFile("vanDerPolAdaptive.dat",sol3,sol4);

	return 0;
}
