FROM gcr.io/distroless/base
COPY ./zio-seed/target/native-image/zio-seed /opt/server
ENTRYPOINT ["/opt/server", "-Xmx300m"]