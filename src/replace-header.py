import os
import os.path as path

header=\
"""/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
"""

header=\
"""/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
"""

import re

comment_regex = re.compile(r"\/\*\*(.|\n)*?\*\/\n*", re.MULTILINE)

def replace_header(lines):
    match = comment_regex.match(lines)
    if match is not None:
        return header + lines[match.span()[1]:]
    else:
        return header + lines

def process_path(p):
    for f in os.listdir(p):
        f_path = path.join(p, f)
        if path.isfile(f_path):
            if f_path.endswith(".java") or f_path.endswith(".scala"):
                print("Replacing " + f + "...")
                with open(f_path, 'r', encoding = 'utf-8') as file:
                    lines = file.read()
                with open(f_path, 'w', encoding = 'utf-8') as file:
                    file.write(replace_header(lines))
        elif path.isdir(f_path):
            process_path(f_path)

if __name__ == "__main__":
    process_path(os.getcwd() + '\\')
    
        