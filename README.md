# MCS_CloneDetect

<h2>0. Introduction</h2>

* Clone detection tool that I made as a MCS project

<h2>1. Install Dependency</h2>

Tool runs under Python3 environment, and you should first install **pandarallel** package. After performing clone detection, the results of clone pairs will be stored in **clonepairs.txt** file.

    pip3 install pandarallel

<h2>2. How to run tool to perform clone detection</h2>

Tool supports detection for c and java language. **runner.py** is the runner file.

    python runner.py -i /path/to/dataset -m common/bcb -l java/c

* -i: path to dataset
* -m: detection mode. If you detect on BigCloneBench, the mode will be **bcb**, otherwise, the mode will be **common**.
* -l: language of datasets, now we support detection for **java** and **c** languages.

***_Example:_*** You can run tool as follows to perform clone detection for BigCloneBench.

    python runner.py -i /home/datasets/BigCloneEval-master/ijadataset/bcb_reduced -m bcb -l java