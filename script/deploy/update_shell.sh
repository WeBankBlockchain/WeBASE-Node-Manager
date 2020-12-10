#!/usr/bin/env bash

curl -s https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/v2.7.0/tools/build_chain.sh > build_chain.sh
curl -s https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/v2.7.0/tools/gen_agency_cert.sh > gen_agency_cert.sh
curl -s https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/v2.7.0/tools/gen_node_cert.sh > gen_node_cert.sh

# delete pwd
row=$(grep -i  -n output_dir build_chain.sh |grep -i  "pwd"| awk -F ":" '{print $1}')
regex="$row"'s#$(pwd)/##g'
sed -i -e ${regex} build_chain.sh
