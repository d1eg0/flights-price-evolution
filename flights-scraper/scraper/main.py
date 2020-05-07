import asyncio

from scraper.entities.ports.raynair import RynairRequest, RyanairResponseConverter
from scraper.services.operators import Operations


async def main():
    flight_request = RynairRequest()
    flight_response = RyanairResponseConverter()
    airline_respone = await Operations.get_data(flight_request, flight_response)
    await Operations.push_data(airline_respone)


if __name__ == '__main__':
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
