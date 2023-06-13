FROM ghcr.io/navikt/poao-baseimages/java:17
COPY /build/libs/amt-aktivitetskort-publisher-*.jar app.jar
