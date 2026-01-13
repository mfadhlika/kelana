FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y libsqlite3-mod-spatialite

WORKDIR /app

COPY target/kelana kelana
COPY target/libsqlitejdbc.so /lib

ENV DATA_DIR=/data

ENTRYPOINT ["/app/kelana"]
