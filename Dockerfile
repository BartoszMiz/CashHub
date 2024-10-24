FROM openjdk:23-jdk-bookworm AS build

WORKDIR /build
COPY app/ app 
COPY Makefile .

ENV DEBIAN_FRONTEND=noninteractive
RUN apt update && apt install -y make && rm -rf /var/lib/apt/lists/*
RUN make jar 

FROM openjdk:23-slim AS run

WORKDIR /app
COPY --from=build /build/cashhub.jar .
COPY app/wwwroot/ ./wwwroot
ENTRYPOINT ["java", "-jar", "cashhub.jar"]
EXPOSE 8080/tcp
