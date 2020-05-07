from unittest import TestCase

from scraper.entities.response import AirlineResponse


class TestAirlineResponse(TestCase):

    def test_to_json_str(self):
        airline_response = AirlineResponse(ts=1000, trips='trips')
        self.assertEqual(airline_response.to_json_str(), '{"trips": "trips", "ts": 1000}')

    def test_to_dict(self):
        airline_response = AirlineResponse(ts=1000, trips='trips')
        self.assertEqual(airline_response.to_dict(), {"trips": "trips", "ts": 1000})
