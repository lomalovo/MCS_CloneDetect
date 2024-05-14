import sys

import verifySim
import hashlib


def uniqueActionTokensList(codeBlock):
    AT = (codeBlock["variable"] + codeBlock["field"] + codeBlock["method"] +
          codeBlock["keyword"] + codeBlock["type"] + codeBlock["basic type"])
    AT = list(map(lambda x: x[0], AT))
    return AT


def actionTokensList(codeBlock):
    AT = []
    info = (codeBlock["variable"] + codeBlock["field"] + codeBlock["method"] +
            codeBlock["keyword"] + codeBlock["type"] + codeBlock["basic type"])
    for token in info:
        for _ in token[1]:
            AT.append(token[0])


def hash_tokens(tokens):
    return hashlib.sha256(''.join(tokens).encode()).hexdigest()


def slide_window(tokens, k):
    for i in range(len(tokens) - k + 1):
        yield tuple(tokens[i:i + k])


def buildKTokensIndex(blocks, k):
    inverted_index = {}

    for block_name, codeBlock in blocks.items():
        sorted_tokens = sorted(uniqueActionTokensList(codeBlock))

        for k_tokens in slide_window(sorted_tokens, k):
            token_hash = hash_tokens(k_tokens)
            if token_hash not in inverted_index:
                inverted_index[token_hash] = []
            inverted_index[token_hash].append(block_name)

    return inverted_index


def countSameAction(actionTokens, candidateActionTokens):
    count = 0
    for token in actionTokens:
        if token in candidateActionTokens:
            count += 1
    return count


# for string "filePath:/Users/matvey/Desktop/project/bcb-reduced/45/selected/1448701.java, startline:307, endline:580,
# validTokenNum:662, totalTokenNum: 740"
# returns totalTokenNum
def countTotalTokenNum(block_path):
    return int(block_path.split(", ")[-1].split(" ")[1])


# for string "filePath:/Users/matvey/Desktop/project/bcb-reduced/45/selected/1448701.java, startline:307, endline:580,
# validTokenNum:662, totalTokenNum: 740"
# returns validTokenNum
def countValidTokenNum(block_path):
    return int(block_path.split(", ")[-2].split(":")[1])


def cloneDetectAlgorithm(CM, k, beta, theta, phi, eta):
    clonePairs = []

    # build a global k-tokens index on Action tokens
    kIndex = buildKTokensIndex(CM, k)

    """ location step """
    for block_path, block_data in CM.items():

        # candidate block set
        cloneCandidates = set()

        # action tokens list
        actionTokens = uniqueActionTokensList(block_data)
        sortedActionTokens = sorted(actionTokens)

        for k_tokens in slide_window(sortedActionTokens, k):
            token_hash = hash_tokens(k_tokens)
            if token_hash in kIndex:
                cloneCandidates = cloneCandidates.union(kIndex[token_hash])

        """filtration step"""
        # candidate blocks after filtering
        filteredClones = set()
        # count the total token number of cj
        totalTokenNum = countValidTokenNum(block_path)

        for candidate in cloneCandidates:
            candidateActionTokens = uniqueActionTokensList(CM[candidate])
            totalCandidateTokenNum = countValidTokenNum(candidate)
            # count the number of same Action tokens
            sat = countSameAction(actionTokens, candidateActionTokens)

            ato = sat / min(len(sortedActionTokens), len(candidateActionTokens))

            # token ratio
            tr = min(totalTokenNum, totalCandidateTokenNum) / max(totalTokenNum, totalCandidateTokenNum)

            if ato > beta and tr > theta:
                filteredClones.add(candidate)

        """verification step"""
        for candidate in filteredClones:
            simVT = verifySim.verifySim(CM[candidate]["variable group"], block_data["variable group"], phi)
            simET = verifySim.verifySim(CM[candidate]["method group"], block_data["method group"], phi)
            simCT = verifySim.verifySim(CM[candidate]["relation"], block_data["relation"], phi)
            if (simVT + simET + simCT) / 3 > eta:
                clonePairs.append((block_path, candidate))

    return clonePairs
