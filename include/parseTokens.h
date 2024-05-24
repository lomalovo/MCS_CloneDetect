#ifndef PARSETOKENS_H
#define PARSETOKENS_H
#include <unordered_map>
#include <vector>
#include <string>
#include <tuple>

using block_type = std::unordered_map<std::string, std::vector<std::tuple<std::string, int, std::vector<int>>>>;

std::unordered_map<std::string, block_type> parseTokens(const std::string& filename);

#endif // PARSETOKENS_H