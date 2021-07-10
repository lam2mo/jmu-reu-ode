#include "psmGeneralFunctions.h"

#include <iostream>
#include <iomanip>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>

using namespace std;
using namespace std::chrono;


ofstream out;

class FlameEquation: public PSM{
	public:
		void updateNthCoefficient(const vector<double>& params, vector<vector<double>> &y, int n){
			/*We have the equation x' = x^2-x^3
			 * We rewrite as the system:
			 * y0' = y1 - y1 y0
			 * y1mak = y0^2
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
        cout << "Usage: " << argv[0] << " <x0> <n> <tn> " << endl;
        return EXIT_FAILURE;
	}
	double x0 = stod(argv[1],NULL);
	int n = stod(argv[2],NULL);
	double end = stod(argv[3],NULL);
	
	FlameEquation flame;
	vector<double> params = {0};
	vector<double> initialConditions = {x0,x0*x0};
	
	double eps = .000001;
	
	out.open("timeComparisonFlame.dat");
	/*
	out<<"Flame Equation \n";
	out<<"----------------------\n";
	out<<"Initial condition "<<x0<<"\n";
	out<<"Degree "<<n<<"\n";
	out<<"Fixed Step "<<step<<"\n";
	out<<"Interval "<<end<<"\n";
	out<<setw(15)<<"Method"<<setw(15)<<"Time"<<setw(15)<<"Steps\n";
	*/
	flame.setMaxDegree(n);
	auto start = high_resolution_clock::now();
	/*
	PSM::Solution sol2 = flame.findSolution(params,initialConditions,step, end, n, 1);
	PSM::Solution sol1 = flame.findSolution(params,initialConditions,step, end, n, 0);
	*/
	auto stop = high_resolution_clock::now();
	auto duration = duration_cast<microseconds>(stop - start);
	//out<<setw(15)<<"Fixed"<<setw(15)<< duration.count()<<setw(15)<<(sol2.steps.size()+sol1.steps.size()-1)<<"\n";

	


	start = high_resolution_clock::now();
	PSM::Solution sol4 = flame.findSolutionAdaptive(params,initialConditions,end,1,eps);
	PSM::Solution sol3 = flame.findSolutionAdaptive(params,initialConditions,end,0,eps);
	stop = high_resolution_clock::now();
	duration = duration_cast<microseconds>(stop - start);
	//out<<setw(15)<<"Adaptive1"<<setw(15)<< duration.count()<<setw(15)<<(sol3.steps.size()+sol4.steps.size()-1)<<"\n";
	out<<"Adaptive1"<<" "<< n<<" "<<duration.count()<<" "<<(sol3.steps.size()+sol4.steps.size()-1)<<"\n";
	
	start = high_resolution_clock::now();

	PSM::Solution sol6 = flame.findAdaptiveSolutionTruncation(params,initialConditions,end,1,eps);
	PSM::Solution sol5 = flame.findAdaptiveSolutionTruncation(params,initialConditions,end,0,eps);
	
	stop = high_resolution_clock::now();
	duration = duration_cast<microseconds>(stop - start);
	out<<"Adaptive2"<<" "<<n<<" "<< duration.count()<<" "<<(sol5.steps.size()+sol6.steps.size()-1)<<"\n";
	

  	start = high_resolution_clock::now();
	PSM::Solution sol8 = flame.findAdaptiveSolutionJorbaAndZou(params,initialConditions,end,1,eps);
	PSM::Solution sol7 = flame.findAdaptiveSolutionJorbaAndZou(params,initialConditions,end,0,eps);
	stop = high_resolution_clock::now();
	duration = duration_cast<microseconds>(stop - start);
	out<<"Adaptive3"<<" "<<n<<" "<< duration.count()<<" "<<(sol8.steps.size()+sol7.steps.size()-1)<<"\n";
	out.close();
	
	/*
	flame.writeToFile("flame.dat",sol1,sol2);
	flame.writeToFile("flameAdaptive.dat",sol3,sol4);
	flame.writeToFile("flameAdaptive2.dat",sol5,sol6);
	flame.writeToFile("flameAdaptive3.dat",sol7,sol8);
	*/


	
}
