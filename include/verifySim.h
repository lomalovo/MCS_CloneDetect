#ifndef VERIFYSIM_H
#define VERIFYSIM_H
#include <vector>

using vectorType = std::vector<std::tuple<std::string, int, std::vector<int>>>;

double verifySim(const vectorType& P, const vectorType& Q, double phi);

#endif // VERIFYSIM_H