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
			 if(n+1>= (int)y[0].size()){
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
	
	if(argc != 4){
        cout << "Usage: " << argv[0] << " <x0> <tn> <dt> " << endl;
        return EXIT_FAILURE;
	}
	double x0 = stod(argv[1],NULL);
	double end = stod(argv[2],NULL);
	double step = stod(argv[3],NULL);
	
	FlameEquation flame;
	vector<double> params = {0};
	vector<double> initialConditions = {x0,x0*x0};
	int n = 10;
	PSM::Solution sol2 = flame.findSolution(params,initialConditions,step, end, n, 1);
	PSM::Solution sol1 = flame.findSolution(params,initialConditions,step, end, n, 0);
	
	PSM::Solution sol3 = flame.findSolutionAdaptive(params,initialConditions,end,1);
	PSM::Solution sol4 = flame.findSolutionAdaptive(params,initialConditions,end,0);
	
	double eps = .000001;
	PSM::Solution sol6 = flame.findAdaptiveSolutionTruncation(params,initialConditions,end,1,eps);
	PSM::Solution sol5 = flame.findAdaptiveSolutionTruncation(params,initialConditions,end,0,eps);
	

	
	flame.writeToFile("flame.dat",sol1,sol2);
	flame.writeToFile("flameAdaptive.dat",sol3,sol4);
	flame.writeToFile("flameAdaptive2.dat",sol5,sol6);


	
}
