# Table of Contents

1.Overview

2.Features

3.Architecture Diagram

4.Technologies Used


##Overview

This service provides:

A mechanism to retrieve location data from various sources or a database.

Generation of H3 indexes (hexagonal indexes) for the retrieved locations.

Distance calculations between coordinates using the Haversine formula.

A reactive communication approach via RSocket for real-time data streaming and request-response patterns.

##Features

Location Fetching: Retrieve geographic location data from your data source or external APIs.

H3 Indexing: Convert latitude/longitude coordinates into H3 hexagons for spatial queries, clustering, or geospatial analytics.

Proximity & Distance Calculations: Use Haversine to compute distance in meters/kilometers between locations for range queries.

Reactive Communication: Leverage RSocket for async and streaming scenarios.

## Architecture Diagram
Below is a simple PlantUML-based representation of the service architecture. (You can embed this in your documentation by using any PlantUML renderer or service.)

![diagram](https://github.com/user-attachments/assets/5bc7cd64-826d-4daf-a3a7-80907c12e6d5)

### The overall flow is:

1) The client connects to the service via RSocket (Request/Response, Channel, or Stream).

2) The service fetches location data from a source.

3) The service generates H3 indexes based on lat/long.

4) (Optional) The service performs distance calculations using Haversine.

5) The processed data (with H3 indexes and distances, if applicable) is returned to the client via RSocket.

## Technologies Used
### RSocket

Enables reactive, bi-directional, and real-time communication between client and service.

### H3

A hexagonal hierarchical geospatial indexing system for lat/long coordinates.

### Haversine

A formula to calculate great-circle distances between two points on a sphere given their longitudes and latitudes.
