package io.getmedusa.hydra;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.util.Config;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@EnableScheduling
public class KubernetesService {

    private final String uniqueID = UUID.randomUUID().toString();

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void scheduledK8sUpdate() {
        System.out.println("K8S SETUP");
        System.out.println("----");

        final String selfIP = System.getenv("SELF_POD_IP");

        final Set<String> instanceIPs = determinePodIPs();

        //printing out ips
        for(String ip : instanceIPs) {
            String ipNote = ip;
            if(selfIP.equals(ip)) {
                ipNote += " (self)";
            }
            System.out.println(uniqueID + ": " + ipNote);
        }
        System.out.println("/----");
    }


    private static Set<String> determinePodIPs() {
        try {
            final String namespace = System.getenv("SELF_POD_NAMESPACE");

            System.out.println(namespace);

            Set<String> instanceIPs = new HashSet<>();

            ApiClient client = Config.defaultClient();
            CoreV1Api api = new CoreV1Api(client);
            final List<V1Pod> pods = api.listNamespacedPod(namespace).execute().getItems();

            for (V1Pod pod : pods) {
                final V1ObjectMeta metadata = pod.getMetadata();
                if (null != metadata && null != metadata.getLabels()) {
                    final String appLabel = metadata.getLabels().getOrDefault("app", "none");
                    if ("hydra-proxy".equals(appLabel)) {
                        final V1PodStatus status = pod.getStatus();
                        if (null != status) {
                            instanceIPs.add(status.getPodIP());
                        }
                    }
                }
            }
            return instanceIPs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
