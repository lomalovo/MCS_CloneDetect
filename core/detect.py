import numpy as np
import pandas as pd
from pandarallel import pandarallel
import os
import parseTokens as pT
import cloneDetect as cD


# pandarallel.initialize(progress_bar=True)

def process_files(directory):
    file_paths = [os.path.join(directory, f) for f in os.listdir(directory) if
                  os.path.isfile(os.path.join(directory, f))]

    df_files = pd.DataFrame({'file_path': file_paths})

    def read_and_parse(file_path):
        with open(file_path, 'r') as file:
            content = file.read()
        return pT.parse_data(content)

    df_files['parsed_data'] = df_files['file_path'].apply(read_and_parse)

    combined_data = {}
    for data in df_files['parsed_data']:
        combined_data.update(data)

    return combined_data


if __name__ == '__main__':
    dir = './tokens/45'
    all_blocks = process_files(dir)

    beta = 0.5
    theta = 0.4
    eta = 0.65
    phi = -0.1
    k = 5
    clones = cD.cloneDetectAlgorithm(all_blocks, k, beta, theta, phi, eta)

    with open("./clonepairs.txt", 'w') as file:
        for clone in clones:
            if clone[0] != clone[1]:
                file.write(str(clone) + "\n")
