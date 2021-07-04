#include "psmGeneralFunctions.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>
#include <string>

using namespace std;
using namespace std::chrono;

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
	
	
	double eps = .000001;
	
	out.open("timeComparisonVanDerPol.dat");
	
	out<<"Van der Pol Equation \n";
	out<<"----------------------\n";
	out<<"Initial condition "<<x0<<"\n";
	out<<"Initial derivative "<<px0<<"\n";
	out<<"Parameter a "<<a<<"\n";
	out<<"Degree "<<n<<"\n";
	out<<"Fixed Step "<<step<<"\n";
	out<<"Interval "<<end<<"\n";
	out<<setw(15)<<"Method"<<setw(15)<<"Time"<<setw(15)<<"Steps\n";
	VanDerPol.setMaxDegree(n);
	auto start = high_resolution_clock::now();
	
	PSM::Solution sol2 = VanDerPol.findSolution(params,initialConditions,step, end, n, 1);
	PSM::Solution sol1 = VanDerPol.findSolution(params,initialConditions,step, end, n, 0);
	
	auto stop = high_resolution_clock::now();
	auto duration = duration_cast<microseconds>(stop - start);
	out<<setw(15)<<"Fixed"<<setw(15)<< duration.count()<<setw(15)<<(sol2.steps.size()+sol1.steps.size()-1)<<"\n";
	


	start = high_resolution_clock::now();
		
	PSM::Solution sol4 = VanDerPol.findSolutionAdaptive(params,initialConditions,end,1,eps);
	PSM::Solution sol3 = VanDerPol.findSolutionAdaptive(params,initialConditions,end,0,eps);

	stop = high_resolution_clock::now();
	duration = duration_cast<microseconds>(stop - start);
	out<<setw(15)<<"Adaptive1"<<setw(15)<< duration.count()<<setw(15)<<(sol3.steps.size()+sol4.steps.size()-1)<<"\n";
	
	start = high_resolution_clock::now();

	PSM::Solution sol6 = VanDerPol.findAdaptiveSolutionTruncation(params,initialConditions,end,1,eps);
	PSM::Solution sol5 = VanDerPol.findAdaptiveSolutionTruncation(params,initialConditions,end,0,eps);
	
	stop = high_resolution_clock::now();
	duration = duration_cast<microseconds>(stop - start);
	out<<setw(15)<<"Adaptive2"<<setw(15)<< duration.count()<<setw(15)<<(sol5.steps.size()+sol6.steps.size()-1)<<"\n";
	out.close();
	
/*
  	start = high_resolution_clock::now();
	PSM::Solution sol8 = VanDerPol.findAdaptiveSolutionJorbaAndZou(params,initialConditions,end,1,eps);
	PSM::Solution sol7 = VanDerPol.findAdaptiveSolutionJorbaAndZou(params,initialConditions,end,0,eps);
	stop = high_resolution_clock::now();
	duration = duration_cast<microseconds>(stop - start);
	out<<setw(15)<<"Adaptive3"<<setw(15)<< duration.count()<<setw(15)<<(sol8.steps.size()+sol7.steps.size()-1)<<"\n";
	out.close();
*/
	VanDerPol.writeToFile("vanDerPol.dat",sol1,sol2);
	VanDerPol.writeToFile("vanDerPolAdaptive.dat",sol3,sol4);
	VanDerPol.writeToFile("vanDerPolAdaptive2.dat",sol5,sol6);
	//VanDerPol.writeToFile("vanDerPolAdaptive3.dat",sol7,sol8);



	return 0;
}
