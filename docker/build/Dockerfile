FROM ubuntu:22.04 as prod

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get update \
    && apt-get install -y python3-pip \
    && apt-get install -y ansible \
    && apt-get install -y openssh-client \
    && apt-get -y install openjdk-8-jre \
    && apt-get -y install mysql-client \
    && apt-get install -y rsync \
    && apt-get install -y curl \
    && mkdir -p /etc/ansible \
    && chmod -R 777 /etc/ansible \
    && mkdir -p ~/.ssh \
    && rm -rf /var/lib/apt/lists/*

ADD openssl-1.1.1q.tar.gz /tmp/
RUN cd /tmp/openssl-1.1.1q \
    && ./config \
    && make \
    && make install \
    && ldconfig

COPY script                /dist/script
COPY gradle                /dist/gradle
COPY lib                  /dist/lib
COPY conf_template        /dist/conf
COPY apps                 /dist/apps

WORKDIR /dist
EXPOSE 6001

ENV CLASSPATH "/dist/conf/:/dist/apps/*:/dist/lib/*"

ENV JAVA_OPTS " -server -Dfile.encoding=UTF-8 -Xmx512m -Xms512m -Xmn256m -Xss512k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log/heap_error.log  -XX:+UseG1GC -XX:MaxGCPauseMillis=200 "
ENV APP_MAIN "com.webank.webase.node.mgr.Application"

# start commond
ENTRYPOINT java ${JAVA_OPTS} -Djdk.tls.namedGroups="secp256k1", -Duser.timezone="Asia/Shanghai" -Djava.security.egd=file:/dev/./urandom, -Djava.library.path=/dist/conf -cp ${CLASSPATH}  ${APP_MAIN}
