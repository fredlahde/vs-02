package software.kloud.vs.hrw;

import com.google.common.hash.HashFunction;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HRW<T extends Workload> {
    public static final int LENGHT_HASH = 4096;
    private final List<Node<T>> nodes;
    private final HashFunction hashFunc;

    public HRW(List<Node<T>> nodes, HashFunction hashFunc) {
        this.nodes = nodes;
        this.hashFunc = hashFunc;
    }

    public void distribute(List<T> workloads) {
        workloads.forEach(workload -> {
            //noinspection ConstantConditions
            nodes
                    .stream()
                    .map(node -> new NodeHashHolder<>(node, hash(workload, node)))
                    .max(Comparator.comparing(NodeHashHolder::getHash))
                    .map(NodeHashHolder::getNode)
                    .get()
                    .add(workload);
        });

    }

    private long hash(T workload, Node<T> node) {
        return hashFunc
                .newHasher()
                .putString(workload.getId(), Charset.defaultCharset())
                .putString(node.getId(), Charset.defaultCharset())
                .hash()
                .asLong();
    }

    public void remove(Node<T> nodeToRemove) {
        var workloadsToDistribute = new ArrayList<T>(nodeToRemove.getWorkloads());
        nodeToRemove.clear();
        nodes.removeIf(n -> n.getId().equals(nodeToRemove.getId()));
        distribute(workloadsToDistribute);
    }

    public void add(Node<T> nodeToAdd) {
        var workloadsToDistribute = nodes.stream()
                .flatMap(node -> node.getWorkloads().stream())
                .collect(Collectors.toList());
        nodes.forEach(Node::clear);
        nodes.add(nodeToAdd);
        distribute(workloadsToDistribute);
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
