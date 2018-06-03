import com.google.common.hash.Hashing;
import org.junit.Test;
import software.kloud.vs.hrw.HRW;
import software.kloud.vs.hrw.Node;
import software.kloud.vs.hrw.RandomString;
import software.kloud.vs.hrw.Workload;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HRWTest {

    @Test
    public void demo() {
        var random = new RandomString();
        var hashFunc = Hashing.murmur3_128();
        var numNodes = 3;
        var numWorkload = 1000;
        var lenHash = 64;

        var workloads = IntStream.range(1, numWorkload + 1)
                .mapToObj(i -> random.generate(lenHash))
                .map(Workload::new)
                .collect(Collectors.toList());

        var nodes = IntStream.range(1, numNodes + 1)
                .mapToObj(i -> String.format("s%d.example.com", i))
                .map(Node::new)
                .collect(Collectors.toList());

        final HRW<Workload> hrw = new HRW<>(nodes, hashFunc);

        hrw.distribute(workloads);

        nodes.stream()
                .map(n -> String.format("Node: %s Workloads: %d", n.getId(), n.count()))
                .forEach(System.out::println);

        System.out.println();
        hrw.remove(nodes.get(2));

        nodes.stream()
                .map(n -> String.format("Node: %s Workloads: %d", n.getId(), n.count()))
                .forEach(System.out::println);

        Set<String> distribution = new HashSet<>();
        nodes.forEach(node -> distribution.addAll(node.getWorkloads().stream()
                .map(w -> String.format("%s-%s", node.getId(), w.getId()))
                .collect(Collectors.toSet())));

        hrw.add(new Node<>("s4.example.com"));
        System.out.println();

        nodes.stream()
                .map(n -> String.format("Node: %s Workloads: %d", n.getId(), n.count()))
                .forEach(System.out::println);

        var newlyDistributedWorkloads = new AtomicInteger();
        nodes.forEach(node -> node.getWorkloads().forEach(w -> {
            var key = String.format("%s-%s", node.getId(), w.getId());
            if (!distribution.add(key)) {
                newlyDistributedWorkloads.incrementAndGet();
            }
        }));

        System.out.printf("Newly distributed workloads: %.3f", (100d / numWorkload * newlyDistributedWorkloads.get()));
    }
}
