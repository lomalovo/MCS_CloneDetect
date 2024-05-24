#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <filesystem>
#include <map>
#include "parseTokens.h"
#include "cloneDetect.h"

namespace fs = std::filesystem;

std::unordered_map<std::string, block_type> process_files(const std::string& directory) {
    std::vector<std::string> file_paths;
    for (const auto& entry : fs::directory_iterator(directory)) {
        if (entry.is_regular_file()) {
            file_paths.push_back(entry.path());
        }
    }

    std::unordered_map<std::string, block_type> combined_data;
    for (const auto& file_path : file_paths) {
        auto tokens = parseTokens(file_path);
        for (const auto& token : tokens) {
            combined_data.insert(token);
        }
    }
    return combined_data;
}

int main() {
    std::string dir = "./tokens";
    std::vector<std::string> dirs_paths;
    for (const auto& entry : fs::directory_iterator(dir)) {
        
        if (entry.is_directory()) {
            dirs_paths.push_back(entry.path());
        }
    }


    double beta = 0.5;
    double theta = 0.4;
    double eta = 0.65;
    double phi = 0.1;
    int k = 5;

    std::ofstream outfile("./clonepairs.txt");

    if (!outfile.is_open()) {
        throw std::invalid_argument("Возникла ошибка при открытии входного файла.");
    }

    for (const auto& directory : dirs_paths) {
        std::cout << "Creating map of: " << directory << std::endl;
        auto data = process_files(directory);

        std::cout << "Detecting clones in: " << directory << std::endl;
        std::vector<std::pair<std::string, std::string>> clone_detect_result;
        clone_detect_result = cloneDetectAlgorithm(data, k, beta, theta, phi, eta);

        std::cout << "Saving results of: " << directory << std::endl;
        for (const auto& clone : clone_detect_result) {
            if (clone.first != clone.second) {
                outfile << "{ " << clone.first << ", " << clone.second << " }\n";
            }
        }

    }
    return 0;
}
