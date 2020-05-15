import asyncio
import logging
import random
import signal
import sys
from time import sleep
import argparse

from scraper.entities.ports.ryanair import RynairRequest, RyanairResponseConverter
from scraper.services.operators import Operations

# configure logging
logging.basicConfig(format='[%(levelname)s] - %(asctime)s - %(name)s - %(message)s', level=logging.ERROR)
logging.getLogger("__main__").setLevel(logging.DEBUG)
logging.getLogger("scraper").setLevel(logging.DEBUG)
logger = logging.getLogger(__name__)


def terminate_process(signal_number, frame):
    logger.error('(SIGTERM) terminating the process')
    sys.exit()


def run_process(interval=3600, kafka_bootstrap_servers="0.0.0.0:9092", kafka_topic="flights", origins=[],
                destinations=[]):
    Operations.kafka_topic = kafka_topic
    Operations.kafka_bootstrap_servers = kafka_bootstrap_servers
    flight_requests = []
    for origin in origins:
        for destination in destinations:
            flight_request = RynairRequest()
            flight_request.origin = origin
            flight_request.destination = destination
            flight_requests.append(flight_request)
    flight_response_converter = RyanairResponseConverter()
    loop = asyncio.get_event_loop()
    while True:
        # get prices
        price_tasks = [Operations.get_data(flight_request, flight_response_converter) for flight_request in
                       flight_requests]
        price_futures = asyncio.gather(*price_tasks)
        price_responses = loop.run_until_complete(price_futures)

        # push data to Kafka
        push_tasks = [Operations.push_data(airline_response) for airline_response in price_responses]
        push_futures = asyncio.gather(*push_tasks)
        loop.run_until_complete(push_futures)

        # sleep random seconds
        sleep_time = random.randint(int(interval - interval * 0.10), interval)
        sleep(sleep_time)


if __name__ == '__main__':
    signal.signal(signal.SIGTERM, terminate_process)
    signal.signal(signal.SIGINT, terminate_process)
    parser = argparse.ArgumentParser(description='Parse flights prices.')
    parser.add_argument('--interval', type=int, default=3600)
    parser.add_argument('--topic', type=str, default="flights")
    parser.add_argument('--bootstrap-servers', type=str, default="0.0.0.0:9092")
    parser.add_argument('--origins', nargs="+", required=True)
    parser.add_argument('--destinations', nargs="+", required=True)
    args = parser.parse_args()
    logger.info("Starting process")
    logger.info("Scrape interval: {} seconds".format(args.interval))
    logger.info("Origins:{origins} Destinations:{destinations}".format(origins=args.origins,
                                                                       destinations=args.destinations))
    run_process(interval=args.interval,
                kafka_bootstrap_servers=args.bootstrap_servers,
                kafka_topic=args.topic,
                origins=args.origins,
                destinations=args.destinations)
    logger.info("Ending process")
