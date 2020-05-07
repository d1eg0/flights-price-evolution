from scraper.entities.request import AirlineRequest
from scraper.entities.response import AirlineResponse, AirlineResponseConverter


class RyanairResponseConverter(AirlineResponseConverter):
    @staticmethod
    def get_response_from_json(raw_json: dict) -> AirlineResponse:
        airline_response = AirlineResponse(ts=raw_json['serverTimeUTC'], trips=raw_json['trips'])
        return airline_response


class RynairRequest(AirlineRequest):

    def get_url(self):
        url_template = "https://www.ryanair.com/api/booking/v4/es-es/availability?" \
                       "ADT=1&CHD=0&DateIn={date_in}&DateOut={date_out}&Destination={destination}&Disc={discount}&" \
                       "INF=0&Origin={origin}&RoundTrip={round_trip}&TEEN={teens}&" \
                       "FlexDaysIn=2&FlexDaysBeforeIn=2&FlexDaysOut=2&FlexDaysBeforeOut=2&ToUs=AGREED&" \
                       "IncludeConnectingFlights=false"
        return url_template.format(adults=self.adults,
                                   teens=self.teens,
                                   children=self.children,
                                   infants=self.infants,
                                   origin=self.origin,
                                   destination=self.destination,
                                   date_in=self.date_in,
                                   date_out=self.date_out,
                                   discount=self.discount,
                                   round_trip=self.round_trip,
                                   is_connected=self.is_connected)
