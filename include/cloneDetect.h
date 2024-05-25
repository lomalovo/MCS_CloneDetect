#ifndef CLONEDETECT_H
#define CLONEDETECT_H
#include "parseTokens.h"
#include <vector>

std::vector<std::pair<std::string, std::string>> cloneDetectAlgorithm(
        const std::unordered_map<std::string, block_type>& CM,
        int k, double beta, double theta, double phi, double eta);
    
#endif // CLONEDETECT_H