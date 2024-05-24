#ifndef VERIFYSIM_H
#define VERIFYSIM_H
#include <vector>

double verifySim(const std::vector<std::tuple<std::string, int, std::vector<int>>>& P, const std::vector<std::tuple<std::string, int, std::vector<int>>>& Q, double phi);

#endif // VERIFYSIM_H