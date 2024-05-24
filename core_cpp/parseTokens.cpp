#include <regex>
#include <fstream>
#include "parseTokens.h"

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
        } else {
            std::regex item_match(R"(([^\s]+),(\d+): \[(.*?)\])");
            std::smatch match;
            if (std::regex_search(line, match, item_match)) {
                std::string name = match[1];
                int count = std::stoi(match[2]);
                std::vector<int> values;

                std::string values_str = match[3];
                std::size_t start = 0;
                std::size_t end = values_str.find(", ");
                while (end != std::string::npos) {
                    values.push_back(std::stoi(values_str.substr(start, end - start)));
                    start = end + 2; // Skip the delimiter ", "
                    end = values_str.find(", ", start);
                }
                current_block[current_category].emplace_back(name, count, values);
            }
        }
    }
    if (!current_block.empty()) {
        data_blocks[block_data] = current_block;
    }
    return data_blocks;
}
