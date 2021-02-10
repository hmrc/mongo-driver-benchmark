# mongo-driver-benchmark

This benchmark suite was created to compare the performance of two mongodb scala libraries, [simple-reactivemongo](https://github.com/hmrc/simple-reactivemongo) and [hmrc-mongo](https://github.com/hmrc/hmrc-mongo).
It uses a sbt plugin [sbt-jmh](https://github.com/ktoso/sbt-jmh) to run java [JMH](https://github.com/openjdk/jmh) benchmarks.

## Run benchmarks

- Edit your /etc/hosts file to add the below entries. These are the mongo nodes hostnames to use in the connection string.

```
127.0.0.1 mongo0
127.0.0.1 mongo1
127.0.0.1 mongo2
```

- Start mongodb replica set. Run the following command from the `replica-set` directory.
```shell
docker-compose up
```

and wait till you see the following line in the docker-compose output

```shell
transition to primary complete; database writes are now permitted
```    
    
Now the replica set is ready to accept any commands from our benchmark suite.

- This project has two sbt sub-projects for each mongodb library
    
    - `simpleReactiveMongoBenchmarks`
    - `hmrcMongoBenchmarks`

To run the benchmark for a library execute the following command from the root directory of this repo.

```shell
sbt <sbt-projectname>/jmh:run -i 3 -wi 5 -rf json -rff ../result.json
```

where,

`-i` = Number of iterations

`-wi` = Number of warmup iterations

`-rf` = result format (other formats are `latex`, `text`, `xsv)`

`-rff` = result file path

The class `runner.DefaultRunner` has defined configurations like time measurements, benchmark mode, profilers and other configuration.


## Visualize results

The benchmarks (single/multi) can be uploaded on this [website](https://jmh.morethan.io/) to do comparisons.
It accepts JMH result files in JSON format. 
