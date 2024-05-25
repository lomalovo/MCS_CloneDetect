#include <regex>
#include <fstream>
#include "parseTokens.h"
#include "iostream"

std::tuple<std::string, int, std::vector<int>> parseString(const std::string &input) {
    size_t commaPos = input.find(',');
    size_t colonPos = input.find(':');
    size_t bracketPos = input.find('[');
    size_t bracketEndPos = input.find(']');

    std::string name = input.substr(0, commaPos);
    int number = std::stoi(input.substr(commaPos + 1, colonPos - commaPos - 1));

    std::string vectorPart = input.substr(bracketPos + 1, bracketEndPos - bracketPos - 1);
    std::vector<int> vec;
    std::stringstream ss(vectorPart);
    std::string item;

    while (std::getline(ss, item, ',')) {
        vec.push_back(std::stoi(item));
    }

    return std::make_tuple(name, number, vec);
}

std::unordered_map<std::string, block_type> parseTokens(const std::string& filename) {

    std::ifstream file(filename);
    if (!file.is_open()) {
        throw std::invalid_argument("Возникла ошибка при открытии входного файла.");
    }

    std::string line;
    std::unordered_map<std::string, block_type> data_blocks;
    block_type current_block = {
            {"variable", {}},
            {"field", {}},
            {"method", {}},
            {"keyword", {}},
            {"type", {}},
            {"basic type", {}},
            {"AT", {}},
            {"variable group", {}},
            {"method group", {}},
            {"relation", {}}
    };
    std::string current_category;
    std::string block_data;

    while (std::getline(file, line)) {
        if (line.empty() || line == "\n") {
            continue;
        }

        if (line.find("<block") != std::string::npos) {
            if (!current_block.empty()) {
                for (const auto& key : {"variable", "field", "method", "keyword", "type",
                                        "basic type", "variable group", "method group", "relation"}) {
                    for (const auto& token : current_block.at(key)) {
                        current_block["AT"].push_back(token);
                    }
                }

                data_blocks[block_data] = current_block;
            }

            block_data = line.substr(7, line.size() - 8);
            current_block = {
                    {"variable", {}},
                    {"field", {}},
                    {"method", {}},
                    {"keyword", {}},
                    {"type", {}},
                    {"basic type", {}},
                    {"AT", {}},
                    {"variable group", {}},
                    {"method group", {}},
                    {"relation", {}}
            };

        } else if (line.find('<') != std::string::npos && line.find("</") == std::string::npos) {
            current_category = line.substr(1, line.size() - 2);
        } else if (line.find("</") == std::string::npos){
            current_block[current_category].push_back(parseString(line));
        }
    }
    if (!current_block.empty()) {
        data_blocks[block_data] = current_block;
    }
    return data_blocks;
}
