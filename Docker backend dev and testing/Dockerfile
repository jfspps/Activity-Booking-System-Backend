FROM centos

RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

RUN yum install -y java-11-openjdk-devel 

CMD /bin/bash

 VOLUME /tmp 
 ADD /breakthemould-1.3.19.jar myApp.jar
 RUN sh -c 'touch /myApp.jar' 
 ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/myApp.jar", "--email.username=${EMAIL_USERNAME}", "--email.password=${EMAIL_PASSWORD}", "--email.from=${EMAIL_FROM}", "--spring.datasource.url=${SPRING_DATASOURCE_URL}", "--spring.datasource.username=${SPRING_DATASOURCE_USERNAME}", "--spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", "--btm.login.page=${LOGIN_PAGE}"]
