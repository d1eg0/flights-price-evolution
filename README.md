# Flight price evolution
## Overview
This is a pet project to gather airline prices from their APIs and monitor minimum prices per route.

The project has 2 main modules. The following diagram shows the workflow:

![Components diagram](https://github.com/d1eg0/flights-price-evolution/raw/master/docs/components-flow.png "Components diagram")

- flights-scraper: gathers the prices from different airlines and sends them to Kafka in a common format.
- flights-streams: reads the prices from Kafka and filters minimum prices by route for a given window time.

## Donwload this repository
```bash
git clone https://github.com/d1eg0/flights-price-evolution.git
cd flights-price-evolution
```

## Install flights-scraper module
Install the flights-scraper Python package:
```bash
cd flights-scraper
python setup.py install
```

## Install Kafka and MongoDB
- Install Kafka and start the server as described in <https://kafka.apache.org/quickstart>.

- Install MongoDB

## Configure Kafka and MongoDB
Once the server is up, create the Kafka topic **flights**:
```bash
./kafka-topics.sh --create --bootstrap-server 0.0.0.0:9092 --replication-factor 1 --partitions 1 --topic flights
```

Create the collection **prices** in the db **flights** in MongoDB and the index to make faster updates:
```bash
cd scripts
mongo < flights.js
```


## Running in local
Run flights-streams to process incoming prices in 10 minutes windows:
```bash
cd flights-streams
sbt run
```
Running with another time window:
```bash
sbt 'run --window-duration "60 seconds"'
```

Run flights-scraper to gather prices:
```bash
python -m scraper.run --interval 3600
```

## Run tests
flights-scraper:
```bash
cd flights-scraper
pytest
```
flights-streams:
```bash
cd flights-streams
sbt test
```
