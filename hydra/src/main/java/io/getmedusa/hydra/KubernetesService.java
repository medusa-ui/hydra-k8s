package io.getmedusa.hydra;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KubernetesService {

    public KubernetesService() throws IOException, ApiException {
        System.out.println("K8S SETUP");
        System.out.println("----");
        String namespace = "hydra-proxy"; // Your namespace

        ApiClient client = Config.defaultClient();
        CoreV1Api api = new CoreV1Api(client);
        api.listNamespacedPod(namespace).execute().getItems().forEach(System.out::println);
        //api.listNode().execute().getItems().forEach(System.out::println);
        System.out.println("/----");
    }

}
