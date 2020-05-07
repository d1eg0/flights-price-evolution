import asyncio

import asynctest
from aiohttp import ClientSession

from scraper.entities.ports.raynair import RyanairResponseConverter, RynairRequest
from scraper.entities.response import AirlineResponse
from scraper.services.operators import Operations


class TestOperations(asynctest.TestCase):
    @asynctest.patch('aiohttp.ClientSession.get')
    def test_fetch(self, mock_get):
        # mock the ClientSession.get value
        mock_get.return_value.__aenter__.return_value.json = asynctest.CoroutineMock(side_effect=[
            {'ts': 1000, 'trips': []}
        ])

        # coroutine to test fetch
        async def _test_fetch():
            async with ClientSession() as session:
                result = await Operations.fetch(session, "url")
                return result

        result = asyncio.run(_test_fetch())
        self.assertEqual(result['ts'], 1000)

    @asynctest.patch('aiohttp.ClientSession.get')
    def test_get_data(self, mock_get):
        # mock the ClientSession.get value
        mock_get.return_value.__aenter__.return_value.json = asynctest.CoroutineMock(side_effect=[
            {'serverTimeUTC': 1000, 'trips': []}
        ])

        flight_request = RynairRequest()
        flight_response = RyanairResponseConverter()
        airline_respone = asyncio.run(Operations.get_data(flight_request, flight_response))
        self.assertEqual(airline_respone.to_dict()['ts'], 1000)

    def test_push_data(self):

        class MockFutureRecordMetadata:
            def get(self, **kwargs):
                return -1

        class MockKafkaProducer:
            def send(self, topic=None, value=None):
                print("debug")
                return MockFutureRecordMetadata()

        Operations._kafka_producer = MockKafkaProducer()
        response = asyncio.run(Operations.push_data(AirlineResponse({"ts": 1000}, {"trips": []})))
        self.assertEqual(response, -1)
