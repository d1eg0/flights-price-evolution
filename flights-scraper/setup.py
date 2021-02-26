#!/usr/bin/env python

from setuptools import setup

setup(name='Flights Scraper',
      version='1.0',
      description='Flights data scraper',
      author='Diego García',
      author_email='diego.garcia.valverde@gmail.com',
      url='',
      packages=['scraper'],
      install_requires=[
          "aiohttp==3.7.4",
          "kafka-python==2.0.1"
      ],
      test_requires=[
            "asynctest",
            "pytest"
      ]
      )
