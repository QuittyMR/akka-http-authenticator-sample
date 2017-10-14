FROM docker.repo:5000/scala-alpine:2.11.8
MAINTAINER Tomer Raz <qtomerr@gmail.com>

ENV APP_NAME authenticator
ENV APP_HOME /opt/authenticator

WORKDIR ${APP_HOME}

ADD ./target/scala-2.11/*.jar ${APP_HOME}
ADD ./run.sh ${APP_HOME}

EXPOSE 4242
STOPSIGNAL SIGTERM

ENV PATH ${PATH}:${APP_HOME}

CMD ./run.sh
