FROM ubuntu
COPY ./modules/api/target/native-image/api /opt/server
ENTRYPOINT ["/opt/server"]