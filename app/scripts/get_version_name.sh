#!/bin/bash

apk_file_path=$1

if [ -z "$apk_file_path" ]; then
    echo "Usage: $0 <apk_file_path>"
    exit 1
fi

aapt dump badging "$apk_file_path"  | grep -o "versionName='[^']*'" | cut -d"'" -f2