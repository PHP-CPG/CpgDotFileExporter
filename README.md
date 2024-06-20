# CpgDotfileExporter

Export a given CPG (based on shiftleft framework) into dot files

## Build

The tool can be built using
```
sbt stage
```

## Usage

Either use the tool directly like this (see help for options)
```
./target/universal/stage/bin/cpg-dotfile-exporter
```
or using one of the convenience scripts in the top-level folder of this repo.

## Docker

The tool can be wrapped in a docker container which uses two mounts (`in_dir` and `out_dir`), taking all CPG binary files from `in_dir` and exporting dot files to `out_dir`.

1. Build the docker image (needs to be executed only once)
   ```
   ./build_docker
   ```

2. Create and run a container
   ```
   ./run_docker $IN_DIR $OUT_DIR
   ```


## Known Issues

- Rendering the resulting dot files using graphviz hides all attributes except `label`. This is because quotes are not escaped and is intended. It simplifies parsing with common libraries when attributes like `CODE`contain quotes as well.
