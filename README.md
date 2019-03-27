# Process
Before doing anything install
- Isitioctl
- Helm
- Kubectl
- Minikube

and download the istio install files (same place as you get istioctl)

1) minikube start --vm-driver=kvm2 --cpus=4 --memory=8192
2) install istio as specified on their webpage
3) cd to kubernetes
4) for file in *; do istioctl kube-inject -f $file | kubectl apply -f -; done


# Conclusions

- Istio is able to route thrift traffic
- Istio is able to instrument traffic and can count successful reuqests


# Todo
- Load balancing
- Route to specific version/instance
- Route based on some id
- Kill controlplane
- Fiddle with data-plane proxy
- Instrument with tracing
- Instrument with logs
- Visualize topology using kiali
- Security