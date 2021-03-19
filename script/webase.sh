#!/usr/bin/env bash


echo -e "\n init start...."

IP=${1}
PORT=${2}

if [[ ! $IP || ! $PORT ]] ; then
    echo "Usage: sh ${0} ip port"
    echo "eg: sh ${0} 127.0.0.1 8501"
    exit 1
fi

#dbUser
DBUSER="defaultAccount"
#dbPass
PASSWD="defaultPassword"
#dbName
DBNAME="webasenodemanager"


#connect to database then execute init
cat webase-sql.list | mysql --user=$DBUSER --password=$PASSWD --host=$IP --database=$DBNAME --port=$PORT --default-character-set=utf8;

if [ "$?" == "0" ]; then
    echo -e "init success... \n"
else
    echo -e "init fail... \n"
fi

exit
