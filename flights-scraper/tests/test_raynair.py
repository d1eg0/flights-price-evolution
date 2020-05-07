from unittest import TestCase
import json
from scraper.entities.ports.ryanair import RyanairResponseConverter
import pprint

class TestRyanairResponseConverter(TestCase):
    def test_get_response_from_json(self):
        fixture_path = "fixtures/ryanair_response.json"
        with open(fixture_path, "rb") as f:
            response = b"".join(f.readlines())
        json_fixture = json.loads(response)
        response = RyanairResponseConverter.get_response_from_json(json_fixture)
        pprint.pprint(response.to_dict(), indent=2)
        self.assertEqual(response.to_dict()["trips"][0]["origin"], "PMI")
        self.assertEqual(response.to_dict()["trips"][0]["destination"], "BCN")
        self.assertEqual(response.to_dict()["trips"][0]["dates"][0]["flights"][0]["fares"][0]["amount"], 19.99)


