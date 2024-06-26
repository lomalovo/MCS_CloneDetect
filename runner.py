import os
import sys

if __name__ == '__main__':
    if len(sys.argv) != 7:
        print('python3 runner.py -i /path/to/dataset -l c/java -m common/bcb')
        exit(0)

    inputDir = ''
    language = 'java'
    mode = 'common'

    for i in [1, 3, 5]:
        if sys.argv[i] == '-i':
            inputDir = sys.argv[i + 1]
        elif sys.argv[i] == '-l':
            language = sys.argv[i + 1]
        elif sys.argv[i] == '-m':
            mode = sys.argv[i + 1]

    print('language: %s' % language)

    if len(inputDir) == 0 or not os.path.exists(inputDir):
        print('input dir not exist: {}'.format(inputDir))
        exit(0)

    inputDir = os.path.realpath(inputDir)

    # extract semantic tokens
    print('extract semantic tokens...')
    os.system('rm -rf tokens; mkdir tokens')
    if language == 'java':
        os.system('cd semantic-token-extract/java; python3 parse.py -i %s -o ../../tokens -m %s' % (inputDir, mode))
    elif language == 'c':
        os.system('cd semantic-token-extract/c; java -jar c-parser.jar -i %s -o ../../tokens' % inputDir)
    else:
        print('unknown language: {}'.format(language))
        exit(0)

    # clone detection
    print('performing clone detection...')
    os.system('rm -rf results; mkdir results')
    os.system('./clonedetect %s' % mode)

    # collect results
    print('collect detection results...')
    os.system('find ./results -type f -name "*.pair" | xargs cat >> ./clonepairs.txt')
    os.system('rm -rf tokens results')

    print('clone pairs are stored in ./clonepairs.txt')
