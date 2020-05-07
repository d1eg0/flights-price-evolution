from abc import ABC, abstractmethod
import json


class AirlineResponse:
    def __init__(self, ts, trips):
        self._trips: dict = trips
        self._timestamp: dict = ts

    def to_json_str(self) -> str:
        data = {"trips": self._trips, "ts": self._timestamp}
        json_str = json.dumps(data)
        return json_str

    def to_dict(self) -> dict:
        return {"trips": self._trips, "ts": self._timestamp}

    def __str__(self):
        return "timestamp: {ts} trips: {trips}".format(ts=self._timestamp, trips=self._trips)


class AirlineResponseConverter(ABC):
    @staticmethod
    @abstractmethod
    def get_response_from_json(raw_json: dict) -> AirlineResponse:
        pass
