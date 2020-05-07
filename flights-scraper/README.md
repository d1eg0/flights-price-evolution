# Pet project: Flight Price Scraper
This project aims to get prices from different airlines, convert them to a common format
and send them to the Flight Price Stream Process via Kafka.


# Install
    
Requirements:
    python setup.py
    pip install -r requirements.txt

Create topic flights:

    ./kafka-topics.sh --create --bootstrap-server 0.0.0.0:9092 --replication-factor 1 --partitions 1 --topic flights
    
