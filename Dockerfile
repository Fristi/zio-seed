FROM gcr.io/distroless/base
COPY ./target/native-image/zio-seed /opt/server
ENTRYPOINT ["/opt/server", "-Xmx300m"]