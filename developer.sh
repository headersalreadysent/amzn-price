#!/bin/bash
adb -s R58MA1QY7AJ shell am start -n co.ec.amazonfiyattakip/.MainActivity \
    -e destination developer \
    -f 0x10008000
    
adb -s R8YW205CTLY shell am start -n co.ec.amazonfiyattakip/.MainActivity \
    -e destination developer \
    -f 0x10008000
