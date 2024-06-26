This is v2 of Hydra, rebuilt from the ground up. It assumes Kubernetes is present and focuses on facilitating the micro-frontend aspect. It does not assume or require Medusa. It tries to minimize the amount of different services you need to just 2: a hydra proxy and a ui service with a hydra client lib.

It uses Tilt to simulate a local k8s environment. It uses Spring Cloud Gateway for the reverse proxy, this is the only part that needs to be publicly available. It does not require manual configuration of the gateway, this is handled through internal registration.
It uses Kubernetes to get information about its scaling pods and endpoints. It syncs its information between scaled nodes via ZeroMQ. It uses JJWT to generate JWT tokens encrypted with ECC so that the gateway doubles as an IDP.

# Getting started

Install Docker

Install Tilt: https://docs.tilt.dev/install.html

Run the command "tilt up"

Try "http://localhost:8080/_hydra/health"
