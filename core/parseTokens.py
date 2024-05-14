import re
import numpy as np


def parse_data(content):
    lines = content.strip().split('\n')

    data_blocks = {}
    current_block = None
    current_category = None
    block_data = None

    for line in lines:
        line = line.strip()
        if line.startswith('<block'):
            if current_block:
                data_blocks[block_data] = current_block
            block_data = line[7:-1]
            current_block = {
                'variable': [], 'field': [], 'method': [],
                'keyword': [], 'type': [], 'basic type': [],
                'variable group': [], 'method group': [], 'relation': []
            }
        elif (line.startswith('<variable>') or line.startswith('<field>') or
              line.startswith('<method>') or line.startswith('<keyword>') or
              line.startswith('<type>') or line.startswith('<basic type>') or
              line.startswith('<variable group>') or line.startswith('<method group>') or
              line.startswith('<relation>')):
            current_category = line[1:-1]  # Remove < and >
        else:
            item_match = re.match(r'([\d\w-]*),(\d+): \[(.*?)\]', line)
            if item_match:
                name = item_match.group(1)
                count = int(item_match.group(2))
                values = np.array(list(map(int, item_match.group(3).split(', '))))
                current_block[current_category].append((name, count, values))

    if current_block:
        data_blocks[block_data] = current_block

    return data_blocks
