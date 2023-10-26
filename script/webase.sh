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
DBUSER="sysdba"
#dbPass
PASSWD="1qaz2wsx!@#$RFV"
#dbName
DBNAME="wm"


#connect to database then execute init
cat webase-sql.list | psql -U $DBUSER -W -h $IP -d $DBNAME -p $PORT -a --set=client_encoding=utf8;

if [ "$?" == "0" ]; then
    echo -e "init success... \n"
else
    echo -e "init fail... \n"
fi

exit
