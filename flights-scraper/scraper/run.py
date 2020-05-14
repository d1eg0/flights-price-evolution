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


def run_process(interval=3600):
    flight_request_pmi_bcn = RynairRequest()
    flight_request_pmi_bcn.destination = "BCN"
    flight_request_pmi_mad = RynairRequest()
    flight_request_pmi_mad.destination = "MAD"
    all_requests = [flight_request_pmi_bcn, flight_request_pmi_mad]
    flight_response_converter = RyanairResponseConverter()
    loop = asyncio.get_event_loop()
    while True:
        # get prices
        price_tasks = [Operations.get_data(flight_request, flight_response_converter) for flight_request in all_requests]
        price_futures = asyncio.gather(*price_tasks)
        price_responses = loop.run_until_complete(price_futures)

        # push data to Kafka
        push_tasks = [Operations.push_data(airline_response) for airline_response in price_responses]
        push_futures = asyncio.gather(*push_tasks)
        loop.run_until_complete(push_futures)

        # sleep random seconds
        sleep_time = random.randint(int(interval-interval*0.10), interval)
        sleep(sleep_time)


if __name__ == '__main__':
    signal.signal(signal.SIGTERM, terminate_process)
    signal.signal(signal.SIGINT, terminate_process)
    parser = argparse.ArgumentParser(description='Parse flights prices.')
    parser.add_argument('--interval', type=int, default=3600)
    args = parser.parse_args()
    logger.info("Starting process")
    logger.info("Scrape interval: {} seconds".format(args.interval))
    run_process(args.interval)
    logger.info("Ending process")
