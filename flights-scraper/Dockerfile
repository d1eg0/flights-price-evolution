FROM python:3.7

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD [ "python", "-m", "scraper.run", "--bootstrap-servers", "kafka-server:9092", "--interval", "60", "--origins", "PMI", "--destinations", "BCN", "MAD", "VLC" ]
