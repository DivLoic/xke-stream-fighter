#!/bin/bash

usage() {
  echo -n "$(basename $0) [PARAMS]

Description of this script.

 Options:
  -h, --help        Display this help and exit
      --version     Output version information and exit
"
}

VERSION=0.10
APPID=$1

while getopts ':vh' opt; do
	case $opt in
		v)
          echo VERSION
          exit 0
          ;;
		h)
          usage
          exit 0
          ;;
		\?)
		  echo "Unknown option: -${opt} \n" >&2
		  usage
		  exit 1
		;;
	esac
done

if [ -z $APPID ]; then
    usage
    exit 1
fi

watch -t -n 0.2 "cat /tmp/XKE-KSTREAM-$APPID-TABLE.txt"