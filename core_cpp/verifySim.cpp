#include <cmath>
#include <string>
#include "verifySim.h"

double cosine_similarity(const std::vector<int>& vector1, const std::vector<int>& vector2) {
    double norm1 = 0, norm2 = 0, dot_product = 0;

    for (size_t i = 0; i < vector1.size(); ++i) {
        dot_product += vector1[i] * vector2[i];
        norm1 += vector1[i] * vector1[i];
        norm2 += vector2[i] * vector2[i];
    }

    norm1 = std::sqrt(norm1);
    norm2 = std::sqrt(norm2);
    if (norm1 == 0 || norm2 == 0) {
        return 0;
    }

    return dot_product / (norm1 * norm2);
}

double verifySim(const std::vector<std::tuple<std::string, int, std::vector<int>>>& P, const std::vector<std::tuple<std::string, int, std::vector<int>>>& Q, double phi) {
    double totalSim = 0;
    size_t len_p = P.size(), len_q = Q.size();

    if (len_p == 0 || len_q == 0) {
        return 1;
    }

    std::vector<int> MP(len_p, 0), MQ(len_q, 0);
    for (double t = 1.0; t > 0; t -= phi) {
        for (size_t i = 0; i < len_p; ++i) {
            if (MP[i] == 1) continue;

            for (size_t j = 0; j < len_q; ++j) {
                if (MQ[j] == 1) continue;

                double sim = cosine_similarity(std::get<2>(P[i]), std::get<2>(Q[j]));
                if (sim >= t) {
                    totalSim += sim;
                    MP[i] = 1;
                    MQ[j] = 1;
                }
            }
        }
    }

    return totalSim / std::max(len_p, len_q);
}