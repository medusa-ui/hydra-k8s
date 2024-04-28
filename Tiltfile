# -*- mode: Python -*-

docker_build('hydra-proxy', 'hydra')
k8s_yaml('hydra/k8s/kubernetes.yaml')
k8s_resource('hydra-proxy', port_forwards='8080:9988')

docker_build('example-client', 'example-client')
k8s_yaml('example-client/k8s/kubernetes.yaml')
k8s_resource('example-client', port_forwards='8085:8080')