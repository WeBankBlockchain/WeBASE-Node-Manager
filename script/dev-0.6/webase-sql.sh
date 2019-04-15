#!/bin/sh


echo -e "\n init start...."

IP=${1}
PORT=${2}

if [[ ! $IP || ! $PORT ]] ; then
    echo "Usage: sh ${0} ip port"
    echo "eg: sh ${0} 10.0.0.1 8501"
    exit 1
fi

#dbUser
DBUSER="defaultAccount"
#dbPass
PASSWD="defaultPassword"
#dbName
DBNAME="fisco-bcos-data"

read -t 60 -p "本次执行会删除相关表，请确保已备份原数据，是否继续(y/n):" inputVal
echo -e "\n"

if [ "$inputVal" != "y" ];then
echo "init stop..."
exit
fi

#connect to database then execute init
cat fisco-bcos.list | mysql --user=$DBUSER --password=$PASSWD --host=$IP --database=$DBNAME --port=$PORT --default-character-set=utf8;

if [ "$?" == "0" ]; then
    echo -e "init success... \n"
else
    echo -e "init fail... \n"
fi

exit
