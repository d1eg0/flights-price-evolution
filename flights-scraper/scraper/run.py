import asyncio
import random
import logging
import signal
import sys
from asyncio import sleep

from scraper.entities.ports.ryanair import RynairRequest, RyanairResponseConverter
from scraper.services.operators import Operations

# configure logging
logging.basicConfig(format='[%(levelname)s] - %(asctime)s - %(name)s - %(message)s', level=logging.ERROR)
logging.getLogger("scraper").setLevel(logging.DEBUG)
logger = logging.getLogger(__name__)


def terminateProcess(signalNumber, frame):
    logger.error('(SIGTERM) terminating the process')
    sys.exit()


async def main():
    while True:
        flight_request_pmi_bcn = RynairRequest()
        flight_request_pmi_bcn.destination = "BCN"
        flight_request_pmi_mad = RynairRequest()
        flight_request_pmi_mad.destination = "MAD"
        all_requests = [flight_request_pmi_bcn, flight_request_pmi_mad]
        for flight_request in all_requests:
            flight_response = RyanairResponseConverter()
            airline_respone = await Operations.get_data(flight_request, flight_response)
            await Operations.push_data(airline_respone)
            sleep_time = random.randint(30, 60)
            await sleep(sleep_time)


if __name__ == '__main__':
    signal.signal(signal.SIGTERM, terminateProcess)
    signal.signal(signal.SIGINT, terminateProcess)

    logger.info("Starting process")
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
    logger.info("Ending process")
