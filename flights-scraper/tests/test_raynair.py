from unittest import TestCase

from scraper.entities.ports.raynair import RyanairResponseConverter


class TestRyanairResponseConverter(TestCase):
    def test_get_response_from_json(self):
        fixture = {'serverTimeUTC': 1000,
                   'trips': 'trips'}
        response = RyanairResponseConverter.get_response_from_json(fixture)
        self.assertEqual(response.to_dict(), {'ts': 1000, 'trips': 'trips'})
