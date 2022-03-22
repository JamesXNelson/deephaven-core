FROM deephaven/node:local-build
WORKDIR /usr/src/app

FROM node as build

# Pull in the published package from npmjs and extract it
ARG WEB_VERSION=0.11.2

# Pull in the published code-studio package from npmjs and extract it
RUN set -eux; \
    npm pack @deephaven/code-studio@${WEB_VERSION}; \
    tar --touch -xf deephaven-code-studio-${WEB_VERSION}.tgz; \
    mv package/build ide; \
    rm -r package; \
    rm deephaven-code-studio-${WEB_VERSION}.tgz;

# Pull in the published embed-grid package from npmjs and extract it
RUN set -eux; \
    npm pack @deephaven/embed-grid@${WEB_VERSION}; \
    tar --touch -xf deephaven-embed-grid-${WEB_VERSION}.tgz; \
    mkdir iframe; \
    mv package/build iframe/table; \
    rm -r package; \
    rm deephaven-embed-grid-${WEB_VERSION}.tgz;
