from abc import ABC, abstractmethod


class AirlineRequest(ABC):
    def __init__(self):
        self.date_out = "2020-07-01"
        self.date_in = "2020-07-17"
        self.origin = "PMI"
        self.destination = "BCN"
        self.discount = "0"
        self.round_trip = "true"
        self.is_connected = "false"
        self.infants = "0"
        self.children = "0"
        self.teens = "0"
        self.adults = "1"

    @abstractmethod
    def get_url(self) -> str:
        pass
