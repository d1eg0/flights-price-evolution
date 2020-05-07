import json

import aiohttp
from kafka import KafkaProducer

from scraper.entities.request import AirlineRequest
from scraper.entities.response import AirlineResponse, AirlineResponseConverter


class Operations:
    _kafka_producer = None

    @staticmethod
    async def fetch(session, url) -> dict:
        async with session.get(url) as response:
            return await response.json()

    @staticmethod
    async def get_data(airline_request: AirlineRequest,
                       airline_response_converter: AirlineResponseConverter) -> AirlineResponse:
        async with aiohttp.ClientSession() as session:
            response = await Operations.fetch(session, airline_request.get_url())
            return airline_response_converter.get_response_from_json(response)

    @staticmethod
    async def push_data(airline_response: AirlineResponse) -> object:
        config = {"bootstrap_servers": "0.0.0.0:9092",
                  "client_id": "flights-scraper",
                  "acks": 1}
        if Operations._kafka_producer is None:
            Operations._kafka_producer = KafkaProducer(**config, value_serializer=lambda m: json.dumps(m).encode('ascii'))

        future = Operations._kafka_producer.send(topic="flights", value=airline_response.to_dict())
        record_metadata = future.get(timeout=10)
        return record_metadata
