import numpy as np


def cosine_similarity(vector1, vector2):
    return np.dot(vector1, vector2) / (np.linalg.norm(vector1) * np.linalg.norm(vector2))


def verifySim(P, Q, phi):
    totalSim = 0
    len_p = len(P)
    len_q = len(Q)

    if len_p == 0 or len_q == 0:
        return 1

    # array MP,MQ are used to mark whether the vectors
    # at the corresponding position have been matched
    MP = [0] * len_p
    MQ = [0] * len_q

    for t in np.arange(1, 0, phi):
        for i in range(len_p):
            # skip i-th vector in P if it has been matched
            if MP[i] == 1:
                continue

            for j in range(len_q):
                # skip j-th vector in Q if it has been matched
                if MQ[j] == 1:
                    continue

                # calculate cosine similarity of two vectors
                sim = cosine_similarity(P[i][2], Q[j][2])

                # match i-th and j-th vector of P,Q, respectively
                if sim >= t:
                    totalSim += sim
                    MP[i] = 1
                    MQ[j] = 1

    return totalSim / max(len_p, len_q)
