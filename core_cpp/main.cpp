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
    // Найти позиции ключевых слов
    size_t filePathPos = input.find("filePath:") + 9;
    size_t startlinePos = input.find("startline:") + 10;
    size_t endlinePos = input.find("endline:") + 8;

    // Найти конец строки пути
    size_t filePathEndPos = input.find(',', filePathPos);
    std::string filePath = input.substr(filePathPos, filePathEndPos - filePathPos);

    // Найти конец строки startline
    size_t startlineEndPos = input.find(',', startlinePos);
    std::string startline = input.substr(startlinePos, startlineEndPos - startlinePos);

    // Найти конец строки endline
    size_t endlineEndPos = input.find(',', endlinePos);
    std::string endline = input.substr(endlinePos, endlineEndPos - endlinePos);

    // Форматировать строку
    std::string result = filePath + "," + startline + "," + endline;

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

        std::cout << "\nDetecting clones in: " << directory << std::endl;
        std::vector<std::pair<std::string, std::string>> clone_detect_result;
        cloneDetectAlgorithm(data, k, beta, theta, phi, eta, directory);

        size_t last = directory.rfind('/');
        std::string new_directory = directory.substr(last + 1);
        std::string diric("./results/");
        std::ofstream outf(diric + new_directory + ".pair", std::ios_base::app);
        std::cout << "\nSaving results to: " << diric + new_directory + ".pair" << std::endl;

        for (const auto& clone : clone_detect_result) {
            outf << parseAndFormatString(clone.first) << "," << parseAndFormatString(clone.second) << std::endl;
        }

    }
    return 0;
}
