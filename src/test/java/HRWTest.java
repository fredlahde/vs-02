import com.google.common.hash.Hashing;
import org.junit.Test;
import software.kloud.vs.hrw.Node;
import software.kloud.vs.hrw.Workload;

import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HRWTest {

    @Test
    public void demo() {
        var random = new software.kloud.vs.hrw.RandomString();
        var hashFunc = Hashing.murmur3_128();
        var numNodes = 5;
        var numWorkload = 1000;
        var lenHash =  64;

        var workloads = IntStream.range(1, numWorkload)
                .mapToObj(i -> random.generate(lenHash))
                .map(Workload::new)
                .collect(Collectors.toList());

        var nodes = IntStream.range(1, numNodes)
                .mapToObj(i -> String.format("s%d.example.com", i))
                .map(Node::new)
                .collect(Collectors.toList());

        workloads.forEach(workload -> {
            //noinspection ConstantConditions
            nodes
                    .stream()
                    .map(node -> new NodeHashHolder<>(node, hashFunc
                            .newHasher()
                            .putString(workload.getId(), Charset.defaultCharset())
                            .putString(node.getId(), Charset.defaultCharset())
                            .hash()
                            .asLong()))
                    .max(Comparator.comparing(NodeHashHolder::getHash))
                    .map(NodeHashHolder::getNode)
                    .get()
                    .add(workload);
        });

        nodes
                .stream()
                .map(n -> String.format("Node: %s Workloads: %d", n.getId(), n.count()))
                .forEach(System.out::println);
    }

    static class NodeHashHolder<T extends Workload> {
        private final Node<T> node;
        private final long hash;


        NodeHashHolder(Node<T> n, long hash) {
            this.node = n;
            this.hash = hash;
        }

        Node<T> getNode() {
            return node;
        }

        long getHash() {
            return hash;
        }
    }
}
