#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <filesystem>
#include <map>
#include "parseTokens.h"
#include "cloneDetect.h"
#include "threadPool.h"


namespace fs = std::filesystem;

std::unordered_map<std::string, block_type> process_files(const std::string& directory) {
    std::vector<std::string> file_paths;
    for (const auto& entry : std::filesystem::directory_iterator(directory)) {
        if (entry.is_regular_file()) {
            file_paths.push_back(entry.path().string());
        }
    }

    std::unordered_map<std::string, block_type> combined_data;
    std::mutex data_mutex;

    const size_t num_threads = std::thread::hardware_concurrency();
    ThreadPool pool(num_threads);

    for (const auto& file_path : file_paths) {
        pool.enqueue([&combined_data, &data_mutex, file_path] {
            auto tokens = parseTokens(file_path);
            std::lock_guard<std::mutex> guard(data_mutex);
            for (const auto& token : tokens) {
                combined_data.insert(token);
            }
        });
    }

    pool.wait_for_completion();

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
        cloneDetectAlgorithm(data, k, beta, theta, phi, eta, directory);
    }
    return 0;
}
