FROM ubuntu
COPY ./target/native-image/zio-seed /opt/server
ENTRYPOINT ["/opt/server"]