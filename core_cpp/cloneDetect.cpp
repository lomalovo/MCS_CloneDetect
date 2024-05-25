#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <string>
#include <algorithm>
#include <numeric>
#include <set>
#include <iostream>
#include <fstream>
#include "verifySim.h"
#include "threadPool.h"


using block_type = std::unordered_map<std::string, std::vector<std::tuple<std::string, int, std::vector<int>>>>;

std::vector<std::string> uniqueActionTokensList(const block_type& codeBlock) {
    std::unordered_set<std::string> AT;
    for (const auto& token : codeBlock.at("AT")) {
        AT.insert(std::get<0>(token));
    }

    return std::vector<std::string>(AT.begin(), AT.end());
}

std::vector<std::string> actionTokensList(const block_type& codeBlock) {
    std::vector<std::string> AT;
    for (const auto& key : {"variable", "field", "method", "keyword", "type", "basic type"}) {
        for (const auto& token : codeBlock.at(key)) {
            AT.insert(AT.end(), std::get<1>(token), std::get<0>(token));
        }
    }
    return AT;
}

std::size_t hash_tokens(const std::vector<std::string>& tokens) {
    std::string concatenated_tokens = std::accumulate(tokens.begin(), tokens.end(), std::string());
    return std::hash<std::string>{}(concatenated_tokens);
}

std::vector<std::pair<std::size_t, std::vector<std::string>>> slide_window(const std::vector<std::string>& tokens, size_t k) {
    std::vector<std::pair<std::size_t, std::vector<std::string>>> windows;
    windows.reserve(10000);
    for (int i = 0; i <= (int)tokens.size() - (int)k; i++) {
        std::vector<std::string> window_tokens(tokens.begin() + i, tokens.begin() + i + (int)k);
        windows.emplace_back(hash_tokens(window_tokens), std::move(window_tokens));
    }
    return windows;
}

std::unordered_map<std::size_t, std::vector<std::string>> buildKTokensIndex(
    const std::unordered_map<std::string, block_type>& blocks, size_t k) {
    std::unordered_map<std::size_t, std::vector<std::string>> inverted_index;
    inverted_index.reserve(50000);
    for (const auto& [block_name, codeBlock] : blocks) {
        std::vector<std::string> sorted_tokens = uniqueActionTokensList(codeBlock);
        std::sort(sorted_tokens.begin(), sorted_tokens.end());
        for (const auto& [token_hash, tokens] : slide_window(sorted_tokens, k)) {
            inverted_index[token_hash].push_back(block_name);
        }
    }
    return inverted_index;
}

int countSameAction(const std::vector<std::string>& actionTokens, const std::vector<std::string>& candidateActionTokens) {
    std::unordered_map<std::string, int> counter;
    for (const auto& token : actionTokens) {
        counter[token]++;
    }
    int count = 0;
    for (const auto& token : candidateActionTokens) {
        if (counter[token] > 0) {
            count++;
            counter[token]--;
        }
    }
    return count;
}

int parseBlock(const std::string& block_path, const std::string& key) {
    if (block_path.empty()) {
        return 0;
    }

    size_t startPos = block_path.find(key);
    startPos += key.length();

    while (std::isspace(block_path[startPos])) {
        ++startPos;
    }

    size_t endPos = block_path.find(',', startPos);
    if (endPos == std::string::npos) {
        endPos = block_path.length();
    }

    std::string numberStr = block_path.substr(startPos, endPos - startPos);
    return std::stoi(numberStr);
}


std::vector<std::pair<std::string, std::string>> cloneDetect(
    const std::unordered_map<std::string, block_type>& CM,
    const std::string& block_path,
    const block_type& block_data,
    const std::unordered_map<std::size_t, std::vector<std::string>>& kIndex,
    int k, double beta, double theta, double phi, double eta) {
    
    std::vector<std::pair<std::string, std::string>> clonePairs;

    std::set<std::string> cloneCandidates;

//        std::cout << "building unique AT" << std::endl;
    auto actionTokens = uniqueActionTokensList(block_data);
//        std::cout << "sort unique AT" << std::endl;
    std::sort(actionTokens.begin(), actionTokens.end());

    for (int i = 0; i <= (int)actionTokens.size() - (int)k; ++i) {
        std::vector<std::string> k_tokens(actionTokens.begin() + i, actionTokens.begin() + i + k);
        std::size_t token_hash = hash_tokens(k_tokens);
        if (kIndex.find(token_hash) != kIndex.end()) {
            cloneCandidates.insert(kIndex.at(token_hash).begin(), kIndex.at(token_hash).end());
        }
    }

//        std::cout << "filtering" << std::endl;
    std::set<std::string> filteredClones;
    int totalTokenNum = parseBlock(block_path, std::string("validTokenNum:"));

    for (const auto& candidate : cloneCandidates) {
        auto candidateActionTokens = uniqueActionTokensList(CM.at(candidate));
        int totalCandidateTokenNum = parseBlock(candidate, std::string("validTokenNum:"));
        int sat = countSameAction(actionTokens, candidateActionTokens);
        double ato = static_cast<double>(sat) / std::min(actionTokens.size(), candidateActionTokens.size());
        double tr = static_cast<double>(std::min(totalTokenNum, totalCandidateTokenNum)) / std::max(totalTokenNum, totalCandidateTokenNum);

        if (ato > beta && tr > theta) {
            filteredClones.insert(candidate);
        }
    }

//        std::cout << "verifying" << std::endl;
    for (const auto& candidate : filteredClones) {
        double simVT = verifySim(CM.at(candidate).at("variable group"), block_data.at("variable group"), phi);
        double simET = verifySim(CM.at(candidate).at("method group"), block_data.at("method group"), phi);
        double simCT = verifySim(CM.at(candidate).at("relation"), block_data.at("relation"), phi);
        if ((simVT + simET + simCT) / 3 > eta) {
            clonePairs.emplace_back(block_path, candidate);
        }
    }

    return clonePairs;
}

void cloneDetectAlgorithm(
    const std::unordered_map<std::string, block_type>& CM,
    int k, double beta, double theta, double phi, double eta, const std::string& directory) {

    auto kIndex = buildKTokensIndex(CM, k);

    std::mutex file_mutex;
    const size_t num_threads = std::thread::hardware_concurrency();
    ThreadPool pool(num_threads);

    for (const auto& block : CM) {
        pool.enqueue([&, block] {
            auto res = cloneDetect(CM, block.first, block.second, kIndex, k, beta, theta, phi, eta);

            std::lock_guard<std::mutex> guard(file_mutex);
            std::cout << "Saving results of: " << directory << std::endl;
            size_t last = directory.rfind('/');
            std::string new_directory = directory.substr(last + 1);
            std::string dir("./results/");
            std::ofstream outfile(dir + new_directory + ".pair", std::ios_base::app);

            for (const auto& clone : res) {
                if (clone.first != clone.second) {
                    outfile << clone.first << ", " << clone.second << std::endl;
                }
            }
        });
    }

    pool.wait_for_completion();
}