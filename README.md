# Flight price evolution
## Overview
This is a pet project to gather airline prices from their APIs and monitor minimum prices per route.

The project has 2 main modules. The following diagram shows the workflow:

![Components diagram](https://github.com/d1eg0/flights-price-evolution/raw/master/docs/components-flow.png "Components diagram")

- flights-scraper: gathers the prices from different airlines and sends to Kafka.
- flights-streams: reads prices from Kafka and filters minimum prices by route for a given window time.

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

## Install

Install the flights-scraper python package:
```bash
cd flights-scraper
python setup.py
```

Install Kafka and start the server as described in <https://kafka.apache.org/quickstart>. Once the server is up, create the Kafka topic **flights**:
```bash
./kafka-topics.sh --create --bootstrap-server 0.0.0.0:9092 --replication-factor 1 --partitions 1 --topic flights
```

## Run in local

Run flights-streams to process incoming prices:
```bash
cd flights-streams
sbt run
```

Run flights-scraper to gather prices:
```bash
python -m scraper.run
```
