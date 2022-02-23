FROM ubuntu
COPY ./api/target/native-image/api /opt/server
ENTRYPOINT ["/opt/server"]