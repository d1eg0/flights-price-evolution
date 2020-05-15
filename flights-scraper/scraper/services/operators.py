import json

import aiohttp
import logging

from kafka import KafkaProducer

from scraper.entities.request import AirlineRequest
from scraper.entities.response import AirlineResponse, AirlineResponseConverter

logger = logging.getLogger(__name__)


class Operations:
    _kafka_producer = None
    kafka_bootstrap_servers = "0.0.0.0:9092"
    kafka_topic = "flights"

    @staticmethod
    async def fetch(session, url) -> dict:
        logger.debug("Fetching url: {}".format(url))
        async with session.get(url) as response:
            json_response = await response.json()
            logger.debug("Fetched json data: {}".format(json_response))
            return json_response

    @staticmethod
    async def get_data(airline_request: AirlineRequest,
                       airline_response_converter: AirlineResponseConverter) -> AirlineResponse:
        logger.debug("Getting data")
        async with aiohttp.ClientSession() as session:
            response = await Operations.fetch(session, airline_request.get_url())
            return airline_response_converter.get_response_from_json(response)

    @staticmethod
    async def push_data(airline_response: AirlineResponse) -> object:
        start_message = """Pushing data to Kafka topic:{topic} server:{server}"""
        logger.debug(start_message.format(topic=Operations.kafka_topic, server=Operations.kafka_bootstrap_servers))
        config = {"bootstrap_servers": Operations.kafka_bootstrap_servers,
                  "client_id": "flights-scraper",
                  "acks": 1,
                  "retries": 3}
        if Operations._kafka_producer is None:
            Operations._kafka_producer = KafkaProducer(**config,
                                                       value_serializer=lambda m: json.dumps(m).encode('ascii'))

        future = Operations._kafka_producer.send(topic=Operations.kafka_topic, value=airline_response.to_dict())
        record_metadata = future.get(timeout=10)
        logger.debug("Pushed data to Kafka")
        return record_metadata
