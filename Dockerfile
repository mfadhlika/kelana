FROM debian:bookworm-slim

WORKDIR /app

COPY target/kelana kelana

ENTRYPOINT ["/app/kelana"]
