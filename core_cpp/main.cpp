#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <filesystem>
#include <map>
#include "parseTokens.h"
#include "cloneDetect.h"
#include "tqdm.h"

namespace fs = std::filesystem;

std::string parseAndFormatString(const std::string &input) {
    size_t filePathStart = input.find("filePath:") + 9;
    size_t filePathEnd = input.find(", startline:");

    std::string filePath = input.substr(filePathStart, filePathEnd - filePathStart);

    size_t fileNameStart = filePath.rfind('/') + 1;
    std::string fileName = filePath.substr(fileNameStart);

    size_t startlineStart = input.find("startline:") + 10;
    size_t startlineEnd = input.find(", endline:");
    int startline = std::stoi(input.substr(startlineStart, startlineEnd - startlineStart));

    size_t endlineStart = input.find("endline:") + 8;
    size_t endlineEnd = input.find(", validTokenNum:");
    int endline = std::stoi(input.substr(endlineStart, endlineEnd - endlineStart));

    size_t directoryStart = filePath.rfind('/', fileNameStart - 2);
    std::string directory = filePath.substr(directoryStart + 1, fileNameStart - directoryStart - 2);

    std::string result = directory + "," + fileName + "," + std::to_string(startline) + "," + std::to_string(endline);

    return result;
}



std::unordered_map<std::string, block_type> process_files(const std::string& directory) {
    std::vector<std::string> file_paths;
    for (const auto& entry : fs::directory_iterator(directory)) {
        if (entry.is_regular_file()) {
            file_paths.push_back(entry.path());
        }
    }

    std::unordered_map<std::string, block_type> combined_data;
    for (const auto& file_path : tq::tqdm(file_paths)) {
        auto tokens = parseTokens(file_path);
        for (const auto& token : tokens) {
            combined_data.insert(token);
        }
    }
    return combined_data;
}

int main([[maybe_unused]] int argc, char *argv[]) {
    std::string mode(argv[1]);
    std::string dir = "./tokens";
    std::vector<std::string> dirs_paths;

    double beta = 0.5;
    double theta = 0.4;
    double eta = 0.65;
    double phi = 0.1;
    int k = 5;

    if (mode == "bcb") {
        for (const auto &entry: fs::directory_iterator(dir)) {
            if (entry.is_directory()) {
                dirs_paths.push_back(entry.path());
            }
        }
    } else if (mode == "common"){
        dirs_paths.push_back(dir);
    } else {
        std::cout << "wrong mode" << std::endl;
    }

    for (const auto& directory : dirs_paths) {
        std::cout << "\nCreating map of: " << directory << std::endl;
        auto data = process_files(directory);

        std::cout << "\nDetecting clones in: " << directory << std::endl;
        std::vector<std::pair<std::string, std::string>> clone_detect_result;
        clone_detect_result = cloneDetectAlgorithm(data, k, beta, theta, phi, eta);

        size_t last = directory.rfind('/');
        std::string new_directory = directory.substr(last + 1);
        std::string direc("./results/");
        std::ofstream outf(direc + new_directory + ".pair", std::ios_base::app);
        std::cout << "\nSaving results to: " << direc + new_directory + ".pair" << std::endl;

        if (mode == "bcb") {
            for (const auto& clone : clone_detect_result) {
                if (clone.first != clone.second) {
                    outf << parseAndFormatString(clone.first) << "," << parseAndFormatString(clone.second) << std::endl;
                }
            }
        } else {
            for (const auto& clone : clone_detect_result) {
                if (clone.first != clone.second) {
                    outf << clone.first << "," << clone.second << std::endl;
                }
            }
        }
    }
    return 0;
}
