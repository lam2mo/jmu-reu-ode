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
			 if(n+1>= (int)y[0].size()){
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
			for(unsigned int i = 0; i<sol1.solutions[0].size(); i++){
				out<<setw(15)<<sol1.steps[i]<<setw(15)<<sol1.solutions[0][i]<<"\n";
			}
			out.close();
		}	
		void writeToFile(string file,Solution sol1, Solution sol2){
			/* Writes to the file
			 * It receives sol1 the negative part and sol2 the positive
			 */

			out.open(file);
			for(int i = sol1.solutions[0].size()-1; i>=0 ;i--){
				out<<setw(15)<<sol1.steps[i]<<setw(15)<<sol1.solutions[0][i]<<"\n";
			}
			for(unsigned int i = 1; i<sol2.solutions[0].size(); i++){
				out<<setw(15)<<sol2.steps[i]<<setw(15)<<sol2.solutions[0][i]<<"\n";
			}
				
			out.close();
		}	
};



int main(int argc, const char* argv[]){
	
	if(argc != 5){
		cout<<"Usage <x0> <step> <end> <n>\n";
		return EXIT_FAILURE;
	}
	double x0 = stod(argv[1],NULL);
	double step = stod(argv[2],NULL);
	double end = stod(argv[3],NULL);
	int n = stod(argv[4],NULL);

	DoublePendulum pendulum;
	vector<double> params = {0};
	vector<double> initialConditions = {x0, sin(x0),cos(x0)};
	PSM::Solution sol2 = pendulum.findSolution(params,initialConditions,step, end, n, 1);
	PSM::Solution sol1 = pendulum.findSolution(params,initialConditions,step, end, n, 0);
	
	PSM::Solution sol4 = pendulum.findSolutionAdaptive(params,initialConditions,end,1);
	PSM::Solution sol3 = pendulum.findSolutionAdaptive(params,initialConditions,end,0);
	
	pendulum.writeToFile("doublePendulum.dat",sol1,sol2);
	pendulum.writeToFile("doublePendulumAdaptive.dat",sol3,sol4);

	
}
