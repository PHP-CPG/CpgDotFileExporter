FROM ubuntu:focal

ENV IN_DIR=/in_dir
ENV OUT_DIR=/out_dir
ENV BASE_DIR=/CpgDotfileExporter

COPY ../.. $BASE_DIR/


# switch default shell from /bin/sh to /bin/bash
SHELL ["/bin/bash", "-c"]

# install dependencies (java and sbt using sdkman)
RUN apt update && \
    apt install -y zip unzip curl && \
    curl -s "https://get.sdkman.io" | bash && \
    source "/root/.sdkman/bin/sdkman-init.sh" && \
    sdk install java $(sdk list java | grep -o "8\.[0-9]*\.[0-9]*\.hs-adpt" | head -1) && \
    sdk install sbt

# build clangast2cpg tool
WORKDIR $BASE_DIR
RUN source "/root/.sdkman/bin/sdkman-init.sh" && sbt stage

# default command when executing the container
# NOTE: requires a volume mount to /data
CMD source "/root/.sdkman/bin/sdkman-init.sh" && ./export_dir $IN_DIR $OUT_DIR
