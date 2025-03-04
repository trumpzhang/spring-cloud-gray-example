FROM openjdk:8-jre-alpine

ENV TZ=Asia/Shanghai \
    LANG=en_US.UTF-8

#指定docker容器时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo $TZ >/etc/timezone

#RUN apk --update --no-cache add tini && \
#	rm -rf /var/cache/apk/*

WORKDIR /opt
COPY kerwin-gateway-1.0-SNAPSHOT.jar .

ENTRYPOINT java -jar kerwin-gateway-1.0-SNAPSHOT.jar
