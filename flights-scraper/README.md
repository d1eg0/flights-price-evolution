# Pet project: Flight Price Scraper
This pet project aims to get prices from different airlines, convert them to a common format
and send them to the Flight Price Stream Process via Kafka.


# Install

Local development:

    pip install -e .
    
Install module:

    python setup.py

Create topic flights:

    ./kafka-topics.sh --create --bootstrap-server 0.0.0.0:9092 --replication-factor 1 --partitions 1 --topic flights
    
