#ifndef CLONEDETECT_H
#define CLONEDETECT_H
#include "parseTokens.h"
#include <vector>

void cloneDetectAlgorithm(
        const std::unordered_map<std::string, block_type>& CM,
        int k, double beta, double theta, double phi, double eta, const std::string& directory);
    
#endif // CLONEDETECT_H