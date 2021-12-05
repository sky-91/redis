FROM tomcat:9.0.55-jre8

ADD redis-0.0.1-SNAPSHOT.war /usr/local/tomcat/

RUN unzip /usr/local/tomcat/redis-0.0.1-SNAPSHOT.war -d webapps/redis

EXPOSE 8080

CMD ["catalina.sh", "run"]

